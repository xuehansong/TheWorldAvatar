# This script contains utility functions that can be used by
# multiple test scripts.
#
# Author: Michael Hillman

import re
import requests
import xml.etree.ElementTree as ET
from py4jps.resources import JpsBaseLib
from requests.models import HTTPBasicAuth

# Cached module view of the JPS Base Library
JPS_BASE_VIEW = None


def getJPSGateway():
	"""
		Return a cached instance of the JPS Base Library's
		module view (initialising it if not done already).

		Returns:
			Cached module view for JPS Base Library
	"""
	global JPS_BASE_VIEW
	if JPS_BASE_VIEW is None:
		jpsBaseLibGW = JpsBaseLib()
		jpsBaseLibGW.launchGateway()
		JPS_BASE_VIEW = jpsBaseLibGW.createModuleView()
		jpsBaseLibGW.importPackages(JPS_BASE_VIEW, "uk.ac.cam.cares.jps.base.query.*")

	return JPS_BASE_VIEW


def getUsername():
	"""
		Returns the username to be used on all protected CMCL blazegraph
		endpoints by grabbing it from an environment variables (which should
		have been set up within the GitHub Actions workflow).

		Returns:
			Username for protected endpoints
	"""
	return "bg_user"
	#return os.getenv("CMCL_BLAZEGRAPH_USER")


def getPassword():
	"""
		Returns the password to be used on all protected CMCL blazegraph
		endpoints by grabbing it from an environment variables (which should
		have been set up within the GitHub Actions workflow).

		Returns:
			Username for protected endpoints
	"""
	return "tZWJ4W882NLDxnGFtSe9"
	#return os.getenv("CMCL_BLAZEGRAPH_PASS")

def buildEndpointURL(baseURL, reportedURL):
	"""
		Endpoints returned from the RDF file may not include the 
		port that the KG is redirected from. If the original KG
		URL contains a port, this function will add it to the 
		endpoint URL.

		Parameters:
			baseURL - Original base URL for KG
			reportedURL - endpoint URL from RDF file

		Returns:
			Endpoint URL with same port as base URL
	
	"""
	pattern = re.compile("namespace\/.*")
	matches = pattern.findall(reportedURL)

	if len(matches) > 0:
		if baseURL.endswith("/"):
			finalEndpoint = baseURL + matches[0]
		else:
			finalEndpoint = baseURL + "/" + matches[0]
		return finalEndpoint
	else:
		return None


def discoverEndpoints(kgURL, **kwargs):
	"""
		Given the base URL of Blazegraph instance, this function
		determines all useable namespace endpoints.

		Parameters:
			kgURL - base URL (e.g. "kg.cmclinnovations.com/blazegraph")
			kwargs - optional parameters (e.g. "username" and "password")

		Returns:
			Array of endpoint URLs
	"""
	# Sanitise the URL
	finalURL = (kgURL + "namespace") if kgURL.endswith("/") else (kgURL + "/namespace")
	print(finalURL)

	# Get optional credentials
	username = kwargs.get("username")
	password = kwargs.get("password")

	# Get RDF file
	if username is not None and password is not None:
		print("with credentials")
		result = requests.get(finalURL, auth = HTTPBasicAuth(username, password))
	else:
		print("without credentials")
		result = requests.get(finalURL)

	print(result)

	# Parse result as XML and find endpoints
	endpoints = []
	xmlTree = ET.ElementTree(ET.fromstring(result.text))

	for description in xmlTree.findall("{http://www.w3.org/1999/02/22-rdf-syntax-ns#}Description"):	
		endpointElement = description.find("{http://rdfs.org/ns/void#}sparqlEndpoint")
		reportedURL = endpointElement.attrib["{http://www.w3.org/1999/02/22-rdf-syntax-ns#}resource"]

		# URLs reported in the RDF may have incorrect host names
		correctedEndpoint = buildEndpointURL(kgURL, reportedURL)
		if correctedEndpoint is not None:
			endpoints.append(correctedEndpoint)

	return endpoints