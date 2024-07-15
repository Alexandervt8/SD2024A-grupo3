from flask import Flask, request, jsonify, render_template
import subprocess
import serial
import time
import os
import xml.etree.ElementTree as ET
import requests

app = Flask(__name__, template_folder='src/main/webapp/templates', static_folder='src/main/webapp')

# Configuración del servidor principal
SERVER_URL = 'http://192.168.56.1:8080/sensores/upload'
ARCHIVOS_CREADOS = 0  # Variable global para contar los archivos creados

@app.route('/execute_curl_commands', methods=['POST'])
def execute_curl_commands():
    try:
        # Obtener los comandos curl desde el cuerpo JSON de la solicitud
        curl_commands = request.json.get('curlCommands', [])

        # Limpiar la salida previa
        resultado = ''

        for command in curl_commands:
            try:
                # Ejecutar el comando curl
                result = subprocess.run(command, capture_output=True, text=True, shell=True)
                # Capturar la salida y los errores (si los hay)
                output = result.stdout.strip()
                
                #error = result.stderr.strip()
                # Construir el mensaje a mostrar en el textarea resultado
                #resultado += f'Comando ejecutado: {command}\n'
                
                resultado += f'Resultado:\n{output}\n\n'
                #if error:
                #    resultado += f'Error:\n{error}\n\n'
            except Exception as e:
                resultado += f'Error al ejecutar comando {command}: {str(e)}\n\n'

        return jsonify({'status': 'success', 'message': 'Comandos curl ejecutados correctamente', 'resultado': resultado})
    except Exception as e:
        return jsonify({'status': 'error', 'message': f'Error al ejecutar comandos curl: {str(e)}', 'resultado': ''}), 500


def create_xml_from_txt(txt_filename, xml_filename,ideEs,estacionEs):
    # Crear la estructura base del XML
    root = ET.Element("estacion")
    ide = ET.SubElement(root, "ide")
    ide.text = ideEs
    nombreEst = ET.SubElement(root, "nombreEst")
    nombreEst.text = estacionEs
    sensores = ET.SubElement(root, "sensores")

    with open(txt_filename, 'r') as file:
        lines = file.readlines()

        sensor_element = None
        current_sensor = {}

        for line in lines:
            stripped_line = line.strip()
            if stripped_line == "<sensor>":
                current_sensor = {}
            elif stripped_line == "</sensor>":
                if all(key in current_sensor for key in ["id", "nombre", "valor", "unidad", "tiempo"]):
                    sensor_element = ET.SubElement(sensores, "sensor")
                    for key, value in current_sensor.items():
                        element = ET.SubElement(sensor_element, key)
                        element.text = value
                current_sensor = None
            elif current_sensor is not None:
                if stripped_line.startswith("<id>"):
                    current_sensor["id"] = stripped_line[len("<id>"):-len("</id>")]
                elif stripped_line.startswith("<nombre>"):
                    current_sensor["nombre"] = stripped_line[len("<nombre>"):-len("</nombre>")]
                elif stripped_line.startswith("<valor>"):
                    current_sensor["valor"] = stripped_line[len("<valor>"):-len("</valor>")]
                elif stripped_line.startswith("<unidad>"):
                    current_sensor["unidad"] = stripped_line[len("<unidad>"):-len("</unidad>")]
                elif stripped_line.startswith("<tiempo>"):
                    current_sensor["tiempo"] = stripped_line[len("<tiempo>"):-len("</tiempo>")]

    # Crear el árbol XML
    tree = ET.ElementTree(root)

    # Guardar el archivo XML sin la declaración
    tree.write(xml_filename, encoding='utf-8', xml_declaration=False)

    # Leer el contenido generado y agregar saltos de línea manualmente
    with open(xml_filename, 'r') as file:
        content = file.read()

    content_with_newlines = content.replace('><', '>\n<')

    # Escribir el contenido final con la declaración correcta y el comentario
    with open(xml_filename, 'w') as file:
        file.write('<?xml version="1.0" encoding="UTF-8"?>\n<!-- <lecturas_sensores> -->\n')
        file.write(content_with_newlines)

    print(f"XML creado: {xml_filename}")

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/conf_estacion.html')
def conf_estacion():
    return render_template('conf_estacion.html')

@app.route('/conf_estacion_automatica.html')
def conf_estacion_automatica():
    return render_template('conf_estacion_automatica.html')

@app.route('/conf_estacion_manual.html')
def conf_estacion_manual():
    global ARCHIVOS_CREADOS
    return render_template('conf_estacion_manual.html', archivos_creados=ARCHIVOS_CREADOS)

@app.route('/get_config', methods=['GET'])
def get_config():
    folder_path = os.path.join(os.getcwd(), 'archivos')
    xml_filename = os.path.join(folder_path, 'configuracion_estacion.xml')

    if os.path.exists(xml_filename):
        tree = ET.parse(xml_filename)
        root = tree.getroot()

        config_data = {
            'stationName': root.find('nombreEstacion').text,
            'stationLetter': root.find('letraIdentificadora').text,
            'localPath': root.find('rutaLocalEstacion').text,
            'serverPath': root.find('rutaServidorArchivos').text,
            'serverSubirPath': root.find('rutaServidorSubirArchivos').text,
            'serverDireccionarPath': root.find('rutaServidorDireccionarArchivos').text
        }
    else:
        config_data = {
            'stationName': '',
            'stationLetter': '',
            'localPath': '',
            'serverPath': '',
            'serverSubirPath': '',
            'serverDireccionarPath': ''
        }

    return jsonify(config_data)

@app.route('/save_config', methods=['POST'])
def save_config():
    data = request.json

    station_name = data.get('stationName')
    station_letter = data.get('stationLetter')
    local_path = data.get('localPath')
    server_path = data.get('serverPath')
    server_subir_path = data.get('serverSubirPath')
    server_direccionar_path = data.get('serverDireccionarPath')

    # Crear el árbol XML
    root = ET.Element("configuracion")
    ET.SubElement(root, "nombreEstacion").text = station_name
    ET.SubElement(root, "letraIdentificadora").text = station_letter
    ET.SubElement(root, "rutaLocalEstacion").text = local_path
    ET.SubElement(root, "rutaServidorArchivos").text = server_path
    ET.SubElement(root, "rutaServidorSubirArchivos").text = server_subir_path
    ET.SubElement(root, "rutaServidorDireccionarArchivos").text = server_direccionar_path

    tree = ET.ElementTree(root)

    # Crear la carpeta 'archivos' si no existe
    folder_path = os.path.join(os.getcwd(), 'archivos')
    if not os.path.exists(folder_path):
        os.makedirs(folder_path)

    # Guardar el archivo XML en la carpeta 'archivos'
    xml_filename = os.path.join(folder_path, 'configuracion_estacion.xml')
    tree.write(xml_filename, encoding='utf-8', xml_declaration=True)

    return jsonify({'status': 'success', 'message': 'Configuración guardada correctamente en XML'})


@app.route('/create_xml', methods=['POST'])
def create_xml():
    global ARCHIVOS_CREADOS
    data = request.json
    
    # Obtener los parámetros ideE y nombreE del cuerpo JSON
    num_archivos = int(data.get('num_archivos'))
    ideE = data.get('ideE')
    nombreE = data.get('nombreE')
    archivos_creados = []

    try:
        for i in range(1, num_archivos + 1):
            archivo_nombre_txt = f'datos_arduino_{ARCHIVOS_CREADOS + i}.txt'
            archivo_nombre_xml = f'sensor_data_{ARCHIVOS_CREADOS + i}.xml'
            create_xml_from_txt(archivo_nombre_txt, archivo_nombre_xml,ideE,nombreE)
            print(f"XML creado: {archivo_nombre_xml}")
            archivos_creados.append(archivo_nombre_xml)
         

        ARCHIVOS_CREADOS += num_archivos
        return jsonify({'status': 'success', 'message': 'Archivos XML creados correctamente', 'archivos': archivos_creados})
    except Exception as e:
        return jsonify({'status': 'error', 'message': f'Error al crear archivos XML: {str(e)}', 'archivos': []}), 500

@app.route('/start_reading', methods=['POST'])
def start_reading():
    data = request.json
    letra = data['letra']
    n = int(data['n'])
    m = int(data['m'])

    try:
        subprocess.Popen(['python', 'c_serial.py', letra, str(n), str(m)])
    except Exception as e:
        error_message = f"Error al iniciar la lectura: {str(e)}"
        return jsonify({'status': 'error', 'message': error_message}), 500

    return jsonify({'status': 'success', 'message': 'Proceso de lectura iniciado correctamente'})


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
