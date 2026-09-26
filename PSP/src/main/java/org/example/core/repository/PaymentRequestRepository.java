package org.example.core.repository;

import org.example.core.collections.PaymentRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentRequestRepository extends MongoRepository<PaymentRequest, String> {
}
