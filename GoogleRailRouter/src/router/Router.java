package router;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApi.RouteRestriction;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TravelMode;

public class Router {
	
	//Method for table with start_end routes included in one table
	public void router() throws ApiException, InterruptedException, IOException {
		
		Map<String, String> cityPairs = produceFromToRelation(Constants.METRO_PAIRS);
		
        for (Map.Entry<String,String> entry : cityPairs.entrySet())  {
        	String cityFrom = entry.getKey();
        	String cityTo = entry.getValue();
        	
        	
        	List<DirectionsRoute> routes = routeListProducer(cityFrom, cityTo);
        	String fileName = cityFrom + "_" + cityTo;
        	
        	if(routes != null) {
				saveRoute(routes, fileName);
				System.out.println("Saved: " + fileName);

			} else {
				System.out.println("__________________Not saved: " + fileName);

			}
        }

	}
	
//Method for iterating over all tables
//	public void router() throws ApiException, InterruptedException, IOException {
//
//		List<String> cityList = produceCityList(Constants.METRO_REGIONS);
//		List<String> cityListStart = produceCityList(Constants.METRO_REGIONS_Start);
//
//		for (ListIterator<String> from = cityListStart.listIterator(); from.hasNext();) {
//			String cityFrom = from.next();
//
//			for (ListIterator<String> to = cityList.listIterator(); to.hasNext();) {
//				String cityTo = to.next();
//
//				if (cityTo != cityFrom) {
//
//					System.out.println(cityFrom +" to "+ cityTo);
//
//					List<DirectionsRoute> routes = routeListProducer(cityFrom, cityTo);
//				
//					
//					String fileName = cityFrom + "_" + cityTo;
//					if(routes != null) {
//						saveRoute(routes, fileName);
//						System.out.println("Saved: " + fileName);
//
//					} else {
//						System.out.println("__________________Not saved: " + fileName);
//
//					}
//
//				}
//			}
//		}
//
//	}
	

	public static List<DirectionsRoute> routeListProducer(String cityFrom, String cityTo)
			{

		List<DirectionsRoute> routeList = new ArrayList<>();

		GeoApiContext context = new GeoApiContext.Builder().apiKey(Constants.API_KEY).build();
		DirectionsRoute[] route;

		Instant instant = Constants.DEP_TIME;
		Integer timeadder = 0;

		// Search for several departure times
		for (int j = 0; j < 3; j++) {

			instant = instant.plusSeconds(timeadder);

			try {
			route = DirectionsApi.getDirections(context, cityFrom, cityTo).alternatives(true).mode(TravelMode.TRANSIT)
					.transitMode(TransitMode.RAIL).avoid(RouteRestriction.HIGHWAYS, RouteRestriction.FERRIES)
					.departureTime(instant).await().routes;
			} catch (com.google.maps.errors.MaxRouteLengthExceededException RouteTooLong) {
				route = null;
			} catch (ApiException e) {
				route = null;
				e.printStackTrace();
			} catch (InterruptedException e) {
				route = null;
				e.printStackTrace();
			} catch (IOException e) {
				route = null;
				e.printStackTrace();
			}
			
			
			if(route != null) {
			for (int i = 0; i < route.length; i++) {
					routeList.add(route[i]);
				}
			}

			timeadder = timeadder + 7600;// corresponds to 2h steps
		}

		boolean nullsOnly = routeList.stream().noneMatch(Objects::nonNull);

		if(nullsOnly == true) {
			routeList=null;
		} 
		return routeList;

	}

	public static void saveRoute(List<DirectionsRoute> routes, String fileName) throws IOException {

		String file = Constants.FILE_OUT_PATH + fileName + ".txt";
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(routes);
		oos.close();
	}

	public static List<String> produceCityList(String pathToFile) throws IOException {
		
		
		FileInputStream inputStream = new FileInputStream(pathToFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

		List<String> cityList = new ArrayList<>();

		reader.readLine();
		String s = reader.readLine();
		while (s != null) {

			cityList.add(s);
			s = reader.readLine();
		}
		reader.close();

		return cityList;

	}
	
	public static Map<String, String> produceFromToRelation(String pathToFile) throws IOException {
		
		Map<String,String> myMap = new HashMap<String,String>();

		String splitBy=";";
		FileInputStream inputStream = new FileInputStream(pathToFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

		List<String> cityList = new ArrayList<>();

		reader.readLine();
		String s = reader.readLine();
		while (s != null) {
			
			String[] b = s.split(splitBy);
			String from = b[2];
			String to = b[3];
			
			myMap.put(from, to);

			cityList.add(s);
			s = reader.readLine();
		}
		reader.close();

		return myMap;

	}

//	public static DateTime times(DateTime startTime) {
//		
//		
//		
//		
//		return times;
//		
//	}

}
