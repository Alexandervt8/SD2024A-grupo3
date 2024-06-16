package com.mycompany.sensores;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SensorData implements Serializable {
    private String sensorId;
    private double value;
    private LocalDateTime timestamp;

    public SensorData(String sensorId, double value, LocalDateTime timestamp) {
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "sensorId='" + sensorId + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                '}';
    }
}






