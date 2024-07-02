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

public class VariableClient {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("VariableClient - Usar: java VariableClient <xml_file>");
            return;
        }

        String xmlFile = args[0];

        try {
            List<VariableData> variableDataList = readVariableDataFromXml(xmlFile);
            sendVariableDataToServer(variableDataList);
        } catch (JAXBException e) {
            System.err.println("VariableClient - Error al procesar el archivo XML: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<VariableData> readVariableDataFromXml(String filePath) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Estacion.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Estacion estacion = (Estacion) unmarshaller.unmarshal(new File(filePath));
        
        List<VariableData> variableDataList = estacion.getSensores();
        return variableDataList;
    }
    
    private static void sendVariableDataToServer(List<VariableData> variableDataList) {
        try {
            // Convertir lista a JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // Registrar el módulo JavaTimeModule
            String json = mapper.writeValueAsString(variableDataList);
            
            // Imprimir el JSON que se enviará al servidor
            System.out.println("VariableClient - JSON a enviar al servidor: " + json);

            // Conectar al servlet
            URL url = new URL("http://localhost:8080/sensores/variables");
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
                System.out.println("VariableClient - Datos enviados correctamente al servidor.");
            } else {
                System.err.println("VariableClient - Error al enviar datos al servidor. Código de respuesta: " + responseCode + " - " + con.getResponseMessage());
            }
        } catch (Exception e) {
            System.err.println("VariableClient - Error al enviar datos al servidor: " + e.getMessage());
        }
    }
}


