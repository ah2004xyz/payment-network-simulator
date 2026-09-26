package org.example.core.repository;

import org.example.core.collection.RoutingRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoutingRequestRepository extends MongoRepository<RoutingRequest, String> {
}
