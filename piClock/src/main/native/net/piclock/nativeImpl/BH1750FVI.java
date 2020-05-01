package net.piclock.nativeImpl;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import net.piclock.handlers.PiHandler;

/*
 * Refer to https://www.mouser.com/datasheet/2/348/bh1750fvi-e-186247.pdf
 *
 * @author s5uishida
 *
 */
public class BH1750FVI {
	private static final Logger logger = Logger.getLogger( BH1750FVI.class.getName() );

	public static final byte I2C_ADDRESS_23 = 0x23;
	public static final byte I2C_ADDRESS_5C = 0x5c;

	private static final byte OPECODE_POWER_DOWN							= 0x00;
	private static final byte OPECODE_POWER_ON								= 0x01;
	private static final byte OPECODE_RESET									= 0x07;
	private static final byte OPECODE_CONTINUOUSLY_H_RESOLUTION_MODE		= 0x10;
	private static final byte OPECODE_CONTINUOUSLY_H_RESOLUTION_MODE2	= 0x11;
	private static final byte OPECODE_CONTINUOUSLY_L_RESOLUTION_MODE		= 0x13;
	private static final byte OPECODE_ONE_TIME_H_RESOLUTION_MODE			= 0x20;
	private static final byte OPECODE_ONE_TIME_H_RESOLUTION_MODE2		= 0x21;
	private static final byte OPECODE_ONE_TIME_L_RESOLUTION_MODE			= 0x23;

	private static final int H_RESOLUTION_MODE_MEASUREMENT_TIME_MILLIS	= 120;
	private static final int H_RESOLUTION_MODE2_MEASUREMENT_TIME_MILLIS	= 120;
	private static final int L_RESOLUTION_MODE_MEASUREMENT_TIME_MILLIS	= 16;

	private static final int SENSOR_DATA_LENGTH = 2;

	private final byte i2cAddress;
	private final I2CBus i2cBus;
	private final I2CDevice i2cDevice;
	private final String i2cName;
	private final String logPrefix;

	private final AtomicInteger useCount = new AtomicInteger(0);

	private static final ConcurrentHashMap<String, BH1750FVI> map = new ConcurrentHashMap<String, BH1750FVI>();

	synchronized public static BH1750FVI getInstance(int i2cBusNumber, byte i2cAddress) {
		String key = i2cBusNumber + ":" + String.format("%x", i2cAddress);
		BH1750FVI bh1750fvi = map.get(key);
		if (bh1750fvi == null) {
			bh1750fvi = new BH1750FVI(i2cBusNumber, i2cAddress);
			map.put(key, bh1750fvi);
		}
		return bh1750fvi;
	}

	private BH1750FVI(int i2cBusNumber, byte i2cAddress) {
		if (i2cBusNumber != I2CBus.BUS_0 && i2cBusNumber != I2CBus.BUS_1) {
			throw new IllegalArgumentException("The set " + i2cBusNumber + " is not " +
					I2CBus.BUS_0 + " or " + I2CBus.BUS_1 + ".");
		}
		if (i2cAddress == I2C_ADDRESS_23 || i2cAddress == I2C_ADDRESS_5C) {
			this.i2cAddress = i2cAddress;
		} else {
			throw new IllegalArgumentException("The set " + String.format("%x", i2cAddress) + " is not " +
					String.format("%x", I2C_ADDRESS_23) + " or " + String.format("%x", I2C_ADDRESS_5C) + ".");
		}

		i2cName = "I2C_" + i2cBusNumber + "_" + String.format("%x", i2cAddress);
		logPrefix = "[" + i2cName + "] ";

		try {
			this.i2cBus = I2CFactory.getInstance(i2cBusNumber);
			this.i2cDevice = i2cBus.getDevice(i2cAddress);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	synchronized public void open() throws IOException {
		try {
			logger.config(logPrefix + "before - useCount:{} "  + useCount.get());
			if (useCount.compareAndSet(0, 1)) {
				logger.info(logPrefix + "opened");
			}
		} finally {
			logger.config(logPrefix + "after - useCount:{}: " +  useCount.get());
		}
	}

	synchronized public void close() throws IOException {
		try {
			logger.config(logPrefix + "before - useCount:{} " + useCount.get());
			if (useCount.compareAndSet(1, 0)) {
				i2cBus.close();
				logger.info(logPrefix + "closed");
			}
		} finally {
			logger.config(logPrefix + "after - useCount:{} "+ useCount.get());
		}
	}

	public int getI2cBusNumber() {
		return i2cBus.getBusNumber();
	}

	public byte getI2cAddress() {
		return i2cAddress;
	}

	public String getName() {
		return i2cName;
	}

	public String getLogPrefix() {
		return logPrefix;
	}

	private void dump(byte data, String tag) {
		if (logger.isLoggable(Level.CONFIG)) {
			StringBuffer sb = new StringBuffer();
			sb.append(String.format("%02x", data));
			logger.config(logPrefix + " Tag: " + tag + " String: " +  sb.toString());
		}
	}

	private void dump(byte[] data, String tag) {
		if (logger.isLoggable(Level.CONFIG)) {
			StringBuffer sb = new StringBuffer();
			for (byte data1 : data) {
				sb.append(String.format("%02x ", data1));
			}
//			logger.trace(logPrefix + "{}{}", tag, sb.toString().trim());
		}
	}

	private void write(byte out) throws IOException {
		try {
			dump(out, "BH1750FVI sensor command: write: ");
			i2cDevice.write(out);
		} catch (IOException e) {
			String message = logPrefix + "failed to write.";
			logger.info(message);
			throw new IOException(message, e);
		}
	}

	private byte[] read(int length) throws IOException {
		try {
			byte[] in = new byte[length];
			i2cDevice.read(in, 0, length);
			dump(in, "BH1750FVI sensor command: read:  ");
			return in;
		} catch (IOException e) {
			String message = logPrefix + "failed to read.";
			logger.info(message);
			throw new IOException(message, e);
		}
	}

	public float getOptical() throws IOException {
		write(OPECODE_ONE_TIME_H_RESOLUTION_MODE);

		try {
			Thread.sleep(H_RESOLUTION_MODE_MEASUREMENT_TIME_MILLIS);
		} catch (InterruptedException e) {
		}

		byte[] data = read(SENSOR_DATA_LENGTH);

		return (float)((((int)(data[0] & 0xff) << 8) + (int)(data[1] & 0xff)) / 1.2);
	}

	/******************************************************************************************************************
	 * Sample main
	 ******************************************************************************************************************/
	public static void main(String[] args) throws IOException {
		BH1750FVI bh1750fvi = null;
		try {
			bh1750fvi = BH1750FVI.getInstance(I2CBus.BUS_1, BH1750FVI.I2C_ADDRESS_23);
			bh1750fvi.open();

			while (true) {
				float value = bh1750fvi.getOptical();
				logger.info("optical:" + value);

				Thread.sleep(10000);
			}
		} catch (InterruptedException e) {
			logger.log(Level.INFO, "caught - {}", e.toString());
		} catch (IOException e) {
			logger.log(Level.INFO, "caught - {}", e.toString());
		} finally {
			if (bh1750fvi != null) {
				bh1750fvi.close();
			}
		}
	}
}
