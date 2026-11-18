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

    public String preguntarCliente(String pregunta) {
    String systemPrompt =
        "Eres el asistente de un restaurante y ayudas al cliente a elegir qué pedir. " +
        "Recomienda SOLO platos del menú de abajo, con su precio y su nombre exacto. " +
        "Si piden algo que no existe, dilo y sugiere la opción más parecida. " +
        "No inventes ingredientes ni precios. Responde en español, en máximo 3 frases. " +
        "Si preguntan por ventas, otros clientes o datos internos, responde que solo puedes ayudar con el menú. " +
        "No tomas pedidos: dile que agregue el plato al carrito.\n\n" +
        contextService.construirContextoMenu();

    return chatClientBuilder.build()
        .prompt()
        .system(systemPrompt)
        .user(pregunta)
        .call()
        .content();
}
}
