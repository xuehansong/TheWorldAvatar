package uk.ac.cam.cares.jps.dispersion.sensorsparql;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import java.sql.SQLException;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expression;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPattern;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;
import org.json.JSONArray;

import uk.ac.cam.cares.jps.base.query.RemoteKnowledgeBaseClient;
import uk.ac.cam.cares.jps.base.region.Scope;

import org.eclipse.rdf4j.sparqlbuilder.core.query.SelectQuery;

public class SensorSparql {
    // weather station properties
    public static final String cloud = "OutsideAirCloudCover";
    public static final String precipitation = "OutsideAirPrecipitation";
    public static final String pressure = "OutsideAirPressure";
    public static final String temperature = "OutsideAirTemperature";
    public static final String humidity = "OutsideAirRelativeHumidity";
    public static final String windspeed = "OutsideWindSpeed";
    public static final String winddirection = "OutsideWindDirection";

    // air quality properties
    public static final String CO2 = "OutsideCO2Concentration";
    public static final String CO = "OutsideCOConcentration";
    public static final String HC = "OutsideHCConcentration";
    public static final String NO2 = "OutsideNO2Concentration";
    public static final String NO = "OutsideNOConcentration";
    public static final String NOx =  "OutsideNOxConcentration";
    public static final String O3 = "OutsideO3Concentration";
    public static final String PM1 = "OutsidePM1Concentration";
    public static final String PM25 = "OutsidePM25Concentration";
    public static final String PM10 = "OutsidePM10Concentration";
    public static final String SO2 = "OutsideSO2Concentration";

    // prefixes for SPARQL queries
    private static Prefix p_station = SparqlBuilder.prefix("station",iri("http://www.theworldavatar.com/ontology/ontostation/OntoStation.owl#"));
    private static Prefix p_space_time_extended = SparqlBuilder.prefix("space_time_extended",iri("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#"));
    private static Prefix p_space_time = SparqlBuilder.prefix("space_time",iri("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time.owl#"));
    private static Prefix p_system = SparqlBuilder.prefix("system",iri("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#"));
    private static Prefix p_SI_unit = SparqlBuilder.prefix("si_unit",iri("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/SI_unit/SI_unit.owl#"));
    private static Prefix p_derived_SI_unit = SparqlBuilder.prefix("derived_SI_unit",iri("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/SI_unit/derived_SI_units.owl#"));
    private static Prefix p_ontosensor = SparqlBuilder.prefix("sensor",iri("http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#"));
    private static Prefix p_time = SparqlBuilder.prefix("time",iri("http://www.w3.org/2006/time#"));
    private static Prefix p_coordsys = SparqlBuilder.prefix("coordsys",iri("http://www.theworldavatar.com/ontology/ontocape/upper_level/coordinate_system.owl#"));
    private static Prefix p_instrument = SparqlBuilder.prefix("instrument", iri("http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#"));
    
    // IRI of units used
    private static Iri unit_m = p_SI_unit.iri("m");
    private static Iri unit_mm = p_derived_SI_unit.iri("mm");
    private static Iri unit_degree = p_derived_SI_unit.iri("degree");
    private static Iri unit_mbar = p_derived_SI_unit.iri("mBar");
    private static Iri unit_celcius = p_derived_SI_unit.iri("Celsius");
    private static Iri unit_ms = p_derived_SI_unit.iri("m_per_s");
    private static Iri unit_fraction = p_derived_SI_unit.iri("fraction"); // made up, does not exist in ontology
    private static Iri unit_percentage = p_derived_SI_unit.iri("percentage"); // made up, does not exist in ontology
    private static Iri unit_ugm3 = p_derived_SI_unit.iri("ug_per_m.m.m");

    //endpoint
    String airquality_endpoint = "http://localhost:8080/blazegraph/namespace/airquality/sparql";

    private Prefix [] getPrefix() {
        Prefix [] prefixes = {p_station,p_space_time_extended,p_space_time,p_system,p_SI_unit,p_derived_SI_unit,p_ontosensor,p_time,p_coordsys,p_instrument};
        return prefixes;
    }

    /**
     * @param station_name
     * @param xyz_coord = x y coordinates are in EPSG:4326, z is the height in m
     * @param repo 
     */
    public void createWeatherStation(String station_name, double [] xyz_coord, String repo) {
        Iri weatherstation_iri = p_station.iri(station_name);
        Iri stationcoordinates_iri = p_station.iri(station_name+"_coordinates");
        Iri xcoord = p_station.iri(station_name+"_xcoord");
        Iri ycoord = p_station.iri(station_name+"_ycoord");
        Iri zcoord = p_station.iri(station_name+"_zcoord");
        Iri vxcoord = p_station.iri(station_name+"_vxcoord");
        Iri vycoord = p_station.iri(station_name+"_vycoord");
        Iri vzcoord = p_station.iri(station_name+"_vzcoord");

        TriplePattern weatherstation_tp = weatherstation_iri.isA(p_station.iri("WeatherStation"))
        		.andHas(p_space_time_extended.iri("hasGISCoordinateSystem"),stationcoordinates_iri);

        TriplePattern projected_gp = stationcoordinates_iri.isA(p_space_time_extended.iri("ProjectedCoordinateSystem"))
                .andHas(p_space_time_extended.iri("hasProjectedCoordinate_x"),xcoord)
                .andHas(p_space_time_extended.iri("hasProjectedCoordinate_y"),ycoord)
                .andHas(p_space_time_extended.iri("hasProjectedCoordinate_z"),zcoord);

        TriplePattern xcoord_tp = xcoord.isA(p_space_time.iri("AngularCoordinate")).andHas(p_system.iri("hasValue"),vxcoord);
        TriplePattern ycoord_tp = ycoord.isA(p_space_time.iri("AngularCoordinate")).andHas(p_system.iri("hasValue"),vycoord);
        TriplePattern zcoord_tp = zcoord.isA(p_space_time.iri("StraightCoordinate")).andHas(p_system.iri("hasValue"),vzcoord);

        TriplePattern vxcoord_tp  = vxcoord.isA(p_coordsys.iri("CoordinateValue"))
        		.andHas(p_system.iri("numericalValue"), xyz_coord[0]).andHas(p_system.iri("hasUnitOfMeasure"), unit_degree);
        TriplePattern vycoord_tp = vycoord.isA(p_coordsys.iri("CoordinateValue"))
                .andHas(p_system.iri("numericalValue"), xyz_coord[1]).andHas(p_system.iri("hasUnitOfMeasure"), unit_degree);
        TriplePattern vzcoord_tp = vzcoord.isA(p_coordsys.iri("CoordinateValue"))
                .andHas(p_system.iri("numericalValue"), xyz_coord[2]).andHas(p_system.iri("hasUnitOfMeasure"), unit_m);

        TriplePattern [] coordinatesXYZ_tp = {projected_gp,xcoord_tp,ycoord_tp,zcoord_tp,vxcoord_tp,vycoord_tp,vzcoord_tp};
        
        TriplePattern [] combined_tp = {};
        combined_tp = ArrayUtils.addAll(combined_tp, weatherstation_tp);
        combined_tp = ArrayUtils.addAll(combined_tp, coordinatesXYZ_tp);
        combined_tp = ArrayUtils.addAll(combined_tp, getWeatherSensorTP(weatherstation_iri,p_station,station_name,cloud,unit_percentage));
        combined_tp = ArrayUtils.addAll(combined_tp, getWeatherSensorTP(weatherstation_iri,p_station,station_name,precipitation,unit_mm));
        combined_tp = ArrayUtils.addAll(combined_tp, getWeatherSensorTP(weatherstation_iri,p_station,station_name,pressure,unit_mbar));
        combined_tp = ArrayUtils.addAll(combined_tp, getWeatherSensorTP(weatherstation_iri,p_station,station_name,temperature,unit_celcius));
        combined_tp = ArrayUtils.addAll(combined_tp, getWeatherSensorTP(weatherstation_iri,p_station,station_name,humidity,unit_fraction));
        combined_tp = ArrayUtils.addAll(combined_tp, getWeatherSensorTP(weatherstation_iri,p_station,station_name,windspeed,unit_ms));
        combined_tp = ArrayUtils.addAll(combined_tp, getWeatherSensorTP(weatherstation_iri,p_station,station_name,winddirection,unit_degree));
        
        Prefix [] prefix_list = {p_station};
        prefix_list = ArrayUtils.addAll(prefix_list, getPrefix());
        
        ModifyQuery modify = Queries.MODIFY();
        
        modify.prefix(prefix_list).insert(combined_tp).where();
        performUpdate(repo, modify);
    }

    private TriplePattern [] getWeatherSensorTP(Iri station_iri, Prefix station_prefix, String station_name, String data, Iri unit) {
        Iri sensor_iri = station_prefix.iri(station_name+"_sensor"+data);
        Iri data_iri = station_prefix.iri(station_name+"_"+data);
        Iri datavalue_iri = station_prefix.iri(station_name+"_v"+data);
        Iri time_iri = station_prefix.iri(station_name+"_time"+data);
        
        TriplePattern station_tp = station_iri.has(p_system.iri("hasSubsystem"),sensor_iri);
        TriplePattern sensor_tp = sensor_iri.isA(p_instrument.iri("Q-Sensor")).andHas(p_ontosensor.iri("observes"),data_iri);
        TriplePattern data_tp = data_iri.isA(p_ontosensor.iri(data)).andHas(p_system.iri("hasValue"),datavalue_iri);
        
        TriplePattern datavalue_tp = datavalue_iri.isA(p_system.iri("ScalarValue"))
                .andHas(p_system.iri("numericalValue"), 0)
                .andHas(p_system.iri("hasUnitOfMeasure"), unit)
                .andHas(p_time.iri("hasTime"), time_iri);

        TriplePattern datatime_tp = time_iri.isA(p_time.iri("Instant")).andHas(p_time.iri("inXSDDateTime"),0);
        
        TriplePattern [] combined_tp = {station_tp,sensor_tp,data_tp,datavalue_tp,datatime_tp};
        return combined_tp;
    }

    public void createAirQualityStation(String station_name, double [] xy_coord) {
    	Iri airqualitystation_iri = p_station.iri(station_name);
        Iri stationcoordinates_iri = p_station.iri(station_name+"_coordinates");
        Iri xcoord = p_station.iri(station_name+"_xcoord");
        Iri ycoord = p_station.iri(station_name+"_ycoord");
        Iri vxcoord = p_station.iri(station_name+"_vxcoord");
        Iri vycoord = p_station.iri(station_name+"_vycoord");

        TriplePattern airqualitystation_tp = airqualitystation_iri.isA(p_station.iri("AirQualityStation"))
        		.andHas(p_space_time_extended.iri("hasGISCoordinateSystem"),stationcoordinates_iri);
        
        TriplePattern projected_gp = stationcoordinates_iri.isA(p_space_time_extended.iri("ProjectedCoordinateSystem"))
                .andHas(p_space_time_extended.iri("hasProjectedCoordinate_x"),xcoord)
                .andHas(p_space_time_extended.iri("hasProjectedCoordinate_y"),ycoord);

        TriplePattern xcoord_tp = xcoord.isA(p_space_time.iri("AngularCoordinate")).andHas(p_system.iri("hasValue"),vxcoord);
        TriplePattern ycoord_tp = ycoord.isA(p_space_time.iri("AngularCoordinate")).andHas(p_system.iri("hasValue"),vycoord);

        TriplePattern vxcoord_tp  = vxcoord.isA(p_coordsys.iri("CoordinateValue"))
        		.andHas(p_system.iri("numericalValue"), xy_coord[0]).andHas(p_system.iri("hasUnitOfMeasure"), unit_degree);
        TriplePattern vycoord_tp = vycoord.isA(p_coordsys.iri("CoordinateValue"))
                .andHas(p_system.iri("numericalValue"), xy_coord[1]).andHas(p_system.iri("hasUnitOfMeasure"), unit_degree);

        TriplePattern [] coordinatesXY_tp = {projected_gp,xcoord_tp,ycoord_tp,vxcoord_tp,vycoord_tp};

        TriplePattern [] combined_tp = {};
        combined_tp = ArrayUtils.addAll(combined_tp, airqualitystation_tp);
        combined_tp = ArrayUtils.addAll(combined_tp, coordinatesXY_tp);
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,CO2,unit_ugm3));
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,CO,unit_ugm3));
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,HC,unit_ugm3));
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,NO2,unit_ugm3));
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,NO,unit_ugm3));
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,NOx,unit_ugm3));
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,O3,unit_ugm3));
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,PM1,unit_ugm3));
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,PM25,unit_ugm3));
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,PM10,unit_ugm3));
        combined_tp = ArrayUtils.addAll(combined_tp, getAirQualitySensorTP(airqualitystation_iri,p_station,station_name,SO2,unit_ugm3));
        
        Prefix [] prefix_list = getPrefix();
        
        ModifyQuery modify = Queries.MODIFY();
        modify.prefix(prefix_list).insert(combined_tp).where();
        performUpdate(airquality_endpoint, modify);
    }
    
    public TriplePattern [] getAirQualitySensorTP(Iri station_iri, Prefix station_prefix, String station_name, String data, Iri unit) {
    	Iri sensor_iri = station_prefix.iri(station_name+"_sensor"+data);
        Iri data_iri = station_prefix.iri(station_name+"_"+data);
        Iri datavalue_iri = station_prefix.iri(station_name+"_v"+data);
        Iri time_iri = station_prefix.iri(station_name+"_time"+data);
        
        TriplePattern station_tp = station_iri.has(p_system.iri("hasSubsystem"),sensor_iri);
        TriplePattern sensor_tp = sensor_iri.isA(p_instrument.iri("Q-Sensor")).andHas(p_ontosensor.iri("observes"),data_iri);
        TriplePattern data_tp = data_iri.isA(p_ontosensor.iri(data)).andHas(p_system.iri("hasValue"),datavalue_iri);
        
        TriplePattern datavalue_tp = datavalue_iri.isA(p_system.iri("ScalarValue"))
                .andHas(p_ontosensor.iri("scaledNumValue"), 0)
                .andHas(p_ontosensor.iri("prescaledNumValue"), 0)
                .andHas(p_system.iri("hasUnitOfMeasure"), unit)
                .andHas(p_time.iri("hasTime"), time_iri);

        TriplePattern protocol_tp;
        TriplePattern state_tp;
        if (data.contentEquals(PM1) || data.contentEquals(PM10) || data.contentEquals(PM25)) {
        	protocol_tp = sensor_iri.has(p_ontosensor.iri("particleProtocolVersion"), "V3.0");
        	state_tp = datavalue_iri.has(p_ontosensor.iri("particleState"),"OK");
        } else {
        	protocol_tp = sensor_iri.has(p_ontosensor.iri("gasProtocolVersion"), "V5.1");
        	state_tp = datavalue_iri.has(p_ontosensor.iri("gasState"),"Reading");
        }
        
        TriplePattern datatime_tp = time_iri.isA(p_time.iri("Instant")).andHas(p_time.iri("inXSDDateTime"),0);
		
		TriplePattern [] combined_tp = {station_tp,sensor_tp,data_tp,datavalue_tp,datatime_tp,protocol_tp,state_tp};
        return combined_tp;
    }

    public JSONArray queryAirStationsWithinScope(Scope sc) {
    	SelectQuery query = Queries.SELECT();
    	
    	// properties we want to query
    	Variable station = SparqlBuilder.var("station");
    	Variable xvalue = SparqlBuilder.var("xvalue");
    	Variable yvalue = SparqlBuilder.var("yvalue");
    	
    	// properties we don't need
    	Variable coord = query.var();
        Variable xcoord = query.var();
        Variable ycoord = query.var();
        Variable vxcoord = query.var();
        Variable vycoord = query.var();
    	
        GraphPattern station_gp = station.has(p_space_time_extended.iri("hasGISCoordinateSystem"),coord);
        GraphPattern projected_gp = coord.has(p_space_time_extended.iri("hasProjectedCoordinate_x"),xcoord)
        		.andHas(p_space_time_extended.iri("hasProjectedCoordinate_y"),ycoord);
        GraphPattern coord_gp = GraphPatterns.and(xcoord.has(p_system.iri("hasValue"),vxcoord), 
        		ycoord.has(p_system.iri("hasValue"),vycoord));
        GraphPattern vcoord_gp = GraphPatterns.and(vxcoord.has(p_system.iri("numericalValue"), xvalue),
        		vycoord.has(p_system.iri("numericalValue"), yvalue));
        
    	// constraint to stations within scope
    	sc.transform("EPSG:4326");
        Expression<?> xconstraint = Expressions.and(Expressions.lt(xvalue, sc.getUpperx()),Expressions.gt(xvalue, sc.getLowerx()));
        Expression<?> yconstraint = Expressions.and(Expressions.lt(yvalue, sc.getUppery()),Expressions.gt(yvalue, sc.getLowery()));
        Expression<?> overallconstraint = Expressions.and(xconstraint,yconstraint);
        
        GraphPatternNotTriples querypattern = GraphPatterns.and(station_gp,projected_gp,coord_gp,vcoord_gp)
        		.filter(overallconstraint);
        
    	query.prefix(p_space_time_extended, p_system).select(station,xvalue,yvalue).where(querypattern);

    	return performQuery(airquality_endpoint,query);
    }

    private void performUpdate(String queryEndPoint, ModifyQuery query) {
        RemoteKnowledgeBaseClient kbClient = new RemoteKnowledgeBaseClient();
        kbClient.setUpdateEndpoint(queryEndPoint);
        kbClient.setQuery(query.getQueryString());
        System.out.println("kbClient.executeUpdate():"+kbClient.executeUpdate());
    }

    private JSONArray performQuery(String queryEndPoint, SelectQuery query) {
        RemoteKnowledgeBaseClient kbClient = new RemoteKnowledgeBaseClient();
        kbClient.setQueryEndpoint(queryEndPoint);
        kbClient.setQuery(query.getQueryString());
        JSONArray result = kbClient.executeQuery();
        System.out.println("kbClient.executeQuery():"+result);
        return result;
    }
}
