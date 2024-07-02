package com.mycompany.sensores;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@WebServlet(name = "ClientStartServlet", urlPatterns = {"/start-client"})
public class ClientStartServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String xmlFilePath = request.getParameter("xmlFilePath");

        if (xmlFilePath == null || xmlFilePath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing or empty xmlFilePath parameter");
            return;
        }

        try {
            // Iniciar el cliente con el archivo XML proporcionado
            SensorClient.main(new String[]{xmlFilePath});

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Cliente inicio satisfactoriamente con archivo: " + xmlFilePath);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error no se inicio el cliente: " + e.getMessage());
        }
    }
}

