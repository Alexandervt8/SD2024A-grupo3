
package com.mycompany.sensores;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.BufferedReader;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/calculateETo")
public class EToServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        JsonObject jsonObject = null;

        try {
            jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid JSON\"}");
            return;
        } finally {
            reader.close();
        }

        double lambda = jsonObject.get("lambda").getAsDouble();
        double delta = jsonObject.get("delta").getAsDouble();
        double rho = jsonObject.get("rho").getAsDouble();
        double cp = jsonObject.get("cp").getAsDouble();
        double gamma = jsonObject.get("gamma").getAsDouble();

        double valorRadiacionSolar = jsonObject.get("radiacionSolar").getAsDouble();
        double valorTemperatura = jsonObject.get("temperatura").getAsDouble();
        double valorVelocidadViento = jsonObject.get("velocidadViento").getAsDouble();
        double valorPresionAtmosferica = jsonObject.get("presionAtmosferica").getAsDouble();
        double valorHumedadRelativa = jsonObject.get("humedadRelativa").getAsDouble();

        EToCalculos calculos = new EToCalculos();
        double eto = calculos.calculateETr(lambda, delta, rho, cp, valorRadiacionSolar, valorTemperatura,
                                           valorVelocidadViento, valorPresionAtmosferica, valorHumedadRelativa);

        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("eto", eto);

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }

    public class EToCalculos {
        public double calculateETr(double lambda, double delta, double rho, double cp,
                                   double valorRadiacionSolar, double valorTemperatura, double valorVelocidadViento,
                                   double valorPresionAtmosferica, double valorHumedadRelativa) {
            // Convertir unidades y definir constantes
            double Rn = valorRadiacionSolar; // Radiación neta (MJ/m²/día)
            double G = 0.0; // Calor del suelo (MJ/m²/día), supuesto 0 en este ejemplo
            double T = valorTemperatura; // Temperatura del aire (°C)
            double u2 = valorVelocidadViento; // Velocidad del viento a 2m (m/s)
            double es = 0.6108 * Math.exp((17.27 * T) / (T + 237.3)); // Presión de vapor de saturación (kPa)
            double ea = (valorHumedadRelativa / 100) * es; // Presión de vapor real (kPa)
            double VPD = es - ea; // Deficiencia de presión de vapor (kPa)
            double gama = cp * valorPresionAtmosferica / (0.622 * lambda); // Constante psicrométrica (kPa/°C)

            // Fórmula de FAO Penman-Monteith
            double eto = (0.408 * delta * (Rn - G) + gama * (900 / (T + 273)) * u2 * VPD) / (delta + gama * (1 + 0.34 * u2));

            return eto;
        }
    }
}

