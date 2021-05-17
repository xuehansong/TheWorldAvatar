/*
*	HTML templates and constants.
*/

// HTML when no data is available
const noDataText = "<p>No data is currently available,<br>please check back later.</p>";

// HTML when data is loading
const loadingText = "<img width='50px' src='./spinner.gif'></img><br><p>Gathering data, please wait...</p>";

// On existing sensor hover
const sensorHoverTemplate = `
	<span style='font-weight: 600; font-size: 11pt'>TITLE</span>
	<br>
	<span style='font-size: 9pt;'>COORDS</span>
	<br><br>
	<span style='color: grey; font-style: italic; font-size: 9pt;'>Click to view simulation data (if available).</span>
`;

// New sensor confirmation
const newSensorTemplate = `
	<span style='font-weight: 600; font-size: 11pt'>Create a Virtual Sensor?</span>
	<br><br>
	<span style='font-size: 9pt;'>Create a new virtual sensor at the following location?<span>
	<br>
	<span style='font-size: 9pt;'>COORDS</span>
	<br><br>
	<table width="75%" style="margin:auto;">
		<tr>
			<td width="50%" style="text-align:left;"><a href="#" onclick="closePopup()">Cancel</a></td>
			<td width="50%" style="text-align:right;"><a href="#" id="ID" onclick="createSensor(this.id)">Create</a></td>
		</tr>
	</table>	
`;

// Array of possible pollutant species for selection
const species = [
	["O\u2083", "O3"],
	["CO", "CO"],
	["CO\u2082", "CO2"],
	["NO", "NO"],
	["NO\u2082", "NO2"],
	["NO\u2093", "NOx"], 
	["SO\u2082", "SO2"], 
	["HC", "HC"],
	["PM1", "PM1"],
	["PM2.5", "PM2.5"],
	["PM10", "PM10"],
];


// Array of possible heights for selection
const heights = [
	["5m", "5.0"],
	["15m", "15.0"],
	["27.5m", "27.5"],
	["47.5m", "47.5"],
	["80m", "80.0"],
	["150m", "150.0"],
	["350m", "350.0"],
	["750m", "750.0"],
	["1,250m", "1250.0"],
	["1,750m", "1750.0"],
	["2,250m", "2250.0"],
	["2,750m", "2750.0"],
	["3,250m", "3250.0"]
];

// Months
const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];