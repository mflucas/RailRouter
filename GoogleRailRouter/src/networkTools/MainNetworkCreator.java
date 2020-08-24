package networkTools;

import java.io.IOException;
import java.util.List;
import org.eclipse.collections.impl.block.factory.HashingStrategies;
import org.eclipse.collections.impl.utility.ListIterate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;
import geospatialTools.GoogleRoutesToGeoTools;
import geospatialTools.MainMakeRouteShapes;
import geospatialTools.MultiPolygonShapes;
import geospatialTools.PointsToGeoTools;
import geospatialTools.RailRouteByStage;
import router.Constants;

public class MainNetworkCreator {
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchAuthorityCodeException, FactoryException, TransformException {
		
		//Create point features
		List<SimpleFeature> pointFeatures = PointsToGeoTools.featureCollectionCreator(Constants.METRO_WITH_COORD);
		
		Geometry[] bufferedNodes = CreateNetwork.bufferedNodeList(pointFeatures);
//		MultiPolygonShapes.writeShapefile(Constants.BUFFEREDPOINTS_SHAPEFILE_NAME, Constants.FILE_OUT_PATH_SHAPEFILES, bufferedNodes);

		
		System.out.println("___________________________________________________Wrote nodes shapefile ");
				
		//Create route features
		List<RailRouteByStage> fastestRoutes = MainMakeRouteShapes.routesConstructor(Constants.PROCESSED_ROUTES);
		System.out.println("___________________________________________________________________________");
		System.out.println("Route list production completed");
		System.out.println("___________________________________________________________________________");

		List<SimpleFeature> routesAsFeatures = GoogleRoutesToGeoTools.featureCollectionCreator(fastestRoutes);
		
	
		List<SimpleFeature> network = CreateNetwork.networkFromAllRoutesAndPoints(routesAsFeatures, pointFeatures);
		
		GoogleRoutesToGeoTools.writeShapefile(Constants.NETWORK_SHAPEFILE_NAME, Constants.FILE_OUT_PATH_SHAPEFILES, network);
		System.out.println("___________________________________________________WroteWrote network shapefile ");

	}
}