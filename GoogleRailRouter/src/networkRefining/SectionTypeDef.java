package networkRefining;


import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeatureType;

public class SectionTypeDef {

	public static SimpleFeatureType SECTION() {

		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		builder.setName("Section");
		builder.setCRS(DefaultGeographicCRS.WGS84); 

		// Now add the attributes
		builder.add("the_geom", MultiLineString.class); // check if this is working
		builder.add("Route", String.class);
		builder.add("Length", double.class);
		builder.add("Duration", double.class);
		builder.add("Speed", double.class);
		builder.add("FromNode", String.class);
		builder.add("ToNode", String.class);

		final SimpleFeatureType SECTION = builder.buildFeatureType();

		return SECTION;

	}

}

