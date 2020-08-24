package geospatialTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiLineString;
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

		File newShapefile = new File(shapefileFolderPath + shapeFileName + ".shp");

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
 * @throws IOException 
 */

	public static List<SimpleFeature> featureCollectionCreator(String filePath) throws IOException {

		List<SimpleFeature> features = new ArrayList<>();
		FileInputStream inputStream = new FileInputStream(filePath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
		String splitBy=";";
		reader.readLine();
		String s = reader.readLine();
		GeometryFactory geoFact = new GeometryFactory();

		while (s != null) {
			//Read in data from table
			String[] b = s.split(splitBy);
			String name= b[0].replace("\"", "");
			
			System.out.println("Reading point " + name);

			Double lon= Double.parseDouble(b[1].replace("\"", ""));
			Double lat= Double.parseDouble(b[2].replace("\"", ""));
			Coordinate coordinate = new Coordinate();
			coordinate.setX(lon);
			coordinate.setY(lat);
			SimpleFeature thisFeature = createPointFromCoordinate(coordinate, name);

			features.add(thisFeature);
			s = reader.readLine();

		}
		reader.close();
	
		

		return features;
	}

/**
 * 
 * @param featureId
 * @param railRoute
 * @return
 */
	public static SimpleFeature createPointFromCoordinate(Coordinate coord, String name) {
		List<Point> points = new ArrayList<>();
		GeometryFactory geoFact = new GeometryFactory();
		Point firstCoord = geoFact.createPoint(coord);
		points.add(firstCoord);
		Point[] formattedArray = points.toArray(new Point[points.size()]);
		MultiPoint mpoint = geoFact.createMultiPoint(formattedArray);
		
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(PointTypeDef.METROAREA());

		featureBuilder.add(mpoint);
		featureBuilder.add(name);
		
		SimpleFeature feature = featureBuilder.buildFeature(null);
		return feature;
	}

}
