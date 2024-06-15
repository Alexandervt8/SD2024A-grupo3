package com.mycompany.sensores;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;

@WebServlet(name = "SensorDataServlet", urlPatterns = {"/sensor-data"})
public class SensorDataServlet extends HttpServlet {
    private Ignite ignite;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ignite = Ignition.start("META-INF/ignite.xml");

            // Crear la tabla SensorData si no existe
            createSensorDataTableIfNotExists();

            // Insertar datos de prueba
            String insertSql = "INSERT INTO SensorData (id, sensorId, value, timestamp) VALUES (?, ?, ?, ?)";
            ignite.cache("SensorDataCache").query(new SqlFieldsQuery(insertSql)
                    .setArgs(1L, "sensor1", 23.5, Timestamp.valueOf(LocalDateTime.now()))).getAll();
            ignite.cache("SensorDataCache").query(new SqlFieldsQuery(insertSql)
                    .setArgs(2L, "sensor2", 27.8, Timestamp.valueOf(LocalDateTime.now()))).getAll();

            System.out.println("Datos de prueba insertados correctamente.");
        } catch (Exception e) {
            throw new ServletException("Error al iniciar Ignite", e);
        }
    }

    private void createSensorDataTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS SensorData (" +
                "    id LONG PRIMARY KEY, " +
                "    sensorId VARCHAR, " +
                "    value DOUBLE, " +
                "    timestamp TIMESTAMP" +
                ") WITH \"template=replicated\"";

        try {
            ignite.cache("SensorDataCache").query(new SqlFieldsQuery(sql)).getAll();
        } catch (Exception e) {
            throw new RuntimeException("Error creating SensorData table", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (ignite == null) {
            throw new ServletException("Ignite no se ha iniciado correctamente. Verifica la configuración.");
        }

        // Ejemplo de consulta SQL para obtener datos de sensores desde Ignite
        String sql = "SELECT sensorId, value, timestamp FROM SensorData";
        try {
            List<List<?>> result = ignite.cache("SensorDataCache").query(new SqlFieldsQuery(sql)).getAll();

            // Generar la respuesta en formato JSON
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.println("[");
            boolean first = true;
            for (List<?> row : result) {
                if (!first) {
                    out.println(",");
                }
                out.println("{");
                out.println("\"sensorId\": \"" + row.get(0) + "\",");
                out.println("\"value\": " + row.get(1) + ",");
                out.println("\"timestamp\": \"" + row.get(2) + "\"");
                out.println("}");
                first = false;
            }
            out.println("]");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.println("Error al obtener datos de sensores: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        if (ignite != null) {
            Ignition.stop(true); // Detener Ignite cuando el servlet se destruye
        }
        super.destroy();
    }
}
