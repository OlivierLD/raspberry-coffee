package nmea.forwarders.rmi.clientoperations;

import com.google.gson.Gson;
import context.ApplicationContext;
import context.NMEADataCache;
import nmea.forwarders.rmi.Task;

import java.io.Serializable;

/**
 * Task invokable by the client.
 * Runs (executes) on the server.
 *
 * Get ALL the cache
 * This one could be used to implement a full console.
 */
public class NMEACache implements Task<NMEADataCache>, Serializable {

	private static final long serialVersionUID = 227L;

	public NMEACache() {
	}

	/*
		Example of NMEADataCache, in Json.
{
 "Damping":30,
 "HDG Offset":0.0,
 "D":{
		"angle":10.0
 },
 "XTE":{
		"distance":3.0
 },
 "To Waypoint":"RANGI   ",
 "AWA":{
		"angle":-127.0
 },
 "Depth":{
		"depthInMeters":1.7000000476837158
 },
 "CDR":{
		"angle":153.20401371546302
 },
 "Daily":{
		"distance":12.4
 },
 "Bearing to WP":{
		"angle":230.0
 },
 "Max Leeway":10.0,
 "W":{
		"angle":9.01692220976113
 },
 "Speed to WP":{
		"speed":6.9
 },
 "COG":{
		"angle":220.0
 },
 "HDG c.":{
		"angle":226.0
 },
 "CMG":{
		"angle":235.01692220976113
 },
 "AWS":{
		"speed":15.6
 },
 "HDG true":{
		"angle":235.01692220976113
 },
 "BSP":{
		"speed":6.5
 },
 "AWA Offset":0.0,
 "TWA":{
		"angle":-146.6086335818184
 },
 "TWD":{
		"angle":88.0
 },
 "Current calculated with damping":{
		"600000":{
			 "bufferLength":600000,
			 "speed":{
					"speed":0.3823256500119228
			 },
			 "direction":{
					"angle":161.74895190013757
			 }
		}
 },
 "CSP":{
		"speed":1.832405479531865
 },
 "d":{
		"angle":-0.9830777902388692
 },
 "Position":{
		"lat":-9.110283333333333,
		"lng":-140.21116666666666
 },
 "Log":{
		"distance":3013.0
 },
 "Solar Time":{
		"date":"Jan 1, 1970 4:48:19 AM"
 },
 "Default Declination":{
		"angle":14.0
 },
 "Deviation file name":"dp_2011_04_15.csv",
 "HDG mag.":{
		"angle":225.01692220976113
 },
 "SOG":{
		"speed":7.0
 },
 "Leeway":{
		"angle":0.0
 },
 "GPS Date \u0026 Time":{
		"date":"Nov 21, 2010 2:09:08 PM"
 },
 "BSP Factor":1.0,
 "WayPoint pos":{
		"lat":0.0,
		"lng":0.0
 },
 "Set and Drift":{
		"speed":0.4,
		"angle":162
 },
 "From Waypoint":"",
 "TWS":{
		"speed":19.3
 },
 "GPS Time":{
		"date":"Jan 1, 1970 2:09:10 PM"
 },
 "Steer":"R",
 "Distance to WP":{
		"distance":561.6
 },
 "AWS Factor":1.0,
 "Water Temperature":{
		"temperature":26.5
 },
 "NMEA":"$IIMTW,+26.5,C*39\n\r"
}
	 */
	public NMEADataCache execute() {
		NMEADataCache cache = ApplicationContext.getInstance().getDataCache();
		if ("true".equals(System.getProperty("remove.dev.curve", "true"))) {
			cache.remove(NMEADataCache.DEVIATION_DATA);
		}
		try {
			String content = new Gson().toJson(cache).toString(); // For tests
			System.out.println(">>> Cache, as Json:" + content);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return cache;
	}
}
