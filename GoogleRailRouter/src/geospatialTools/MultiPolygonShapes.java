package geospatialTools;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class MultiPolygonShapes{
	
	public static void writeShapefile(String shapeFileName, String shapefileFolderPath,
			Geometry[] polygons) throws IOException {

		
		List<SimpleFeature> polygonFeatures = featureCollectionCreator(polygons);
		/*
		 * Create a shapefile from feature collection
		 */

		File newShapefile = createNewShapefile(shapeFileName, shapefileFolderPath);

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

		Map<String, Serializable> params = new HashMap<>();
		params.put("url", newShapefile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

		newDataStore.createSchema(BufferedPointDef.BUFFPOINT());

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
            SimpleFeatureCollection collection = new ListFeatureCollection(BufferedPointDef.BUFFPOINT(), polygonFeatures);
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
	
	
	
	public static List<SimpleFeature> featureCollectionCreator(Geometry[] geom) {

		List<SimpleFeature> features = new ArrayList<>();

		int featureId = 0;
		for (int i = 0; i < geom.length; i++) {
			Geometry thisGeom = geom[i];
			SimpleFeature thisFeature = createPolygonFeature(Integer.toString(featureId), thisGeom);
			features.add(thisFeature);
			featureId++;
		}
		

		return features;
	}
	
	
	/**
	 * Build Polygon Feature
	 * @param featureId
	 * @param geometry
	 * @return
	 */
	public static SimpleFeature createPolygonFeature (String featureId, Geometry geometry) {
		List<Polygon> polygonList = new ArrayList<>();
		GeometryFactory geoFact = new GeometryFactory();
		
		Polygon polygon = (Polygon) geometry;
		polygonList.add(polygon);
		
		Polygon[] formattedArray = polygonList.toArray(new Polygon[polygonList.size()]);

		MultiPolygon mPolygon = geoFact.createMultiPolygon(formattedArray);
		

		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(BufferedPointDef.BUFFPOINT());

		featureBuilder.add(mPolygon);
		
		SimpleFeature feature = featureBuilder.buildFeature(featureId);
		return feature;

		
	}

}
