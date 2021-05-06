/**
* HTML templates and constants.
*/

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
	["100m", "100"],
	["200m", "200"],
	["300m", "300"],
	["400m", "400"],
	["400m", "500"],
];

// Months
const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];