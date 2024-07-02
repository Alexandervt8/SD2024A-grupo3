package com.mycompany.sensores;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(name = "FileUploadServlet", urlPatterns = {"/upload"})
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

    private static final String MAIN_DIR = "estaciones";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Obtener el nombre de la estación desde la solicitud
        String stationName = request.getParameter("stationName");
        if (stationName == null || stationName.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing or empty stationName parameter");
            return;
        }

        // Obtener el archivo cargado
        Part filePart = request.getPart("file");
        if (filePart == null || filePart.getSize() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing or empty file parameter");
            return;
        }

        // Obtener el nombre del archivo a partir del Part
        String fileName = getFileName(filePart);
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String newFileName = timeStamp + "_" + fileName;

        // Crear el directorio principal si no existe
        String applicationPath = request.getServletContext().getRealPath("");
        String mainDirPath = applicationPath + File.separator + MAIN_DIR;
        File mainDir = new File(mainDirPath);
        if (!mainDir.exists()) {
            mainDir.mkdirs();
        }

        // Crear el directorio de la estación si no existe
        String stationDirPath = mainDirPath + File.separator + stationName;
        File stationDir = new File(stationDirPath);
        if (!stationDir.exists()) {
            stationDir.mkdirs();
        }

        // Guardar el archivo en el servidor
        String filePath = stationDirPath + File.separator + newFileName;
        File file = new File(filePath);

        try {
            // Verificar si el archivo ya existe
            if (file.exists()) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("El archivo ya existe. Confirme la sobrescritura.");
                return;
            }

            // Guardar el archivo, reemplazando si existe
            Files.copy(filePart.getInputStream(), file.toPath());

        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error al guardar el archivo: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Responder con la ruta del archivo en el servidor
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(file.getAbsolutePath());
    }

    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String token : contentDisposition.split(";")) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 2, token.length() - 1);
            }
        }
        return "";
    }
}

