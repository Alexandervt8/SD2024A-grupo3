package com.mycompany.sensores;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@XmlRootElement(name = "cultivo")
public class CultivoData implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String region;
    private String cultivo;
    private String variedad;
    private String fechaSiembra;
    private String fechaCosecha;
    private int siembraAEmergenciaDias;
    private int emergenciaAFloracionDias;
    private int floracionAMaduracionDias;
    private int maduracionACosechaDias;
    private double kcInicial;
    private double kcFloracion;
    private double kcFinCiclo;
    
    public CultivoData() {
        // Constructor vacío necesario para serialización
    }
    
    public CultivoData(int id, String region, String cultivo, String variedad, 
                       String fechaSiembra, String fechaCosecha, 
                       int siembraAEmergenciaDias, int emergenciaAFloracionDias, 
                       int floracionAMaduracionDias, int maduracionACosechaDias, 
                       double kcInicial, double kcFloracion, double kcFinCiclo) {
        this.id = id;
        this.region = region;
        this.cultivo = cultivo;
        this.variedad = variedad;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
        this.siembraAEmergenciaDias = siembraAEmergenciaDias;
        this.emergenciaAFloracionDias = emergenciaAFloracionDias;
        this.floracionAMaduracionDias = floracionAMaduracionDias;
        this.maduracionACosechaDias = maduracionACosechaDias;
        this.kcInicial = kcInicial;
        this.kcFloracion = kcFloracion;
        this.kcFinCiclo = kcFinCiclo;
    }
    
    @XmlElement(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement(name = "region")
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @XmlElement(name = "cultivo")
    public String getCultivo() {
        return cultivo;
    }

    public void setCultivo(String cultivo) {
        this.cultivo = cultivo;
    }

    @XmlElement(name = "variedad")
    public String getVariedad() {
        return variedad;
    }

    public void setVariedad(String variedad) {
        this.variedad = variedad;
    }

    @XmlElement(name = "fecha_siembra")
    public String getFechaSiembra() {
        return fechaSiembra;
    }

    public void setFechaSiembra(String fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }

    @XmlElement(name = "fecha_cosecha")
    public String getFechaCosecha() {
        return fechaCosecha;
    }

    public void setFechaCosecha(String fechaCosecha) {
        this.fechaCosecha = fechaCosecha;
    }

    @XmlElement(name = "siembra_a_emergencia_dias")
    public int getSiembraAEmergenciaDias() {
        return siembraAEmergenciaDias;
    }

    public void setSiembraAEmergenciaDias(int siembraAEmergenciaDias) {
        this.siembraAEmergenciaDias = siembraAEmergenciaDias;
    }

    @XmlElement(name = "emergencia_a_floracion_dias")
    public int getEmergenciaAFloracionDias() {
        return emergenciaAFloracionDias;
    }

    public void setEmergenciaAFloracionDias(int emergenciaAFloracionDias) {
        this.emergenciaAFloracionDias = emergenciaAFloracionDias;
    }

    @XmlElement(name = "floracion_a_maduracion_dias")
    public int getFloracionAMaduracionDias() {
        return floracionAMaduracionDias;
    }

    public void setFloracionAMaduracionDias(int floracionAMaduracionDias) {
        this.floracionAMaduracionDias = floracionAMaduracionDias;
    }

    @XmlElement(name = "maduracion_a_cosecha_dias")
    public int getMaduracionACosechaDias() {
        return maduracionACosechaDias;
    }

    public void setMaduracionACosechaDias(int maduracionACosechaDias) {
        this.maduracionACosechaDias = maduracionACosechaDias;
    }

    @XmlElement(name = "kc_inicial")
    public double getKcInicial() {
        return kcInicial;
    }

    public void setKcInicial(double kcInicial) {
        this.kcInicial = kcInicial;
    }

    @XmlElement(name = "kc_floracion")
    public double getKcFloracion() {
        return kcFloracion;
    }

    public void setKcFloracion(double kcFloracion) {
        this.kcFloracion = kcFloracion;
    }

    @XmlElement(name = "kc_fin_ciclo")
    public double getKcFinCiclo() {
        return kcFinCiclo;
    }

    public void setKcFinCiclo(double kcFinCiclo) {
        this.kcFinCiclo = kcFinCiclo;
    }
}
