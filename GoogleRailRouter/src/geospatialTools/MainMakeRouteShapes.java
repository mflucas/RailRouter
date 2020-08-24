package geospatialTools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.block.factory.HashingStrategies;
import org.eclipse.collections.impl.utility.ListIterate;
import org.opengis.feature.simple.SimpleFeature;
import com.google.maps.model.DirectionsRoute;
import router.Constants;
import staticRoutes.MainReaderIn;

public class MainMakeRouteShapes {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		List<RailRouteByStage> fastestRoutes = routesConstructor(Constants.PROCESSED_ROUTES);
		// Create Shapefiles
//		createPointShapefile(fastestRoutes);
//		createRoutesShapefile(fastestRoutes);
	}
	
	
/**
 * Method to create the fastest routes out of metro regions list
 * @param metroRegionList
 * @return
 * @throws IOException
 * @throws ClassNotFoundException
 */
	public static List<RailRouteByStage> routesConstructor(String filePath)
	    throws IOException, ClassNotFoundException {
		ArrayList<RailRouteByStage> fastestRoutes = new ArrayList<RailRouteByStage>();
		FileInputStream inputStream = new FileInputStream(filePath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		String splitBy=";";
		reader.readLine();
		String s = reader.readLine();
		int k=1;
		while (s != null) {
			//Read in data from table
			String[] b = s.split(splitBy);
			String from=b[4].replace("\"", "");
			String to=b[5].replace("\"", "");
			System.out.println(k + ": reading in " + from + " to " + to);
			int isManual = Integer.parseInt(b[20]);
			Long duration = Long.parseLong(b[18]);
			
			//Read in routes
			String fileName = from + "_" + to;

			List<DirectionsRoute> routes = MainReaderIn.serializeDataIn(fileName);
			List<RailRouteByStage> railRoutes = directionsRouteToRailRouteList(routes, duration, isManual);		
			List<RailRouteByStage> filteredRoutes = railRoutes.stream().filter(c->c.getTransitTime()>0).collect(Collectors.toList());
			
			RailRouteByStage fastestRoute = filteredRoutes
				      .stream()
				      .min(Comparator.comparing(RailRouteByStage::getTransitTime))
				      .orElseThrow(NoSuchElementException::new);

				fastestRoutes.add(fastestRoute);
		
			s = reader.readLine();
			k++;
		}
		reader.close();
		
		

//		for (ListIterator<String> from = cityList.listIterator(); from.hasNext();) {
//			String cityFrom = from.next();
//			for (ListIterator<String> to = cityList.listIterator(); to.hasNext();) {
//				String cityTo = to.next();
//				if (cityTo != cityFrom) {
//					String fileName = cityFrom + "_" + cityTo;
//					
//					try {
//						List<DirectionsRoute> routes = MainReaderIn.serializeDataIn(fileName);
//
//					
//
//					List<RailRouteByStage> railRoutes = directionsRouteToRailRoute(routes);
//
//					if (Constants.SELECT_FASTEST_BY_TRANSIT_LEGS_ONLY) {
//						RailRouteByStage fastestRoute = Collections.min(railRoutes,
//								Comparator.comparing(s -> s.getTransitTime()));
//						fastestRoutes.add(fastestRoute);
//
//					} else {
//						RailRouteByStage fastestRoute = Collections.min(railRoutes,
//								Comparator.comparing(s -> s.legs[0].duration.inSeconds));
//						fastestRoutes.add(fastestRoute);
//					}
//					} catch (java.lang.NullPointerException e) {
//						System.out.println("_________________"+ cityFrom +" to " + cityTo + " not found");
//					}
//
//				}
//			}
//		}
		return fastestRoutes;
	}
/**

* Create Route List
 * @param route2
 * @return
 */
	public static List<RailRouteByStage> directionsRouteToRailRouteList(List<DirectionsRoute> route2, Long duration, int isManual) {
		List<RailRouteByStage> railRoutes = new ArrayList<RailRouteByStage>();
		
		for (ListIterator<DirectionsRoute> iter = route2.listIterator(); iter.hasNext();) {
			DirectionsRoute route = iter.next();
				RailRouteByStage railRoute = new RailRouteByStage(route, duration, isManual);
			railRoutes.add(railRoute);
		}

		return railRoutes;

	}
/**
 * Create Points list
 * @param routes
 * @throws IOException
 */
//	public static void createPointShapefile(List<RailRouteByStage> routes) throws IOException {
//		List<RailRouteByStage> uniqueStartCities = ListIterate.distinct(routes,
//				HashingStrategies.fromFunction(p -> p.getLeg().startAddress));
//		List<SimpleFeature> pointFeatures = PointsToGeoTools.featureCollectionCreator(uniqueStartCities);
//		PointsToGeoTools.writeShapefile(Constants.POINTS_SHAPEFILE_NAME, Constants.SHAPEFILE_PATH, pointFeatures);
//	}

	public static void createRoutesShapefile(List<RailRouteByStage> routes) throws IOException {
		List<SimpleFeature> routeFeatures = GoogleRoutesToGeoTools.featureCollectionCreator(routes);
		GoogleRoutesToGeoTools.writeShapefile(Constants.SHAPEFILE_NAME, Constants.SHAPEFILE_PATH, routeFeatures);
	}

}
