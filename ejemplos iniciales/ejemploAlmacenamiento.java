package ejemplo;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.Scanner;

public class ejemploAlmacenamiento {
    public static void main(String[] args) {
        // Iniciar Ignite con la configuración específica
        Ignite ignite = null;
        try {
            ignite = Ignition.start("examples/config/example-ignite.xml");

            // Configuración de la caché
            CacheConfiguration<Integer, String> cfg = new CacheConfiguration<>("myCache");

            // Obtener o crear la caché
            IgniteCache<Integer, String> cache = ignite.getOrCreateCache(cfg);

            // Scanner para leer entrada del usuario
            @SuppressWarnings("resource")
			Scanner scanner = new Scanner(System.in);

            // Menú de operaciones CRUD
            while (true) {
                System.out.println("\nOpciones:");
                System.out.println("1. Crear/Actualizar entrada");
                System.out.println("2. Leer entrada");
                System.out.println("3. Eliminar entrada");
                System.out.println("4. Salir");
                System.out.print("Ingrese opción: ");

                int opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea después de leer la opción

                switch (opcion) {
                    case 1:
                        System.out.print("Ingrese clave (entero): ");
                        int key = scanner.nextInt();
                        scanner.nextLine(); // Consumir el salto de línea después de leer la clave
                        System.out.print("Ingrese valor (cadena): ");
                        String value = scanner.nextLine(); // Leer toda la línea, incluidos espacios
                        cache.put(key, value);
                        System.out.println("Entrada creada/actualizada: clave = " + key + ", valor = " + value);
                        break;
                    case 2:
                        System.out.print("Ingrese clave (entero): ");
                        int readKey = scanner.nextInt();
                        scanner.nextLine(); // Consumir el salto de línea después de leer la clave
                        String retrievedValue = cache.get(readKey);
                        if (retrievedValue != null) {
                            System.out.println("Valor recuperado: clave =" + readKey + ", valor =" + retrievedValue);
                        } else {
                            System.out.println("No se encontró valor para la clave: " + readKey);
                        }
                        break;
                    case 3:
                        System.out.print("Ingrese clave (entero): ");
                        int removeKey = scanner.nextInt();
                        scanner.nextLine(); // Consumir el salto de línea después de leer la clave
                        cache.remove(removeKey);
                        System.out.println("Entrada eliminada para la clave: " + removeKey);
                        break;
                    case 4:
                        // Cerrar Ignite y salir del programa
                        ignite.close();
                        System.out.println("Programa finalizado.");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Opción inválida. Por favor, ingrese una opción válida.");
                }
            }
        } catch (Exception e) {
            // Manejo de excepciones
            System.err.println("Error al ejecutar el programa: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar Ignite en caso de que se haya iniciado
            if (ignite != null) {
                ignite.close();
            }
        }
    }
}

