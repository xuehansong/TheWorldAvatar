# This script contains tests to ascertain the availability of Knowledge Graph endpoints
# by querying the number of triples within the KG. Tests should fail if the endpoint
# cannot be reached, returns 0, or throws an error.
#
# Note that all SPARQL requests should be made using the JPS Base Library via
# the python wrapper. Additionally, this test does not include checks for RDF
# KGs as they are planned for phase out soon.
#
# Author: Michael Hillman

import json
import pytest
from utils.common import *
from py4jps.resources import JpsBaseLib

# SPARQL query string
QUERY="SELECT (COUNT(*) AS ?NO_OF_TRIPLES) WHERE { ?x ?y ?z . }"

# Credentials to be use for protected endpoints
CREDENTIALS = {"username": getUsername(), "password": getPassword()}

# Endpoints for development Blazegraph at CMCL
DEV_CMCL_ENDPOINTS = discoverEndpoints("http://kg.cmclinnovations.com:81/blazegraph", **CREDENTIALS)

# Endpoints for development Blazegraph_Geo at CMCL
DEV_CMCL_GEO_ENDPOINTS = discoverEndpoints("http://kg.cmclinnovations.com:81/blazegraph_geo", **CREDENTIALS)

# Endpoints for production Blazegraph at CMCL
PROD_CMCL_ENDPOINTS = discoverEndpoints("http://kg.cmclinnovations.com/blazegraph", **CREDENTIALS)

# Endpoints for production Blazegraph_Geo at CMCL
PROD_CMCL_GEO_ENDPOINTS = discoverEndpoints("http://kg.cmclinnovations.com/blazegraph_geo", **CREDENTIALS)


def countTriples(kgURL, **kwargs):
	"""
		Submits a query to the input KG endpoint and returns
		the number of triples found.

		Parameters:
			kgURL - Endpoint of KG namespace to test
			kwargs - Optional arguments (can include 'username' and 'password')
		
		Returns:
			Number of triples in namespace (or -1 if error occurs)
	"""

	print("Querying endpoint at:", kgURL)
	jpsBaseView = getJPSGateway()
	kgClient = jpsBaseView.RemoteKnowledgeBaseClient(kgURL)

	# Get optional credentials
	username = kwargs.get("username")
	password = kwargs.get("password")

	try:
		# Make query and get response as list
		if username is not None and password is not None:
			print("INFO: Running query with login credentials...")
			kgClient.setUser(username)
			kgClient.setPassword(password)
			responseList = kgClient.executeQuery(QUERY).toList()
		else:
			print("INFO: Running query without any login credentials...")
			responseList = kgClient.executeQuery(QUERY).toList()

		# Get first entry, fix incorrect JSON
		firstResponse = str(responseList[0])
		firstResponse = firstResponse.replace("'", "\"")
		
		# Parse as JSON and check triple count
		firstResponseJSON = json.loads(firstResponse)
		tripleCount = firstResponseJSON["NO_OF_TRIPLES"]
		print("Triple count reported as:", tripleCount)

		return -1 if tripleCount is None else int(tripleCount)

	except Exception as exception:
		print("ERROR: Exception occured, endpoint may be offline!")
		print(exception)
		return -1


@pytest.mark.parametrize("endpoint", DEV_CMCL_ENDPOINTS)
def test_dev_cmcl_blazegraph(endpoint):
	"""
		Parameterized test that should check the triple count
		for all endpoints of CMCL's Blazegraph instance 
		running on the development server.

		Parameters:
			endpoint - SPARQL endpoint for single namespace
	"""
	tripleCount = countTriples(endpoint, **CREDENTIALS)
	assert tripleCount > 0, "Expected number of triples to be above 0!"


@pytest.mark.parametrize("endpoint", DEV_CMCL_GEO_ENDPOINTS)
def test_dev_cmcl_blazegraph_geo(endpoint):
	"""
		Parameterized test that should check the triple count
		for all endpoints of CMCL's Blazegraph_Geo instance 
		running on the development server.

		Parameters:
			endpoint - SPARQL endpoint for single namespace
	"""
	tripleCount = countTriples(endpoint, **CREDENTIALS)
	assert tripleCount > 0, "Expected number of triples to be above 0!"


@pytest.mark.parametrize("endpoint", PROD_CMCL_ENDPOINTS)
def test_prod_cmcl_blazegraph(endpoint):
	"""
		Parameterized test that should check the triple count
		for all endpoints of CMCL's Blazegraph instance 
		running on the production server.

		Parameters:
			endpoint - SPARQL endpoint for single namespace
	"""
	tripleCount = countTriples(endpoint, **CREDENTIALS)
	assert tripleCount > 0, "Expected number of triples to be above 0!"


@pytest.mark.parametrize("endpoint", PROD_CMCL_GEO_ENDPOINTS)
def test_prod_cmcl_blazegraph_geo(endpoint):
	"""
		Parameterized test that should check the triple count
		for all endpoints of CMCL's Blazegraph_Geo instance 
		running on the production server.

		Parameters:
			endpoint - SPARQL endpoint for single namespace
	"""
	tripleCount = countTriples(endpoint, **CREDENTIALS)
	assert tripleCount > 0, "Expected number of triples to be above 0!"
