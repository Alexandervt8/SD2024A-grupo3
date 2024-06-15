
package com.mycompany.sensores;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.ScanQuery;

import javax.cache.Cache;
import java.util.Scanner;

public class ChatClient {
    private static Ignite ignite;
    private static IgniteCache<Long, SensorData> sensorDataCache;

    public static void main(String[] args) {
        ignite = Ignition.start();
        sensorDataCache = ignite.getOrCreateCache("SensorDataCache");

        new Thread(ChatClient::displaySensorData).start();
    }

    private static void displaySensorData() {
        while (true) {
            ScanQuery<Long, SensorData> qry = new ScanQuery<>();
            for (Cache.Entry<Long, SensorData> entry : sensorDataCache.query(qry)) {
                System.out.println(entry.getValue());
            }
            try {
                Thread.sleep(2000); // Actualizar cada 2 segundos
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

