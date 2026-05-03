package com.chatbot.service;

import com.chatbot.model.Conversation;
import com.chatbot.model.Message;
import com.chatbot.repository.ConversationRepository;
import com.chatbot.repository.MessageRepository;
import jakarta.transaction.Transactional;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    private static final int DEFAULT_TOKEN_LIMIT = 4000;

    @Transactional
    public void saveMessage(String conversationId, String role, String content) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setId(conversationId);
                    return conversationRepository.save(newConv);
                });

        com.chatbot.model.Message jpaMessage = new com.chatbot.model.Message();
        jpaMessage.setRole(role);
        jpaMessage.setContent(content);
        jpaMessage.setConversation(conversation);

        messageRepository.save(jpaMessage);
    }

    public List<org.springframework.ai.chat.messages.Message> getRecentMessages(String conversationId, int maxTokens) {
        // 1. Fetch full history for this session from PostgreSQL
        List<com.chatbot.model.Message> allMessages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);

        List<org.springframework.ai.chat.messages.Message> recentMessages = new ArrayList<>();
        int currentTokens = 0;

        // 2. Apply your sliding window logic (iterating backwards)
        for (int i = allMessages.size() - 1; i >= 0; i--) {
            com.chatbot.model.Message dbMsg = allMessages.get(i);
            int msgTokens = dbMsg.getContent().length() / 4;

            if (currentTokens + msgTokens > maxTokens) {
                break;
            }

            // Convert DB entity to Spring AI Message type
            org.springframework.ai.chat.messages.Message aiMsg = "user".equalsIgnoreCase(dbMsg.getRole())
                    ? new UserMessage(dbMsg.getContent())
                    : new AssistantMessage(dbMsg.getContent());

            recentMessages.add(0, aiMsg);
            currentTokens += msgTokens;
        }

        return recentMessages;
    }

    public List<org.springframework.ai.chat.messages.Message> getRecentMessages(String conversationId) {
        return getRecentMessages(conversationId, DEFAULT_TOKEN_LIMIT);
    }

    @Transactional
    public void clearConversation(String conversationId) {
        conversationRepository.deleteById(conversationId);
    }

    public Map<String, Object> getConversationInfo(String conversationId) {
        return conversationRepository.findById(conversationId)
                .map(conv -> Map.<String, Object>of( // Explicitly define <String, Object>
                        "exists", true,
                        "conversationId", conv.getId(),
                        "messageCount", conv.getMessages().size(),
                        "createdAt", conv.getCreatedAt().toString()
                )).orElse(Map.of("exists", false));
    }

    public List<String> listConversations() {
        // Fetches all conversations and returns just their IDs as a List of Strings
        return conversationRepository.findAll()
                .stream()
                .map(Conversation::getId)
                .toList();
    }
}