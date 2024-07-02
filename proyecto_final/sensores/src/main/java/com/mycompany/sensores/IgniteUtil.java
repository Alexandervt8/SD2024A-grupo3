package com.mycompany.sensores;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;

public class IgniteUtil {
    private static Ignite igniteInstance;

    private IgniteUtil() {
        // Constructor privado para prevenir la instancia directa
    }

    public static Ignite getIgniteInstance() {
        if (igniteInstance == null) {
            synchronized (IgniteUtil.class) {
                if (igniteInstance == null) {
                    try {
                        igniteInstance = Ignition.start("META-INF/ignite.xml");
                    } catch (Exception e) {
                        throw new RuntimeException("Error al iniciar Ignite", e);
                    }
                }
            }
        }
        return igniteInstance;
    }

    public static void stopIgniteInstance() {
        if (igniteInstance != null) {
            igniteInstance.close();
            igniteInstance = null;
        }
    }
}
