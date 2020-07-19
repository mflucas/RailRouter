package geospatialTools;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.MultiPoint;
import org.opengis.feature.simple.SimpleFeatureType;

public class PointTypeDef {
	
	public static SimpleFeatureType METROAREA() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("MetroArea");
		builder.setCRS(DefaultGeographicCRS.WGS84);

		// Now add the attributes
		builder.add("the_geom", MultiPoint.class); // check if this is working
		builder.add("name", String.class);


		final SimpleFeatureType METROAREA = builder.buildFeatureType();

		return METROAREA;

	}

}
