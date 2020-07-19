package staticRoutes;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.ListIterator;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import router.Constants;
import router.Router;

public class MainReaderIn {

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		BufferedWriter stepsWriter = new BufferedWriter(new FileWriter(Constants.STEPS_TABLE));
		stepsWriter.write(
				"ROUTE_ALTERNATIVE;FROM;TO;STEP;DURATION;DISTANCE;STARTLOCATION_LAT;STARTLOCATION_LON;MODE;TRANSIT_DEP_STOP;TRANSIT_ARR_STOP;SERVICE_TYPE;SERVICE_NAME");
		stepsWriter.newLine();

		BufferedWriter routesWriter = new BufferedWriter(new FileWriter(Constants.ROUTES_TABLE));
		routesWriter.write(
				"OD_ID;ROUTE_ALTERNATIVE;FROM;TO;DURATION;DISTANCE;STARTLOCATION_LAT;STARTLOCATION_LON;ENDLOCATION_LAT;ENDLOCATION_LON;STEPS;DEP_TIME;ARR_TIME;LON_LIST;LAT_LIST");
		routesWriter.newLine();

//		BufferedWriter fastestRouteWriter = new BufferedWriter(new FileWriter(Constants.FASTEST_ROUTE_TABLE));
//		fastestRouteWriter.write(
//				"OD_ID;FROM;TO;DURATION;DISTANCE;STARTLOCATION_LAT;STARTLOCATION_LON;ENDLOCATION_LAT;ENDLOCATION_LON;STEPS;DEP_TIME;ARR_TIME");
//		fastestRouteWriter.newLine();

		List<String> cityList = Router.produceCityList(Constants.METRO_REGIONS);

		int jj = 0;
		for (ListIterator<String> from = cityList.listIterator(); from.hasNext();) {
			String cityFrom = from.next();
			for (ListIterator<String> to = cityList.listIterator(); to.hasNext();) {
				String cityTo = to.next();
				if (cityTo != cityFrom) {
					String fileName = cityFrom + "_" + cityTo;
					List<DirectionsRoute> routes = serializeDataIn(fileName);

//					DirectionsRoute fastestRoute = Collections.min(routes,
//							Comparator.comparing(s -> s.legs[0].duration.inSeconds));
//					DirectionsLeg fastestLeg = fastestRoute.legs[0];
//
//					String fastestlineRoute = TableWriter.fastestRouteWriter(fastestLeg, jj, cityFrom, cityTo);
//					fastestRouteWriter.write(fastestlineRoute);
//					fastestRouteWriter.newLine();
					
					if(routes != null) {

					int k = 0;

					for (ListIterator<DirectionsRoute> iter = routes.listIterator(); iter.hasNext();) {
						DirectionsRoute route = iter.next();

						String lineRoute = TableWriter.routesWriter(route, jj, k, cityFrom, cityTo);

						routesWriter.write(lineRoute);
						routesWriter.newLine();



						DirectionsStep[] steps = route.legs[0].steps;

						for (int i = 0; i < steps.length; i++) {
							DirectionsStep step = steps[i];
							String line = TableWriter.stepsWriter(step, jj, k, i, cityFrom, cityTo);

							stepsWriter.write(line);
							stepsWriter.newLine();

						}

						k++;
					}
				}
					
					

					System.out.println("Read " + cityFrom + " to " + cityTo);
				}
				jj++;
			}
		}

		routesWriter.flush();
		routesWriter.close();
		
//		fastestRouteWriter.flush();
//		fastestRouteWriter.close();

		stepsWriter.flush();
		stepsWriter.close();

	}

	public static List<DirectionsRoute> serializeDataIn(String fileName)  {
		String file = Constants.FILE_OUT_PATH + fileName + ".txt";
		try {
			FileInputStream fin = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fin);
			@SuppressWarnings("unchecked")
			List<DirectionsRoute> routes = (List<DirectionsRoute>) ois.readObject();
			ois.close();
			return routes;
			
		} catch (java.io.FileNotFoundException e) {
			System.out.println("-------- "+ fileName + " NOT FOUND");
			return null;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;

		}

	}

}
