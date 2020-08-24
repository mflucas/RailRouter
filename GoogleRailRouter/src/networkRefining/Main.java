package networkRefining;

import java.io.IOException;
import java.util.List;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

public class Main {
	
	public static void main(String[] args) throws NoSuchAuthorityCodeException, FactoryException, TransformException, IOException {
		String folder = "T:\\214329\\200_DG Move HS-Rail\\40_BEARBEITUNG\\3_OD Matrix\\Graph\\Shapefiles\\";
		
		String networkFile = folder + "200813.shp";
		String junctionsFile = folder + "Junctions.shp";
		String metroFile = folder + "MetroAreas.shp";

		//Read in network and intersection points
			FeatureCollection<SimpleFeatureType, SimpleFeature> network = networkTools.Tools.readShapefile(networkFile);
			FeatureCollection<SimpleFeatureType, SimpleFeature> junctions = networkTools.Tools.readShapefile(junctionsFile);
			FeatureCollection<SimpleFeatureType, SimpleFeature> metroPoints = networkTools.Tools.readShapefile(metroFile);

		//Junctions to Buffer Points
		List<SimpleFeature> simpleJunctions = Processor.junctionsToSimple(junctions);
		List<SimpleFeature> simpleMetro = Processor.metroToSimple(metroPoints);

		//New network sections have to have information of all routes served
		List<SimpleFeature> networkAsSections = Processor.routesToSections(network, simpleJunctions, simpleMetro);
		
		Processor.writeShapefile("NetworkAsSections", folder, networkAsSections);

	}
}