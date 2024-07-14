package com.mycompany.sensores;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
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
import java.util.Arrays;
import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;

@WebServlet(name = "CultivosServlet", urlPatterns = {"/cultivos"})
public class CultivosServlet extends HttpServlet {
    private Ignite ignite;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            ignite = IgniteUtil.getIgniteInstance();

            // Asegurarse de que la caché CultivosCache exista
            if (ignite.cache("CultivosCache") == null) {
                CacheConfiguration<Long, CultivoData> cacheCfg = new CacheConfiguration<>("CultivosCache");
                ignite.createCache(cacheCfg);
            }
            createCultivoDataTableIfNotExists();
            insertInitialData();

            System.out.println("CultivosServlet - Servlet iniciado correctamente.");
        } catch (Exception e) {
            throw new ServletException("CultivosServlet - Error al iniciar Ignite", e);
        }
    }
    
    private void createCultivoDataTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS CultivoData (" +
                "    id LONG PRIMARY KEY, " +
                "    region VARCHAR, " +
                "    cultivo VARCHAR, " +
                "    variedad VARCHAR, " +
                "    fecha_siembra VARCHAR, " +
                "    fecha_cosecha VARCHAR, " +
                "    siembra_a_emergencia_dias INT, " +
                "    emergencia_a_floracion_dias INT, " +
                "    floracion_a_maduracion_dias INT, " +
                "    maduracion_a_cosecha_dias INT, " +
                "    kc_inicial DECIMAL(3,2), " +
                "    kc_floracion DECIMAL(3,2), " +
                "    kc_fin_ciclo DECIMAL(3,2) " +
                ") WITH \"template=replicated\"";

        try {
            ignite.cache("CultivosCache").query(new SqlFieldsQuery(sql)).getAll();
        } catch (Exception e) {
            throw new RuntimeException("CultivosServlet - Error creando CultivoData table", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Agregar encabezados CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Manejo de solicitudes POST si es necesario
        response.setContentType("text/plain");
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.println("CultivosServlet - Solicitud POST recibida.");
    }
    
    private void insertInitialData() {
        List<CultivoData> initialData = Arrays.asList(
            // Sierra Norte
            new CultivoData(1, "Sierra Norte", "Papa", "Canchan-INIA", "Nov-Dic", "Abrl-May", 25, 15, 20, 70, 0.6, 1.1, 0.4),
            new CultivoData(2, "Sierra Norte", "Papa", "Amarilis", "Nov-Dic", "Abrl-May", 25, 30, 20, 75, 0.6, 1.1, 0.4),
            new CultivoData(3, "Sierra Norte", "Papa", "Perrricholi-INIA", "Nov-Dic", "Abrl-May", 25, 30, 25, 80, 0.6, 1.1, 0.4),
            new CultivoData(4, "Sierra Norte", "Papa", "Serranita-INIA", "Nov-Dic", "Abrl-May", 25, 30, 25, 80, 0.6, 1.1, 0.4),
            new CultivoData(5, "Sierra Norte", "Trigo", "Moray", "Nov-Dic", "Abrl-May", 10, 50, 90, 30, 0.15, 0.4, 0.9),
            new CultivoData(6, "Sierra Norte", "Maíz Amiláceo", "INIA 603", "Oct-Nov", "May-Jun", 8, 92, 33, 67, 0.6, 0.7, 0.6),
            new CultivoData(7, "Sierra Norte", "Frijol", "Varios", "Oct-Nov", "Ene-Feb", 5, 60, 25, 85, 0.4, 1.15, 0.35),
            new CultivoData(8, "Sierra Norte", "Maíz Choclo", "INIA 603", "Oct-Nov", "Abrl-May", 8, 92, 33, 27, 0.6, 0.7, 0.6),
            new CultivoData(9, "Sierra Norte", "Cebada", "Var. Liberadas", "Nov-Dic", "Abrl-May", 8, 42, 38, 42, 0.15, 1.1, 0.15),       

            // Sierra Central
            new CultivoData(10, "Sierra Central", "Papa", "Canchan-INIA", "Nov-Dic", "Abrl-May", 25, 15, 20, 70, 0.6, 1.1, 0.4),
            new CultivoData(11, "Sierra Central", "Papa", "Amarilis", "Nov-Dic", "Abrl-May", 25, 30, 20, 75, 0.6, 1.1, 0.4),
            new CultivoData(12, "Sierra Central", "Papa", "Perrricholi-INIA", "Nov-Dic", "Abrl-May", 25, 30, 25, 80, 0.6, 1.1, 0.4),
            new CultivoData(13, "Sierra Central", "Papa", "Serranita-INIA", "Nov-Dic", "Abrl-May", 25, 30, 25, 80, 0.6, 1.1, 0.4),
            new CultivoData(14, "Sierra Central", "Trigo", "Var. Liberadas", "Nov-Dic", "Abrl-May", 15, 40, 20, 15, 0.30, 1.15, 0.40),
            new CultivoData(15, "Sierra Central", "Maíz Amiláceo", "Blanco", "Ago-Set", "Abrl-May", 10, 100, 30, 100, 0.60, 0.70, 0.60),
            new CultivoData(16, "Sierra Central", "Arveja", "INIA Remate", "Noviembre", "Febrero", 7, 73, 27, 43, 0.50, 1.15, 1.10),
            new CultivoData(17, "Sierra Central", "Haba", "Var. Liberadas", "Set-Oct", "Abril", 10, 50, 32, 28, 0.15, 1.10, 0.20),
            new CultivoData(18, "Sierra Central", "Maiz choclo", "Blanco Urubamba", "Agosto", "Mayo", 10, 100, 30, 100, 0.70, 1.15, 1.05),
            new CultivoData(19, "Sierra Central", "Cebada", "La Milagrosa", "Dic-Ene", "Mzo-Abrl", 10, 60, 40, 30, 0.30, 1.15, 1.40),

            // Sierra Sur
            new CultivoData(20, "Sierra Sur", "Papa", "Canchan-INIA", "Nov-Dic", "Abrl-May", 25, 15, 20, 70, 0.6, 1.1, 0.4),
            new CultivoData(21, "Sierra Sur", "Papa", "Amarilis", "Nov-Dic", "Abrl-May", 25, 30, 20, 75, 0.6, 1.1, 0.4),
            new CultivoData(22, "Sierra Sur", "Papa", "Perrricholi-INIA", "Nov-Dic", "Abrl-May", 25, 30, 25, 80, 0.6, 1.1, 0.4),
            new CultivoData(23, "Sierra Sur", "Papa", "Serranita-INIA", "Nov-Dic", "Abrl-May", 25, 30, 25, 80, 0.6, 1.1, 0.4),
            new CultivoData(24, "Sierra Sur", "Trigo", "Var. Liberadas", "Oct-Nov", "Abrl", 10, 60, 25, 85, 1.15, 0.30, 1.00),
            new CultivoData(25, "Sierra Sur", "Maíz Amiláceo", "Blanco", "Ago-Set", "Abrl", 10, 100, 30, 100, 0.60, 0.70, 0.60),
            new CultivoData(26, "Sierra Sur", "Avena", "Varios", "Dic-Ene", "Mzo-Abrl", 10, 60, 40, 30, 0.30, 1.15, 1.40),
            new CultivoData(27, "Sierra Sur", "Quinua", "Var. Pasankalla", "Jun-Jul", "Set-Oct", 7, 14, 28, 56, 0.30, 0.95, 0.85),
            new CultivoData(28, "Sierra Sur", "Quinua", "Var. 415 Rosada", "Jun-Jul", "Set-Oct", 7, 21, 49, 56, 0.40, 0.80, 0.40),
            new CultivoData(29, "Sierra Sur", "Maíz Choclo", "Blanco Urubamba", "Agosto", "Mayo", 10, 100, 30, 80, 0.70, 1.15, 1.05),
            
            // Costa Norte
            new CultivoData(30, "Costa Norte", "Arroz", "Mallares", "Noviembre", "Marzo", 8, 55, 55, 30, 1.0, 1.2, 0.9),
            new CultivoData(31, "Costa Norte", "Arroz", "Tinajones", "Noviembre", "Marzo", 8, 55, 50, 30, 1.0, 1.2, 0.9),
            new CultivoData(32, "Costa Norte", "Arroz", "IR 43", "Noviembre", "Marzo", 8, 55, 55, 30, 1.0, 1.2, 0.9),
            new CultivoData(33, "Costa Norte", "Arroz", "Iris", "todo el año", "todo el año", 15, 205, 14, 6, 0.9, 1.1, 1.0),
            new CultivoData(34, "Costa Norte", "Maíz", "INIA 619 - Megahíbrido", "verano", "", 6, 60, 20, 59, 0.8, 1.8, 0.9),
            new CultivoData(35, "Costa Norte", "Maíz", "INIA 619 - Megahíbrido", "invierno", "", 8, 70, 25, 62, 0.8, 1.8, 0.9),
            new CultivoData(36, "Costa Norte", "Frijol", "Varios", "Oct-Nov", "Dic-Ene", 8, 22, 18, 50, 0.15, 1.1, 0.25),
            new CultivoData(37, "Costa Norte", "Frijol", "Blanco Larán", "Mzo-Jul", "May-Set", 10, 25, 20, 35, 0.15, 1.1, 0.25),
            new CultivoData(38, "Costa Norte", "Camote", "Benjamin/Jhonathan", "Set-Oct", "Ener-Feb", 5, 40, 45, 50, 0.5, 1.15, 0.65),
            new CultivoData(39, "Costa Norte", "Ají", "Escabeche Panca y Rocoto", "Julio-Set", "Enet-Marz", 10, 70, 60, 50, 0.60, 1.05, 0.90),

            // Costa Central
            new CultivoData(40, "Costa Central", "Papa", "Canchan-INIA", "Mayo-Junio", "Set-Oct", 20, 25, 20, 60, 0.6, 1.1, 0.4),
            new CultivoData(41, "Costa Central", "Papa", "Amarilis-INIA", "Mayo-Junio", "Set-Oct", 25, 25, 20, 70, 0.6, 1.1, 0.4),
            new CultivoData(42, "Costa Central", "Papa", "Unica", "Mayo-Junio", "Set-Oct", 25, 25, 25, 75, 0.6, 1.1, 0.4),
            new CultivoData(43, "Costa Central", "Yuca", "Iris", "todo el año", "todo el año", 15, 205, 14, 6, 0.9, 1.1, 1.0),
            new CultivoData(44, "Costa Central", "Arroz", "Mallares", "Noviembre", "Marzo", 8, 55, 55, 30, 1.0, 1.2, 0.9),
            new CultivoData(45, "Costa Central", "Arroz", "Tinajones", "Noviembre", "Marzo", 8, 55, 50, 30, 1.0, 1.2, 0.9),
            new CultivoData(46, "Costa Central", "Arroz", "IR 43", "Noviembre", "Marzo", 8, 55, 55, 30, 1.0, 1.2, 0.9),
            new CultivoData(47, "Costa Central", "Maíz Choclo", "Blanco Urubamba", "Mrzo-Abrl", "Jul-Ago", 5, 105, 25, 30, 0.6, 0.7, 0.7),
            new CultivoData(48, "Costa Central", "Maíz Choclo", "Blanco Urubamba", "Mrzo-Abrl", "Jul-Ago", 10, 110, 30, 30, 0.7, 0.8, 0.8),
            new CultivoData(49, "Costa Central", "Maíz Amarillo Duro", "INIA 611 - NutriPerú", "Jun-Jul", "Oct-Nov", 8, 70, 25, 62, 0.8, 1.8, 0.9),
            new CultivoData(50, "Costa Central", "Camote", "Jhonathan", "Oct-Nov", "Feb-Mzo", 5, 30, 40, 45, 0.5, 1.15, 0.65),
            new CultivoData(51, "Costa Central", "Tomate", "Hibridos, tipo Rio Grande Dominator", "Jul-Set", "Nov-Mar", 10, 55, 55, 30, 0.6, 1.15, 0.8),

            // Costa Sur
            new CultivoData(52, "Costa Sur", "Arroz", "Mallares", "Dic-Jul", "May-Nov", 5, 60, 30, 30, 1.0, 1.2, 0.9),
            new CultivoData(53, "Costa Sur", "Arroz", "Tinajones", "Dic-Jul", "May-Nov", 5, 65, 30, 40, 1.0, 1.2, 0.9),
            new CultivoData(54, "Costa Sur", "Arroz", "IR", "Dic-Jul", "May-Nov", 7, 85, 30, 30, 1.0, 1.2, 0.9),
            new CultivoData(55, "Costa Sur", "Cebolla", "Americana perilla y camaneja", "Abrl-Nov", "Dic-Abril", 10, 72, 80, 30, 0.7, 1.05, 0.75),
            new CultivoData(56, "Costa Sur", "Cebolla", "Americana perilla y camaneja", "Abrl-Nov", "Dic-Abril", 8, 65, 90, 30, 0.7, 1.05, 0.75),
            new CultivoData(57, "Costa Sur", "Ajo", "Napuri y Criollo", "Abrl-May", "Nov-Dic", 8, 82, 90, 30, 0.7, 1.0, 0.7),
            new CultivoData(58, "Costa Sur", "Zapallo", "Macre Mejorado", "Abrl-Jul", "Ago-Dic", 10, 80, 20, 40, 0.5, 1.0, 0.8),

            // Selva
            new CultivoData(59, "Selva", "Platano", "Harton o Bellaco", "Mar-Jun y Set-Nov", "todo el año", 30, 120, 100, 90, 0.9, 1.1, 1.0),
            new CultivoData(60, "Selva", "Platano", "Isla", "Mar-Jun y Set-Nov", "todo el año", 30, 90, 90, 60, 0.9, 1.1, 1.0),
            new CultivoData(61, "Selva", "Platano", "Seda", "Abrl-Jun", "todo el año", 30, 270, 14, 16, 0.9, 1.1, 1.0),
            new CultivoData(62, "Selva", "Yuca", "Señorita", "todo el año", "todo el año", 15, 205, 14, 6, 0.9, 1.1, 1.0),
            new CultivoData(63, "Selva", "Arroz", "Capirona", "Ene-Jul", "May-Nov", 8, 55, 40, 35, 1.05, 1.2, 1.0),
            new CultivoData(64, "Selva", "Arroz", "La Conquista", "Ene-Jul", "May-Nov", 8, 55, 35, 35, 1.05, 1.2, 1.0),
            new CultivoData(65, "Selva", "Arroz", "La Esperanza", "Ene-Jul", "May-Nov", 8, 55, 40, 35, 1.05, 1.2, 1.0),
            new CultivoData(66, "Selva", "Arroz", "Capirona", "Ene-Jul", "May-Nov", 8, 60, 50, 35, 1.05, 1.2, 1.0),
            new CultivoData(67, "Selva", "Arroz", "La Conquista", "Ene-Jul", "May-Nov", 8, 60, 45, 35, 1.05, 1.2, 1.0),
            new CultivoData(68, "Selva", "Arroz", "La Esperanza", "Ene-Jul", "May-Nov", 8, 60, 50, 35, 1.05, 1.2, 1.0),
            new CultivoData(69, "Selva", "Café", "Tipica", "Oct-Dic", "Mrz-Ago", 65, 90, 120, 85, 1.05, 1.1, 1.1),
            new CultivoData(70, "Selva", "Frijol", "Ucayalino", "Mar-May", "Jul-Ago", 8, 22, 18, 50, 0.15, 1.1, 0.25),
            new CultivoData(71, "Selva", "Maíz Amarillo Duro", "Marginal 28T", "Feb-Set", "May-Dic", 5, 45, 15, 55, 0.7, 1.2, 0.35),
            new CultivoData(72, "Selva", "Ají", "Escabeche Panca y Rocoto", "Jul-Set", "Ene-Marz", 10, 50, 30, 60, 0.6, 1.05, 0.9)
        );

        insertCultivoData(initialData);
    }

    private void insertCultivoData(List<CultivoData> cultivoDataList) {
        String insertSql = "MERGE INTO CultivoData (id, region, cultivo, variedad, fecha_siembra, fecha_cosecha, " +
                "siembra_a_emergencia_dias, emergencia_a_floracion_dias, floracion_a_maduracion_dias, " +
                "maduracion_a_cosecha_dias, kc_inicial, kc_floracion, kc_fin_ciclo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (CultivoData cultivoData : cultivoDataList) {
            try {
                ignite.cache("CultivosCache").query(new SqlFieldsQuery(insertSql)
                        .setArgs(cultivoData.getId(), cultivoData.getRegion(), cultivoData.getCultivo(), cultivoData.getVariedad(),
                                cultivoData.getFechaSiembra(), cultivoData.getFechaCosecha(), cultivoData.getSiembraAEmergenciaDias(),
                                cultivoData.getEmergenciaAFloracionDias(), cultivoData.getFloracionAMaduracionDias(),
                                cultivoData.getMaduracionACosechaDias(), cultivoData.getKcInicial(), cultivoData.getKcFloracion(),
                                cultivoData.getKcFinCiclo())).getAll();
            } catch (Exception e) {
                throw new RuntimeException("CultivosServlet - Error inserting cultivo data", e);
            }
        }
        System.out.println("CultivosServlet - " + cultivoDataList.size() + " datos de cultivos insertados correctamente.");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Agregar encabezados CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String sql = "SELECT id, region, cultivo, variedad, fecha_siembra, fecha_cosecha, siembra_a_emergencia_dias, " +
                "emergencia_a_floracion_dias, floracion_a_maduracion_dias, maduracion_a_cosecha_dias, kc_inicial, " +
                "kc_floracion, kc_fin_ciclo FROM CultivoData";
        try {
            List<List<?>> data = ignite.cache("CultivosCache").query(new SqlFieldsQuery(sql)).getAll();

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
