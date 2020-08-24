package networkTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import geospatialTools.GoogleRoutesToGeoTools;
import geospatialTools.PointsToGeoTools;
import router.Constants;


public class NetworkPostProcessing {
	
	public static void main(String[] args) throws IOException, NoSuchAuthorityCodeException, FactoryException, TransformException {
		
		String networkPath= "T:\\214329\\200_DG Move HS-Rail\\40_BEARBEITUNG\\3_OD Matrix\\Graph\\Shapefiles\\Networktest.shp";

		//Create point features
		List<SimpleFeature> pointFeatures = PointsToGeoTools.featureCollectionCreator(Constants.METRO_WITH_COORD);
			
		//Read in processed network shapefile
		FeatureCollection<SimpleFeatureType, SimpleFeature> network = shapefileToFeatureCollection(networkPath);
	
			
		List<SimpleFeature> networkFeature =simpleNetwork(pointFeatures, network);
		
		
		String filName = "PostProcessedNetwork";
		GoogleRoutesToGeoTools.writeShapefile(filName, Constants.FILE_OUT_PATH_SHAPEFILES, networkFeature);

//		Geometry route = geom; 
		
		
		
	}
	

	/**
	 * Read in the pre-processed network shapefile
	 * @param networkPath
	 * @return
	 * @throws IOException
	 */
	public static FeatureCollection<SimpleFeatureType, SimpleFeature> shapefileToFeatureCollection(String networkPath) throws IOException{
		       File file = new File(networkPath);
		        Map<String, Object> map = new HashMap<>();
		        map.put("url", file.toURI().toURL());

		        DataStore dataStore = DataStoreFinder.getDataStore(map);
		        String typeName = dataStore.getTypeNames()[0];

		        FeatureSource<SimpleFeatureType, SimpleFeature> source =
		                dataStore.getFeatureSource(typeName);
		        
		        
		        Filter filter = Filter.INCLUDE;
		        
		        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

		        try (FeatureIterator<SimpleFeature> features = collection.features()) {
		            while (features.hasNext()) {
		                SimpleFeature feature = features.next();
		                System.out.print(feature.getID());
		                System.out.print(": ");
		                System.out.println(feature.getDefaultGeometryProperty().getValue());
		            }
		        }
		        
		        
		return collection;
		
	}
	/**
	 * Get the name for each start and end point for each line
	 * @param metroAreas
	 * @param network
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws TransformException
	 */
	public static List<SimpleFeature> simpleNetwork(List<SimpleFeature> metroAreas,  FeatureCollection<SimpleFeatureType, SimpleFeature> network) throws NoSuchAuthorityCodeException, FactoryException, TransformException {
		List<SimpleFeature> networkFeature = new ArrayList<>();
		
		for (FeatureIterator<SimpleFeature> iter = network.features(); iter.hasNext();) {
			SimpleFeature route = iter.next();
			Geometry routeGeometry = (Geometry) route.getDefaultGeometry();
			
			Coordinate[] line = routeGeometry.getCoordinates();
						
			Coordinate first = line[0];
			Coordinate last = line[line.length-1];

			String startMetro = nearest(first, metroAreas);
			String endMetro = nearest(last, metroAreas);

			System.out.println(startMetro);
			System.out.println(endMetro);
			System.out.println("_______________________________________________________");
			
			route.setAttribute("from_to", startMetro+"_"+endMetro);
			networkFeature.add(route);
		}
		
		return networkFeature;
		
	}
	
public static String nearest(Coordinate nearestFrom, List<SimpleFeature> metroPoints) throws NoSuchAuthorityCodeException, FactoryException, TransformException {
	
	SimpleFeature point = PointsToGeoTools.createPointFromCoordinate(nearestFrom, "this");
	
	Geometry finalPoint = Tools.FeatureToPoint(point);
	
	
	double distance = Double.POSITIVE_INFINITY; 
	String name =null;
	
	for (int i=0; i<metroPoints.size(); i++) {
		Geometry thisMetro = Tools.FeatureToPoint(metroPoints.get(i));
		double actualdist = finalPoint.distance(thisMetro);
		
		if(actualdist<distance) {
			distance = actualdist;
		name = metroPoints.get(i).getAttribute("name").toString();
		}
	
	}
	
	
	return name;

}


/**
 * Node list to Geometry points
 * @param metroAreasAsNodes
 * @return
 * @throws NoSuchAuthorityCodeException
 * @throws FactoryException
 * @throws TransformException
 */
public static Geometry[] featureListToGeometryPointList(List<SimpleFeature> points) throws NoSuchAuthorityCodeException, FactoryException, TransformException {

	List<Geometry> nodes = new ArrayList<>();

		
	for (ListIterator<SimpleFeature> iter = points.listIterator(); iter.hasNext();) {
		SimpleFeature metroPoint = iter.next();
		Geometry point = Tools.FeatureToPoint(metroPoint);
		nodes.add(point);
	}

	Geometry[] formattedArray = nodes.toArray(new Geometry[nodes.size()]);

	return formattedArray;

}

}
