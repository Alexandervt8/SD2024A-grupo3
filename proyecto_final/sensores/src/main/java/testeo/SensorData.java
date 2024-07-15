package com.mycompany.sensores;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Adaptador para LocalDateTime
class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public LocalDateTime unmarshal(String v) throws Exception {
        return LocalDateTime.parse(v, formatter);
    }

    @Override
    public String marshal(LocalDateTime v) throws Exception {
        return v.format(formatter);
    }
}

// Clase SensorData
public class SensorData implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String sensorId;
    private double value;
    private LocalDateTime timestamp;

    public SensorData() {
    }

    public SensorData(long id, String sensorId, double value, LocalDateTime timestamp) {
        this.id = id;
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    @XmlElement
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @XmlElement
    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    @XmlElement
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @XmlElement
    @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "id=" + id +
                ", sensorId='" + sensorId + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                '}';
    }
}