// menu.js
document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.getElementById('sidebar');
    sidebar.innerHTML =
    `
        <h3>Menú</h3>
        <ul>
            <li><a href="index.html" class="menu-link">Inicio</a></li>
            <li><a href="cliente_a.html" class="menu-link">Agricultor</a></li>
            <li><a href="cliente_e.html" class="menu-link">Datos de sensores</a></li>
            <li><a href="vista_graficos.jsp" class="menu-link">Monitor</a></li>
            <li><a href="configura_variable.html" class="menu-link">Configuración</a></li>
        </ul>
    `;
});
