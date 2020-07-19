package geospatialTools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.collections.impl.block.factory.HashingStrategies;
import org.eclipse.collections.impl.utility.ListIterate;
import org.opengis.feature.simple.SimpleFeature;
import com.google.maps.model.DirectionsRoute;
import router.Constants;
import router.Router;
import staticRoutes.MainReaderIn;

public class MainMakeRouteShapes {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		List<RailRouteByStage> fastestRoutes = fastestRoutesCreator(Constants.METRO_REGIONS);
		// Create Shapefiles
		createPointShapefile(fastestRoutes);
		createRoutesShapefile(fastestRoutes);
	}
	
	
/**
 * Method to create the fastest routes out of metro regions list
 * @param metroRegionList
 * @return
 * @throws IOException
 * @throws ClassNotFoundException
 */
	public static List<RailRouteByStage> fastestRoutesCreator(String metroRegionList)
			throws IOException, ClassNotFoundException {
		List<String> cityList = Router.produceCityList(Constants.METRO_REGIONS);
		ArrayList<RailRouteByStage> fastestRoutes = new ArrayList<RailRouteByStage>();

		System.out.println(cityList);

		for (ListIterator<String> from = cityList.listIterator(); from.hasNext();) {
			String cityFrom = from.next();
			for (ListIterator<String> to = cityList.listIterator(); to.hasNext();) {
				String cityTo = to.next();
				if (cityTo != cityFrom) {
					String fileName = cityFrom + "_" + cityTo;
					
					try {
						List<DirectionsRoute> routes = MainReaderIn.serializeDataIn(fileName);

					

					List<RailRouteByStage> railRoutes = directionsRouteToRailRoute(routes);

					if (Constants.SELECT_FASTEST_BY_TRANSIT_LEGS_ONLY) {
						RailRouteByStage fastestRoute = Collections.min(railRoutes,
								Comparator.comparing(s -> s.getTransitTime()));
						fastestRoutes.add(fastestRoute);

					} else {
						RailRouteByStage fastestRoute = Collections.min(railRoutes,
								Comparator.comparing(s -> s.legs[0].duration.inSeconds));
						fastestRoutes.add(fastestRoute);
					}
					} catch (java.lang.NullPointerException e) {
						System.out.println("_________________"+ cityFrom +" to " + cityTo + " not found");
					}

				}
			}
		}
		return fastestRoutes;
	}
/**
 * Create Route List
 * @param routes
 * @return
 */
	public static List<RailRouteByStage> directionsRouteToRailRoute(List<DirectionsRoute> routes) {
		ArrayList<RailRouteByStage> railRoutes = new ArrayList<RailRouteByStage>();
		for (ListIterator<DirectionsRoute> iter = routes.listIterator(); iter.hasNext();) {
			DirectionsRoute route = iter.next();
			RailRouteByStage railRoute = new RailRouteByStage(route);
			railRoutes.add(railRoute);
		}

		return railRoutes;

	}
/**
 * Create Points list
 * @param routes
 * @throws IOException
 */
	public static void createPointShapefile(List<RailRouteByStage> routes) throws IOException {
		List<RailRouteByStage> uniqueStartCities = ListIterate.distinct(routes,
				HashingStrategies.fromFunction(p -> p.getLeg().startAddress));
		List<SimpleFeature> pointFeatures = PointsToGeoTools.featureCollectionCreator(uniqueStartCities);
		PointsToGeoTools.writeShapefile(Constants.POINTS_SHAPEFILE_NAME, Constants.SHAPEFILE_PATH, pointFeatures);
	}

	public static void createRoutesShapefile(List<RailRouteByStage> routes) throws IOException {
		List<SimpleFeature> routeFeatures = GoogleRoutesToGeoTools.featureCollectionCreator(routes);
		GoogleRoutesToGeoTools.writeShapefile(Constants.SHAPEFILE_NAME, Constants.SHAPEFILE_PATH, routeFeatures);
	}

}
