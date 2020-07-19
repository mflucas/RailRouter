package transformToTable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.ListIterator;
import com.google.maps.model.DirectionsRoute;
import router.Constants;
import router.Router;

public class Transformer {
	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
				
		List<String> cityList = Router.produceCityList(Constants.METRO_REGIONS);
				
		for (ListIterator<String> from = cityList.listIterator(); from.hasNext();) {
			String cityFrom = from.next();
			for (ListIterator<String> to = cityList.listIterator(); to.hasNext();) {
				String cityTo = to.next();
				if (cityTo != cityFrom) {
					String fileName = cityFrom + "_" + cityTo;
					@SuppressWarnings("unused")
					List<DirectionsRoute> routes = serializeDataIn(fileName);
					
					
				}
			}
		}

		
				
		
		
}
	
	public static List<DirectionsRoute> serializeDataIn(String fileName) throws IOException, ClassNotFoundException{
		String file = Constants.FILE_OUT_PATH + fileName + ".txt";
		   FileInputStream fin = new FileInputStream(file);
		   ObjectInputStream ois = new ObjectInputStream(fin);
		   @SuppressWarnings("unchecked")
		List<DirectionsRoute> routes= (List<DirectionsRoute>) ois.readObject();
		   ois.close();
		   return routes;
		}
	
	
	
	
	

}
