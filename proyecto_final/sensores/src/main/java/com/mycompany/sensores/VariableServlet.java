package com.mycompany.sensores;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;

@WebServlet(name = "VariableServlet", urlPatterns = {"/variables"})
public class VariableServlet extends HttpServlet {
    private Ignite ignite;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ignite = IgniteUtil.getIgniteInstance();

            // Asegurarse de que la caché VariableDataCache exista
            if (ignite.cache("VariableDataCache") == null) {
                CacheConfiguration<Long, VariableData> cacheCfg = new CacheConfiguration<>("VariableDataCache");
                ignite.createCache(cacheCfg);
            }
            createVariableDataTableIfNotExists();

            System.out.println("VariableServlet - Servlet iniciado correctamente.");
        } catch (Exception e) {
            throw new ServletException("VariableServlet - Error al iniciar Ignite", e);
        }
    }

    private void createVariableDataTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS VariableData (" +
                "    id BIGINT PRIMARY KEY, " +
                "    idSen VARCHAR, " +
                "    nombreEst VARCHAR, " +
                "    nombre VARCHAR, " +
                "    valor DOUBLE, " +
                "    unidad VARCHAR, " +
                "    tiempo TIMESTAMP" +
                ") WITH \"template=replicated\"";

        try {
            ignite.cache("VariableDataCache").query(new SqlFieldsQuery(sql)).getAll();
        } catch (Exception e) {
            throw new RuntimeException("VariableServlet - Error creando VariableData table", e);
        }
    }

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Agregar encabezados CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        try {
            // Leer el cuerpo de la solicitud como String
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String requestBody = stringBuilder.toString();

            // Procesar el cuerpo de la solicitud como la ruta del archivo XML
            String xmlFilePath = requestBody.split("=")[1].trim();

            // Leer y procesar el archivo XML
            List<VariableData> variableDataList = readXmlFile(xmlFilePath);

            // Insertar datos en la base de datos de Ignite
            insertVariableData(variableDataList);

            // Devolver respuesta exitosa
            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = response.getWriter();
            out.println("VariableServlet - Datos recibidos y almacenados correctamente.");
            System.out.println("VariableServlet - Datos recibidos y almacenados correctamente.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            e.printStackTrace(out); // Imprimir el stack trace en el PrintWriter
            System.out.println("VariableServlet - Error al procesar los datos: " + e.getMessage());
        }
    }
    
    private List<VariableData> readXmlFile(String xmlFilePath) throws JAXBException, IOException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Estacion.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Estacion estacion = (Estacion) jaxbUnmarshaller.unmarshal(new File(xmlFilePath));

        // Establecer ide y nombreEst en cada VariableData
        List<VariableData> variableDataList = estacion.getSensores();
        for (VariableData variableData : variableDataList) {
            variableData.setIde(estacion.getIde());
            variableData.setNombreEst(estacion.getNombreEst());
        }

        return variableDataList;
    }
    
    private void insertVariableData(List<VariableData> variableDataList) {
        String maxIdQuery = "SELECT COALESCE(MAX(id), 0) FROM VariableData";
        String insertSql = "MERGE INTO VariableData (id, idSen, nombreEst, nombre, valor, unidad, tiempo) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            List<List<?>> maxIdResult = ignite.cache("VariableDataCache").query(new SqlFieldsQuery(maxIdQuery)).getAll();
            long maxId = (long) maxIdResult.get(0).get(0);

            for (VariableData variableData : variableDataList) {
                maxId++;
                ignite.cache("VariableDataCache").query(new SqlFieldsQuery(insertSql)
                        .setArgs(maxId, variableData.getId(), variableData.getNombreEst(),
                                variableData.getNombre(), variableData.getValor(), variableData.getUnidad(),
                                Timestamp.valueOf(variableData.getTiempo()))).getAll();
            }
            System.out.println("VariableServlet - " + variableDataList.size() + " datos de sensores insertados correctamente.");
        } catch (Exception e) {
            throw new RuntimeException("VariableServlet - Error inserting variable data", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Agregar encabezados CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String sql = "SELECT id, idSen, nombreEst, nombre, valor, unidad, tiempo FROM VariableData";
        try {
            List<List<?>> data = ignite.cache("VariableDataCache").query(new SqlFieldsQuery(sql)).getAll();

            // Convertir los datos a JSON
            ObjectMapper mapper = new ObjectMapper();
            out.println(mapper.writeValueAsString(data));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace(out); // Imprimir el stack trace en el PrintWriter
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}

