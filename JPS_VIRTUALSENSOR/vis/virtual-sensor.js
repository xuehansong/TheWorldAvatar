/*
*	Main script for Virtual Sensor visualisation.
*/

// Cached MapBox map object
var map = null;

// Cached MapBox popup
var popup = null;

// Cached GeoJSON data on sensor locations
var sensors = null;

// Details of the currently selected sensor (could be null)
var currentSensorIRI = null;
var currentSensorCoords = null;
var currentContourData = null;


/**
 *	Resets the state of the side panel.
 */
function resetSidePanel() {
	var titleContainer = document.getElementById('titleContainer');
	titleContainer.innerHTML = "";
	
	var subtitleContainer = document.getElementById('subtitleContainer');
	subtitleContainer.innerHTML = "";

	// Hide the inner panel
	var innerPanel = document.getElementById('side-panel-inner');
	innerPanel.style.display = "none";

	// Show the "no-data" message
	document.getElementById('no-data').style.display = "table";
	document.getElementById('no-data-inner').innerHTML = noDataText;
}


/**
*	Displays the side panel.
*/
function showSidePanel() {
	var sidePanel = document.getElementById('side-panel');
	sidePanel.style.display = "block";
	
	if((map != null) && (typeof map !== 'undefined')) {
		var mapPanel = document.getElementById('map');
		mapPanel.style.width = "calc(100% - 400px)";
			
		map.resize();
	}

	resetSidePanel();
}


/**
 *	Closes the side panel.
 */
function closeSidePanel() {
	var sidePanel = document.getElementById('side-panel');
	sidePanel.style.display = "none";
	
	var mapPanel = document.getElementById('map');
	mapPanel.style.width = "100%";
	
	if((map != null) && (typeof map !== 'undefined')) {
		map.resize();
	}
	
	currentSensorIRI = null;
	currentSensorCoords = null;
	currentContourData = null;

	setSensorSelectionState("null");
}


/**
*	Hides the floating MapBox popup.
*/
function closePopup() {
	if(typeof popup !== 'undefined') {
		popup.remove();
	}
}


/**
 *	Starts the process of gathering data and plotting the locations of
 *	existing virtual sensors.
 *
 * 	Parameters:
 * 		newMap: new MapBox map instance
 */
function startPlottingSensors(newMap) {
	map = newMap;
	
	// Call agent-bridge.js to load sensor data and pass it to a callback
	getSensorLocations(plotSensors);
}

/**
*	Given a GeoJSON object containing locations and IRIs of sensors, this 
*	function plots the locations on the map and adds interaction logic.
*	
*	Parameters:
*		allSensors:	GeoJSON of all existing sensors
*/
function plotSensors(allSensors) {
	// Cache the sensor data
	sensors = allSensors;

	// Bug out if in invalid state
	if(sensors == null || map == null) return;
	
	// Add data source for sensors
	var sourceData = map.getSource("sensors");
	
	if(sourceData == null || sourceData == "undefined") {
		// Add new data
		map.addSource("sensors", {
			"type": "geojson",
			"data": sensors
		});
		console.log("INFO: Added new 'sensors' source.");
		
	} else {
		// Update existing data
		sourceData.setData(sensors);
		console.log("INFO: Updated existing 'sensors' source.");
	}
	
	// Add layer style for sensors ()if not already added)
	var sensorsLayer = map.getLayer("sensorsLayer");
	if(sensorsLayer == null || sensorsLayer == "undefined") {
		map.addLayer({
			'id': 'sensorsLayer',
			'type': 'circle',
			'source': 'sensors',
			'paint': {
				'circle-radius': 6,
				'circle-color': ['match', ['get', 'state'], 'selected', '#22b45f', '#2277b4'],
				'circle-opacity': 0.9,
				"circle-stroke-width": 1,
				"circle-stroke-color": '#FFFFFF',
			}
		});
		console.log("INFO: Added new 'sensorsLayer' layer.");
	}
}

/**
*	Sets the selection state of the sensor with the input IRI. All other
*	sensors will be deselected.
*
*	iri: IRI of sensor to be selected.
*/
function setSensorSelectionState(iri) {
	if(sensors != null) {
		for(var i = 0; i < sensors["features"].length; i++) {
			var tempIRI = sensors["features"][i].properties["iri"];
			if(tempIRI === iri) {
				sensors["features"][i].properties["state"] = "selected";
			} else {
				sensors["features"][i].properties["state"] = "unselected";
			}
		}

		console.log("INFO: Updated sensor selection states.");
	}	

	// Replot existing sensors
	plotSensors(sensors);
}


/**
*	Initialises map listeners.
*/
function initialiseMapListeners() {
	
	// Create a popup, but don't add it to the map yet.
	popup = new mapboxgl.Popup({
		closeButton: false,
		closeOnClick: false,
		maxWidth: 500
	});
	
	// Add a listener for double clicks on the map (at any location)
	map.on('dblclick', function(e) {	
		var coords = e.lngLat;
		var locationStr = roundN(coords.lat, 5) + ", " + roundN(coords.lng, 5);
		
		var description = newSensorTemplate.replace("COORDS", locationStr);
		description = description.replace("ID", "[" + coords.lng + ", " + coords.lat + "]");
		popup.setLngLat(coords).setHTML(description).addTo(map);
	});

	// On mouse enter within sensorsLayer
	map.on('mouseenter', 'sensorsLayer', function (e) {
		map.getCanvas().style.cursor = 'pointer';
		
		var coordinates = e.features[0].geometry.coordinates.slice();
		while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
			coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
		}

		var lat = roundN(coordinates[0], 6);
		var lng = roundN(coordinates[1], 6);
		var iri = e.features[0].properties["iri"];
		iri = iri.split("#")[1];
		
		var locationStr = roundN(coordinates[0], 5) + ", " + roundN(coordinates[1], 5);
		
		var description = sensorHoverTemplate.replace("TITLE", iri);
		description = description.replace("COORDS", locationStr);
		popup.setLngLat(coordinates).setHTML(description).addTo(map);
	});
	
	// On mouse leave within sensorsLayer
	map.on('mouseleave', 'sensorsLayer', function () {
		map.getCanvas().style.cursor = '';
		popup.remove();
	});
	
	// Add a selection event for sensorsLayer
	map.on('click', 'sensorsLayer', function(e) {
		var coordinates = e.features[0].geometry.coordinates.slice();
		while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
			coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
		}
		
		// Run on-selection logic
		var iri = e.features[0].properties["iri"];
		selectSensor(coordinates, iri);
	});
}


/**
*	Logic when an existing virtual sensor is selected.
*
*	Parameters:
*		coords:	coordinates of selected sensor.
*		iri:	IRI of selected sensor.
*/
function selectSensor(coords, iri) {
	currentSensorIRI = iri;
	currentSensorCoords = coords;
	
	// Show the side panel
	showSidePanel();
	document.getElementById('no-data-inner').innerHTML = loadingText;
	
	// Update sensor selection state
	setSensorSelectionState(iri);
	
	// Update labels
	var name = iri.split("#")[1];
	var titleContainer = document.getElementById('titleContainer');
	titleContainer.innerHTML = name;
	
	var locationStr = roundN(coords[0], 5) + ", " + roundN(coords[1], 5);
	var subtitleContainer = document.getElementById('subtitleContainer');
	subtitleContainer.innerHTML = locationStr;

	// Pan to this sensor
	map.panTo(
		coords, 
		{
			duration: 1000, 
			zoom: 14,
			pitch: 0.0,
			bearing: 0.0
		}
	);

	// Remove contour data from last selected sensor
	removeContour();
	
	// Get the contour data for this sensor
	getAllContourData(iri, function(json) {
		currentContourData = json;

		if(currentContourData == null) {
			document.getElementById('no-data-inner').innerHTML = noDataText;
			return;
		} else {
			// Show the inner panel
			var innerPanel = document.getElementById('side-panel-inner');
			innerPanel.style.display = "block";

			// Hide the "no-data" message
			document.getElementById('no-data').style.display = "none";
		}

		// Populate options on comboboxes
		populateSelectControls(json);

		// Get time series data for this sensor.
		getTimeSeriesData(iri, buildTable)
	});
	
	console.log("INFO: Selected sensor: " + name);
}


/**
 * Pans back to the currently selected sensor.
 */
function jumpToSensor() {
	// Pan to this sensor
	map.panTo(
		currentSensorCoords, 
		{
			duration: 1000, 
			zoom: 14,
			pitch: 0.0,
			bearing: 0.0
		}
	);
}

/**
 *	Given the JSON object containing contour data, this function
 * 	will parse it to determine possible pollutant and height options
 * 	and populate the relevant selection controls.
 *
 * 	Parameters:
 * 		json:	Raw JSON containing all contour data
 */
function populateSelectControls(json) {
	// Reset previous values
	document.getElementById("heightBox").innerText = "";
	document.getElementById("pollutantBox").innerText = "";

	// Get the possible heights
	var heights = json["dz"];

	// Add heights to heightBox select
	var heightBox = document.getElementById("heightBox");
	for(var i = 0; i < heights.length; i++) {
		var entry = parseFloat(heights[i]).toFixed(1);
		heightBox.options[heightBox.options.length] = new Option(entry, i);
	}

	// Get the possible pollutants
	var pollutantBox = document.getElementById("pollutantBox");

	Object.keys(json).forEach(function(key) {
		if(key != "dz") {
			pollutantBox.options[pollutantBox.options.length] = new Option(key, key);
		}
	});
}


/**
*
*/
function showSensorData() {
	var pollutant = document.getElementById("pollutantBox").value;
	var height = document.getElementById("heightBox").value;

	// Find the GeoJSON data for the current pollutant and height
	var geoJSON = findContourData(currentContourData, pollutant, height);

	centerCoords(currentSensorCoords, geoJSON);

	// Zoom out a little
	map.zoomTo(
		10,
		{duration: 1000}
	);

	// Remove any previous contour data
	removeContour();

	// Add data source to map
	map.addSource('contour-data', {
		'type': 'geojson',
		'data': geoJSON
	});
	
	// Visualise contour data
	map.addLayer({
		'id': 'contourLayer',
		'source': 'contour-data',
		'type': 'fill',
		'paint': {
			'fill-outline-color': ['get', 'stroke'],
			'fill-color':  ['get', 'fill'],
			'fill-opacity':  ['get', 'fill-opacity']
		}
	});

	console.log("INFO: Contour data added to map.");
}

/**
 *	Removes the current contour data.
 */
function removeContour() {
	if(map.getLayer('contourLayer') != null) map.removeLayer('contourLayer');
	if(map.getSource('contour-data') != null) map.removeSource('contour-data');
}


/**
*	Creates a new Virtual Sensor at the input coordinates.
*
*	Parameters:
*		coords:	coordinates for new sensor.
*/
function createSensor(coords) {
	closePopup();
		
	// Make request to add new sensor
	var coordsArray = JSON.parse(coords);
	var newIRI = registerSensor(coordsArray, sensors);
	console.log("INFO: New sensor created.");

	// Replot sensors
	plotSensors(sensors);
	
	// Select the new sensor
	selectSensor(coordsArray, newIRI);
}


/**
 *	Populates data table in side panel with the input time series data.
 *
 *	Parameters:
 *		json: time series data in JSON form.
 */
function buildTable(json) {
	// Get currently selected species
	var currentSpecies = document.getElementById("pollutantBox").value;
	var speciesData = json[currentSpecies];

	var timeData = speciesData[0]["t"];
	var concData = speciesData[1]["conc"];
	
	// Build the HTML table of raw data
	var htmlTable = "<table id=\"dataTable\">";
	htmlTable += "<tr><th>Datetime</th>";
	htmlTable += "<th>Concentration</th></tr>";
	
	// Add the rows
	for (var i = 0; i < timeData.length; i++) {
		var date = new Date(timeData[i]);
		
		// Build HTML row
		htmlTable += "<tr>";
		htmlTable += "<td>" + prettyPrintDate(date) + "</td>";
		htmlTable += "<td>" + roundN(concData[i], 6) + "</td>";
		htmlTable += "</tr>";
	}
	htmlTable += "</table>";
	
	// Add the HTML table
	var tableContainer = document.getElementById('tableContainer');
	tableContainer.innerHTML = htmlTable;

	console.log("INFO: Time series data added to table.");
}


/**
 *	Resets the MapBox camera back to the original position and clears
 *	all selection states.
 */
function resetCamera() {
	if(typeof map !== 'undefined') {
	
		map.flyTo({
			curve: 1.9,
			speed: 1.6,
			zoom: 10,
			pitch: 0.0,
			bearing: 0.0,
			center: [103.80977999427901, 1.3533492751332865]
		});
	}
	
	closePanel();
}