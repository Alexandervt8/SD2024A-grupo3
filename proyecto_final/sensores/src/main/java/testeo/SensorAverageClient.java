package com.mycompany.sensores;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SensorAverageClient {

    public static void main(String[] args) {
        try {
            // URL del servlet
            String url = "http://localhost:8080/sensores/average-data";
            
            // Crear una conexión HTTP
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");

            // Leer la respuesta del servidor
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            
            // Imprimir el contenido de la respuesta
            System.out.println("Respuesta del servidor: " + content.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
