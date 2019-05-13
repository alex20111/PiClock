package net.piclock.enums;

public enum CheckWifiStatus {
	STARTING, SUCCESS, END_NO_INET, END_TIMEOUT, END_INTERRUPTED, END_DISCONNECT, END_WIFI_OFF;
	
	
	
	public boolean isConnected() {
		return this == CheckWifiStatus.SUCCESS || this == CheckWifiStatus.END_NO_INET;
	}
	
	public boolean isError() {
		return this != CheckWifiStatus.SUCCESS && this != CheckWifiStatus.END_NO_INET;
	}
}
