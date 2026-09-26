package org.example.core.repository;

import org.example.core.collections.PaymentResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PaymentResponseRepository extends MongoRepository<PaymentResponse, String> {
}
