package net.piclock.thread;

import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import home.common.data.Temperature;
import home.inet.Connect;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;

//get the outside temperature sensors informations
public class TempSensorWorker implements Runnable{
	private static final Logger logger = Logger.getLogger( TempSensorWorker.class.getName() );

	private SwingContext ct = SwingContext.getInstance();

	@Override
	public void run() {
		PiHandler handler = PiHandler.getInstance();
		logger.log(Level.INFO, "Fetching outside sensor. Wifi connected: " + handler.isWifiConnected());

		try {
			if (handler.isWifiConnected()){

				
				Connect c = new Connect("http://192.168.1.110:8081/web/temperature/currTemp");
				String result = c.connectToUrlUsingGET().getResultAsStr();
				
//				System.out.println("result: " + resultAA);
				Gson gson = new Gson();
				Temperature tempResult = gson.fromJson(result, Temperature.class );
				
//				System.out.println("temp aa: " + tempAA);
				
				if (tempResult == null) {
					tempResult = new Temperature();
				}				

				ct.putSharedObject(Constants.SENSOR_INFO, tempResult);

				logger.log(Level.CONFIG, "SensorTemp pooling finished!!!!!!!!!!");

			}
		}catch(SocketException se) {
			logger.log(Level.INFO, "Socket exception Temp Sensor", se);
		}catch(Throwable ex) {
			try {
				ErrorHandler eh = (ErrorHandler)ct.getSharedObject(Constants.ERROR_HANDLER);
				eh.addError(ErrorType.WEATHER, new ErrorInfo(new FormatStackTrace(ex).getFormattedException()));
				logger.log(Level.SEVERE, "error in getting sensor weather", ex);
			}catch(Throwable tr) {
				logger.log(Level.SEVERE, "error " ,ex);
			}
		}
	}

}