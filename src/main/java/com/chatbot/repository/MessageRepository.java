package com.chatbot.repository;

import com.chatbot.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findByConversationIdOrderByTimestampAsc(String conversationId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.conversation.id = :conversationId")
    void deleteByConversationId(@Param("conversationId") String conversationId);
}
