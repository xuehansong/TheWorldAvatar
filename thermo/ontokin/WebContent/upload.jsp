<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="sj" uri="/struts-jquery-tags"%>
<%@ taglib prefix="sb" uri="/struts-bootstrap-tags"%>

<!DOCTYPE html>
<html>
<!--  after pressing refresh button it clears content of page. -->
<head>

<meta charset="UTF-8">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/static/group/bootstrap.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/index.css">

<link rel="icon"
	href="${pageContext.request.contextPath}/css/static/group/favicon.ico" />

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/static/group/ontokin.css">

<title>J-Park Simulator</title>

</head>

<body>
<div class="jumbotron text-center" id="topBanner">
    <a id="cares-banner" href="http://www.cares.cam.ac.uk/node/454#overlay-context=c4t-research">
        <img id="care-banner-img" src="images/cam_lang_negativ1%20NEW_0.png">
    </a>
		<h1 id="title">OntoKin</h1>
		<p id="description">
			A Knowledge Base built with the integration of semantic technologies and software agents for 
			enhancing the experience of chemists in querying and visualising chemical kinetic reaction 
			mechanisms.
		</p>
</div>
	<div class="container">


            <div class="row">

                <div class="col-md-4 div-left">
                
                        <s:actionerror />
						<s:actionmessage />			
					
						<s:form action="upload" method="post"
							enctype="multipart/form-data" theme="bootstrap" label="Select CHEMKIN files to upload:">
							<hr class="line">
							<s:file name="myMechFile" label="Select a mechanism file to upload:"
								theme="bootstrap" />
							<s:file name="myThermoFile" label="Select a thermo chemistry file to upload:"
								theme="bootstrap" />
							<s:file name="mySurfaceFile" label="Select a surface chemistry file to upload:"
								theme="bootstrap" />
							<s:file name="myTransportFile" label="Select a transport file to upload:"
								theme="bootstrap" />								
							<s:textfield name="myMechanismName" type="text" label="Provide a name for the mechanism:" theme="bootstrap"/>
							<s:submit value="Upload" label="Select files" theme="bootstrap" />
						</s:form>
						
					<p></p>				
						
					<s:iterator value="column" var="c">
						<hr class="line">
						<legend>Mechanism upload report </legend>
						<table class="table table-bordered table-hover">
							<tr>
								<th>Item</th>
								<th>Result</th>
							</tr>
							<tr>
								<td ><s:property value="column1" /></td>
								<td><s:property value="myMechanismName" /></td>
							</tr>
							<tr>
								<td ><s:property value="column2" /></td>
								<td><s:property value="myMechFileFileName" /></td>
							</tr>
							<tr>
								<td ><s:property value="column3" /></td>
								<td><s:property value="myThermoFileFileName" /></td>
							</tr>
							<tr>
								<td ><s:property value="column4" /></td>
								<td><s:property value="mySurfaceFileFileName" /></td>
							</tr>
							<tr>
								<td ><s:property value="column5" /></td>
								<td><s:property value="myTransportFileFileName" /></td>
							</tr>
							<tr>
								<td ><s:property value="column6" /></td>
								<td><s:property value="myChemkinValidationReport" /></td>
							</tr>
								<tr>
								<td ><s:property value="column7" /></td>
								<td><s:property value="myOWLConsistencyReport" /></td>
							</tr>
						</table>
						
					</s:iterator>	
				 			
						
					</div>
					
					
					<div class="col-md-8">
						<s:actionerror/>
						
							<span id ="errorQuery" style="display:none; color:red">Please provide a query</span>
							<s:textfield name="term" class="form-control"  placeholder="Search OntoKin" theme="bootstrap"/>
							<span id ="errorType" style="display:none; color:red">Please select a query type</span>
							<s:select
		headerKey="-1" headerValue="Select query type"
		list="#{'mech':'Show Mechanism', 'thermo':'Thermodyanmic Data', 'compthermo':'Compare Thermodyanmic Data', 'rateconstant':'Show Arrhenius Rate Constant Parameters', 'comparerate':'Compare Rate Constant Parameters'}" 
		name="querySelection" 
		value="thermo" theme="bootstrap" />
<%-- 							<s:submit id="search_btn" value="OntoKin Search" theme="bootstrap"/> --%>
							<button id="execute" theme="bootstrap">OntoKin Search</button>
							<button id="refresh" theme="bootstrap">Refresh</button>
							<p></p><span id ="noResult" style="display:none; color:red">No results found.</span>
						 	<p></p>
						 	<div class="Container Flipped">
						 	<div id="table" class="Content">
						 		<table class="table table-bordered table-hover">
						 			<thead>
						    			<tr class="row-header">
						    			</tr>
						   			</thead>
						   			<tbody id="table-query-results">
						   			</tbody>
						   		</table>
						   	</div>
						   	</div>
					</div>
					
	
   	
   	
		</div>
		</div>


<script type="text/javascript">

$( function() {
	
	let getTableResultRowString = (index, resultObj) => {
		let tdNodes = '';
		
		//console.log(resultObj);
		for (let x in resultObj) {
			tdNodes += '<td>' + resultObj[x] + '</td>';
		}
		
		return '<tr class="row-query-results"> ' +
		    		'<td class="index">' + index + '</td>' +
	    		 
	    		tdNodes +
				'</tr>'
	};
	
	$('#refresh').click(function() {
		window.location.href = '/ontokin';
	});

	$("#execute").on("click", (event) => {
		
		let queryString;
		
		let search_term_name = $("#term").val(); //cl2
		let search_querySelection = $("#querySelection").val(); //thermo
	
		$("#errorQuery").hide();
		$("#errorType").hide();
		$("#noResult").hide();
		if (search_term_name.trim() ==  '' || search_querySelection ==  -1 || search_querySelection ==  undefined || search_querySelection ==  null) {
			if(search_term_name ==  '') {
				$("#errorQuery").show();
			}
			if(search_querySelection ==  -1 || search_querySelection ==  undefined || search_querySelection ==  null) {
				$("#errorType").show();
			}			
			event.preventDefault(); 
		}
		else {
			$("#errorQuery").hide();
			$("#errorType").hide();
			$("#noResult").hide();
		
			if (search_querySelection == 'mech' && !(search_term_name.indexOf('=') > -1)) {				 
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismIRI ?MechanismName' + '\n' +
				'WHERE {' + '\n' +
					'?Species rdfs:label \"' + search_term_name + '\" . ?Species ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
				    '?MechanismIRI rdf:type ontokin:ReactionMechanism .' + '\n' +
				    '?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				'} ';
								
			} else if (search_querySelection == 'mech' && search_term_name.indexOf('=') > -1) {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismIRI ?MechanismName ' + '\n' +
				'WHERE {' + '\n' +
					'?Reaction ontokin:hasEquation \"' + search_term_name + '\" . ?Reaction ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
				    '?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				'} ';
							
			} else if(search_querySelection == 'thermo') {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismIRI ?SpeciesIRI ?ThermoModelIRI ?coefficientValues ?mintemp ?maxtemp' + '\n' +
				'WHERE {' + '\n' +
					'?SpeciesIRI rdfs:label \"' + search_term_name + '\" . ?SpeciesIRI ontokin:belongsToPhase ?phase . ?phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
				    '?MechanismIRI rdf:type ontokin:ReactionMechanism .' + '\n' +
				    '?SpeciesIRI ontokin:hasThermoModel ?ThermoModelIRI .' + '\n' +
				    '?ThermoModelIRI ontokin:hasCoefficientValues ?coefficientValues .' + '\n' +
				    '?ThermoModelIRI ontokin:hasMinimumTemperature ?mintemp .' + '\n' +
				    '?ThermoModelIRI ontokin:hasMaximumTemperature  ?maxtemp .' + '\n' +
				'} ';
				
			} else if(search_querySelection == 'compthermo') {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?Mechanism1IRI ?Mechanism2IRI ?ThermoModelIRIinMechanism1 ?ThermoModelIRIinMechanism2 ?coefficientValues1 ?coefficientValues2 ?mintemp1 ?mintemp2 ?maxtemp1 ?maxtemp2 ?pressure1 ?pressure2' + '\n' +
				'WHERE {' + '\n' +
					'?species rdfs:label \"' + search_term_name + '\" . ?species1 ontokin:belongsToPhase ?phase1 . ?phase1 ontokin:containedIn ?Mechanism1IRI .'+ '\n' +
				    '?Mechanism1IRI rdf:type ontokin:ReactionMechanism .' + '\n' +
				    '?Mechanism1IRI rdfs:label ?mechanism1Name .' + '\n' +
				    '?species1 ontokin:hasThermoModel ?ThermoModelIRIinMechanism1 .' + '\n' +
				    '?ThermoModelIRIinMechanism1 ontokin:hasCoefficientValues ?coefficientValues1 .' + '\n' +
				    '?ThermoModelIRIinMechanism1 ontokin:hasMinimumTemperature ?mintemp1 .' + '\n' +
				    '?ThermoModelIRIinMechanism1 ontokin:hasMaximumTemperature  ?maxtemp1 .' + '\n' +
				    '?ThermoModelIRIinMechanism1 ontokin:hasPressure  ?pressure1 .' + '\n' +
				    '?species2 rdfs:label "CH4" . ?species2 ontokin:speciesBelongsTo ?phase2 . ?phase2 ontokin:containedIn ?Mechanism2IRI .' + '\n' +
				    '?Mechanism2IRI rdf:type ontokin:ReactionMechanism .' + '\n' +
				    '?Mechanism2IRI rdfs:label ?mechanism2Name .' + '\n' +
				    '?species2 ontokin:hasThermoModel ?ThermoModelIRIinMechanism2 .' + '\n' +
				    '?ThermoModelIRIinMechanism2 ontokin:hasCoefficientValues ?coefficientValues2 .' + '\n' +
				    '?ThermoModelIRIinMechanism2 ontokin:hasMinimumTemperature ?mintemp2 .' + '\n' +
				    '?ThermoModelIRIinMechanism2 ontokin:hasMaximumTemperature  ?maxtemp2 .' + '\n' +
				    '?ThermoModelIRIinMechanism2 ontokin:hasPressure  ?pressure2 .' + '\n' +
				    'FILTER ((?mintemp1 = ?mintemp2 ) && (?maxtemp1 = ?maxtemp2 ) && (?pressure1 = ?pressure2 ) && (?coefficientValues1 != ?coefficientValues2))' + '\n' +
				'} ';

			} else if(search_querySelection == 'rateconstant') {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT ?MechanismIRI ?ReactionIRI ?ActivationEnergy ?ActivationEnergyUnits ?PreExpFactor ?PreExpFactorUnits ?TempExponent ?TempExpUnits' + '\n' +
				'WHERE {' + '\n' +
					'?ReactionIRI ontokin:hasEquation \"' + search_term_name + '\" . ?ReactionIRI ontokin:belongsToPhase ?Phase . ?Phase ontokin:containedIn ?MechanismIRI .'+ '\n' +
			    	'?MechanismIRI rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRI ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergy ?ActivationEnergy .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasActivationEnergyUnits ?ActivationEnergyUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactor ?PreExpFactor .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasPreExponentialFactorUnits ?PreExpFactorUnits .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponent ?TempExponent .' + '\n' +
				    '?ArrheniusRateCoefficients ontokin:hasTemperatureExponentUnits ?TempExpUnits .' + '\n' +
				'} ';

			} else if(search_querySelection == 'comparerate') {
			 
				queryString = 'PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' + '\n' +
			 	'PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' + '\n' + 
				'PREFIX ontokin:' + '\n' +
				'<http://www.theworldavatar.com/kb/ontokin/ontokin.owl#>'+ '\n' +
				'SELECT distinct ?Mechanism1IRI ?Mechanism2IRI ?ReactionIRIinMechanism1 ?ReactionIRIinMechanism2 ?ActivationEnergy1 ?ActivationEnergy2 ?PreExpFactor1 ?PreExpFactor2' + '\n' +
				'WHERE {' + '\n' +
					'?ReactionIRIinMechanism1 ontokin:hasEquation \"' + search_term_name + '\" . ?ReactionIRIinMechanism1 ontokin:belongsToPhase ?Phase1 . ?Phase1 ontokin:containedIn ?Mechanism1IRI .'+ '\n' +
			    	'?ReactionIRIinMechanism1 rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRIinMechanism1 ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients1 .' + '\n' +
				    '?ArrheniusRateCoefficients1 ontokin:hasActivationEnergy ?ActivationEnergy1 .' + '\n' +
				    '?ArrheniusRateCoefficients1 ontokin:hasPreExponentialFactor ?PreExpFactor1 .' + '\n' +
				    '?ArrheniusRateCoefficients1 ontokin:hasTemperatureExponent ?TempExponent1 .' + '\n' +
					'?ReactionIRIinMechanism2 ontokin:hasEquation \"' + search_term_name + '\" . ?ReactionIRIinMechanism2 ontokin:belongsToPhase ?Phase2 . ?Phase2 ontokin:containedIn ?Mechanism2IRI .'+ '\n' +
			    	'?ReactionIRIinMechanism2 rdfs:label ?MechanismName .' + '\n' +
				    '?ReactionIRIinMechanism2 ontokin:hasArrheniusCoefficient ?ArrheniusRateCoefficients2 .' + '\n' +
				    '?ArrheniusRateCoefficients2 ontokin:hasActivationEnergy ?ActivationEnergy2 .' + '\n' +
				    '?ArrheniusRateCoefficients2 ontokin:hasPreExponentialFactor ?PreExpFactor2 .' + '\n' +
				    '?ArrheniusRateCoefficients2 ontokin:hasTemperatureExponent ?TempExponent2 .' + '\n' +
				    		'} ';
			}
		
		let queryResultsTable = $("#table-query-results");
		$("#num-results").text("");
		$(".row-query-results").remove();
		
		
		$.ajax({
			type: 'GET',
			url: "http://localhost:8080/RDF4J_SPARQL_GUI/SPARQLEndpointProxy",
			data: {queryString},
			success: data => {
				// console.log(data)
				let trimmedResult = data.slice(1, data.length-2);
				let resultArray = trimmedResult.split('}');
				if (resultArray.length == 1) {
// 					$("#num-results").text("No results found.");
					$("#noResult").show();
				} else {
					resultArray.pop();
					
					firstResult = resultArray[0] + '}';
					firstResult = firstResult.replace(/\n +/g, "");
					firstResultObj = JSON.parse(firstResult);
					$(".row-header").append(`<th class="row-query-results first-column">Index</th>`);
					console.log(firstResultObj)
					for (let x in firstResultObj) {
						console.log(x)
						var theader = '<th class="row-query-results">' + x +'</th>';
						$(".row-header").append(theader);
					}
					
					
					let count = 1;
					for (let result of resultArray) {
						jsonString = result + '}'
						jsonString = jsonString.replace(/\n +/g, "");
						resultObj = JSON.parse(jsonString);
						queryResultsTable.append(getTableResultRowString(count++, resultObj));
					}
//					alert(`Your query return ${count-1} results.`);
					$("#num-results").text(`${count-1} results found.`);
				}
			},
			error: (XMLHttpRequest, textStatus, errorThrown) => { 
//	            alert("Status: " + textStatus); 
//	            alert("Error: " + errorThrown);
				alert("INCORRECT SPARQL QUERY!")
	        }
		})
	}

	});
	
});


</script>

</body>
</html>