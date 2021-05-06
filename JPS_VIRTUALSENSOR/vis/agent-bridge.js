/**
*	Handles all communications with the remote Virtual Sensor agent.
*
*	Author: Michael Hillman
*/


/**
*	Returns the location of all existing sensors in GeoJSON format.
*	Regular JSON from the HTTP query is converted into GeoJSON as MapBox provides more configuration
* 	options when using GeoJSON data vs programatically created markers.
*
* 	TODO: When the agent is ready, this function should get the sensor data via a HTTP request.		
*/
function getSensors(callback) {
	// Asynchronously read the JSON file
	$.getJSON("data/sensorlist.json", function(json) {
		
		// Convert from regular JSON to geoJSON 
		var geoJSON = {};
		geoJSON["type"] = "FeatureCollection";
		
		var features = [];
		json.forEach((item) => {
			var feature = {};
					
			feature["type"] = "Feature";
			feature["properties"] = {iri:item.airStationIRI};
			feature["geometry"] = {type:"Point", coordinates:[item.lng, item.lat]};
			
			features.push(feature);
		});
		
		geoJSON["features"] = features;
		
		// Call back
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
	// TODO:	Send request to agent to register new sensor.
	//			Agent should respond with IRI for new sensor?
	
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
	// TODO:	Send request to agent to get data.
	
	// Asynchronously read the JSON file
	$.getJSON("data/timeseries.json", function(json) {
		// Pass to call back
		callback(json);
	});
}

function getContourData() {
	
}