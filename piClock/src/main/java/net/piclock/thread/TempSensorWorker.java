package net.piclock.thread;

import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import home.inet.Connect;
import net.piclock.bean.ErrorHandler;
import net.piclock.bean.ErrorInfo;
import net.piclock.bean.ErrorType;
import net.piclock.handlers.PiHandler;
import net.piclock.main.Constants;
import net.piclock.swing.component.SwingContext;
import net.piclock.util.FormatStackTrace;
import net.piclock.weather.Temperature;
import net.piclock.weather.WeatherBean;

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

				WeatherBean wb = new WeatherBean();

				//recorder AA, Shade
				Connect c = new Connect("http://192.168.1.110:8081/web/service.action?cmd=get&type=temp&jsonObject={\"recorderName\":\"AA\"}&max=true");
				String resultAA = c.connectToUrlUsingGET().getResultAsStr();

				Gson gson = new Gson();
				Temperature tempAA = gson.fromJson(resultAA, Temperature.class );

				if (tempAA != null && tempAA.getTempC() != null){
					wb.setTempShade(tempAA);
				}

				//recorder BB
				String urlBB = "http://192.168.1.110:8081/web/service.action?cmd=get&type=temp&jsonObject={\"recorderName\":\"BB\"}&max=true";

				c = new Connect(urlBB);
				String resultBB = c.connectToUrlUsingGET().getResultAsStr();

				Temperature tempBB = gson.fromJson(resultBB, Temperature.class );

				if (tempBB != null && tempBB.getTempC() != null){
					wb.setTempSun(tempBB);
				}

				ct.putSharedObject(Constants.SENSOR_INFO, wb);

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