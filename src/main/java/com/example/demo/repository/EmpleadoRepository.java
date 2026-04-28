package com.example.demo.repository;

import com.example.demo.model.Empleados;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmpleadoRepository extends MongoRepository<Empleados, String> {
    Empleados findByUsername(String username);
}
