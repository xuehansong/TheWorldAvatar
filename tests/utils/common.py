# This script contains utility functions that can be used by
# multiple test scripts.
#
# Author: Michael Hillman

import os
from py4jps.resources import JpsBaseLib

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
	return os.getenv("CMCL_BLAZEGRAPH_USER")


def getPassword():
	"""
		Returns the password to be used on all protected CMCL blazegraph
		endpoints by grabbing it from an environment variables (which should
		have been set up within the GitHub Actions workflow).

		Returns:
			Username for protected endpoints
	"""
	return os.getenv("CMCL_BLAZEGRAPH_PASS")