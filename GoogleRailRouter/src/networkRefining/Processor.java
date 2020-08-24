package networkRefining;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import networkTools.Tools;
import si.uom.SI;
import tec.uom.se.unit.Units;

public class Processor {

	/**
	 * Mains tool: Transform the network to simple features.
	 * 
	 * @param network
	 * @param junctions
	 * @return
	 * @throws TransformException
	 * @throws FactoryException
	 * @throws NoSuchAuthorityCodeException
	 */

	public static List<SimpleFeature> routesToSections(FeatureCollection<SimpleFeatureType, SimpleFeature> network,
			List<SimpleFeature> juncPoints, List<SimpleFeature> metroAreas)
			throws NoSuchAuthorityCodeException, FactoryException, TransformException {

		List<SimpleFeature> newNetworkFeatures = new ArrayList<>();

		// Iterate through all features of the network
		for (FeatureIterator<SimpleFeature> iter = network.features(); iter.hasNext();) {
			SimpleFeature route = iter.next();
			String from_to = route.getAttribute("from_to").toString();
			Double speed = Double.parseDouble(route.getAttribute("Speed").toString());

			Geometry routeGeometry = (Geometry) route.getDefaultGeometry();
			Coordinate[] routeCoords = routeGeometry.getCoordinates();

			System.out.println("________________________________________________-");

			// For each feature find its junctions

			Map<Integer, String> map = new HashMap<Integer, String>();

			double dist = Double.POSITIVE_INFINITY;
			// Find closest to first point
			SimpleFeature closestMetro = null;

			// junction
			for (ListIterator<SimpleFeature> metroIter = metroAreas.listIterator(); metroIter.hasNext();) {
				SimpleFeature metro = metroIter.next();
				Geometry metroGeom = (Geometry) metro.getDefaultGeometry();
				Coordinate metroCoord = metroGeom.getCoordinate();
				double actualdist = routeCoords[0].distance(metroCoord);

				if (actualdist < dist) {
					dist = actualdist;
					closestMetro = metro;
				}
			}

			map.put(0, closestMetro.getAttribute("name").toString());

			// Now find the closest one to the last point
			dist = Double.POSITIVE_INFINITY;

			// junction
			for (ListIterator<SimpleFeature> metroIter = metroAreas.listIterator(); metroIter.hasNext();) {
				SimpleFeature metro = metroIter.next();
				Geometry metroGeom = (Geometry) metro.getDefaultGeometry();
				Coordinate metroCoord = metroGeom.getCoordinate();
				double actualdist = routeCoords[routeCoords.length - 1].distance(metroCoord);

				if (actualdist < dist) {
					dist = actualdist;
					closestMetro = metro;
				}
			}

			map.put(routeCoords.length, closestMetro.getAttribute("name").toString());

			for (ListIterator<SimpleFeature> juncIter = juncPoints.listIterator(); juncIter.hasNext();) {

				SimpleFeature thisJunctionPoint = juncIter.next();
				Geometry bufferedIntersection = Tools.bufferPoint(600, thisJunctionPoint);

				if (routeGeometry.intersects(bufferedIntersection)) {
					String junctionName = thisJunctionPoint.getAttribute("Location").toString();

					Coordinate junctionCoordinate = bufferedIntersection.getCoordinate();

					double distance = Double.POSITIVE_INFINITY;

					Coordinate closestToJunction = null;
					int index = 0;
					// Loop through intersection (of the route) points to get the point of the route
					// closest to the
					// junction
					for (int k = 0; k < routeCoords.length; k++) {
						double actualdist = routeCoords[k].distance(junctionCoordinate);
						if (actualdist < distance) {
							distance = actualdist;
							index = k;

						}
					}

					map.put(index, junctionName);
				}
			}

			// List the keys by ascending order
			List<Integer> sortedKeys = new ArrayList(map.keySet());
			Collections.sort(sortedKeys);

			// Produce new features
			String pointBefore = "";
			Integer indBefore = 0;
			for (ListIterator<Integer> keyIter = sortedKeys.listIterator(); keyIter.hasNext();) {
				Integer key = keyIter.next();
				String currentPoint = map.get(key);

				if (key > 0) {
					Coordinate[] newArray = Arrays.copyOfRange(routeCoords, indBefore, key);
					addFeature(newNetworkFeatures, newArray, from_to, pointBefore, currentPoint, speed);
				}

				pointBefore = currentPoint;
				indBefore = key;

			}

		}
		return newNetworkFeatures;

	}

	/**
	 * Coords to multilineString
	 * 
	 * @param coords
	 * @return
	 */
	public static MultiLineString coordinatesToMultiLineString(Coordinate[] coords) {
		GeometryFactory geometryFactory = new GeometryFactory();

		List<LineString> lineArray = new ArrayList<>();
		LineString section = geometryFactory.createLineString(coords);
		lineArray.add(section);
		LineString[] formattedArray = lineArray.toArray(new LineString[lineArray.size()]);
		MultiLineString mlineString = geometryFactory.createMultiLineString(formattedArray);

		return mlineString;
	}

	/**
	 * Feature Collection to Simple Features
	 * 
	 * @param junctions
	 * @return
	 */
	public static List<SimpleFeature> junctionsToSimple(FeatureCollection<SimpleFeatureType, SimpleFeature> junctions) {
		List<SimpleFeature> simpleJunctions = new ArrayList<>();
		int oo = 1;
		for (FeatureIterator<SimpleFeature> iter = junctions.features(); iter.hasNext();) {
			SimpleFeature junction = iter.next();
			junction.setAttribute("id", oo);
			oo++;
			simpleJunctions.add(junction);
		}
		return simpleJunctions;
	}

	public static List<SimpleFeature> metroToSimple(FeatureCollection<SimpleFeatureType, SimpleFeature> junctions) {
		List<SimpleFeature> simpleJunctions = new ArrayList<>();
		for (FeatureIterator<SimpleFeature> iter = junctions.features(); iter.hasNext();) {
			SimpleFeature junction = iter.next();
			simpleJunctions.add(junction);
		}
		return simpleJunctions;
	}

	/**
	 * Buffer Point Simple Features
	 * 
	 * @param metroAreasAsNodes
	 * @return
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws TransformException
	 */
	public static Geometry[] bufferedNodeList(List<SimpleFeature> metroAreasAsNodes)
			throws NoSuchAuthorityCodeException, FactoryException, TransformException {

		List<Geometry> bufferedNodes = new ArrayList<>();

		for (ListIterator<SimpleFeature> iter = metroAreasAsNodes.listIterator(); iter.hasNext();) {
			SimpleFeature metroPoint = iter.next();
			Geometry bufferedPoint = Tools.bufferPoint(500, metroPoint);
			bufferedNodes.add(bufferedPoint);
		}

		Geometry[] formattedArray = bufferedNodes.toArray(new Geometry[bufferedNodes.size()]);

		return formattedArray;

	}

	/**
	 * Calculate
	 * 
	 * @param geom
	 * @return
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws org.opengis.referencing.operation.TransformException
	 */
	public static double lengthGetter(SimpleFeature geom) throws NoSuchAuthorityCodeException, FactoryException,
			org.opengis.referencing.operation.TransformException {
		MathTransform toTransform, fromTransform = null;
		Geometry geometry = (Geometry) geom.getDefaultGeometry();
		CoordinateReferenceSystem sourceCRS = geom.getDefaultGeometryProperty().getDescriptor()
				.getCoordinateReferenceSystem();
		Geometry pGeom = geometry;
		Unit<Length> sourceUnit = Units.METRE;

		if (!(sourceCRS instanceof ProjectedCRS)) {

			double x = geometry.getCoordinate().x;
			double y = geometry.getCoordinate().y;

			String code = "AUTO:42001," + x + "," + y;
			// System.out.println(code);
			CoordinateReferenceSystem auto;

			try {
				auto = CRS.decode(code);
				toTransform = CRS.findMathTransform(sourceCRS, auto);
				fromTransform = CRS.findMathTransform(auto, sourceCRS);
				pGeom = JTS.transform(geometry, toTransform);
				sourceUnit = SI.METRE;
			} catch (MismatchedDimensionException | FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		double length = pGeom.getLength();
		return length;
	}

	/**
	 * Creates a new section feature
	 * 
	 * @param mlineString
	 * @param from_to
	 * @param fromPoint
	 * @param toPoint
	 * @return
	 */
	public static SimpleFeature sectionFeature(MultiLineString mlineString, String from_to, String fromPoint,
			String toPoint, double speed) {

		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(SectionTypeDef.SECTION());
		featureBuilder.add(mlineString);
		featureBuilder.add(from_to);
		featureBuilder.add(9999);
		featureBuilder.add(9999);
		featureBuilder.add(speed);
		featureBuilder.add(fromPoint);
		featureBuilder.add(toPoint);

		SimpleFeature feature = featureBuilder.buildFeature(null);
		return feature;
	}

	/**
	 * Write a shapfile of network sections
	 * 
	 * @param shapeFileName
	 * @param shapefileFolderPath
	 * @param routeFeatures
	 * @throws IOException
	 */
	public static void writeShapefile(String shapeFileName, String shapefileFolderPath,
			List<SimpleFeature> routeFeatures) throws IOException {
		System.out.println("Writing Shapefile");

		File newShapefile = new File(shapefileFolderPath + shapeFileName + ".shp");
		System.out.println(0);

		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		System.out.println(1);

		Map<String, Serializable> params = new HashMap<>();
		params.put("url", newShapefile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);
		System.out.println(2);

		ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

		newDataStore.createSchema(SectionTypeDef.SECTION());
		System.out.println(3);
		/*
		 * Write the features to the shapfile
		 */
		Transaction transaction = new DefaultTransaction("create");
		System.out.println(4);

		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);
		SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
		System.out.println("SHAPE:" + SHAPE_TYPE);

		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
			/*
			 * SimpleFeatureStore has a method to add features from a
			 * SimpleFeatureCollection object, so we use the ListFeatureCollection class to
			 * wrap our list of features.
			 */
			SimpleFeatureCollection collection = new ListFeatureCollection(SectionTypeDef.SECTION(), routeFeatures);
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
	 * add network section
	 * 
	 * @param newNetworkFeatures
	 * @param coordinateList
	 * @param from_to
	 * @param from
	 * @param to
	 * @param speed
	 * @throws NoSuchAuthorityCodeException
	 * @throws FactoryException
	 * @throws TransformException
	 */
	private static void addFeature(List<SimpleFeature> newNetworkFeatures, Coordinate[] coordinateList, String from_to,
			String from, String to, Double speed)
			throws NoSuchAuthorityCodeException, FactoryException, TransformException {
		MultiLineString section = coordinatesToMultiLineString(coordinateList);
		System.out.println("Feature: " + from + " to " + to);
		SimpleFeature sectionFeature = sectionFeature(section, from_to, from, to, speed);
		double thisLength = lengthGetter(sectionFeature);
		sectionFeature.setAttribute("Length", thisLength);
		sectionFeature.setAttribute("Duration", thisLength / ((double) sectionFeature.getAttribute("Speed") / 3.6));
		newNetworkFeatures.add(sectionFeature);
	}

}