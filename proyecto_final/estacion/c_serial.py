import serial
import time
import sys

if len(sys.argv) != 5:
    print("Uso: python c_serial.py <letra> <num_repeticiones> <duracion_segundos> <puerto_COM>")
    sys.exit(1)

letra = sys.argv[1]
num_repeticiones = int(sys.argv[2])
duracion_segundos = int(sys.argv[3])
puerto_COM = sys.argv[4]

# Abre el puerto serial
ser = serial.Serial(puerto_COM, 9600)
time.sleep(2)  # Esperar a que el Arduino se reinicie

# Enviar la letra al Arduino
ser.write(letra.encode())  # Enviar la letra

try:
    for i in range(1, num_repeticiones + 1):
        # Nombre del archivo con el número de repetición
        nombre_archivo = f'datos_arduino_{i}.txt'

        with open(nombre_archivo, 'w') as archivo:
            tiempo_inicio = time.time()  # Tiempo de inicio de la captura
            try:
                while (time.time() - tiempo_inicio) < duracion_segundos:
                    if ser.in_waiting > 0:
                        linea = ser.readline().decode('utf-8').strip()
                        archivo.write(linea + '\n')  # Escribe la línea en el archivo
                        print(linea)  # Opcional: imprime la línea en la consola
                    time.sleep(1)  # Espera 1 segundo antes de leer la siguiente línea (ajustable según necesidad)
            except KeyboardInterrupt:
                print("Captura de datos interrumpida manualmente.")
                break

        print(f"Captura de datos finalizada. Los datos se han guardado en '{nombre_archivo}'.")

finally:
    ser.close()  # Cierra el puerto serial al finalizar

print("Todas las capturas han sido finalizadas.")

