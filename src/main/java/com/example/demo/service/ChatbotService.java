package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatbotService {

    @Autowired private ChatClient.Builder chatClientBuilder;
    @Autowired private KitchenContextService contextService;

    public String preguntar(String pregunta) {
        String contexto = contextService.construirContexto();

        String systemPrompt =
            "Eres el asistente inteligente de Kitchen Manager, un sistema de gestión " +
            "para una ghost kitchen. Responde SOLO basándote en los datos del negocio " +
            "que se te proporcionan a continuación. Si la pregunta no tiene relación con " +
            "los datos del negocio, indica amablemente que solo puedes responder sobre " +
            "Kitchen Manager.\n\n" + contexto;

        return chatClientBuilder.build()
            .prompt()
            .system(systemPrompt)
            .user(pregunta)
            .call()
            .content();
    }
}
