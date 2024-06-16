package com.mycompany.sensores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class SensorClient {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SensorClient <xml_file>");
            return;
        }

        String xmlFile = args[0];

        try {
            List<SensorData> sensorDataList = readSensorDataFromXml(xmlFile);
            sendSensorDataToServer(sensorDataList);
        } catch (JAXBException e) {
            System.err.println("Error al procesar el archivo XML: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<SensorData> readSensorDataFromXml(String filePath) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(SensorReadings.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        SensorReadings readings = (SensorReadings) unmarshaller.unmarshal(new File(filePath));
        return readings.getSensors();
    }

    private static void sendSensorDataToServer(List<SensorData> sensorDataList) {
        try {
            // Convertir lista a JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // Registrar el módulo JavaTimeModule
            String json = mapper.writeValueAsString(sensorDataList);
            
            // Imprimir el JSON que se enviará al servidor
            System.out.println("JSON a enviar al servidor: " + json);

            // Conectar al servlet
            URL url = new URL("http://localhost:8080/sensores/sensor-data");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            // Enviar datos JSON
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = json.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Datos enviados correctamente al servidor.");
            } else {
                System.err.println("Error al enviar datos al servidor. Código de respuesta: " + responseCode + " - " + con.getResponseMessage());
            }
        } catch (Exception e) {
            System.err.println("Error al enviar datos al servidor: " + e.getMessage());
        }
    }
}

@XmlRootElement(name = "SensorReadings")
class SensorReadings {
    private List<SensorData> sensors;

    @XmlElementWrapper(name = "sensors")
    @XmlElement(name = "sensor")
    public List<SensorData> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorData> sensors) {
        this.sensors = sensors;
    }
}


