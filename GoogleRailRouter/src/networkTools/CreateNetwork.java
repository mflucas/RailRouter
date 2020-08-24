package networkTools;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import router.Constants;

public class CreateNetwork {

//	public static List<SimpleFeature> networkFromAllRoutesAndShapes(List<SimpleFeature> routeFeatures,
//			Geometry[] metroAreas) {
//
//		List<SimpleFeature> network = new ArrayList<>();
//		int k=1;
//
//		for (ListIterator<SimpleFeature> iter = routeFeatures.listIterator(); iter.hasNext();) {
//			SimpleFeature railLeg = iter.next();
//			Geometry routeGeometry = (Geometry) railLeg.getDefaultGeometry();
//
//			int intersections = 0;
//			for (int i = 0; i < metroAreas.length; i++) {
//				if (routeGeometry.intersects(metroAreas[i])) {
//					++intersections;
//				}
//			}
//			if (intersections == 2) {
//				network.add(railLeg);
//			}
//k++;
//		}
//
//		return network;
//
//	}

	public static List<SimpleFeature> networkFromAllRoutesAndPoints(List<SimpleFeature> routeFeatures,
			List<SimpleFeature> metroAreasAsNodes) throws NoSuchAuthorityCodeException, FactoryException, TransformException {

		List<SimpleFeature> network = new ArrayList<>();
		Geometry[] bufferedNodes = bufferedNodeList(metroAreasAsNodes);
int k=1;
		for (ListIterator<SimpleFeature> iter = routeFeatures.listIterator(); iter.hasNext();) {
			SimpleFeature railLeg = iter.next();

			Geometry routeGeometry = (Geometry) railLeg.getDefaultGeometry();

			int intersections = 0;
			for (int i = 0; i < bufferedNodes.length; i++) {
				if (routeGeometry.intersects(bufferedNodes[i])) {
					++intersections;
				}
			}
			System.out.println("Feature has " + intersections + " intersections");

			if (intersections == 2) {
				network.add(railLeg);

			}
k++;
		}

		return network;

	}

	public static Geometry[] bufferedNodeList(List<SimpleFeature> metroAreasAsNodes) throws NoSuchAuthorityCodeException, FactoryException, TransformException {

		List<Geometry> bufferedNodes = new ArrayList<>();

			
		for (ListIterator<SimpleFeature> iter = metroAreasAsNodes.listIterator(); iter.hasNext();) {
			SimpleFeature metroPoint = iter.next();

			Geometry bufferedPoint = Tools.bufferPoint(Constants.DISTANCE_BUFFER_IN_METERS, metroPoint);
			
			
			bufferedNodes.add(bufferedPoint);

		}

		Geometry[] formattedArray = bufferedNodes.toArray(new Geometry[bufferedNodes.size()]);
	
		return formattedArray;

	}

}
