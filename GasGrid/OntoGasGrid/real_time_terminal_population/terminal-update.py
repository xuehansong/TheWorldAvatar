
from tqdm import tqdm
from datetime import timezone
from py4jps.resources import JpsBaseLib

import time
import os
import datetime
import uuid
import sys
import traceback
import csv
import wget


# Local KG location (fallback)
FALLBACK_KG = "http://localhost:9999/blazegraph/"

# Defining namespaces of each terminal
TERMINAL_DICTIONARY = {
	"BACTON IPs Terminal": "<http://www.theworldavatar.com/kb/ts_backup/offtakes_abox/BactonIPsTerminal>",
	"BACTON UKCS Terminal": "<http://www.theworldavatar.com/kb/ts_backup/offtakes_abox/BactonUKCSTerminal>",
	"BARROW TERMINAL": "<http://www.theworldavatar.com/kb/ts_backup/offtakes_abox/BarrowTerminal>",
	"EASINGTON TERMINAL": "<http://www.theworldavatar.com/kb/ts_backup/offtakes_abox/EasingtonTerminal>",
	"ISLE OF GRAIN TERMINAL": "<http://www.theworldavatar.com/kb/ts_backup/offtakes_abox/IsleofGrainTerminal>",
	"MILFORD HAVEN TERMINAL": "<http://www.theworldavatar.com/kb/ts_backup/offtakes_abox/MilfordHavenTerminal>",
	"ST FERGUS TERMINAL": "<http://www.theworldavatar.com/kb/ts_backup/offtakes_abox/StFergusTerminal>",
	"TEESSIDE TERMINAL": "<http://www.theworldavatar.com/kb/ts_backup/offtakes_abox/TeessideTerminal>",
	"THEDDLETHORPE TERMINAL": "<http://www.theworldavatar.com/kb/ts_backup/offtakes_abox/TheddlethorpeTermin>"
}

def initialiseGateway():
	"""
		Initialise the JPS Base Library
	"""
	jpsBaseLibGW = JpsBaseLib()
	jpsBaseLibGW.launchGateway()

	jpsBaseLibView = jpsBaseLibGW.createModuleView()
	jpsBaseLibGW.importPackages(jpsBaseLibView, "uk.ac.cam.cares.jps.base.query.*")
	return jpsBaseLibView.RemoteStoreClient(getKGLocation("ts_backup"), getKGLocation("ts_backup"))

		
def getKGLocation(namespace):
	"""
		Determines the correct URL for the KG's SPARQL endpoint.
		
		Arguments:
			namespace - KG namespace.
			
		Returns:
			Full URL for the KG.
	"""
	
	# Check for the KG_LOCATION environment variable, using local fallback
	kgRoot = os.getenv('KG_LOCATION', FALLBACK_KG)
	
	if kgRoot.endswith("/"):
		return kgRoot +  "namespace/" + namespace + "/sparql"
	else:
		return kgRoot +  "/namespace/" + namespace + "/sparql"


def get_flow_data_from_csv():
	print("Downloading latest flow data CSV...")
	url = "https://mip-prd-web.azurewebsites.net/InstantaneousViewFileDownload/DownloadFile"
	filename = wget.download(url)
	
	# 2D array of data (triples [terminalName, time, flow])
	data = []
	
	print("Reading flow data CSV...")
	with open(filename, newline='') as csvfile:
		reader = csv.reader(csvfile)
		
		currentRow = -1
		terminalStartLine = 9999
		terminalEndLine = 9999
		
		for row in reader:  
			currentRow = currentRow + 1
			
			if "Terminal Totals" in row[0] :
				terminalStartLine = currentRow
				print("Terminal data starts on CSV row", currentRow)
			elif "Total System Supply" in row[0]:
				terminalEndLine = currentRow
				print("Terminal data ends on CSV row", currentRow)
				
			if (currentRow > terminalStartLine) and (currentRow < terminalEndLine):
			
				# Parse the CSV rows
				terminalName = row[0]
				
				dateTimeObj = datetime.datetime.strptime(row[3], "%d/%m/%Y %H:%M:%S")
				dateTimeStr = dateTimeObj.strftime("%Y-%m-%dT%H:%M:%S.000Z")

				flowValue = row[2]
				data.append([terminalName, dateTimeStr, flowValue])

	print("Finished reading flow data CSV, removing file...")
	os.remove(filename)
	return data


def update_triple_store(kgClient):
	today = datetime.datetime.now()
	print("\nPerforming update at: ", today)

	# Build the correct KG URL
	kgURL = getKGLocation("ts_backup")
	print("Determined KG URL as", kgURL)
		
	# Get the flow data from CSV
	# 2D array of tiples ([terminal-name, time, flow])
	data = get_flow_data_from_csv()
   
	for i in range(0, len(data)):
		dataRow = data[i]
		terminalURI = TERMINAL_DICTIONARY[dataRow[0]]
		time = dataRow[1]
			
		# Convert flow from MCM/Day to M^3/S
		flow = dataRow[2]
		gasVolume = str( (float(flow) * 1000000) / (24*60*60) )
			
		# Create UUID for IntakenGas, quantity and measurement. 
		gas_uuid = uuid.uuid1()
		quan_uuid = uuid.uuid1()
		mes_uuid = uuid.uuid1()
		
		query = '''PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
		PREFIX rdf:	 <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
		PREFIX comp:	<http://www.theworldavatar.com/ontology/ts_backup/gas_network_components.owl#>
		PREFIX compa:   <http://www.theworldavatar.com/kb/ts_backup/offtakes_abox/>
		PREFIX om:	  <http://www.ontology-of-units-of-measure.org/resource/om-2/>

		INSERT DATA
		{ compa:%s rdf:type comp:IntakenGas.
		%s comp:hasTaken compa:%s.
		compa:%s rdf:type om:Measure;
				 om:hasNumericalValue %s;
				om:hasUnit om:cubicMetrePerSecond-Time.
		compa:%s rdf:type om:VolumetricFlowRate;
			  om:hasPhenomenon compa:%s;
			  om:hasValue compa:%s.
		compa:%s comp:atUTC "%s"^^xsd:dateTime .} '''%(gas_uuid,
													   terminalURI,
													   gas_uuid,
													   mes_uuid,
													   gasVolume,
													   quan_uuid,
													   gas_uuid,
													   mes_uuid,
													   gas_uuid,
													   time)

		# Run the update
		print("Running SPARQL update " + str(i + 1) + " of " + str(len(data)) + "...")
		response = kgClient.executeUpdate(query)

	print("All SPARQL updates finished.")
	return 


def continuous_update():
	kgClient = initialiseGateway()

	while True:
		start = time.time()
		
		try:
			update_triple_store(kgClient)
		except Exception:
			print("Encountered exception, will try again in 15 minutes...")
			print(traceback.format_exc())
			   
		end = time.time()
		
		# wait for 12 minutes taking into account time to update queries
		for i in tqdm(range(60*12-int((end-start)))):
			time.sleep(1)
	return 


def single_update():
	kgClient = initialiseGateway()
	update_triple_store(kgClient)
	return 


# Try to detect argument and launch update method
if len(sys.argv) <= 1:
	single_update()
elif sys.argv[1] == '-single':
	print('Detected \'-single\' argument, running single update...')
	single_update()
elif sys.argv[1] == '-continuous':
	print('Detected \'-continuous\' argument, running continuous updates...')
	continuous_update()
else:
	single_update()

