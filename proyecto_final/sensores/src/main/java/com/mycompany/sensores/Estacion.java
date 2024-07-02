package com.mycompany.sensores;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "estacion")
public class Estacion {

    private int ide;
    private String nombreEst;
    private List<VariableData> sensores;

    @XmlElement(name = "ide")
    public int getIde() {
        return ide;
    }

    public void setIde(int ide) {
        this.ide = ide;
    }

    @XmlElement(name = "nombreEst")
    public String getNombreEst() {
        return nombreEst;
    }

    public void setNombreEst(String nombreEst) {
        this.nombreEst = nombreEst;
    }

    @XmlElementWrapper(name = "sensores")
    @XmlElement(name = "sensor")
    public List<VariableData> getSensores() {
        return sensores;
    }

    public void setSensores(List<VariableData> sensores) {
        this.sensores = sensores;
    }
}

