package com.chatbot.model;

import org.springframework.ai.chat.messages.Message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatbotHistory {

    private String conversationId;
    private List<Message> messages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int totalTokens;

    public ChatbotHistory(String conversationId) {
        this.conversationId = conversationId;
        this.messages = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.totalTokens = 0;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        this.updatedAt = LocalDateTime.now();
        this.totalTokens += message.getText().length() / 4;
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public List<Message> getRecentMessages(int maxTokens) {
        List<Message> recentMessages = new ArrayList<>();
        int currentTokens = 0;

        for (int i = messages.size() - 1; i >= 0; i--) {
            Message msg = messages.get(i);
            int msgTokens = msg.getText().length() / 4;

            if (currentTokens + msgTokens > maxTokens) {
                break;
            }

            recentMessages.add(0, msg);
            currentTokens += msgTokens;
        }

        return recentMessages;
    }

    public String getConversationId() {
        return conversationId;
    }

    public int getMessageCount() {
        return messages.size();
    }

    public int getTotalTokens() {
        return totalTokens;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
