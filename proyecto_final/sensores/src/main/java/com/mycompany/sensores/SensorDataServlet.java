package com.mycompany.sensores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import com.fasterxml.jackson.core.type.TypeReference;

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

            System.out.println("Servlet iniciado correctamente.");
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Leer los datos JSON del cuerpo de la solicitud
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // Registrar el módulo para manejar tipos de fecha/hora Java 8
            List<SensorData> sensorDataList = mapper.readValue(request.getInputStream(), new TypeReference<List<SensorData>>() {});

            // Insertar datos en la base de datos de Ignite
            insertSensorData(sensorDataList);

            // Devolver respuesta exitosa
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.println("Datos recibidos y almacenados correctamente.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            e.printStackTrace(out); // Imprimir el stack trace en el PrintWriter
            out.println("Error al procesar los datos: " + e.getMessage());
        }
    }

    private void insertSensorData(List<SensorData> sensorDataList) {
        String insertSql = "MERGE INTO SensorData (id, sensorId, value, timestamp) VALUES (?, ?, ?, ?)";

        for (SensorData sensorData : sensorDataList) {
            try {
                ignite.cache("SensorDataCache").query(new SqlFieldsQuery(insertSql)
                        .setArgs(sensorData.getId(), sensorData.getSensorId(), sensorData.getValue(), Timestamp.valueOf(sensorData.getTimestamp()))).getAll();
            } catch (Exception e) {
                throw new RuntimeException("Error inserting sensor data", e);
            }
        }

        System.out.println(sensorDataList.size() + " datos de sensores insertados correctamente.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String sql = "SELECT * FROM SensorData";
        List<List<?>> data = ignite.cache("SensorDataCache").query(new SqlFieldsQuery(sql)).getAll();

        // Convertir los datos a JSON
        ObjectMapper mapper = new ObjectMapper();
        out.println(mapper.writeValueAsString(data));
    }

    @Override
    public void destroy() {
        if (ignite != null) {
            Ignition.stop(true); // Detener Ignite cuando el servlet se destruye
        }
        super.destroy();
    }
}
