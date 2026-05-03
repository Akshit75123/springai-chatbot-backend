package com.chatbot.service;

import com.chatbot.ChatbotApplication;
import com.chatbot.model.ChatbotHistory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatbotService {

    private final Map<String, ChatbotHistory> conversations = new ConcurrentHashMap<>();

    private static final int DEFAULT_TOKEN_LIMIT = 4000;

    public ChatbotHistory getConversation(String conversationId) {
        return conversations.computeIfAbsent(
                conversationId,
                id -> new ChatbotHistory(id)
        );
    }

    public void addUserMessage(String conversationId, String content) {
        ChatbotHistory history = getConversation(conversationId);
        history.addMessage(new UserMessage(content));
    }

    public void addAssistantMessage(String conversationId, String content) {
        ChatbotHistory history = getConversation(conversationId);
        history.addMessage(new AssistantMessage(content));
    }

    public List<Message> getMessages(String conversationId) {
        ChatbotHistory history = getConversation(conversationId);
        return history.getMessages();
    }

    public List<Message> getRecentMessages(String conversationId, int maxTokens) {
        ChatbotHistory history = getConversation(conversationId);
        return history.getRecentMessages(maxTokens);
    }

    public List<Message> getRecentMessages(String conversationId) {
        return getRecentMessages(conversationId, DEFAULT_TOKEN_LIMIT);
    }

    public void clearConversation(String conversationId) {
        conversations.remove(conversationId);
    }

    public Map<String, Object> getConversationInfo(String conversationId) {
        ChatbotHistory history = conversations.get(conversationId);

        if (history == null) {
            return Map.of("exists", false);
        }

        return Map.of(
                "exists", true,
                "conversationId", history.getConversationId(),
                "messageCount", history.getMessageCount(),
                "totalTokens", history.getTotalTokens(),
                "createdAt", history.getCreatedAt().toString(),
                "updatedAt", history.getUpdatedAt().toString()
        );
    }

    public List<String> listConversations() {
        return conversations.keySet().stream().toList();
    }
}
