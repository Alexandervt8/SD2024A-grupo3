package com.mycompany.sensores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@WebServlet(name = "ConstantServlet", urlPatterns = {"/constants"})
public class ConstantServlet extends HttpServlet {

    private Map<String, Double> constants = new HashMap<>();
    private File xmlFile;
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
            loadConstantsFromFile();
        } catch (Exception e) {
            throw new ServletException("Error initializing servlet or loading constants from file", e);
        }
    }

    private void loadConstantsFromFile() throws ServletException {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            URL resource = classLoader.getResource("constants.xml");

            if (resource == null) {
                throw new ServletException("Archivo constants.xml no encontrado en resources");
            }
            xmlFile = new File(resource.toURI());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            NodeList constantNodes = doc.getElementsByTagName("constante");
            for (int i = 0; i < constantNodes.getLength(); i++) {
                Element constantElement = (Element) constantNodes.item(i);
                String name = constantElement.getElementsByTagName("nombre").item(0).getTextContent();
                String valueStr = constantElement.getElementsByTagName("valor").item(0).getTextContent().trim();

                if (!valueStr.isEmpty()) {
                    Double value = Double.parseDouble(valueStr);
                    constants.put(name, value);
                } else {
                    throw new ServletException("Valor vacío para la constante: " + name);
                }
            }
        } catch (NumberFormatException | NullPointerException e) {
            throw new ServletException("Error al cargar constantes: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new ServletException("Error loading constants from file", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            loadConstantsFromFile();

            response.setContentType("application/json");
            ObjectMapper mapper = new ObjectMapper();
            PrintWriter out = response.getWriter();
            mapper.writeValue(out, constants);
        } catch (ServletException | IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error interno al cargar las constantes: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Enable CORS headers
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Leer el contenido de la solicitud
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
        } catch (IOException ex) {
            throw new ServletException("Error reading the request payload", ex);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw new ServletException("Error closing the buffer reader", ex);
                }
            }
        }

        // Log the received payload
        String payload = stringBuilder.toString();
        System.out.println("Received JSON payload: " + payload);

        // Parse the JSON payload
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Double> newConstants;
        try {
            newConstants = mapper.readValue(payload, new TypeReference<Map<String, Double>>() {});
            constants.putAll(newConstants);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            saveConstantsToFile();
            //saveConstantsToDatabase(newConstants); // Save to the database
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error saving constants to file or database: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Respond with success message
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.println("Constants updated successfully.");
    }

    private void saveConstantsToFile() {
        try {
            // Obtener la ubicación del archivo XML en el directorio de clases
            File xmlFile1 = new File(getClass().getClassLoader().getResource("constants.xml").toURI());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile1);

            NodeList constantNodes = doc.getElementsByTagName("constante");
            for (int i = 0; i < constantNodes.getLength(); i++) {
                Element constantElement = (Element) constantNodes.item(i);
                String name = constantElement.getElementsByTagName("nombre").item(0).getTextContent();
                if (constants.containsKey(name)) {
                    constantElement.getElementsByTagName("valor").item(0).setTextContent(constants.get(name).toString());
                }
            }

            // Guardar el documento en el archivo original
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile1);
            transformer.transform(source, result);

            // Copiar el archivo actualizado a src/main/resources
            Path basePath = Paths.get("").toAbsolutePath();
            String rutaxml = "C:\\Users\\avalt\\OneDrive\\Documentos\\NetBeansProjects\\sensores";
            File srcMainResourcesFile = new File(rutaxml + "/src/main/resources/constants.xml");
            copyFile(xmlFile1, srcMainResourcesFile);

            // Copiar el archivo actualizado a target/classes
            File targetClassesFile = new File(rutaxml + "/target/classes/constants.xml");
            copyFile(xmlFile1, targetClassesFile);

            System.out.println("Archivo XML actualizado y copiado a src/main/resources y target/classes");
        } catch (Exception e) {
            e.printStackTrace();
            // Manejar la excepción según sea necesario, por ejemplo:
            // throw new RuntimeException("Error al guardar constantes en archivo XML", e);
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    private void saveConstantsToDatabase(Map<String, Double> newConstants) {
        for (Map.Entry<String, Double> entry : newConstants.entrySet()) {
            String sql = "INSERT INTO ConstantsTable (nombre, valor) VALUES (?, ?) " +
                         "ON DUPLICATE KEY UPDATE valor = ?";
            ignite.cache("ConstantsCache").query(new SqlFieldsQuery(sql)
                .setArgs(entry.getKey(), entry.getValue(), entry.getValue()));
        }
        System.out.println("Constants saved to database.");
    }
    
}





