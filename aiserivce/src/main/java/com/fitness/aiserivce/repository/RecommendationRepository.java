package com.fitness.aiserivce.repository;

import com.fitness.aiserivce.model.Recommendation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RecommendationRepository extends MongoRepository<Recommendation, String> {
    Optional<Recommendation> findByActivityId(String activityId);

    List<Recommendation> findByUserId(String userId);
}
