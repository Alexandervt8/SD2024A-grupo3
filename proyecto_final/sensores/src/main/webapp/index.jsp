
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Análisis en Tiempo Real de Sensores</title>
    <script>
        function fetchSensorData() {
            fetch('SensorDataServlet')
                .then(response => response.json())
                .then(data => {
                    let sensorDataDiv = document.getElementById('sensorData');
                    sensorDataDiv.innerHTML = '';
                    data.forEach(entry => {
                        sensorDataDiv.innerHTML += `<p>Timestamp: ${entry.timestamp}, Value: ${entry.value}</p>`;
                    });
                });
        }
        setInterval(fetchSensorData, 2000); // Actualizar cada 2 segundos
    </script>
</head>
<body>
    <h1>Análisis en Tiempo Real de Sensores</h1>
    <div id="sensorData">
        <!-- Aquí se mostrarán los datos de los sensores en tiempo real -->
    </div>
</body>
</html>
