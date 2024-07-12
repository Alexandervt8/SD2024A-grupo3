package com.mycompany.sensores;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "ETrDataServlet", urlPatterns = {"/etr-data"})
public class ETrDataServlet extends HttpServlet {
    private Ignite ignite;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ignite = Ignition.ignite("myIgniteInstance");
            if (ignite == null) {
                ignite = Ignition.start("META-INF/ignite.xml");
            }
            System.out.println("Servlet de ETrData iniciado correctamente.");
        } catch (Exception e) {
            throw new ServletException("Error al iniciar Ignite", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            Map<String, Double> averages = new HashMap<>();
            String[] sensorTypes = {"temperatura", "humedad_relativa", "radiacion_solar", "velocidad_viento", "presion_atmosferica"};
            
            for (String sensorType : sensorTypes) {
                String sql = "SELECT AVG(valor) FROM VariableData WHERE nombre = ?";
                List<List<?>> result = ignite.cache("VariableDataCache").query(new SqlFieldsQuery(sql).setArgs(sensorType)).getAll();

                Double average = null;
                if (!result.isEmpty() && !result.get(0).isEmpty() && result.get(0).get(0) != null) {
                    average = (Double) result.get(0).get(0);
                }
                
                averages.put(sensorType, average != null ? average : 0.0);
            }
            
            ObjectMapper mapper = new ObjectMapper();
            out.println(mapper.writeValueAsString(averages));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Error al calcular los promedios: " + e.getMessage() + "\"}");
        }
    }

    @Override
    public void destroy() {
        if (ignite != null) {
            Ignition.stop("myIgniteInstance", true);
        }
        super.destroy();
    }
}

