package geospatialTools;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeatureType;

public class RouteTypeDef {

	public static SimpleFeatureType ROUTE() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Route");
		builder.setCRS(DefaultGeographicCRS.WGS84);

		// Now add the attributes
		builder.add("the_geom", MultiLineString.class); // check if this is working
		builder.add("from_to", String.class);
		builder.add("distance", Long.class);
		builder.add("duration", Long.class);
		builder.add("speed", double.class);
		builder.add("depTime", Long.class);
		builder.add("arrTime", Long.class);
		builder.add("steps", String.class);
		builder.add("products", String.class);

		final SimpleFeatureType ROUTE = builder.buildFeatureType();

		return ROUTE;

	}

}
