package com.example.demo.repository;

import com.example.demo.model.Menu;
import org.springframework.data.mongodb.repository.MongoRepository;



public interface MenuRepository extends MongoRepository<Menu, String> { }
