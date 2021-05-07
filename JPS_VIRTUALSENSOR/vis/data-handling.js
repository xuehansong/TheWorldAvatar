/*
 *	This script houses functions for pure data handling within the 
 *	Virtual Sensor visualisation.
 */


/**
 *	Converts the raw sensor location data (JSON) into GeoJSON so that it
 *	can easily be fed into the MapBox API.
 * 
 *	Parameters:
 *		rawJSON:	vanilla JSON of sensor locations
 * 
 *	Returns:
 *		GeoJSON of sensor locations 
 */
function sensorsToGeoJSON(rawJSON) {
	 
	var geoJSON = {};
	geoJSON["type"] = "FeatureCollection";
	
	var features = [];
	rawJSON.forEach((item) => {
		var feature = {};
				
		feature["type"] = "Feature";
		feature["properties"] = {iri:item.airStationIRI};
		feature["geometry"] = {type:"Point", coordinates:[item.lng, item.lat]};
		
		features.push(feature);
	});
	geoJSON["features"] = features;

	console.log("INFO: Converted sensor locations to GeoJSON.");
	return geoJSON;
 }


/**
 *	Centers the polygons in the input GeoJSON around the input sensor.
 *	This is only require in development when using fake sensors, this
 *	function should be removed in production.
 *
 * 	Parameters:
 * 		sensorCoords:	location of sensor
 * 		geoJSON:		polygon data
 */
function centerCoords(sensorCoords, geoJSON) {
	var lngMin = 999; var lngMax = -999;
	var latMin = 999; var latMax = -999;

	var points = [];

	var features = geoJSON["features"];
	features.forEach(feature => {
		var geometry = feature["geometry"];
		var coordsObject = geometry["coordinates"];

		// Find outside bounds of polygon
		find2DCoords(coordsObject, function(array) {
			var lng = array[0]; var lat = array[1];

			if(lng > lngMax) lngMax = lng;
			if(lng < lngMin) lngMin = lng;
			if(lat > latMax) latMax = lat;
			if(lat < latMin) latMin = lat;
		});	
	});

	var centerLng = (lngMax + lngMin) / 2;
	var centerLat = (latMax + latMin) / 2;
	var diffLng = sensorCoords[0] - centerLng;
	var diffLat = sensorCoords[1] - centerLat;

	console.log("Geom {" + centerLng + ", " + centerLat + "}");
	console.log("Sens {" + sensorCoords[0] + ", " + sensorCoords[1] + "}");
	console.log("Diff {" + diffLng + ", " + diffLat + "}");

	// Apply offset to center the geometry
	features.forEach(feature => {
		var geometry = feature["geometry"];
		var coordsObject = geometry["coordinates"];

		// Find outside bounds of polygon
		find2DCoords(coordsObject, function(array) {
			array[0] = array[0] + diffLng;
			array[1] = array[1] + diffLat;
		});	
	});
}

/**
 * 
 * @param {*} array 
 * @param {*} coords 
 */
function find2DCoords(array, callback) {
	if(array.length == 2 && typeof array[0] === 'number') {
		callback(array);
	} else {
		for(var i = 0; i < array.length; i++) {
			find2DCoords(array[i], callback);
		}
	}
}


/**
 *	Given the JSON object containing all contour data, this function finds and returns
 *	the GeoJSON data for a specific pollutant at at specific height.
 * 
 *	Parameters:
 *		allData:	all JSON contour data
 * 		pollutant:	species name
 * 		height:		height index
 * 
 *	Returns:
 *		Desired GeoJSON data.
 */
 function findContourData(allData, pollutant, height) {
	if(allData == null || pollutant == null || height == null) {
		return null;
	}

	// Get array of species data
	var speciesData = allData[pollutant];

	// Get the data within that species for the height
	var dataForHeight = speciesData[height];
	return dataForHeight;
 }


/**
 *	Pretty print the input date.
 *
 *	Parameters:
 *		date:	date to format
 *
 *	Returns:
 *		String of formated date.
 */
function prettyPrintDate(date) {
	var day = "" + date.getDate();
	var month = months[date.getMonth()];
	
	var hour = "" + date.getHours();
	var minute = "" + date.getMinutes();
	var second = "" + date.getSeconds();
	
	if (day.length < 2) day = "0" + day;
	if (month.length < 2) month = "0" + month;
	if (hour.length < 2) hour = "0" + hour;
	if (minute.length < 2) minute = "0" + minute;
	if (second.length < 2) second = "0" + second;
	
	return addOrd(day) + " " + month + ", " + hour + ":" + minute + ":" + second;
}


/**
 *	Returns number ordinal.
 *
 * 	Parameters:
 * 		n:	number
 * 
 *	Returns:
 * 		Ordinal string.
 */
function addOrd(n) {
  var ords = [, 'st', 'nd', 'rd'];
  var ord, m = n % 100;
  return n + ((m > 10 && m < 14) ? 'th' : ords[m % 10] || 'th');
}


/**
 *	Rounds the input digit to N decimal places.
 *
 *	Parameters:
 *		value:	Number to round
 *		digits:	Number of decimal places.
 *
 *	Returns:
 *		Rounded value.
 */
function roundN(value, digits) {
   var tenToN = 10 ** digits;
   return (Math.round(value * tenToN)) / tenToN;
}