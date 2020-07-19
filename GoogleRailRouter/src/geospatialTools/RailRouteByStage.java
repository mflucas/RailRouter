package geospatialTools;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
/**
 * RailRouteByStage extends a DirectionsRoute with information about the Rail leg if a train was boarded as part of the journey. 
 * It is assumed that the route request DOES NOT INCLUDE WAYPOINTS meaning that only one leg is available per request. 
 * It calculates the distance and the duration as well as produces an arraylist of all train modes boarded during the journey. 
 * Distance = Sum of the total distance of all train steps (excluding transfers, access and egress) -> Total in-vehicle distance for trains
 * Duration = Sum of the total duration of all train steps (excluding transfers, access and egress) -> Total in-vehicle duration for trains
 * @author LMF
 *
 */
@SuppressWarnings("serial")
public class RailRouteByStage extends DirectionsRoute {

	private long transitTime;
	private long transitDistance;
	private double speed;
	private ArrayList<String> transitModes;
	private ArrayList<String> products;
	private DirectionsLeg leg;
	private MultiLineString mlineString; 
	private String startLocation;

	public RailRouteByStage() {
		super();
	}

	public RailRouteByStage(DirectionsRoute route) {
		super();

		DirectionsLeg leg = route.legs[0];
		DirectionsStep[] steps = leg.steps;
		ArrayList<String> transitModes = new ArrayList<String>();
		ArrayList<String> products = new ArrayList<String>();
		List<LineString> lineArray = new ArrayList<>();
		GeometryFactory gFact = new GeometryFactory();

		setStartLocation(leg.startAddress);		
		long transitDistance = 0;
		long transitTime = 0;
		for (int i = 0; i < steps.length; i++) {
			DirectionsStep step = steps[i];
			if (step.transitDetails != null && (step.transitDetails.line.vehicle.name.toLowerCase().contains("train")
					|| (step.transitDetails.line.vehicle.name.toLowerCase().contains("rail")))) {
				transitTime += step.duration.inSeconds;
				transitDistance += step.distance.inMeters;
				transitModes.add(step.transitDetails.line.vehicle.name.toLowerCase());
				products.add(step.transitDetails.line.shortName);
				lineArray.add(GoogleRoutesToGeoTools.encodedPolylineToLineString(step.polyline));
			}
		}
		LineString[] formattedArray = lineArray.toArray(new LineString[lineArray.size()]);
		MultiLineString mlineString = gFact.createMultiLineString(formattedArray);
		try {
			speed = 3.6 *(transitDistance/transitTime);

		} catch(java.lang.ArithmeticException k) {
			speed=-99;
		}

		this.setTransitTime(transitTime);
		this.setTransitDistance(transitDistance);
		this.setSpeed(speed);
		this.setTransitModes(transitModes);
		this.setProducts(products);
		this.setLeg(leg);
		this.setMlineString(mlineString);
		this.setStartLocation(startLocation);

	}

	
	public long getTransitTime() {
		return transitTime;
	}

	public void setTransitTime(long transitTime) {
		this.transitTime = transitTime;
	}

	public long getTransitDistance() {
		return transitDistance;
	}

	public void setTransitDistance(long transitDistance) {
		this.transitDistance = transitDistance;
	}

	public ArrayList<String> getTransitModes() {
		return transitModes;
	}

	public void setTransitModes(ArrayList<String> transitModes) {
		this.transitModes = transitModes;
	}

	public ArrayList<String> getProducts() {
		return products;
	}

	public void setProducts(ArrayList<String> products) {
		this.products = products;
	}

	public DirectionsLeg getLeg() {
		return leg;
	}

	public void setLeg(DirectionsLeg leg) {
		this.leg = leg;
	}

	public MultiLineString getMlineString() {
		return mlineString;
	}

	public void setMlineString(MultiLineString mlineString) {
		this.mlineString = mlineString;
	}

	public String getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(String startLocation) {
		this.startLocation = startLocation;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

}
