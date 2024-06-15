package com.mycompany.sensores;

import java.io.Serializable;
import java.sql.Timestamp;

public class SensorData implements Serializable {
    private Long id;
    private String sensorId;
    private double value;
    private Timestamp timestamp;

    public SensorData(Long id, String sensorId, double value, Timestamp timestamp) {
        this.id = id;
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public double getValue() {
        return value;
    }

    public Timestamp getTimestamp() {
        return timestamp;
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




