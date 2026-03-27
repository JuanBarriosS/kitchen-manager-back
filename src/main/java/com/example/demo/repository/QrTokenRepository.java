package com.example.demo.repository;

import com.example.demo.model.QrToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface QrTokenRepository extends MongoRepository<QrToken, String> {
    Optional<QrToken> findByToken(String token);
}