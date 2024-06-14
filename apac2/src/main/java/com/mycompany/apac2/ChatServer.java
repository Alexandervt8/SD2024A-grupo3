package com.mycompany.apac2;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.Scanner;

public class ChatServer {
    private static Ignite ignite;
    private static IgniteCache<String, UserSession> userCache;
    private static IgniteCache<Long, ChatMessage> messageCache;

    public static void main(String[] args) {
        // Iniciar un nodo de Ignite
        ignite = Ignition.start();

        // Configurar caché para sesiones de usuario
        CacheConfiguration<String, UserSession> userCacheCfg = new CacheConfiguration<>();
        userCacheCfg.setName("UserCache");
        userCache = ignite.getOrCreateCache(userCacheCfg);

        // Configurar caché para mensajes de chat
        CacheConfiguration<Long, ChatMessage> messageCacheCfg = new CacheConfiguration<>();
        messageCacheCfg.setName("MessageCache");
        messageCache = ignite.getOrCreateCache(messageCacheCfg);

        System.out.println("Chat server started.");

        // Hilo para recibir y mostrar mensajes (opcional)
        new Thread(ChatServer::displayChatMessages).start();
    }

    // Método para mostrar los mensajes de chat (opcional)
    private static void displayChatMessages() {
        while (true) {
            System.out.println("\nChat Messages:");
            for (IgniteCache.Entry<Long, ChatMessage> entry : messageCache) {
                System.out.println(entry.getValue());
            }
            try {
                Thread.sleep(2000); // Esperar 2 segundos antes de actualizar los mensajes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

