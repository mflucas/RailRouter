package geospatialTools;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.LatLng;


public class GoogleRoutesToGeoTools {

	/**
	 * Puts the feature collection into a shapefile and writes it to file
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public static void writeShapefile(String shapeFileName, String shapefileFolderPath,
			List<SimpleFeature> routeFeatures) throws IOException {

		/*
		 * Create a shapefile from feature collection
		 */

		File newShapefile = createNewShapefile(shapeFileName, shapefileFolderPath);

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<>();
		params.put("url", newShapefile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

		newDataStore.createSchema(RouteTypeDef.ROUTE());

		/*
		 * Write the features to the shapfile
		 */
	    Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
        SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
        System.out.println("SHAPE:" + SHAPE_TYPE);
        
        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            /*
             * SimpleFeatureStore has a method to add features from a
             * SimpleFeatureCollection object, so we use the ListFeatureCollection
             * class to wrap our list of features.
             */
            SimpleFeatureCollection collection = new ListFeatureCollection(RouteTypeDef.ROUTE(), routeFeatures);
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
            System.exit(0); // success!
        } else {
            System.out.println(typeName + " does not support read/write access");
            System.exit(1);
        }
	}

	/**
	 * Creates a new shapefile based on a shapefileName and saves it to a predefined
	 * path
	 * 
	 * @param shapeFileName
	 * @return
	 * @throws IOException
	 */

	private static File createNewShapefile(String shapeFileName, String shapefileFolderPath) throws IOException {
		String filePath = shapefileFolderPath + shapeFileName + ".shp";

		JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
		chooser.setDialogTitle("Save shapefile");
		chooser.setSelectedFile(new File(filePath));

		int returnVal = chooser.showSaveDialog(null);

		if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
			// the user cancelled the dialog
			System.exit(0);
		}

		File newFile = chooser.getSelectedFile();

		return newFile;
	}

	/**
	 * Create a feature collection out of a list of List<DirectionsRoute>
	 * 
	 * @param routesList
	 * @param _to
	 * @param distance
	 * @param duration
	 * @param depTime
	 * @param arrTime
	 * @param steps
	 * @param featureId
	 * @return
	 */

	public static List<SimpleFeature> featureCollectionCreator(List<RailRouteByStage> routesList) {

		List<SimpleFeature> features = new ArrayList<>();

		int featureId = 0;
		for (ListIterator<RailRouteByStage> iter = routesList.listIterator(); iter.hasNext();) {
			RailRouteByStage railLeg = iter.next();
			DirectionsLeg thisLeg = railLeg.getLeg();
			
			long distance = railLeg.getTransitDistance();
			long duration = railLeg.getTransitTime();
			long depTime = thisLeg.departureTime.toEpochSecond();
			long arrTime = thisLeg.arrivalTime.toEpochSecond();
			String steps = railLeg.getTransitModes().toString();
			String from_to = thisLeg.startAddress + "_" + thisLeg.endAddress;
			String products = railLeg.getProducts().toString();
			double speed = railLeg.getSpeed();

			SimpleFeature thisFeature = createFeature(railLeg, from_to, distance, duration, depTime, arrTime, steps, Integer.toString(featureId), products, speed
					);
			features.add(thisFeature);
			featureId++;
		}
		

		return features;
	}

	/**
	 * Create a single feature from a DirectionRoute
	 * 
	 * @param route
	 * @param from_to
	 * @param distance
	 * @param duration
	 * @param depTime
	 * @param arrTime
	 * @param steps
	 * @param featureId
	 * @return
	 */
	public static SimpleFeature createFeature(RailRouteByStage route, String from_to, Long distance, Long duration,
			Long depTime, Long arrTime, String steps, String featureId, String products,Double speed) {

		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(RouteTypeDef.ROUTE());
		MultiLineString lineGeometry = route.getMlineString();

		featureBuilder.add(lineGeometry);
		featureBuilder.add(from_to);
		featureBuilder.add(distance);
		featureBuilder.add(duration);
		featureBuilder.add(speed);
		featureBuilder.add(depTime);
		featureBuilder.add(arrTime);
		featureBuilder.add(steps);
		featureBuilder.add(products);

		SimpleFeature feature = featureBuilder.buildFeature(featureId);
		return feature;
	}

	/**
	 * Transform a Google EncodedPolyline into a GeoTools LineString
	 * 
	 * @param polyline
	 * @return
	 */

	public static LineString encodedPolylineToLineString(EncodedPolyline polyline) {

		ArrayList<CoordinateXY> points = new ArrayList<CoordinateXY>();

		for (ListIterator<LatLng> iter = polyline.decodePath().listIterator(); iter.hasNext();) {
			LatLng point = iter.next();
			points.add(new CoordinateXY(point.lng, point.lat));

		}

		GeometryFactory geometryFactory = new GeometryFactory();
		

		
		LineString routeAsLineString = geometryFactory
				.createLineString((CoordinateXY[]) points.toArray(new CoordinateXY[] {}));
	


		return routeAsLineString;
	}

}
