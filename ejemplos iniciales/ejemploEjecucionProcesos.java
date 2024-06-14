package ejemplo;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

public class ejemploEjecucionProcesos {
    public static void main(String[] args) {
        Ignite ignite = null;
        try {
            ignite = Ignition.start("examples/config/example-ignite.xml");

            final Ignite finalIgnite = ignite; // Declarar finalIgnite para poder usarlo dentro de la lambda

            // Número de procesos a ejecutar
            int numProcesos = 5;

            ignite.compute().broadcast(() -> {
                String nodeId = finalIgnite.cluster().localNode().id().toString(); // Obtener el ID del nodo local
                for (int i = 1; i <= numProcesos; i++) {
                    // Simular un proceso
                    System.out.println("Proceso " + i + " ejecutándose en el nodo: " + nodeId);
                    // Puedes agregar aquí tu lógica de proceso
                    try {
                        Thread.sleep(1000); // Simular 1 segundo de procesamiento
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("Error al ejecutar el proceso: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (ignite != null) {
                Ignition.stop(ignite.name(), true);
            }
        }
    }
}
