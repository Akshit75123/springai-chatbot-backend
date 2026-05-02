package com.chatbot.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ModelService {
    @Autowired
    @Qualifier("openAIChatClient")
    private ChatClient openAiChatClient;

    @Autowired
    @Qualifier("geminiChatClient")
    private ChatClient geminiChatClient;

    public ChatClient getChatClient(String provider) {

        if (provider==null || provider.isEmpty())
            return openAiChatClient; // default to primary chat client
        else {
            if (provider.equalsIgnoreCase("gemini"))
                return geminiChatClient;
            else
                return openAiChatClient; // default to primary chat client if provider is unrecognized
        }
    }
}
