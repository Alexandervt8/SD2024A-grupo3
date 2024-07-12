import xml.etree.ElementTree as ET
import os

def create_xml_from_txt(txt_filename, xml_filename):
    root = ET.Element("sensores")

    with open(txt_filename, 'r') as file:
        lines = file.readlines()

        for line in lines:
            if line.strip().startswith("<sensor>") and line.strip().endswith("</sensor>"):
                sensor_data = line.strip()
                sensor_element = ET.fromstring(sensor_data)
                root.append(sensor_element)

    tree = ET.ElementTree(root)
    tree.write(xml_filename, encoding='utf-8', xml_declaration=True)

if __name__ == "__main__":
    # Example usage
    txt_filename = 'datos_arduino_1.txt'
    xml_filename = 'sensor_data_1.xml'
    create_xml_from_txt(txt_filename, xml_filename)
