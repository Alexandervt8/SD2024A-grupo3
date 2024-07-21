// funciones.js        
        
document.addEventListener('DOMContentLoaded', (event) => {
    fetch('/get_config')
    .then(response => response.json())
    .then(data => {
        document.getElementById('letra').value = data.stationLetter;
        document.getElementById('nombreE').value = data.stationName;
    })
    .catch(error => console.error('Error:', error));
});

function limpiar_areas() {
    document.getElementById('comando').value = '';
    document.getElementById('resultado').value = '';
    document.getElementById('comando2').value = '';
    document.getElementById('resultado2').value = '';
    
}

function createWorker(fn) {
    const blob = new Blob(['(' + fn.toString() + ')()'], { type: 'application/javascript' });
    return new Worker(URL.createObjectURL(blob));
}

function startReading() {
    return new Promise((resolve, reject) => {
        const worker = createWorker(function() {
            self.onmessage = function(e) {
                const { letra, n, m, baseURL } = e.data;
                fetch(`${baseURL}/start_reading`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ letra: letra, n: n, m: m })
                })
                .then(response => response.json())
                .then(data => self.postMessage({ message: data.message }))
                .catch(error => self.postMessage({ error: error.message }));
            };
        });

        worker.onmessage = function(e) {
            if (e.data.error) {
                console.error('Error:', e.data.error);
                reject(e.data.error);
            } else {
                document.getElementById('output').textContent = e.data.message;
                resolve(e.data.message);
            }
        };

        const letra = document.getElementById('letra').value;
        const n = document.getElementById('n').value;
        const m = document.getElementById('m').value;
        worker.postMessage({ letra, n, m, baseURL: location.origin });
    });
}

function executeScript() {
    limpiar_areas();
    return new Promise((resolve, reject) => {
        const worker = createWorker(function() {
            self.onmessage = function(e) {
                const { numArchivos, ideE, nombreE, baseURL } = e.data;
                fetch(`${baseURL}/create_xml`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ num_archivos: numArchivos, ideE: ideE, nombreE: nombreE })
                })
                .then(response => response.json())
                .then(data => self.postMessage({ data }))
                .catch(error => self.postMessage({ error: error.message }));
            };
        });

        worker.onmessage = function(e) {
            if (e.data.error) {
                console.error('Error:', e.data.error);
                reject(e.data.error);
            } else {
                const output = document.getElementById('output');
                output.textContent = e.data.data.message;

                const fileList = document.getElementById('file-list');
                fileList.innerHTML = '';

                if (e.data.data.archivos && Array.isArray(e.data.data.archivos)) {
                    e.data.data.archivos.forEach(archivo => {
                        const li = document.createElement('li');
                        li.textContent = archivo;
                        fileList.appendChild(li);
                    });
                } else {
                    console.error('No se encontraron archivos en la respuesta:', e.data.data);
                }
                resolve(e.data.data.message);
            }
        };

        const numArchivos = document.getElementById('num_archivos').value;
        const ideE = document.getElementById('letra').value;
        const nombreE = document.getElementById('nombreE').value;
        worker.postMessage({ numArchivos, ideE, nombreE, baseURL: location.origin });
    });
}

// Función para iniciar ambas operaciones en paralelo
function startBothOperations() {
    Promise.all([startReading(), executeScript()])
    .then(results => {
        console.log('Ambas operaciones completadas:', results);
    })
    .catch(error => {
        console.error('Error en alguna de las operaciones:', error);
    });
}

function prepararXML() {
    fetch('/get_config')
    .then(response => response.json())
    .then(data => {
        const nombreE = data.stationName;
        const rutaLocalE = data.localPath;
        const rutaServidorE = data.serverPath + data.serverSubirPath;

        const fileList = document.getElementById('file-list');
        const files = fileList.getElementsByTagName('li');

        const commandOutput = document.getElementById('resultado');
        commandOutput.innerHTML = '';

        Array.from(files).forEach(file => {
            const archivoNombre = file.textContent;
            const localFilePath = `${rutaLocalE}/${archivoNombre}`;
            const curlCommand = `curl -F "stationName=${nombreE}" -F "file=@${localFilePath}" ${rutaServidorE}`;
            comando.value += `${curlCommand}\n`;
        });
    })
    .catch(error => console.error('Error:', error));
}

function subirXML() {
    const textarea = document.getElementById('comando');
    const commandLines = textarea.value.trim().split('\n');
    document.getElementById('resultado').value = '';

    fetch('/execute_curl_commands', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ curlCommands: commandLines })
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('resultado').value = data.resultado;
    })
    .catch(error => {
        console.error('Error:', error);
        document.getElementById('resultado').value = 'Error al ejecutar los comandos curl.';
    });
}

function direccionarXML() {
    fetch('/get_config')
        .then(response => response.json())
        .then(data => {
            const rutaServidorE = data.serverPath + data.serverDireccionarPath;
            const commandOutput = document.getElementById('resultado').value.trim();
            const commandLines = commandOutput.split('\n');

            let comandos = [];

            commandLines.forEach(command => {
                if (command.trim().endsWith('.xml')) {
                    const curlCommand = `curl -X POST -d "xmlFilePath=${command.trim()}" ${rutaServidorE}`;
                    comandos.push(curlCommand);
                }
            });

            const comando2TextArea = document.getElementById('comando2');
            comando2TextArea.value = comandos.join('\n');

            console.log('Comandos generados:');
            console.log(comandos.join('\n'));
        })
        .catch(error => console.error('Error:', error));
}

function procesarXML() {
    const textarea = document.getElementById('comando2');
    const commandLines = textarea.value.trim().split('\n');
    document.getElementById('resultado2').value = '';

    fetch('/execute_curl_commands', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ curlCommands: commandLines })
    })
    .then(response => response.json())
    .then(data => {
        document.getElementById('resultado2').value = data.resultado;
        desplazarE();
    })
    .catch(error => {
        console.error('Error:', error);
        document.getElementById('resultado2').value = 'Error al ejecutar los comandos curl.';
    });
}

function desplazarE() {
    const fileList = document.getElementById('file-list');
    const fileListServ = document.getElementById('file-list-serv');

    while (fileList.firstChild) {
        fileListServ.appendChild(fileList.firstChild);
    }
}
