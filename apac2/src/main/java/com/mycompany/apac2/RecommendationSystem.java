
package com.mycompany.apac2;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import java.util.*;
import javax.cache.Cache;

public class RecommendationSystem {
    public static void main(String[] args) {
        // Iniciar un nodo de Ignite
        Ignite ignite = Ignition.start();

        // Configurar caché para productos
        CacheConfiguration<Long, String> productCacheCfg = new CacheConfiguration<>("ProductCache");
        IgniteCache<Long, String> productCache = ignite.getOrCreateCache(productCacheCfg);

        // Configurar caché para usuarios
        CacheConfiguration<Long, String> userCacheCfg = new CacheConfiguration<>("UserCache");
        IgniteCache<Long, String> userCache = ignite.getOrCreateCache(userCacheCfg);

        // Configurar caché para interacciones
        CacheConfiguration<Long, Set<Long>> interactionCacheCfg = new CacheConfiguration<>("InteractionCache");
        IgniteCache<Long, Set<Long>> interactionCache = ignite.getOrCreateCache(interactionCacheCfg);

        // Añadir datos a la caché de productos
        productCache.put(1L, "Product 1");
        productCache.put(2L, "Product 2");
        productCache.put(3L, "Product 3");

        // Añadir datos a la caché de usuarios
        userCache.put(1L, "User 1");
        userCache.put(2L, "User 2");

        // Añadir datos a la caché de interacciones
        interactionCache.put(1L, new HashSet<>(Arrays.asList(1L, 2L))); // User 1 ha interactuado con Product 1 y Product 2
        interactionCache.put(2L, new HashSet<>(Arrays.asList(2L, 3L))); // User 2 ha interactuado con Product 2 y Product 3

        // Ejemplo de recomendación
        System.out.println("Recommendations for User 1: " + recommendProducts(1L, interactionCache, productCache));
    }

    private static String recommendProducts(Long userId, IgniteCache<Long, Set<Long>> interactionCache, IgniteCache<Long, String> productCache) {
        // Obtener los productos con los que ha interactuado el usuario
        Set<Long> userProducts = interactionCache.get(userId);

        // Crear un mapa para contar las interacciones de otros usuarios con productos no vistos por el usuario actual
        Map<Long, Integer> recommendationCounts = new HashMap<>();

        // Iterar sobre todas las entradas de la caché de interacciones
        for (Cache.Entry<Long, Set<Long>> entry : interactionCache) {
            Long otherUserId = entry.getKey();
            Set<Long> otherUserProducts = entry.getValue();

            // Ignorar las interacciones del usuario actual
            if (!otherUserId.equals(userId)) {
                for (Long productId : otherUserProducts) {
                    // Contar solo productos no vistos por el usuario actual
                    if (!userProducts.contains(productId)) {
                        recommendationCounts.put(productId, recommendationCounts.getOrDefault(productId, 0) + 1);
                    }
                }
            }
        }

        // Ordenar los productos recomendados por el número de interacciones
        List<Map.Entry<Long, Integer>> sortedRecommendations = new ArrayList<>(recommendationCounts.entrySet());
        sortedRecommendations.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Construir la lista de recomendaciones
        List<String> recommendations = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : sortedRecommendations) {
            recommendations.add(productCache.get(entry.getKey()));
        }

        return String.join(", ", recommendations);
    }
}

