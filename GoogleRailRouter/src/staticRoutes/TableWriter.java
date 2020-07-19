package staticRoutes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;

public class TableWriter {

	public static String stepsWriter(DirectionsStep step, Integer cityPairID, Integer routeIndex, Integer stepIndex, String start,
			String end) throws IOException {

		String transitDepStop = new String();
		String transitArrStop = new String();
		String serviceType = new String();
		String serviceName = new String();

		if (step.travelMode.name() == "TRANSIT") {
			transitDepStop = step.transitDetails.departureStop.name;
			transitArrStop = step.transitDetails.arrivalStop.name;
			serviceType = step.transitDetails.line.vehicle.name;
			serviceName = step.transitDetails.line.shortName;
		} else {
			transitDepStop = "-99";
			transitArrStop = "-99";
			serviceType = "-99";
			serviceName = "-99";
		}
		
		
//		String polyline = step.polyline.toString().replace("[EncodedPolyline: ", "\"").replace("]", "\"");
//		String latPoints = latListGen(step.polyline);
//		String lonPoints = latListGen(step.polyline);

		String writableSteps = routeIndex + ";" + start + ";" + end + ";" + stepIndex + ";" + step.duration.inSeconds + ";"
				+ step.distance.inMeters + ";" + step.startLocation.lat + ";" + step.startLocation.lng + ";"
				+ step.travelMode.name() + ";" + transitDepStop + ";" + transitArrStop + ";" + serviceType + ";"
				+ serviceName;
//				+ ";" + lonPoints + ";" + latPoints;

		return writableSteps;
	}
	
	public static String routesWriter(DirectionsRoute route, Integer cityPairID,Integer routeIndex, String start,
			String end) throws IOException {
		
		DirectionsLeg leg = route.legs[0];

//		
		String latPoints = latListGen(route.overviewPolyline);
		String lonPoints = lonListGen(route.overviewPolyline);
//		String polyline = route.overviewPolyline.toString().replace("[EncodedPolyline: ", "\"").replace("]", "\"");

try {
	String writableRoutes = cityPairID + ";" + routeIndex + ";" + start + ";" + end + ";" + 
			leg.duration.inSeconds + ";" + 
			leg.distance.inMeters + ";" + 
			leg.startLocation.lat + ";" + 
			leg.startLocation.lng + ";" + 
			leg.endLocation.lat + ";" + 
			leg.endLocation.lng + ";" + 
			leg.steps.length + ";" + 
			leg.departureTime.toEpochSecond() + ";" + 
			leg.arrivalTime.toEpochSecond() + ";" +
			lonPoints + ";" + 
			latPoints;		
	return writableRoutes;

} catch (java.lang.NullPointerException ex) {
	String writableRoutes = cityPairID + ";" + routeIndex + ";" + start + ";" + end + ";" + 
			"-99" + ";" + 
			"-99" + ";" + 
			"-99" + ";" + 
			"-99" + ";" + 
			"-99" + ";" + 
			"-99" + ";" + 
			"-99" + ";" + 
			"-99" + ";" + 
			"-99" + ";" +
			"-99"+ ";" +
			"-99";	
	return writableRoutes;

}
		
	}
	
	public static String fastestRouteWriter(DirectionsLeg fastestLeg, Integer cityPairID, String start,
			String end) throws IOException {

		//stepsWriter.write("OD_ID;ROUTE_ALTERNATIVE;FROM;TO;DURATION;DISTANCE;STARTLOCATION_LAT;STARTLOCATION_LON;ENDLOCATION_LAT;ENDLOCATION_LON;STEPS;DEP_TIME;ARR_TIME");


		String fastestRoute = cityPairID + ";" + start + ";" + end + ";" + 
				fastestLeg.duration.inSeconds + ";" + 
				fastestLeg.distance.inMeters + ";" + 
				fastestLeg.startLocation.lat + ";" + 
				fastestLeg.startLocation.lng + ";" + 
				fastestLeg.endLocation.lat + ";" + 
				fastestLeg.endLocation.lng + ";" + 
				fastestLeg.steps.length + ";" + 
				fastestLeg.departureTime.toEpochSecond() + ";" + 
				fastestLeg.arrivalTime.toEpochSecond();

		return fastestRoute;
	}
	
	private static String latListGen(EncodedPolyline polyline) {
		
		List<Double> latPoints = new ArrayList<Double>();
		
		for (ListIterator<LatLng> iter = polyline.decodePath().listIterator(); iter.hasNext();) {
			LatLng latLng = iter.next();
			
			latPoints.add(latLng.lat);
		}
		
		
		String lat = latPoints.toString().replace("[", "").replace("]", "");

		
		return lat;	
	}
	
	private static String lonListGen(EncodedPolyline polyline) {
		
		List<Double> lonPoints = new ArrayList<Double>();
		
		for (ListIterator<LatLng> iter = polyline.decodePath().listIterator(); iter.hasNext();) {
			LatLng latLng = iter.next();
			
			lonPoints.add(latLng.lng);
		}
		
		String lon = lonPoints.toString().replace("[", "").replace("]", "");

		
		return lon;	
	}
}
