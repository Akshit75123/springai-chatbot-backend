package com.chatbot.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String role;

    @Column(columnDefinition = "TEXT") // Important for long AI responses
    private String content;

    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY) // Keeps your app fast by not loading the whole conversation unless needed
    @JoinColumn(name = "conversation_id", nullable = false) // Defines the actual column in PostgreSQL
    private Conversation conversation;

    // Getters and Setters

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Message(Long id, String role, String content, LocalDateTime timestamp, Conversation conversation) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
        this.conversation = conversation;
    }

    public Message() {
    }
}
