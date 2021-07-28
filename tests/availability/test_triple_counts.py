# This script contains tests to ascertain the availability of Knowledge Graph endpoints
# by querying the number of triples within the KG. Tests should fail if the endpoint
# cannot be reached, returns 0, or throws an error.
#
# Note that all SPARQL requests should be made using the JPS Base Library via
# the python wrapper.
#
# Author: Michael Hillman

import json
from utils.common import *
from py4jps.resources import JpsBaseLib

# SPARQL query string
QUERY="SELECT (COUNT(*) AS ?NO_OF_TRIPLES) WHERE { ?x ?y ?z . }"


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
	jpsBaseView = getJPSGateway()
	kgClient = jpsBaseView.RemoteKnowledgeBaseClient(kgURL)

	try:
		# Get response as list
		responseList = kgClient.executeQuery(QUERY).toList()

		# Get first entry, fix incorrect JSON
		firstResponse = str(responseList[0])
		firstResponse = firstResponse.replace("'", "\"")

		# Parse as JSON and check triple count
		firstResponseJSON = json.loads(firstResponse)
		tripleCount = firstResponseJSON["NO_OF_TRIPLES"]
		return -1 if tripleCount is None else int(tripleCount)
	except Exception as exception:
		template = "An exception of type {0} occurred. Arguments:\n{1!r}"
		message = template.format(type(exception).__name__, exception.args)
		print(message)
		return -1


def test_cmcl_dev_blazegraph_ontogasgrid():
	"""
		Checks the number of triples within the 'ontogasgrid' namespace of
		the Blazegraph KG running on the development server at CMCL.
	"""
	tripleCount = countTriples("http://kg.cmclinnovations.com:81/blazegraph/namespace/ontogasgrid/sparql")
	assert tripleCount > 0, "Expected number of triples to be above 0!"


def test_cmcl_dev_blazegraph_geo_ontocropenergy():
	"""
		Checks the number of triples within the 'ontocropenergy' namespace of
		the Blazegraph_Geo KG running on the development server at CMCL.
	"""
	tripleCount = countTriples("http://kg.cmclinnovations.com:81/blazegraph_geo/namespace/ontocropenergy/sparql")
	assert tripleCount > 0, "Expected number of triples to be above 0!"


def test_cmcl_dev_blazegraph_geo_ontocropmapgml():
	"""
		Checks the number of triples within the 'ontocropmapgml' namespace of
		the Blazegraph_Geo KG running on the development server at CMCL.
	"""
	tripleCount = countTriples("http://kg.cmclinnovations.com:81/blazegraph_geo/namespace/ontocropmapgml/sparql")
	assert tripleCount > 0, "Expected number of triples to be above 0!"


def test_cmcl_dev_blazegraph_geo_ontolanduse():
	"""
		Checks the number of triples within the 'ontolanduse' namespace of
		the Blazegraph_Geo KG running on the development server at CMCL.
	"""
	tripleCount = countTriples("http://kg.cmclinnovations.com:81/blazegraph_geo/namespace/ontolanduse/sparql")
	assert tripleCount > 0, "Expected number of triples to be above 0!"


def test_cmcl_prod_blazegraph_kb():
	"""
		Checks the number of triples within the 'kb' namespace of
		the Blazegraph KG running on the production server at CMCL.
	"""
	tripleCount = countTriples("https://kg.cmclinnovations.com/blazegraph/namespace/kb/sparql")
	assert tripleCount > 0, "Expected number of triples to be above 0!"


def test_cmcl_prod_blazegraph_geo_landuse():
	"""
		Checks the number of triples within the 'landuse' namespace of
		the Blazegraph_Geo KG running on the production server at CMCL.
	"""
	tripleCount = countTriples("https://kg.cmclinnovations.com/blazegraph_geo/namespace/landuse/sparql")
	assert tripleCount > 0, "Expected number of triples to be above 0!"
	