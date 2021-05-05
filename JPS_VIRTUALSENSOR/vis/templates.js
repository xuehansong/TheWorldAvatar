/**
* HTML templates.
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