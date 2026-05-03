package com.chatbot.repository;

import com.chatbot.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Long, Message> {
    List<Message> findByConversationIdOrderByTimestampAsc(String conversationId);
}
