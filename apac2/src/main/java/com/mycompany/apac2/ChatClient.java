package com.mycompany.apac2;

import java.time.LocalDateTime;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

import java.util.Scanner;

public class ChatClient {
    private static Ignite ignite;
    private static IgniteCache<Long, ChatMessage> messageCache;
    private static String username;

    public static void main(String[] args) {
        // Iniciar un nodo de Ignite (cliente)
        ignite = Ignition.start();

        // Obtener la caché de mensajes del servidor
        messageCache = ignite.getOrCreateCache("MessageCache");

        // Solicitar el nombre de usuario
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        username = scanner.nextLine();

        // Hilo para enviar mensajes
        new Thread(ChatClient::sendMessages).start();

        // Hilo para recibir y mostrar mensajes
        new Thread(ChatClient::receiveMessages).start();
    }

    // Método para enviar mensajes
    private static void sendMessages() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter your message: ");
            String message = scanner.nextLine();
            // Crear un nuevo mensaje y ponerlo en la caché de mensajes
            ChatMessage chatMessage = new ChatMessage(username, message);
            messageCache.put(System.currentTimeMillis(), chatMessage);
        }
    }

    // Método para recibir y mostrar mensajes
    private static void receiveMessages() {
        Scanner scanner = new Scanner(System.in);
    while (true) {
        System.out.print("Enter your message: ");
        String message = scanner.nextLine();
        // Crear un nuevo mensaje con la fecha y hora actual
        ChatMessage chatMessage = new ChatMessage(username, message, LocalDateTime.now());
        messageCache.put(System.currentTimeMillis(), chatMessage);
    }
    }
}


