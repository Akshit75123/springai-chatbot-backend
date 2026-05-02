package com.chatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MultiModelConfiguration {
    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.api.model.name}")
    private String model;

    @Value("${gemini.api.url}")
    private String baseURL;

    @Value("${gemini.api.completions.path}")
    private String completionsPath;


    @Bean("openAIChatClient")
    @Primary
    public ChatClient openAIChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.create(openAiChatModel); // spring boot will auto-configure this bean based on properties
    }

    @Bean("geminiChatClient")
    public ChatClient geminiChatClient(OpenAiChatModel openAiChatModel) {
        OpenAiApi geminiApi = OpenAiApi.builder()
                .apiKey(geminiApiKey)
                .baseUrl(baseURL)
                .completionsPath(completionsPath)
                .build();

        OpenAiChatModel geminiChatModel = OpenAiChatModel.builder()
                .openAiApi(geminiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(model)
                        .build())
                .build();
        return ChatClient.create(geminiChatModel);
    }
}
