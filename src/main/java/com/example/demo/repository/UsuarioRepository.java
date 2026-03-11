package com.example.demo.repository;

import com.example.demo.model.Usuarios;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioRepository extends MongoRepository<Usuarios, String> {
    Usuarios findByUsername(String username);
}
