package uk.ac.cam.cares.jps.base.timeseries;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

import uk.ac.cam.cares.jps.base.exception.JPSRuntimeException;

/**
 * This class provides unit tests for the TimeSeries class
 */

public class TimeSeriesTest {
	
	TimeSeries<Instant> ts;
	List<String> dataIRI = new ArrayList<>();
	List<Instant> timeList = new ArrayList<>();
	List<Double> data1 = new ArrayList<>();
	List<String> data2 = new ArrayList<>();
	List<Integer> data3 = new ArrayList<>();
	List<List<?>> dataToAdd = new ArrayList<>();
	
	@Before
	public void initialiseData() {
		dataIRI.add("http://data1"); dataIRI.add("http://data2"); dataIRI.add("http://data3"); 
		for (int i = 0; i < 10; i++) {
			timeList.add(Instant.now().plusSeconds(i));
			data1.add(Double.valueOf(i));
			data2.add(String.valueOf(i));
			data3.add(Integer.valueOf(i));
		}		
		dataToAdd.add(data1); dataToAdd.add(data2); dataToAdd.add(data3);
	}
	
	/**
	 * Constructor tests
	 */
	
	@Test (expected = JPSRuntimeException.class)
	public void testConstructorWithoutDataIRI() {
		// call constructor with empty dataIRI and data series ArrayLists
		ts = new TimeSeries<Instant>(timeList, new ArrayList<>(), new ArrayList<>());
	}
	
	@Test (expected = JPSRuntimeException.class)
	public void testConstructorWithWrongDataIRIsize() {
		// drop last data series to create mismatch between number of dataIRIs and data series
		dataToAdd.remove(dataToAdd.size()-1);
		ts = new TimeSeries<Instant>(timeList, dataIRI, dataToAdd);
	}
	
	@Test (expected = JPSRuntimeException.class)
	public void testConstructorWithWrongDataSeriesLength() {
		// drop first entry of first data series to create mismatch between length of time series and data series
		dataToAdd.get(0).remove(0);
		ts = new TimeSeries<Instant>(timeList, dataIRI, dataToAdd);
	}
	
	/**
	 *  Method tests
	 */
	
	@Test
    public void testGetTimes() {
		// constructor for TimeSeries object takes: time column, dataIRIs, and corresponding values in lists
		ts = new TimeSeries<Instant>(timeList, dataIRI, dataToAdd);
    	Assert.assertEquals(ts.getTimes(), timeList);
    }
    
	@Test
    public void testGetDataIRI() {
		// constructor for TimeSeries object takes: time column, dataIRIs, and corresponding values in lists
		ts = new TimeSeries<Instant>(timeList, dataIRI, dataToAdd);
    	for (String iri : ts.getDataIRIs()) {
    		Assert.assertTrue(dataIRI.contains(iri));
    	}
    }
    
	@Test
    public void testGetValues() {
		// Constructor for TimeSeries object takes: time column, dataIRIs, and corresponding values in lists
		ts = new TimeSeries<Instant>(timeList, dataIRI, dataToAdd);
    	Assert.assertEquals(ts.getValues(dataIRI.get(0)),data1);
    	Assert.assertEquals(ts.getValues(dataIRI.get(1)),data2);
    	Assert.assertEquals(ts.getValues(dataIRI.get(2)),data3);
    	// Check whether null is returned for non-existing dataIRI keys
    	Assert.assertNull(ts.getValues("data0"));
    }
    
    /**
     * To use this, the original data must be an instance of "Number"
     */
	@Test
    public void testGetValuesAsDouble() {
		// Constructor for TimeSeries object takes: time column, dataIRIs, and corresponding values in lists
		ts = new TimeSeries<Instant>(timeList, dataIRI, dataToAdd);
		Assert.assertEquals(ts.getValuesAsDouble(dataIRI.get(0)), data1);
		Assert.assertEquals(ts.getValuesAsDouble(dataIRI.get(0)).get(0).getClass(), Double.class);
    	Assert.assertEquals(ts.getValuesAsDouble(dataIRI.get(2)).get(0).getClass(), Double.class);
    	// Test for non-existing and non-castable data series
    	Assert.assertNull(ts.getValuesAsDouble("data0"));
    	try {
    		List<Double> v = ts.getValuesAsDouble(dataIRI.get(1));
    	} catch (Exception e) {
    		Assert.assertTrue(e instanceof JPSRuntimeException);
    		Assert.assertEquals("TimeSeries: Values for provided dataIRI are not castable to \"Number\"",
    					        e.getMessage());
    	}
    }
    
    /** 
     * If the original class has a toString() method, this should work
     */
	@Test
    public void testGetValuesAsString() {
		// Constructor for TimeSeries object takes: time column, dataIRIs, and corresponding values in lists
		ts = new TimeSeries<Instant>(timeList, dataIRI, dataToAdd);
    	for (String data : dataIRI) {
    		Assert.assertEquals(ts.getValuesAsString(data).get(0).getClass(), String.class);
    	}
    }
    
    /**
     * To use this, the original data must be an instance of "Number"
     */
	@Test
    public void testGetValuesAsInteger() {
		// Constructor for TimeSeries object takes: time column, dataIRIs, and corresponding values in lists
		ts = new TimeSeries<Instant>(timeList, dataIRI, dataToAdd);
		Assert.assertEquals(ts.getValuesAsInteger(dataIRI.get(0)), data3);
    	Assert.assertEquals(ts.getValuesAsInteger(dataIRI.get(0)).get(0).getClass(), Integer.class);
    	Assert.assertEquals(ts.getValuesAsInteger(dataIRI.get(2)).get(0).getClass(), Integer.class);
    	// Test for non-existing and non-castable data series
    	Assert.assertNull(ts.getValuesAsInteger("data0"));
    	try {
    		List<Integer> v = ts.getValuesAsInteger(dataIRI.get(1));
    	} catch (Exception e) {
    		Assert.assertTrue(e instanceof JPSRuntimeException);
    		Assert.assertEquals("TimeSeries: Values for provided dataIRI are not castable to \"Number\"",
    					        e.getMessage());
    	}
    }
}
