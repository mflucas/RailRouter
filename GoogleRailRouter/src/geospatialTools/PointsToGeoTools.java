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
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class PointsToGeoTools {
	
	/**
	 * Puts the feature collection into a shapefile and writes it to file
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public static void writeShapefile(String shapeFileName, String shapefileFolderPath,
			List<SimpleFeature> pointFeatures) throws IOException {

		/*
		 * Create a shapefile from feature collection
		 */

		File newShapefile = createNewShapefile(shapeFileName, shapefileFolderPath);

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<>();
		params.put("url", newShapefile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

		newDataStore.createSchema(PointTypeDef.METROAREA());

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
            SimpleFeatureCollection collection = new ListFeatureCollection(PointTypeDef.METROAREA(), pointFeatures);
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
 * 
 * @param routesList
 * @return
 */

	public static List<SimpleFeature> featureCollectionCreator(List<RailRouteByStage> routesList) {

		List<SimpleFeature> features = new ArrayList<>();

		int featureId = 0;
		for (ListIterator<RailRouteByStage> iter = routesList.listIterator(); iter.hasNext();) {
			RailRouteByStage railLeg = iter.next();

			SimpleFeature thisFeature = createPointFromRouteStart(Integer.toString(featureId), railLeg);
			features.add(thisFeature);
			featureId++;
		}
		

		return features;
	}

/**
 * 
 * @param featureId
 * @param railRoute
 * @return
 */
	public static SimpleFeature createPointFromRouteStart(String featureId, RailRouteByStage railRoute) {
		List<Point> points = new ArrayList<>();
		GeometryFactory geoFact = new GeometryFactory();
		Point firstCoord = geoFact.createPoint(railRoute.getMlineString().getCoordinates()[0]);
		points.add(firstCoord);
		Point[] formattedArray = points.toArray(new Point[points.size()]);
		MultiPoint mpoint = geoFact.createMultiPoint(formattedArray);
		
		
		String name = railRoute.getStartLocation();
		
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(PointTypeDef.METROAREA());

		featureBuilder.add(mpoint);
		featureBuilder.add(name);
		
		SimpleFeature feature = featureBuilder.buildFeature(featureId);
		return feature;
	}




}
