package com.mycompany.apac2;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

import javax.cache.Cache;
import java.util.Collection;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        // Configuración de Apache Ignite
        IgniteConfiguration cfg = new IgniteConfiguration();

        // Iniciar nodo de Apache Ignite
        try (Ignite ignite = Ignition.start(cfg)) {
            // Configuración de la caché distribuida
            CacheConfiguration<Integer, Integer> cacheCfg = new CacheConfiguration<>("numbersCache");

            // Obtener o crear la caché distribuida
            ignite.getOrCreateCache(cacheCfg);

            // Lista de números para calcular la suma
            Integer[] numbers = {1, 2, 3, 4, 5};

            // Almacenar la lista de números en la caché distribuida
            for (Integer n : numbers) {
                ignite.cache("numbersCache").put(n, n);
            }

            // Ejecutar una tarea distribuida para calcular la suma de los números
            Collection<Integer> sum = ignite.compute().broadcast(() -> {
                // Obtener la caché distribuida
                Cache<Integer, Integer> cache = ignite.cache("numbersCache");

                // Calcular la suma de los valores en la caché
                int totalSum = 0;
                for (Cache.Entry<Integer, Integer> entry : cache) {
                    totalSum += entry.getValue(); // Corregido aquí para asegurar que el valor sea un entero
                }
                return totalSum;
            });

            // Imprimir el resultado
            System.out.println("Suma de los números: " + sum);
        }
    }
}







