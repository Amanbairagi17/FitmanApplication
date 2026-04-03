package com.fitness.aiserivce.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiserivce.model.Activity;
import com.fitness.aiserivce.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity){
        String prompt = createPromptActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("Response from AI {}", aiResponse);
        return processAiResponse(activity, aiResponse);
       // return aiResponse;
    }

//    private void processAiResponse(Activity activity, String aiResponse){
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode rootNode = mapper.readTree(aiResponse);
//
//            JsonNode textNode = rootNode.path("candidates")
//                    .get(0)
//                    .path("content")
//                    .path("parts")
//                    .get(0)
//                    .path("text");
//            String jsonContent = textNode.asText()
//                    .replaceAll("```json\\n","")
//                    .replaceAll("\\n```","")
//                    .trim();
//
//            log.info("Parsed response from AI {} ", jsonContent);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }

    private Recommendation processAiResponse(Activity activity, String aiResponse){

        try {

            ObjectMapper mapper = new ObjectMapper();

            // read full Gemini response
            JsonNode rootNode = mapper.readTree(aiResponse);

            // extract text part from Gemini response
            JsonNode textNode = rootNode
                    .path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text");

            // remove ```json ``` wrapper
            String jsonContent = textNode.asText()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            log.info("Clean JSON from AI {}", jsonContent);

            // parse clean JSON
            JsonNode aiJson = mapper.readTree(jsonContent);

            // map JSON → entity
            Recommendation recommendation = new Recommendation();

            recommendation.setActivityId(activity.getId());
            recommendation.setUserId(activity.getUserId());

            recommendation.setActivityType(
                    aiJson.path("activityType")
                            .asText(activity.getType())
            );

            recommendation.setRecommendation(
                    aiJson.path("recommendation").asText()
            );

            recommendation.setImprovements(
                    mapper.convertValue(
                            aiJson.path("improvements"),
                            new TypeReference<List<String>>() {}
                    )
            );

            recommendation.setSuggestions(
                    mapper.convertValue(
                            aiJson.path("suggestions"),
                            new TypeReference<List<String>>() {}
                    )
            );

            recommendation.setSafety(
                    mapper.convertValue(
                            aiJson.path("safety"),
                            new TypeReference<List<String>>() {}
                    )
            );

            return recommendation;

        }
        catch (Exception e){

            log.error("Error parsing AI response {}", e.getMessage());

            throw new RuntimeException("Failed to parse AI response", e);
        }
    }
    private String createPromptActivity(Activity activity) {

        return String.format("""
            Analyze this fitness activity and generate recommendations.
            
            Return ONLY valid JSON in this exact structure:
            
            {
              "activityType": "",
              "recommendation": "",
              "improvements": [
                "improvement 1",
                "improvement 2"
              ],
              "suggestions": [
                "suggestion 1",
                "suggestion 2"
              ],
              "safety": [
                "safety tip 1",
                "safety tip 2"
              ]
            }
            
            Activity Details:
            Type: %s
            Duration: %d minutes
            Calories Burned: %d
            Additional Metrics: %s
            
            Instructions:
            - recommendation → overall performance summary
            - improvements → areas to improve performance
            - suggestions → tips for better results
            - safety → injury prevention tips
            - return ONLY JSON
            """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics() != null
                        ? activity.getAdditionalMetrics()
                        : "Not provided"
        );
    }
}
