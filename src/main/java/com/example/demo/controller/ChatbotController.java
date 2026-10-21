package com.example.demo.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.ChatbotService;

@RestController
@RequestMapping("/admin/chat")
public class ChatbotController {

    @Autowired private ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<?> ask(@RequestBody Map<String, String> body) {
        String pregunta = body.get("pregunta");
        if (pregunta == null || pregunta.isBlank())
            return ResponseEntity.badRequest().body("Falta el campo 'pregunta'");
        String respuesta = chatbotService.preguntar(pregunta);
        return ResponseEntity.ok(Map.of("respuesta", respuesta));
    }
}
