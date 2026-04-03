package com.fitness.aiserivce.service;

import com.fitness.aiserivce.model.Activity;
import com.fitness.aiserivce.model.Recommendation;
import com.fitness.aiserivce.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final ActivityAIService aiService;
    private final RecommendationRepository recommendationRepository;

    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity){
        log.info("Received activity for processing {}", activity.getId());
        //log.info("Generated Recommendation {}", aiService.generateRecommendation(activity));
//        try{
//
//            String result = aiService.generateRecommendation(activity);
//
//            log.info("AI RESULT {}", result);
//
//        }catch (Exception e){
//
//            log.error("AI failed {}", e.getMessage());
//
//            throw new AmqpRejectAndDontRequeueException(e);
//        }
        Recommendation recommendation = aiService.generateRecommendation(activity);
        recommendationRepository.save(recommendation);
        log.info("Recommendation saved in database {}", recommendation.toString());

    }
}
