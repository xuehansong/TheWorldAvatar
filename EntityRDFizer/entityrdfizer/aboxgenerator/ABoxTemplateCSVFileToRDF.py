##########################################
# Author: Feroz Farazi (msff2@cam.ac.uk) #
# Date: 04 Dec 2020                      #
##########################################

"""This module is designed to convert entities of any domain and their data and metadata into RDF.
It requires the entities and their data to be provided as inputs in an ABox CSV template file."""

from rdflib import Graph, URIRef
from rdflib.extras.infixowl import OWL_NS
from rdflib.namespace import Namespace, XSD
import csv
import entityrdfizer.aboxgenerator.PropertyReader as propread
import entityrdfizer.aboxgenerator.ABoxGeneration as aboxgen
import os
import os.path as path
from pathlib import Path as PathlibPath
import io
import textwrap

"""Declared column headers as constants"""
COLUMN_1 = 'Source'
COLUMN_2 = 'Type'
COLUMN_3 = 'Target'
COLUMN_4 = 'Relation'
COLUMN_5 = 'Value'
TOTAL_NO_OF_COLUMNS = 5

"""Predefined types source entries"""
TYPE_ONTOLOGY = 'Ontology'
TYPE_INSTANCE = 'Instance'
TYPE_DATA     = 'Data Property'

"""Utility constants"""
HASH = '#'
SLASH = '/'
UNDERSCORE = '_'
HTTP='http://'
HTTPS='https://'
DATA_TYPE_STRING = 'string'
DATA_TYPE_INTEGER = 'integer'
DATA_TYPE_FLOAT = 'float'
DATA_TYPE_DOUBLE = 'double'
DATA_TYPE_DATE_TIME = 'datetime'

"""Declared an array to maintain the list of already created instances"""
instances = dict()
g = Graph()

"""This function checks the validity of header in the ABox CSV template file"""
def is_header_valid(row):
    if len(row) >= TOTAL_NO_OF_COLUMNS:
        if row[0].strip().lower()==COLUMN_1.lower() \
                and row[1].strip().lower()==COLUMN_2.lower() \
                and row[2].strip().lower()==COLUMN_3.lower() \
                and row[3].strip().lower()==COLUMN_4.lower() \
                and row[4].strip().lower()==COLUMN_5.lower():
            return True
        else:
            return False

"""This function converts a row into an entity or a link between two entities or a data or annotation property value"""
def process_data(row):
    if len(row) >= TOTAL_NO_OF_COLUMNS:
        if row[0].strip() is None or row[0].strip()  == '' \
                or row[1].strip() is None or row[1].strip()  == '' \
                or row[2].strip() is None or row[2].strip()  == '':
           return

        if row[1].strip().lower() == TYPE_ONTOLOGY.lower():
            if (not(row[3].strip() is None or row[3].strip() == '')) \
                    and (row[4].strip() is None or row[4].strip() == '') \
                    and (row[5].strip() is None or row[5].strip() == ''):
                #print('Creating a statement about the ontology:')
                """Creating a statement to refer to the TBox"""
                if (row[2].startswith(HTTP) or row[2].startswith(HTTPS))\
                        and row[3].strip() == 'http://www.w3.org/2002/07/owl#imports':
                    g.set((g.identifier, OWL_NS['imports'], URIRef(row[2])))
                    """Sets the IRI of the TBox"""
                    propread.setTBoxIRI(row[2])
                    """Sets the name of instance of Ontology as the ABox File Name"""
                    propread.setABoxFileName(row[0])
                if (row[2].startswith(HTTP) or row[2].startswith(HTTPS))\
                        and row[3].strip() == 'base':
                    propread.setABoxIRI(row[2])
        elif row[1].strip().lower() == TYPE_INSTANCE.lower():
            if (row[3].strip() is None or row[3].strip() == '') \
                    and (row[4].strip() is None or row[4].strip() == ''):
                #print('Creating an instance:')
                instance = propread.getABoxIRI()+SLASH+format_iri(row[0])
                type = propread.getTBoxIRI()+HASH+format_iri(row[2])
                http_flag=False
                if row[0].strip().startswith(HTTP) or row[0].strip().startswith(HTTPS):
                    instance = row[0]
                    http_flag=True
                if row[2].strip().startswith(HTTP) or row[2].strip().startswith(HTTPS):
                    type = row[2]
                #print(propread.readInstanceLabelCreationOption().strip().lower())
                if http_flag or propread.readInstanceLabelCreationOption().strip().lower() == 'no':
                    aboxgen.create_instance_without_name(g, URIRef(type), URIRef(instance))
                else:
                    aboxgen.create_instance(g, URIRef(type), URIRef(instance), row[0])
                instances[row[0].strip()] = row[2].strip()

            elif row[2].strip() in instances or row[2].strip().startswith(HTTP) or row[2].startswith(HTTPS):
                # If no relation is provided in the relation column, then instance linking will be skipped.
                if not row[0].strip() in instances or row[3].strip()  == '':
                    return
                else:
                    #print('link instance 1', instances.get(row[0]))
                    #print('link instance 2', instances.get(row[2]))
                    # If both instance 1 and instance 2 have http or https IRIs, then this block of code will be executed.
                    if (row[0].strip().startswith(HTTP) or row[0].startswith(HTTPS)) and (row[2].strip().startswith(HTTP) or row[2].startswith(HTTPS)):
                        aboxgen.link_instance(g, URIRef(row[3]),
                                              URIRef(row[0].strip()),
                                              URIRef(row[2].strip()))
                    # If only instance 1 has an http or https IRI, then this block of code will be executed.
                    elif (row[0].strip().startswith(HTTP) or row[0].startswith(HTTPS)) and (not (row[2].strip().startswith(HTTP) or row[2].startswith(HTTPS))):
                        aboxgen.link_instance(g, URIRef(row[3]),
                                              URIRef(row[0].strip()),
                                              URIRef(propread.getABoxIRI()+SLASH+format_iri(row[2].strip())))
                    # If only instance 2 has an http or https IRI, then this block of code will be executed.
                    elif (not (row[0].strip().startswith(HTTP) or row[0].startswith(HTTPS))) and (row[2].strip().startswith(HTTP) or row[2].startswith(HTTPS)):
                        aboxgen.link_instance(g, URIRef(row[3]),
                                              URIRef(propread.getABoxIRI()+SLASH+format_iri(row[0].strip())),
                                              URIRef(row[2].strip()))
                    # If both instance 1 and instance 2 don't have http or https IRIs, then this block of code will be executed.
                    else:
                        aboxgen.link_instance(g, URIRef(row[3]),
                                              URIRef(propread.getABoxIRI()+SLASH+format_iri(row[0].strip())),
                                              URIRef(propread.getABoxIRI()+SLASH+format_iri(row[2].strip())))

        elif row[1].strip().lower() == TYPE_DATA.lower():
            if (row[2].startswith(HTTP) or row[2].startswith(HTTPS)) and not row[4].strip() == '':
                if not row[5].strip() == '':
                    aboxgen.link_data_with_type(g, URIRef(row[0].strip()),
                                      URIRef(row[2].strip()),
                                      row[4].strip(), get_data_type(row[5].strip()))
                else:
                    aboxgen.link_data(g, URIRef(row[0].strip()),
                                  URIRef(format_iri(row[2].strip())),
                                  row[4].strip())
            elif row[2].strip() in instances and not row[4].strip() == '':
                if not row[5].strip() == '':
                    aboxgen.link_data_with_type(g, URIRef(row[0].strip()),
                                      URIRef(propread.getABoxIRI() + SLASH + format_iri(row[2].strip())),
                                      row[4].strip(), get_data_type(row[5].strip()))
                else:
                    instance = propread.getABoxIRI() + SLASH + format_iri(row[2].strip())
                    # if row[2].strip().startswith(HTTP) or row[2].strip().startswith(HTTPS):
                    #     instance = row[2].strip()
                    if not row[5].strip() == '':
                        aboxgen.link_data_with_type(g, URIRef(row[0].strip()),
                                                    URIRef(instance),
                                                    row[4].strip(), get_data_type(row[5]))
                    else:
                        aboxgen.link_data(g, URIRef(row[0].strip()),
                                          URIRef(propread.getABoxIRI() + SLASH + format_iri(row[2].strip())),
                                          row[4].strip())

"""Returns the corresponding data type syntax for a given data type"""
def get_data_type(data_type):
    if data_type.strip().lower() == DATA_TYPE_STRING:
        return XSD.string
    elif data_type.strip().lower() == DATA_TYPE_INTEGER:
        return XSD.integer
    elif data_type.strip().lower() == DATA_TYPE_FLOAT:
        return XSD.float
    elif data_type.strip().lower() == DATA_TYPE_DOUBLE:
        return XSD.double
    elif data_type.strip().lower() == DATA_TYPE_DATE_TIME:
        return XSD.dateTime
    else:
        return data_type

"""Formats an IRI string to discard characters that are not allowed in an IRI"""
def format_iri(iri):
    iri = iri.replace(",", " ")
    iri = iri.replace(" ","")
    return iri

"""Converts an IRI into a namespace"""
def create_namespace(IRI):
    #print(IRI)
    return Namespace(IRI)

"""This function checks the validity of the CSV template header and iterates over each data row until the whole
content of the template is converted into RDF.
Some example input and output file paths are provided below:
input_file_path = "C:/Users/.../TheWorldAvatar/JPS_Ontology/KBTemplates/ABox/ABoxOntoSpecies.csv"
output_file_path = "C:/Users/.../TheWorldAvatar/JPS_Ontology/KBTemplates/ABoxRDFFiles"
"""
def convert_into_rdf(input_file_path, output_file_path=None):
    input_file_path = PathlibPath(input_file_path)
    input_name = os.path.basename(input_file_path)
    """Checks if the input file path exists. If the path or file does not exist, it skips further processing."""
    if not path.exists(input_file_path):
        print('The following input file path does not exist:',input_file_path)
        return

    """Checks if the output file path exists. If the path does not exist, it creates the path"""
    if output_file_path:
        output_file_path = PathlibPath(output_file_path)
        if not path.exists(output_file_path):
            os.makedirs(output_file_path)
    else:
        output_file_path = os.path.dirname(input_file_path)
    output_file_path = os.path.join(output_file_path,input_name+propread.readABoxFileExtension())

    print(f"Converting abox {input_file_path} into rdf format.")
    with open(input_file_path, 'rt') as csvfile:
        rows = csv.reader(csvfile, skipinitialspace=True)
        line_count = 0
        for csv_row in rows:
            line_count = _serialize_csv_row(csv_row, line_count)
           #print('[', line_count, ']', row)
    g.serialize(destination=output_file_path,format="application/rdf+xml")
    print(f"Conversion complete. Abox created at {output_file_path}")


def convert_csv_string_into_rdf(csv_string):
    # this is a trick so that I can use csv.reader
    # to split string on ',' delimiter even if it is in quotes
    csvfile = io.StringIO(csv_string)
    rows = csv.reader(csvfile, skipinitialspace=True)
    line_count = 0
    for csv_row in rows:
        line_count = _serialize_csv_row(csv_row, line_count)
    csvfile.close()
    return str(g.serialize(format="application/rdf+xml"), 'utf-8')


def _serialize_csv_row(csv_row, line_count):
    if line_count == 0:
        if not is_header_valid(csv_row):
            raise ValueError(textwrap.dedent(f"""
                    Error: Found invalid csv header:
                           {csv_row}
                           ,so it will terminate now."""))
        else:
            global g
            g = Graph()

    if line_count > 0:
        process_data(csv_row)
    line_count+=1
    return line_count