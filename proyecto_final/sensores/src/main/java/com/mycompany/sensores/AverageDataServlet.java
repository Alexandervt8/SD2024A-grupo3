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

@WebServlet(name = "AverageDataServlet", urlPatterns = {"/average-data"})
public class AverageDataServlet extends HttpServlet {
    private Ignite ignite;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            // Check if an Ignite instance is already running
            ignite = Ignition.ignite("myIgniteInstance");
            if (ignite == null) {
                ignite = Ignition.start("META-INF/ignite.xml");
            }
            System.out.println("Servlet de promedio iniciado correctamente.");
        } catch (Exception e) {
            throw new ServletException("Error al iniciar Ignite", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            String sql = "SELECT AVG(value) FROM SensorData";
            List<List<?>> result = ignite.cache("SensorDataCache").query(new SqlFieldsQuery(sql)).getAll();

            double average = result.isEmpty() || result.get(0).isEmpty() ? 0.0 : (Double) result.get(0).get(0);
            
            out.println("{\"average\": " + average + "}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Error al calcular el promedio: " + e.getMessage() + "\"}");
        }
    }

    @Override
    public void destroy() {
        if (ignite != null) {
            Ignition.stop("myIgniteInstance", true); // Detener Ignite cuando el servlet se destruye
        }
        super.destroy();
    }
}
