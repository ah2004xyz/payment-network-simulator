package org.example.core.repository;

import org.example.core.collections.Merchant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends MongoRepository<Merchant, String> {

    Optional<Merchant> findByMerchantNumber(String merchantNumber);

}
