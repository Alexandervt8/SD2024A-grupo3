// menu.js
document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.getElementById('sidebar');
    sidebar.innerHTML = `
        <h3>Menú</h3>
        <ul>
            <li><a href="cliente_a.html" class="menu-link">Agricultor</a></li>
            <li><a href="cliente_e.html" class="menu-link">Estacion</a></li>
            <li><a href="configura_variable.html" class="menu-link">Configurar Variables</a></li>
            <!-- Añade más enlaces según sea necesario -->
        </ul>
    `;
});
