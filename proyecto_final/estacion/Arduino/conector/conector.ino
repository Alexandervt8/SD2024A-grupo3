
#include <Wire.h>
#include <Adafruit_BMP280.h>
#include <Adafruit_Sensor.h>
#include <DHT.h>

// Definir pines y tipos de sensores
#define DHTPIN 2
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);

Adafruit_BMP280 bmp; // I2C

const int tempPin = A0;
const int solarPin = A1;
const int anemometerPin = 3;

volatile unsigned int windCount = 0;
unsigned long lastMillis = 0;

// Definir valores de constantes TERMISTOR
const float BETA = 3950; // Constante Beta del termistor
const float R0 = 5000;  // Valor de la resistencia a temperatura ambiente (5K ohmios)
const float VccT = 3.5;

// Definir valores de constantes LDR
const float Vcc = 5; // Tensión de alimentación
const float R1 = 5000; // 5 kΩ

bool bmpPresent = false;
bool bmpErrorReported = false;

int sensorState = 0; // Estado del sensor a leer
int sensorId = 1;    // ID del sensor que se incrementará con cada lectura
char letra = 'A';    // Letra que se recibirá del script de Python

void setup() {
  Serial.begin(9600);

  while (!Serial.available()) {
    // Esperar a recibir la letra desde Python
  }
  letra = Serial.read();

  dht.begin();
  if (!bmp.begin(0x76) && !bmpErrorReported) {
    Serial.println("No se pudo encontrar un sensor bmp280 válido, ¡verifique la conexión del bmp280!");
    bmpErrorReported = true;
  } else {
    bmpPresent = true;
  }

  pinMode(anemometerPin, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(anemometerPin), countWind, FALLING);
}

void loop() {
  float temperatureDHT, temperatureBMP, humidity, pressure, windSpeed;
  int solarValue;
  String currentTime = "2024-06-24T01:00:00"; // Aquí deberías obtener la hora actual de un RTC si tienes uno

  switch (sensorState) {
    case 0:
      temperatureDHT = dht.readTemperature();
      Serial.println(createSensorXML(sensorId++, "temperatura_dht22", temperatureDHT, "°C", currentTime));
      break;
    case 1:
      humidity = dht.readHumidity();
      Serial.println(createSensorXML(sensorId++, "humedad_relativa", humidity, "%", currentTime));
      break;
    case 2:
      solarValue = readLDR(solarPin);
      Serial.println(createSensorXML(sensorId++, "radiacion_solar", solarValue, "ohmnios", currentTime)); // Cambia "unidad" por la unidad adecuada para la LDR
      break;
    case 3:
      windSpeed = 13.0; // Velocidad del viento constante en km/h
      Serial.println(createSensorXML(sensorId++, "velocidad_viento", windSpeed, "km/h", currentTime));
      break;
    case 4:
      temperatureBMP = bmpPresent ? bmp.readTemperature() : NAN;
      Serial.println(createSensorXML(sensorId++, "temperatura_bmp280", temperatureBMP, "°C", currentTime));
      break;
    case 5:
      pressure = bmpPresent ? bmp.readPressure() / 100.0 : NAN; // Convertir a hPa
      Serial.println(createSensorXML(sensorId++, "presion_atmosferica", pressure, "hPa", currentTime));
      break;
  }

  sensorState = (sensorState + 1) % 6; // Pasar al siguiente sensor
  delay(2000); // Esperar 2 segundos antes de la siguiente lectura
}

void countWind() {
  windCount++;
}

float readLDR(int pin) {
  int analogValue = analogRead(pin);
  float voltage = analogValue * Vcc / 1023.0;
  float resistanceLDR = (Vcc * R1 / voltage) - R1;
  return resistanceLDR;
}

String createSensorXML(int id, String name, float value, String unit, String time) {
  String xml = "    <sensor>\n";
  xml += "      <id>" + String(id) + letra + "</id>\n";
  xml += "      <nombre>" + name + "</nombre>\n";
  xml += "      <valor>" + String(value) + "</valor>\n";
  xml += "      <unidad>" + unit + "</unidad>\n";
  xml += "      <tiempo>" + time + "</tiempo>\n";
  xml += "    </sensor>\n";
  return xml;
}
