package org.example.core.repository;

import org.example.core.collection.RoutingResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoutingResponseRepository extends MongoRepository<RoutingResponse, String> {
}
