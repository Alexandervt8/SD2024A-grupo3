document.addEventListener('DOMContentLoaded', function() {
    fetch('/get_config')
        .then(response => response.json())
        .then(data => {
            const serverPath = data.serverPath;
    
            const sidebar = document.getElementById('sidebar');
            sidebar.innerHTML = `
                <h3>Menú</h3>
                <ul>
                    <li><a href="${serverPath}/index.html" class="menu-link">Inicio</a></li>
                    <li><a href="${serverPath}/cliente_a.html" class="menu-link">Agricultor</a></li>
                    <li><a href="${serverPath}/cliente_e.html" class="menu-link">Estacion</a></li>
                    <li><a href="${serverPath}/configura_variable.html" class="menu-link">Configurar Variables</a></li>
                    <li><a href="conf_estacion.html" class="menu-link">Configurar Estacion</a></li>
                    <li><a href="conf_estacion_manual.html" class="menu-link">Conf. Estación Manual</a></li>
                    <li><a href="conf_estacion_automatica.html" class="menu-link">Conf. Estación Automática</a></li>
                    <!-- Añade más enlaces según sea necesario -->
                </ul>
            `;
        })
        .catch(error => console.error('Error:', error));
});

