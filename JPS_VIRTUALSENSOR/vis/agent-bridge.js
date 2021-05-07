/*
*	Handles all communications with the remote Virtual Sensor agent.
*
*	Author: Michael Hillman
*/


/**
*	Queries the agent for JSON on sensor locations before converting the resulting data to
*	GeoJSON and passing it to the input callback.
*
*	Parameters:
*		callback: function to pass GeoJSON object to.	
*/
function getSensorLocations(callback) {
	// NOTE:	For testing during development, this function currently reads example
	//			JSON data from a local file.

	// Asynchronously read the JSON file
	$.getJSON("data/sensorlist.json", function(rawJSON) {
		console.log("INFO: Got raw JSON on sensor locations.");

		// Convert from regular JSON to GeoJSON
		var geoJSON = sensorsToGeoJSON(rawJSON); 
				

		// Pass result to callbacl
		callback(geoJSON);
	});
}

/**
*	Register a new virtual sensor.
*	
* 	Parameters:
*		coords: location of new sensor.
*
*	Returns:
*		IRI (string) of new sensor.
*/
function registerSensor(coords, allSensors) {
	// NOTE:	Once the Agent is ready, a request will need to be sent to register
	//			a new sensor. It should respond with a confirmation and the newly
	//			assigned IRI.
	
	// Get local cached list of all sensors
	var features = allSensors["features"];
	
	// TEMP: Generate a new IRI
	var newIRI = "http://www.theworldavatar.com/ontology/ontostation/OntoStation.owl#virtualsensor" + (features.length + 1);
	
	// Add the new sensor to the local cache (saves re-requesting from the agent)
	var feature = {};
				
	feature["type"] = "Feature";
	feature["properties"] = {iri:newIRI};
	
	var newCoords = [coords[0].toString(), coords[1].toString()];
	feature["geometry"] = {type:"Point", coordinates:newCoords};
	
	features.push(feature);
	
	return newIRI;
}

/**
* Returns the time series data of the sensor with the input IRI.
*
*	Parameters:
*		iri: IRI of sensor.
*		callback: function to run once data is received.
*
*	Returns:
*		Time series data in JSON form.
*/
function getTimeSeriesData(iri, callback) {
	// NOTE:	This needs to get the data from the Agent once it's ready.
	
	// Asynchronously read the JSON file
	$.getJSON("data/timeseries.json", function(json) {
		// Pass to call back
		callback(json);
	});
}


/**
*	Returns a sensor's contour data (in JSON form) for a particular pollutant and height.
*	Note that the data passed to the callback is raw JSON, in which only some elements are
* 	valid GeoJSON.
*
*	Parameters:
*		iri: sensor IRI
*		callback: function to run once data is received
*/
function getAllContourData(iri, callback) {
	// NOTE:	This needs to get the data from the Agent once it's ready.
	
	// Asynchronously read the JSON file
	$.getJSON("data/contour.geojson", function(json) {
		// Pass to call back		
		callback(json);
	});
}