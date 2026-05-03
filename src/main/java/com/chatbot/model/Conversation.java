package com.chatbot.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="converstaions")
public class Conversation {
    @Id
    private String id = UUID.randomUUID().toString(); // Consistent with your frontend conversationId

    private LocalDateTime createdAt;

    // One Conversation has Many Messages
    // mappedBy refers to the "conversation" field in the Message class
    // cascade = ALL ensures if you save/delete a conversation, messages follow suit
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Conversation() {
    }

    public Conversation(String id, LocalDateTime createdAt, List<Message> messages) {
        this.id = id;
        this.createdAt = createdAt;
        this.messages = messages;
    }
}
