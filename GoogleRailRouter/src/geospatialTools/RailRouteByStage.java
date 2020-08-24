package geospatialTools;

import java.util.ArrayList;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.TravelMode;
import com.google.maps.model.VehicleType;

/**
 * RailRouteByStage extends a DirectionsRoute with information about the Rail
 * leg if a train was boarded as part of the journey. It is assumed that the
 * route request DOES NOT INCLUDE WAYPOINTS meaning that only one leg is
 * available per request. It calculates the distance and the duration as well as
 * produces an arraylist of all train modes boarded during the journey. Distance
 * = Sum of the total distance of all train steps (excluding transfers, access
 * and egress) -> Total in-vehicle distance for trains Duration = Sum of the
 * total duration of all train steps (excluding transfers, access and egress) ->
 * Total in-vehicle duration for trains
 * 
 * @author LMF
 *
 */
@SuppressWarnings("serial")
public class RailRouteByStage  {
	private long finalTransitTime;
	private long transitTime;
	private long transitDistance;
	private double speed;
	private ArrayList<String> transitModes;
	private ArrayList<String> products;
	private DirectionsLeg leg;
	private String startLocation;
	private ArrayList<DirectionsStep> railSteps;

	public RailRouteByStage() {
		super();
	}

	/**
	 * Constructor for final use of the route using manual duration
	 * 
	 * @param route
	 * @param duration
	 * @param isManual
	 */
	public RailRouteByStage(DirectionsRoute route, Long duration, int isManual) {
		
		super();
		DirectionsLeg leg = route.legs[0];
		DirectionsStep[] steps = leg.steps;
		ArrayList<String> transitModes = new ArrayList<String>();
		ArrayList<String> products = new ArrayList<String>();
		ArrayList<DirectionsStep> railSteps = new ArrayList<DirectionsStep>();

		this.setStartLocation(leg.startAddress);
		long transitDistance = 0;
		for (int i = 0; i < steps.length; i++) {
			DirectionsStep step = steps[i];
						
			if (stepIsRail(step, i, steps.length)) {
				
				railSteps.add(step);
				transitTime += step.duration.inSeconds;
				transitDistance += step.distance.inMeters;
				transitModes.add(step.transitDetails.line.vehicle.name);
				products.add(step.transitDetails.line.shortName);
				
			}
		}
		if (isManual == 1) {
			finalTransitTime = duration;
		} else {
			finalTransitTime = transitTime;
		}
		try {
			speed = 3.6 * (transitDistance / transitTime);

		} catch (java.lang.ArithmeticException k) {
			speed = -99;
		}
		this.setTransitTime(transitTime);
		this.setTransitDistance(transitDistance);
		this.setSpeed(speed);
		this.setTransitModes(transitModes);
		this.setProducts(products);
		this.setLeg(leg);
		this.setRailSteps(railSteps);

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

	private boolean stepIsRail(DirectionsStep step, int index, int length) {
		
		boolean isIt = true;

			if (step.travelMode == TravelMode.WALKING) {
			isIt = false;
		}

		if (step.travelMode == TravelMode.TRANSIT && step.distance.inMeters < 3500) {
			isIt = false;

		}
		if (step.travelMode == TravelMode.TRANSIT && step.distance.inMeters < 3500) {
			isIt = false;

		}

		if (step.travelMode == TravelMode.TRANSIT) {

			if ((step.transitDetails.line.vehicle.type == VehicleType.SUBWAY)
					|| (step.transitDetails.line.vehicle.type == VehicleType.TRAM)
					|| (step.transitDetails.line.vehicle.type == VehicleType.TROLLEYBUS)
					|| (step.transitDetails.line.vehicle.type == VehicleType.METRO_RAIL)) {
				isIt = false;

			}
		}
		if (index == 0 || index == length) {

			if (step.travelMode == TravelMode.TRANSIT && step.distance.inMeters < 5100) {
				isIt = false;

			}
			if (step.travelMode == TravelMode.TRANSIT && step.distance.inMeters < 5100) {
				isIt = false;

			}
		}
		return isIt;


	}

	public long getFinalTransitTime() {
		return finalTransitTime;
	}

	public void setFinalTransitTime(long finalTransitTime) {
		this.finalTransitTime = finalTransitTime;
	}

	public ArrayList<DirectionsStep> getRailSteps() {
		return railSteps;
	}

	public void setRailSteps(ArrayList<DirectionsStep> railSteps) {
		this.railSteps = railSteps;
	}

}
