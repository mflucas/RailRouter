package router;

import java.io.IOException;

import com.google.maps.errors.ApiException;

public class MainClass {
	
	public static void main(String[] args) throws ApiException, InterruptedException, IOException {
		Router run = new Router();
		run.router();
	}

}