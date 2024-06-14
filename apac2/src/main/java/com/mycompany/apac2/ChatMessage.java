
package com.mycompany.apac2;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ChatMessage implements Serializable {
    private String user;
    private String message;
    private LocalDateTime timestamp;

    // Constructor para crear un mensaje con fecha y hora actual
    public ChatMessage(String user, String message) {
        this.user = user;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor para crear un mensaje con fecha y hora específicas
    public ChatMessage(String user, String message, LocalDateTime timestamp) {
        this.user = user;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return timestamp + " [" + user + "]: " + message;
    }
}


