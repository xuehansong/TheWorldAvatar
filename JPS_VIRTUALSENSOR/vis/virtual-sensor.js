/**
*	TODO
*/

// Table HTML when no data is available
const noDataTable = "<div id=\"noData\"><p>No available data.</p></div>";



// Cached MapBox map object
var map = null;

// Cached MapBox popup
var popup = null;

// Cached GeoJSON data on sensor locations
var sensors = null;

// Reset
function reset() {
	var titleContainer = document.getElementById('titleContainer');
	titleContainer.innerHTML = "";
	
	var subtitleContainer = document.getElementById('subtitleContainer');
	subtitleContainer.innerHTML = "";
	
	var tableContainer = document.getElementById('tableContainer');
	tableContainer.innerHTML = noDataTable;
}

// Queries for locations of existing sensors and plots them on the map.
function loadSensors(newMap) {
	map = newMap;
	
	// TODO - Query Agent for a list of existing sensors; for now, we'll
	// read these from a static JSON file.
	getSensors(plotSensors);
}

/**
*	Given a GeoJSON object containing locations and IRIs of sensors, this 
*	function plots the locations on the map and adds interaction logic.
*	
*	allSensors:	GeoJSON of all existing sensors
*/
function plotSensors(allSensors) {
	// Cache the sensor data
	sensors = allSensors;
	
	// Add data source for sensors
	var sourceData = map.getSource("sensors");
	
	if(sourceData == null || sourceData == "undefined") {
		// Add new data
		map.addSource("sensors", {
			"type": "geojson",
			"data": sensors
		});
		console.log("Added new 'sensors' data source.");
		
	} else {
		// Update existing data
		sourceData.setData(sensors);
		console.log("Updated existing 'sensors' data source.");
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
	}
}

/**
*	Sets the selection state of the sensor with the input IRI. All other
*	sensors will be deselected.
*
*	iri: IRI of sensor to be selected.
*/
function setSensorSelectionState(iri) {
	for(var i = 0; i < sensors["features"].length; i++) {
		var tempIRI = sensors["features"][i].properties["iri"];
		if(tempIRI === iri) {
			sensors["features"][i].properties["state"] = "selected";
		} else {
			sensors["features"][i].properties["state"] = "unselected";
		}
	}
	
	// Replot existing sensors
	plotSensors(sensors);
}

/**
* Hides the floating MapBox popup.
*/
function closePopup() {
	if(typeof popup !== 'undefined') {
		popup.remove();
	}
}

/**
*	Initialises map listeners.
*/
function initialiseListeners() {
	
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
		
		console.log(coordinates);


		// Run on-selection logic
		var iri = e.features[0].properties["iri"];
		selectSensor(coordinates, iri);
	});
}

/**
*	Logic when a virtual sensor location is selected.
*
*	coords: coordinates of selected sensor.
*	iri: IRI of selected sensor.
*/
function selectSensor(coords, iri) {
	// Show the side panel
	showSidePanel();
	
	// Update sensor selection state
	setSensorSelectionState(iri);
	
	// Store IRI and get sensor name
	var name = iri.split("#")[1];
	
	// Update labels
	var titleContainer = document.getElementById('titleContainer');
	titleContainer.innerHTML = name;
	
	var locationStr = roundN(coords[0], 5) + ", " + roundN(coords[1], 5);
	var subtitleContainer = document.getElementById('subtitleContainer');
	subtitleContainer.innerHTML = locationStr;	
	
	// Pan to this sensor
	map.panTo(
		coords, 
		{duration: 1000, zoom: 13}
	);
}

function showSidePanel() {
	var sidePanel = document.getElementById('side-panel');
	sidePanel.style.display = "block";
	
	if((map != null) && (typeof map !== 'undefined')) {
		var mapPanel = document.getElementById('map');
		mapPanel.style.width = "calc(100% - 400px)";
			
		map.resize();
	}
}

/**
*	Creates a new Virtual Sensor at the input coordinates.
*
*	coords: coordinates for new sensor.
*/
function createSensor(coords) {
	console.log("New sensor at " + coords);
	closePopup();
		
	// Make request to add new sensor
	var coordsArray = JSON.parse(coords);
	console.log(coordsArray);
	var newIRI = registerSensor(coordsArray, sensors);
	
	// Replot sensors
	plotSensors(sensors);
	
	// Select the new sensor
	selectSensor(coordsArray, newIRI);
}


// Generates the raw data table	
function buildTable(data) {
	
	// Build the HTML table of raw data
	var htmlTable = "<table id=\"dataTable\">";
	htmlTable += "<tr><th>" + sampleHeadings[0] + "</th>";
	htmlTable += "<th>" + sampleHeadings[1] + "</th></tr>";
	
	// Add the rows
	for (var i = 0; i < data.length; i++) {
		var rowData = data[i];
		
		// Get data (add some noise for variability)
		var dateTime = prettyPrintDate(rowData.datetime);
		var flow = rowData.value;
		
		// Build HTML row
		htmlTable += "<tr>";
		htmlTable += "<td>" + dateTime + "</td>";
		htmlTable += "<td>" + roundN(flow, 2) + "</td>";
		htmlTable += "</tr>";
	}
	htmlTable += "</table>";
	
	// Add the HTML table
	var tableContainer = document.getElementById('tableContainer');
	tableContainer.innerHTML = htmlTable;
}


// Pretty print date
function prettyPrintDate(date) {
	var day = "" + date.getDate();
	var month = months[date.getMonth()];
	
	var hour = "" + date.getHours();
	var minute = "" + date.getMinutes();
	
	if (day.length < 2) day = "0" + day;
	if (month.length < 2) month = "0" + month;
	if (hour.length < 2) hour = "0" + hour;
	if (minute.length < 2) minute = "0" + minute;
	
	return addOrd(day) + " " + month + ", " + hour + ":" + minute;
}

// Get number with ordinal
function addOrd(n) {
  var ords = [, 'st', 'nd', 'rd'];
  var ord, m = n % 100;
  return n + ((m > 10 && m < 14) ? 'th' : ords[m % 10] || 'th');
}

// Round digit to N decimal places
function roundN(value, digits) {
   var tenToN = 10 ** digits;
   return (Math.round(value * tenToN)) / tenToN;
}