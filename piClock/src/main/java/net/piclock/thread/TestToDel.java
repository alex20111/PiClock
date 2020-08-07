package net.piclock.thread;

import java.io.IOException;
import java.net.MalformedURLException;

import com.google.gson.Gson;

import home.inet.Connect;
import net.piclock.weather.Temperature;

public class TestToDel {

	public static void main(String[] args) throws IOException {
		//recorder AA, Shade
		Connect c = new Connect("http://192.168.1.110:8080/api/temperature/currTemp");
		String result = c.connectToUrlUsingGET().getResultAsStr();
		
//		System.out.println("result: " + resultAA);
		Gson gson = new Gson();
		Temperature tempResult = gson.fromJson(result, Temperature.class );
		
//		System.out.println("temp aa: " + tempAA);
		
		if (tempResult == null) {
			tempResult = new Temperature();
		}
		
		System.out.println("temp: " + tempResult);

	}

}
