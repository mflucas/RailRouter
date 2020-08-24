package router;

import java.time.Instant;


public abstract class Constants {

//	Read in pre-processed routes 
//	public static final String PROCESSED_ROUTES = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\Table\\test.csv"; 
	public static final String PROCESSED_ROUTES = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\Table\\FINAL_fastestRoutes1000.csv"; 

	public static final String METRO_WITH_COORD = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\CityList\\MetroAreasCoord.csv"; 

	
	
	public static final double DISTANCE_BUFFER_IN_METERS = 3000;
	
	public static final String METRO_REGIONS_SHAPEFILE = "T:\\214329\\200_DG Move HS-Rail\\40_BEARBEITUNG\\3_OD Matrix\\Data_Metro\\METRO_REGIONS_SHP\\METRO_REGIONS.shp";

	public static boolean SELECT_FASTEST_BY_TRANSIT_LEGS_ONLY = true; 

	public static final String API_KEY = "AIzaSyA79W1r6Ftf3yr66JoyUhASHuph_VrwKoA"; //Auf Lucas Meyer de Freitas Kreditkarte.

	public static final String METRO_PAIRS = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\CityList\\busRoutes.csv"; 

	public static final String METRO_REGIONS = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\CityList\\MetroAreas.csv"; 
	public static final String METRO_REGIONS_Start = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\CityList\\MetroAreas_START_4.csv"; 

	public static final String FILE_OUT_PATH = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\GoogleRoutes_200713\\"; 
	public static final String FILE_OUT_PATH_SHAPEFILES = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\Shapefile\\200805\\"; 

	public static final String STEPS_TABLE = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\Table\\StepsTable.txt"; 
	public static final String ROUTES_TABLE = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\Table\\RoutesTable.txt"; 
	public static final String FASTEST_ROUTE_TABLE = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\Table\\FastestRouteTable.txt"; 
	
	public static final String SHAPEFILE_PATH = "C:\\Users\\lmf\\Documents\\DG_MOVE HSR Router outputs\\Shapfile\\";
	public static final String SHAPEFILE_NAME = "Routes";
	public static final String NETWORK_SHAPEFILE_NAME = "MostRoutes";

	public static final String POINTS_SHAPEFILE_NAME = "MetroPoints";
	public static final String BUFFEREDPOINTS_SHAPEFILE_NAME = "BuffPoints";

	static String timestamp = "2020-07-31T14:00:30.00Z";
	public static final Instant DEP_TIME = Instant.parse(timestamp);
	
	//Feature type to be written: 
		

}