package staticRoutes;

import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsStep;

@SuppressWarnings("serial")
public class DirectionsLegLongDistanceModes extends DirectionsLeg{
	
	public DirectionsLegLongDistanceModes(DirectionsLeg leg) {
		
		DirectionsStep[] steps = leg.steps;
		
		for (int i=0;i<steps.length; i++) {
			
			DirectionsStep step = steps[i];
			step.travelMode.toString();
			if(step.travelMode.name()=="TRANSIT" ) {
			}			
		}
	}

}
