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
		
		System.out.println("Reading " + Constants.METRO_REGIONS_SHAPEFILE);
				
		//Create route features
		List<RailRouteByStage> fastestRoutes = MainMakeRouteShapes.fastestRoutesCreator(Constants.METRO_REGIONS);
		List<SimpleFeature> routesAsFeatures = GoogleRoutesToGeoTools.featureCollectionCreator(fastestRoutes);
		
		//Create point features
		List<RailRouteByStage> uniqueStartCities = ListIterate.distinct(fastestRoutes,
				HashingStrategies.fromFunction(p -> p.getLeg().startAddress));
		List<SimpleFeature> pointFeatures = PointsToGeoTools.featureCollectionCreator(uniqueStartCities);
		
		Geometry[] bufferedNodes = CreateNetwork.bufferedNodeList(pointFeatures);
		MultiPolygonShapes.writeShapefile(Constants.BUFFEREDPOINTS_SHAPEFILE_NAME, Constants.FILE_OUT_PATH, bufferedNodes);

		List<SimpleFeature> network = CreateNetwork.networkFromAllRoutesAndPoints(routesAsFeatures, pointFeatures);
		
//		GoogleRoutesToGeoTools.writeShapefile(Constants.NETWORK_SHAPEFILE_NAME, Constants.FILE_OUT_PATH, network);
		
	}
}
