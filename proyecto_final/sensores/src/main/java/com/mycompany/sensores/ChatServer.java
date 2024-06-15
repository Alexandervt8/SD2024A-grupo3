package com.mycompany.sensores;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

public class ChatServer {
    private static Ignite ignite;
    private static IgniteCache<Long, SensorData> sensorDataCache;
    private static Random random = new Random();

    public static void main(String[] args) {
        ignite = Ignition.start();

        CacheConfiguration<Long, SensorData> sensorDataCacheCfg = new CacheConfiguration<>();
        sensorDataCacheCfg.setName("SensorDataCache"); // Nombre del cache
        sensorDataCacheCfg.setIndexedTypes(Long.class, SensorData.class); // Tipos de índice

        sensorDataCache = ignite.getOrCreateCache(sensorDataCacheCfg);

        System.out.println("Chat server started.");

        new Thread(ChatServer::generateSensorData).start();
    }

    public static Ignite getIgniteInstance() {
        return ignite;
    }

    private static void generateSensorData() {
        // Generar datos predefinidos para verificar
        SensorData data1 = new SensorData(1L, "sensor-1", 25.5, Timestamp.valueOf(LocalDateTime.now()));
        SensorData data2 = new SensorData(2L, "sensor-2", 30.0, Timestamp.valueOf(LocalDateTime.now()));
        SensorData data3 = new SensorData(3L, "sensor-3", 22.8, Timestamp.valueOf(LocalDateTime.now()));

        // Almacenar datos en el caché con claves únicas basadas en el tiempo actual
        long currentTime1 = System.currentTimeMillis();
        sensorDataCache.put(currentTime1, data1);
        System.out.println("Añadido al caché: Key=" + currentTime1 + ", Value=" + data1);

        long currentTime2 = System.currentTimeMillis() + 1;
        sensorDataCache.put(currentTime2, data2);
        System.out.println("Añadido al caché: Key=" + currentTime2 + ", Value=" + data2);

        long currentTime3 = System.currentTimeMillis() + 2;
        sensorDataCache.put(currentTime3, data3);
        System.out.println("Añadido al caché: Key=" + currentTime3 + ", Value=" + data3);

        // Iniciar generación de datos continuos
        while (true) {
            String sensorId = "sensor-" + random.nextInt(100);
            double value = 20 + random.nextDouble() * 10;
            SensorData data = new SensorData(System.currentTimeMillis(), sensorId, value, Timestamp.valueOf(LocalDateTime.now()));
            long currentTime = System.currentTimeMillis();
            sensorDataCache.put(currentTime, data);
            System.out.println("Añadido al caché: Key=" + currentTime + ", Value=" + data);
            try {
                Thread.sleep(1000); // Generar datos cada segundo
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

