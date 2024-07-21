<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gráficos</title>
    <link rel="stylesheet" href="css/styles.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chartjs-adapter-date-fns"></script> <!-- Adaptador para fechas -->
    <script src="scripts/funciones_servidor.js"></script>
</head>
<body>
    <div id="sidebar"></div>
    <div id="content">
        <h1>Gráficos en tiempo real</h1>
        <div id="graficos">
            <div>
                <canvas id="grafico-temperatura"></canvas>
            </div>
            <div>
                <canvas id="grafico-humedad"></canvas>
            </div>
            <div>
                <canvas id="grafico-radiacion"></canvas>
            </div>
            <div>
                <canvas id="grafico-viento"></canvas>
            </div>
            <div>
                <canvas id="grafico-presion"></canvas>
            </div>
            <div>
                <canvas id="grafico-evapotranspiracion"></canvas>
            </div>
        </div>
    </div>
    <script>
    document.addEventListener('DOMContentLoaded', () => {
        const ctxTemp = document.getElementById('grafico-temperatura').getContext('2d');
        const ctxHum = document.getElementById('grafico-humedad').getContext('2d');
        const ctxRad = document.getElementById('grafico-radiacion').getContext('2d');
        const ctxViento = document.getElementById('grafico-viento').getContext('2d');
        const ctxPresion = document.getElementById('grafico-presion').getContext('2d');
        const ctxEto = document.getElementById('grafico-evapotranspiracion').getContext('2d');

        const chartTemp = createChart(ctxTemp, 'Temperatura (°C)', 'rgba(255, 99, 132, 1)', 'rgba(255, 99, 132, 0.2)');
        const chartHum = createChart(ctxHum, 'Humedad Relativa (%)', 'rgba(54, 162, 235, 1)', 'rgba(54, 162, 235, 0.2)');
        const chartRad = createChart(ctxRad, 'Radiación Solar (MJ/m²/día)', 'rgba(255, 206, 86, 1)', 'rgba(255, 206, 86, 0.2)');
        const chartViento = createChart(ctxViento, 'Velocidad del Viento (m/s)', 'rgba(75, 192, 192, 1)', 'rgba(75, 192, 192, 0.2)');
        const chartPresion = createChart(ctxPresion, 'Presión Atmosférica (KPa)', 'rgba(153, 102, 255, 1)', 'rgba(153, 102, 255, 0.2)');
        const chartEto = createChart(ctxEto, 'Evapotranspiración (mm)', 'rgba(128, 128, 128, 1)', 'rgba(192, 192, 192, 0.2)');
        
        let lambda, delta, rho, cp, gamma;

        function createChart(context, label, borderColor, backgroundColor) {
            return new Chart(context, {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: label,
                        data: [],
                        borderColor: borderColor, // Color de la línea
                        backgroundColor: backgroundColor, // Color de relleno
                        borderWidth: 1,
                        fill: true // Cambia a false si no quieres relleno bajo la línea
                    }]
                },
                options: {
                    scales: {
                        x: {
                            type: 'time',
                            time: {
                                unit: 'second',
                                tooltipFormat: 'll HH:mm:ss' // Formato para mostrar en el tooltip
                            }
                        },
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });
        }

        function updateChart(chart, label, data) {
            chart.data.labels.push(label);
            chart.data.datasets[0].data.push(data);
            if (chart.data.labels.length > 20) {
                chart.data.labels.shift();
                chart.data.datasets[0].data.shift();
            }
            chart.update();
        }
        
        function fetchConstantes() {
            const baseURL = getBaseURL();
            fetch(`${baseURL}/sensores/constants`)
                .then(response => response.json())
                .then(data => {
                    lambda = data.lambda || 2.2; // valor estándar para lambda en MJ/kg
                    delta = data.delta || 0.43; // ejemplo de valor de delta
                    rho = data.rho || 1.225; // densidad del aire en kg/m³
                    cp = data.cp || 1.2255; // calor específico del aire en MJ/kg/°C
                    gamma = data.gamma || 0.5555; // constante psicrométrica en kPa/°C
                })
                .catch(error => {
                    console.error('Error al obtener las constantes:', error);
                });
        }

        function fetchData() {
            const baseURL = getBaseURL();
            fetch(`${baseURL}/sensores/etr-data`)
                .then(response => response.json())
                .then(data => {
                    const timestamp = new Date();
                    updateChart(chartTemp, timestamp, (data.temperatura_dht22 + data.temperatura_bmp280) / 2);
                    updateChart(chartHum, timestamp, data.humedad_relativa);
                    updateChart(chartRad, timestamp, RadiacionSolar(data.radiacion_solar));
                    updateChart(chartViento, timestamp, metrosXseg(data.velocidad_viento));
                    updateChart(chartPresion, timestamp, data.presion_atmosferica / 10);

                    // Calcular ETo
                    fetch(`${baseURL}/sensores/calculateETo`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            lambda: lambda,
                            delta: delta,
                            rho: rho,
                            cp: cp,
                            gamma: gamma,
                            radiacionSolar: RadiacionSolar(data.radiacion_solar),
                            temperatura: (data.temperatura_dht22 + data.temperatura_bmp280) / 2,
                            velocidadViento: metrosXseg(data.velocidad_viento),
                            presionAtmosferica: data.presion_atmosferica / 10,
                            humedadRelativa: data.humedad_relativa
                        })
                    })
                    .then(response => response.json())
                    .then(etoData => {
                        updateChart(chartEto, timestamp, etoData.eto);
                    })
                    .catch(error => console.error('Error al calcular ETo:', error));
                })
                .catch(error => console.error('Error al obtener los datos:', error));
        }
        fetchConstantes();
        setInterval(fetchData, 2000);
    });
    </script>
    <script src="menu.js"></script> <!-- Ruta correcta al archivo JavaScript del menú -->
</body>
</html>

