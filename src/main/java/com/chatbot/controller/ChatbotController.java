package com.chatbot.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.chatbot.service.ChatbotService;
import com.chatbot.service.ModelService;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conversation")
public class ChatbotController {

    @Autowired
    private ModelService modelService;

    @Autowired
    private ChatbotService conversationService;

    @PostMapping("/{conversationId}")
    public Map<String, Object> chat(
            @PathVariable String conversationId,
            @RequestHeader(value = "AI-Provider", defaultValue = "gemini") String provider,
            @RequestHeader(value = "AI-Model", required = false) String model,
            @RequestBody String message
    ) {
        // 1. Validation for specific providers
        if (!"openai".equalsIgnoreCase(provider) && (model == null || model.isEmpty())) {
            return Map.of(
                    "error", true,
                    "message", "AI-Model header is required when using provider: " + provider
            );
        }

        // 2. Persist the User Message to PostgreSQL
        conversationService.saveMessage(conversationId, "user", message);

        // 3. Retrieve the sliding window history (formatted for Spring AI)
        List<Message> history = conversationService.getRecentMessages(conversationId);

        // 4. Initialize the requested ChatClient
        ChatClient chatClient = modelService.getChatClient(provider);
        var promptSpec = chatClient.prompt().messages(history);

        // 5. Apply dynamic model options if provided
        if (model != null && !model.isEmpty()) {
            promptSpec = promptSpec.options(
                    OpenAiChatOptions.builder()
                            .model(model)
                            .temperature(1.0)
                            .build()
            );
        }

        // 6. Execute the AI call
        ChatResponse response = promptSpec.call().chatResponse();
        String aiResponse = response.getResult().getOutput().getText();

        // 7. Persist the AI Response to PostgreSQL
        conversationService.saveMessage(conversationId, "assistant", aiResponse);

        // 8. Return data for the React frontend
        Map<String, Object> info = conversationService.getConversationInfo(conversationId);
        return Map.of(
                "conversationId", conversationId,
                "response", aiResponse,
                "messageCount", info.getOrDefault("messageCount", 0),
                "totalTokens", info.getOrDefault("totalTokens", 0) // Ensure Service tracks this if needed
        );
    }

    @GetMapping("/{conversationId}/history")
    public Map<String, Object> getHistory(@PathVariable String conversationId) {
        // Fetches full history from DB for initial frontend load
        List<Message> messages = conversationService.getRecentMessages(conversationId, Integer.MAX_VALUE);

        return Map.of(
                "conversationId", conversationId,
                "messages", messages.stream()
                        .map(msg -> Map.of(
                                "role", msg.getMessageType().getValue(),
                                "content", msg.getText()
                        ))
                        .toList()
        );
    }

    @GetMapping("/{conversationId}/info")
    public Map<String, Object> getInfo(@PathVariable String conversationId) {
        return conversationService.getConversationInfo(conversationId);
    }

    @DeleteMapping("/{conversationId}")
    public Map<String, Object> clearConversation(@PathVariable String conversationId) {
        conversationService.clearConversation(conversationId);
        return Map.of(
                "message", "Conversation deleted from database",
                "conversationId", conversationId
        );
    }

    @GetMapping("/list")
    public Map<String, Object> listConversations() {
        return Map.of(
                "conversations", conversationService.listConversations()
        );
    }
}
