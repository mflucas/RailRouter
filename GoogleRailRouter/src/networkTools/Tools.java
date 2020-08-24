package networkTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Length;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.operation.MathTransform;
import si.uom.SI;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.Units;


public class Tools {


	/**
	 * Create a FeatureCollection out of Shapfile
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 */

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> readShapefile(String filePath)
			throws IOException {
		File file = new File(filePath);
		Map<String, Object> map = new HashMap<>();
		map.put("url", file.toURI().toURL());

		DataStore dataStore = DataStoreFinder.getDataStore(map);
		String typeName = dataStore.getTypeNames()[0];

		FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
		Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

		FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

		return collection;
	}

	/**
	 * Create a Polygon List out of FeatureCollection
	 * 
	 * @param metroRegions
	 * @return
	 */
	public static Geometry[] createPolygonGeometryList(
			FeatureCollection<SimpleFeatureType, SimpleFeature> metroRegions) {
		List<Geometry> featuresList = new ArrayList<>();

		try (FeatureIterator<SimpleFeature> iterator = metroRegions.features()) {
			while (iterator.hasNext()) {

				Geometry next = (Geometry) iterator.next().getDefaultGeometry();
				featuresList.add(next);
			}
		}

		Geometry[] formattedArray = featuresList.toArray(new Geometry[featuresList.size()]);

		return formattedArray;
	}
	
	
	 @SuppressWarnings("unchecked")
	public static Geometry bufferPoint(double distInMeters, SimpleFeature geom) throws NoSuchAuthorityCodeException, FactoryException, org.opengis.referencing.operation.TransformException {
		MathTransform toTransform, fromTransform = null;
		Geometry geometry = (Geometry) geom.getDefaultGeometry();
		CoordinateReferenceSystem sourceCRS = geom.getDefaultGeometryProperty().getDescriptor().getCoordinateReferenceSystem();
	    Geometry pGeom = geometry;
		    Unit<Length> sourceUnit = Units.METRE;
		    Quantity<Length> distance =Quantities.getQuantity((Number) distInMeters, sourceUnit);

		    
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

		      } else {
		    	  sourceUnit = (Unit<Length>) sourceCRS.getCoordinateSystem().getAxis(0).getUnit();

		      }
			 UnitConverter converter = distance.getUnit().getConverterTo(sourceUnit);
			    // buffer
			    Geometry out = pGeom.buffer(converter.convert(distance.getValue()).doubleValue());
			    Geometry retGeom = out;
			// reproject the geometry to a local projection
			    if (!(sourceCRS instanceof ProjectedCRS)) {
			        try {
			          retGeom = JTS.transform(out, fromTransform);

			        } catch (MismatchedDimensionException e) {
			          // TODO Auto-generated catch block
			          e.printStackTrace();
			        }
			    }

			
		    return retGeom;
		  }
	 
	 
	 
		public static Geometry FeatureToPoint(SimpleFeature geom) {
			
			Geometry geometry = (Geometry) geom.getDefaultGeometry();

			return geometry;
			
		}

}
