package com.chatbot.controller;

import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.chatbot.service.ChatbotService;
import com.chatbot.service.ModelService;

import java.util.ArrayList;
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
        // 1. Validation (remains the same)
        if (!"openai".equalsIgnoreCase(provider) && (model == null || model.isEmpty())) {
            return Map.of("error", true, "message", "AI-Model header is required.");
        }

        // 2. Persist User Message (remains the same)
        conversationService.saveMessage(conversationId, "user", message);

        // 3. Prepare the Minimalist Template
        // This SystemMessage acts as the template to limit the AI's verbosity
        SystemMessage minimalistTemplate =
                new SystemMessage("""
            You are a minimalist assistant. 
            Provide only the essential information requested. 
            Avoid long explanations, greetings, or filler text.
            Reply in 3 sentences or fewer.
        """);

        // 4. Retrieve History and prepend the Template
        List<Message> messages = new ArrayList<>();
        messages.add(minimalistTemplate); // Template goes first
        messages.addAll(conversationService.getRecentMessages(conversationId));

        // 5. Initialize ChatClient
        ChatClient chatClient = modelService.getChatClient(provider);

        // 6. Execute with strict Token Limits and Temperature
        ChatResponse response = chatClient.prompt()
                .messages(messages)
                .options(OpenAiChatOptions.builder()
                        .model(model != null ? model : "gemini-2.5-flash")
                        .temperature(1.0)  // Lower temperature reduces wordiness
//                        .maxTokens(150)    // Strict limit on response length
                        .build())
                .call()
                .chatResponse();

        String aiResponse = response.getResult().getOutput().getText();

        // 7. Persist AI Response (remains the same)
        conversationService.saveMessage(conversationId, "assistant", aiResponse);

        // 8. Return data
        Map<String, Object> info = conversationService.getConversationInfo(conversationId);
        return Map.of(
                "conversationId", conversationId,
                "response", aiResponse,
                "messageCount", info.getOrDefault("messageCount", 0)
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

    @GetMapping("/list")
    public Map<String, Object> listConversations() {
        return Map.of(
                "conversations", conversationService.listConversations()
        );
    }
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Map<String, Object>> deleteConversation(@PathVariable String conversationId) {
        try {
            // Call the service to remove messages and metadata from PostgreSQL
            conversationService.clearConversation(conversationId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Conversation " + conversationId + " successfully deleted",
                    "conversationId", conversationId
            ));
        } catch (Exception e) {
            // If the ID doesn't exist or a DB error occurs
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Error deleting conversation: " + e.getMessage()
            ));
        }
    }
}
