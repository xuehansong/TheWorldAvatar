package uk.ac.ceb.como.molhub.bean;

import java.util.Properties;

import uk.ac.ceb.como.molhub.model.PropertiesManager;
import uk.ac.ceb.como.molhub.model.QueryManager;

/**
 * The Class QueryString.
 *
 * @author nk510
 * <p>The Class QueryString. Class implements methods for parametrised SPARQL queries.</p>
 */
public class QueryString {	
	
	
	private static Properties kbProperties = PropertiesManager.loadProperties(QueryManager.class.getClassLoader().getResourceAsStream("kb.ontocompchem.management.properties"));
	
	private static String ontoCompChemUri = kbProperties.getProperty("ontocompchem.kb.tbox.uri");
	private static String ontoCompChemAboxBaseUri = kbProperties.getProperty("ontocompchem.kb.abox.base.uri");
	private static String ontoCompChemNS = kbProperties.getProperty("ontocompchem.kb.tbox.ns");
	
	/**
	 * Gets the all triples for positive literal.
	 *
	 * @author nk510
	 * @param atomName
	 *            name of literal that represents atom name from periodic table
	 * @param atomNumber
	 *            number of atoms which appear in a molecule for give atom name
	 * @return query
	 *         <p>
	 * 		Query as a String. Result of that query should be list of all
	 *         molecule names which contain selected atom name and selected number
	 *         of atoms.
	 *         </p>
	 */

	public static String getAllTriplesForPositiveLiteral(String atomName, int atomNumber) {

		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" 
		        + "PREFIX gc: <http://purl.org/gc/>"
				+ "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
//				+ "SELECT (strafter(str(?s), 'compchem/') AS ?uuid) ?name " + "WHERE { "
				+ "SELECT (strafter(str(?s), '.owl#') AS ?uuid) ?name " 
                + "WHERE { "
				+ "?s "+ontoCompChemNS+":hasInitialization ?in . " 
				+ "?in gc:hasMoleculeProperty ?mp . "
				+ "?mp gc:hasName ?name ." 
				+ "?mp gc:hasMolecule ?molecule ." 
				+ "?molecule gc:hasAtom ?atom. "
				+ "?atom gc:isElement <http://www.daml.org/2003/01/periodictable/PeriodicTable.owl#" + atomName + ">."
				+ "?molecule gc:hasNumberOfAtoms ?n ." 
				+ "FILTER(str(?n)='" + atomNumber + "') ." 
				+ "}";

		return query;
	}

	/**
	 * Gets the all triples for negative literal.
	 *
	 * @author nk510
	 * @param atomName
	 *            name of literal that represents atom name from periodic table
	 * @param atomNumber
	 *            number of atoms which appear in a molecule for give atom name
	 * @return query
	 *         <p>
	 * 		Query as a String. Result of that query should be list of all
	 *         molecule names which do not contain selected atom name and selected
	 *         number of atoms.
	 *         </p>
	 */

	public static String getAllTriplesForNegativeLiteral(String atomName, int atomNumber) {

		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" 
		        + "PREFIX gc: <http://purl.org/gc/>"
				+ "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
//				+ "SELECT (strafter(str(?s), 'compchem/') AS ?uuid) ?name " + "WHERE { "
                + "SELECT (strafter(str(?s), '.owl#') AS ?uuid) ?name " 
                + "WHERE { "
				+ "?s "+ontoCompChemNS+":hasInitialization ?in . " 
                + "?in gc:hasMoleculeProperty ?mp . "
				+ "?mp gc:hasName ?name . " 
                + "MINUS {" 
				+ "?mp gc:hasMolecule ?molecule . "
				+ "?molecule gc:hasAtom ?atom. " 
				+ "?molecule gc:hasNumberOfAtoms '" + atomNumber + "'^^xsd:string . "				
				+ "?atom gc:isElement <http://www.daml.org/2003/01/periodictable/PeriodicTable.owl#" + atomName + ">. "
				// + "FILTER(str(?n) ='" + atomNumber + "') ."
				+ "}" 
				+ "}";

		return query;
	}

	/**
	 * Gets the all triples molecule property.
	 *
	 * @author nk510
	 * @param moleculeName
	 *            a name of molecule (for example: Cl 2 O 6).
	 * @return query
	 *         <p>
	 * 		Query as a string. Result of that query should be a list of molecule
	 *         properties visible on search page.
	 *         </p>
	 */

	public static String getAllTriplesMoleculeProperty(String moleculeName) {

		String query =  "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
				+ "PREFIX gc: <http://purl.org/gc/>" 
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "SELECT DISTINCT (strafter(str(?g09), '.owl#') AS ?uuid) ?levelOfTheory ?basisSetValue "
				+ "WHERE {" 
				+ "?g09 "+ontoCompChemNS+":hasInitialization ?mn0 ." 
				+ "?mn0 gc:hasMoleculeProperty ?mp0 ."
				+ "?mp0 gc:hasName  "+ moleculeName +" ." 
				+ "?mn0 gc:hasParameter ?p1 ."
				+ "?p1  rdf:type <"+ontoCompChemUri+"LevelOfTheory> ."
				+ "?p1  "+ontoCompChemNS+":hasLevelOfTheory ?levelOfTheory ." 
				+ "?mn0 gc:hasParameter ?p2 ."
				+ "?p2  rdf:type <http://purl.org/gc/BasisSet> ." 
				+ "?p2  gc:hasBasisSet ?basisSetValue ."				
				+ "}";

		return query;
	}

	/**
	 * Ge non compositet molecule properties.
	 *
	 * @param uuid
	 *            the uuid
	 * @return the string.
	 *         <p>
	 * 		Query as a string. Result of that query should be molecule name,
	 *         level of theory, basis value set, geometry type.
	 *         </p>
	 */
	public static String geNonCompositetMoleculeProperties(String uuid) {

		String query = "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
				+ "PREFIX gc: <http://purl.org/gc/>" + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "SELECT ?moleculeName ?levelOfTheory ?basisSetValue ?geometryTypeValue " 
				+ "WHERE {"
				+ "<"+ontoCompChemAboxBaseUri+uuid+"/"+uuid+".owl#"+ uuid + "> "+ontoCompChemNS+":hasInitialization ?mn0 ."
				+ "?mn0 gc:hasMoleculeProperty ?mp0 ." 
				+ "?mp0 gc:hasName ?moleculeName ."
				+ "<"+ontoCompChemAboxBaseUri+uuid+"/" +uuid+".owl#"+ uuid + "> "+ontoCompChemNS+":hasInitialization ?mn0 ."
				+ "?mn0 gc:hasParameter ?p1 ."
				+ "?p1  rdf:type "+ontoCompChemNS+":LevelOfTheory ."
				+ "?p1  <"+ontoCompChemUri+"hasLevelOfTheory> ?levelOfTheory ."
				+ "<"+ontoCompChemAboxBaseUri+uuid+"/" +uuid+".owl#"+ uuid + ">  "+ontoCompChemNS+":hasInitialization ?mn2 ."
				+ "?mn2 gc:hasParameter ?p2 ." + "?p2  rdf:type <http://purl.org/gc/BasisSet> ."
				+ "?p2  gc:hasBasisSet ?basisSetValue ." 
				+ "<"+ontoCompChemAboxBaseUri+uuid+"/" +uuid+".owl#"+ uuid + "> gc:isCalculationOn ?c1. " 
                + "?c1 a <"+ontoCompChemUri+"GeometryType> ."
				+ "?c1 "+ontoCompChemNS+":hasGeometryType ?geometryTypeValue ." 
                + "}";

		return query;
	}

	/**
	 * Ge frequency.
	 *
	 * @param uuid
	 *            the uuid
	 * @return the string.
	 *         <p>
	 * 		Query as a string. Result of that query should be frequency size,
	 *         frequency value, frequency unit.
	 *         </p>
	 */
	public static String geFrequency(String uuid) {

		String query = "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
				+ "PREFIX gc: <http://purl.org/gc/>" + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "SELECT ?frequenciesSize ?frequenciesValue ?frequenciesUnit " + "WHERE {"
//				+ "<http://como.cheng.cam.ac.uk/molhub/compchem/" + uuid + ">  gc:isCalculationOn ?fc1 ."
				+ "<"+ontoCompChemAboxBaseUri+ uuid+"/" +uuid+".owl#"+ uuid + ">  gc:isCalculationOn ?fc1 ."
				+ "?fc1 rdf:type gc:VibrationalAnalysis." 
				+ "?fc1 gc:hasResult ?r ." 
				+ "?r rdf:type gc:Frequency ."
				+ "?r gc:hasVibrationCount ?frequenciesSize ." 
				+ "?r "+ontoCompChemNS+":hasFrequencies ?frequenciesValue ."
				+ "?r gc:hasUnit ?frequenciesUnit ." 
				+ "}";

		return query;
	}

	/**
	 * Gets the atomic mass.
	 *
	 * @param uuid
	 *            the uuid
	 * @return the atomic mass.
	 *         <p>
	 * 		Query as a string. Result of that query should be atom name, mass
	 *         value, and mass unit.
	 *         </p>
	 */
	public static String getAtomicMass(String uuid) {

		String query = "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
				+ "PREFIX gc: <http://purl.org/gc/>" 
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "SELECT (strafter(str(?atomName), '#') AS ?atomicName) ?massValue ?massUnit " 
				+ "WHERE {"
//				+ "<http://como.cheng.cam.ac.uk/molhub/compchem/" + uuid + ">  gc:isCalculationOn ?go ."
				+ "<"+ontoCompChemAboxBaseUri+uuid+"/" +uuid+".owl#"+ uuid +">  gc:isCalculationOn ?go ."
				+ "?go rdf:type gc:GeometryOptimization ." 
				+ "?go gc:hasMolecule ?mol1. " 
				+ "?mol1 gc:hasAtom ?at1 ."
				+ "?at1 gc:isElement ?atomName ." 
				+ "?at1 gc:hasMass ?mass ." 
				+ "?mass gc:hasValue ?massValue . "
				+ "?mass gc:hasUnit ?massUnit . " 
				+ "}";

		return query;
	}

	/**
	 * Gets the spin multiplicity.
	 *
	 * @param uuid
	 *            the uuid
	 * @return the spin multiplicity.
	 *         <p>
	 * 		Query as a string. Result of that query should be spin multiplicity value.
	 *         </p>
	 */
	
	public static String getSpinMultiplicity(String uuid) {

		String query = "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
				+ "PREFIX gc: <http://purl.org/gc/>" 
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" 
				+ "SELECT ?spinMultiplicityValue "
//				+ "WHERE {" + "<http://como.cheng.cam.ac.uk/molhub/compchem/" + uuid + ">   gc:isCalculationOn ?go1 . "
				+ "WHERE {" 
                + "<"+ontoCompChemAboxBaseUri+ uuid+"/" +uuid+".owl#"+ uuid +">   gc:isCalculationOn ?go1 . "				
				+ "?go1 rdf:type gc:GeometryOptimization ." 
                + "?go1 gc:hasMolecule ?mol2."
				+ "?mol2 "+ontoCompChemNS+":hasSpinMultiplicity ?spinMultiplicityValue ." 
                + "}";

		return query;
	}
	
	/**
	 * Gets the formal charge.
	 *
	 * @param uuid the uuid
	 * @return the formal charge
	 * <p>Query as a string. Result of that query should be formal charge value. </p>
	 */

	public static String getFormalCharge(String uuid) {

		String query = "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
				+ "PREFIX gc: <http://purl.org/gc/>" 
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" 
				+ "SELECT ?formalChargeValue  ?formalChargeUnit "
				+ "WHERE {" 
//				+ "<http://como.cheng.cam.ac.uk/molhub/compchem/" + uuid + ">   gc:isCalculationOn ?go1 . "
				+ "<"+ontoCompChemAboxBaseUri+uuid+"/" +uuid+".owl#"+ uuid +">   gc:isCalculationOn ?go1 . "
				+ "?go1 rdf:type gc:GeometryOptimization ." 
				+ "?go1 gc:hasMolecule ?mol2."
				+ "?mol2 gc:hasFormalCharge ?formalCharge ."
				+ "?formalCharge rdf:type <http://purl.org/gc/IntegerValue> . "
				+ "?formalCharge gc:hasValue  ?formalChargeValue . "
				+ "?formalCharge gc:hasUnit ?formalChargeUnit . "
				+ "}";

		return query;
	}
	
	/**
	 * Gets the rotational symmetry number.
	 *
	 * @param uuid
	 *            the uuid
	 * @return the string.
	 *         <p>
	 * 		Query as a string. Result of that query should be the rotational symmetry number.
	 *         
	 *         </p>
	 */
	public static String getRotationalSymmertyNumber(String uuid) {

		String query = "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
				+ "PREFIX gc: <http://purl.org/gc/>" 
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "SELECT DISTINCT ?rotationalSymmetryNumber " 
				+ "WHERE {"
//				+ "<http://como.cheng.cam.ac.uk/molhub/compchem/" + uuid + ">   gc:isCalculationOn ?go5 ."
				+ "<"+ontoCompChemAboxBaseUri+ uuid +"/" +uuid+".owl#"+ uuid +">   gc:isCalculationOn ?go5 ."
				+ "?go5 rdf:type "+ontoCompChemNS+":RotationalSymmetry ."
				+ "?go5 "+ontoCompChemNS+":hasRotationalSymmetryNumber ?rotationalSymmetryNumber . " + "}";

		return query;
	}

	/**
	 * Gets the rotational constant.
	 *
	 * @param uuid
	 *            the uuid as a parameter for SPARQL query.
	 * @return the string.
	 *         <p>
	 * 		Query as a string. Result of that query should be the rotational
	 *         constant as 3-tuple (rotational constant size, rotational constant
	 *         value, and rotational constant unit)
	 *         </p>
	 */
	public static String getRotationalConstant(String uuid) {

		String query = "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
				+ "PREFIX gc: <http://purl.org/gc/>" 
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "SELECT DISTINCT ?rotationalConstantsSize  ?rotationalConstantsValue ?rotationalConstantsUnit "
//				+ "WHERE {" + "<http://como.cheng.cam.ac.uk/molhub/compchem/" + uuid + ">   gc:isCalculationOn ?go4 ."
				+ "WHERE {" 
                + "<"+ontoCompChemAboxBaseUri+ uuid+"/"+uuid+".owl#"+ uuid +">   gc:isCalculationOn ?go4 ."
				+ "?go4 rdf:type "+ontoCompChemNS+":RotationalConstants ."
				+ "?go4 "+ontoCompChemNS+":hasRotationalConstants ?rotationalConstantsValue ."
				+ "?go4 gc:hasUnit ?rotationalConstantsUnit ."
				+ "?go "+ontoCompChemNS+":hasRotationalConstantsCount ?rotationalConstantsSize ." + "}";

		return query;
	}

	/**
	 * @author NK510 (caresssd@hermes.cam.ac.uk)
	 * 
	 * @param uuid the uuid as a parameter for SPARQL query.
	 * @param energyClass different type of electronic energy classes such as ScfEnergy, ZeroPointEnergy
	 * @return A SPARQL query as a String. Result of this query should be scf energy value and scf energy unit.
	 * 
	 */
	public static String getElectronicEnergy(String uuid, String energyClass) {
		

		String query = "PREFIX "+ontoCompChemNS+": <"+ontoCompChemUri+">"
				+ "PREFIX gc: <http://purl.org/gc/>" 
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "SELECT DISTINCT ?energyValue  ?energyUnit "
				+ "WHERE {" 
                + "<"+ontoCompChemAboxBaseUri+ uuid+"/"+uuid+".owl#"+ uuid +"> gc:isCalculationOn ?cal1 ."
				+ "?cal1 rdf:type "+ontoCompChemNS+":"+energyClass+" ."
				+ "?cal1 gc:hasElectronicEnergy ?elecEn . " 
				+ "?elecEn gc:hasValue  ?energyValue . "
				+ "?elecEn gc:hasUnit ?energyUnit . "
				+ "}";
		
		return query;
	}
	
	
	
}