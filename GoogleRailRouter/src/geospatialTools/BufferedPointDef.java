package geospatialTools;

import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeatureType;

public class BufferedPointDef {
	public static SimpleFeatureType BUFFPOINT() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("MetroArea");
		builder.setCRS(DefaultGeographicCRS.WGS84);

		// Now add the attributes
		builder.add("the_geom", MultiPolygon.class); // check if this is working


		final SimpleFeatureType BUFFPOINT = builder.buildFeatureType();

		return BUFFPOINT;

	}

}
