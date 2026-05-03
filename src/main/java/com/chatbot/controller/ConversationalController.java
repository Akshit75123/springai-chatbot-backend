package com.chatbot.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@RestController
@RequestMapping("/api/conversation")
public class ConversationalController {

    private static final Logger log = LoggerFactory.getLogger(ConversationalController.class);

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

        if (!"openai".equalsIgnoreCase(provider) &&
                (model == null || model.isEmpty())) {
            return Map.of(
                    "error", true,
                    "message", "AI-Model header is required when using provider: " + provider
            );
        }

        conversationService.addUserMessage(conversationId, message);

        List<Message> history = conversationService.getRecentMessages(conversationId);

//        log.info("=== REQUEST RECEIVED ===");
//        log.info("Provider: {}", provider);
//        log.info("Model header: {}", model);
//        log.info("Message: {}", message);

        ChatClient chatClient = modelService.getChatClient(provider);
//        log.info("ChatClient class: {}", chatClient.getClass().getName());
//        log.info("ChatClient: {}", chatClient);

        var promptSpec = chatClient.prompt().messages(history);

        if (model != null && !model.isEmpty()) {
            promptSpec = promptSpec.options(
                    OpenAiChatOptions.builder()
                            .model(model)
                            .temperature(1.0)
                            .build()
            );
        }

//        log.info("Using default model from ChatClient bean");
//        log.info("=== CALLING PROMPT ===");

        ChatResponse response = promptSpec.call().chatResponse();
        String aiResponse = response.getResult().getOutput().getText();

//        log.info("aiResponse = "+ aiResponse);

        conversationService.addAssistantMessage(conversationId, aiResponse);

        return Map.of(
                "conversationId", conversationId,
                "response", aiResponse,
                "messageCount", conversationService.getConversationInfo(conversationId).get("messageCount"),
                "totalTokens", conversationService.getConversationInfo(conversationId).get("totalTokens")
        );
    }

    @GetMapping("/{conversationId}/info")
    public Map<String, Object> getInfo(@PathVariable String conversationId) {
        return conversationService.getConversationInfo(conversationId);
    }

    @GetMapping("/{conversationId}/history")
    public Map<String, Object> getHistory(@PathVariable String conversationId) {
        List<Message> messages = conversationService.getMessages(conversationId);

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

    @DeleteMapping("/{conversationId}")
    public Map<String, Object> clearConversation(@PathVariable String conversationId) {
        conversationService.clearConversation(conversationId);
        return Map.of(
                "message", "Conversation cleared",
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
