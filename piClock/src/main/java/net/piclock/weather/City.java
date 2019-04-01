package net.piclock.weather;

public class City {

	private String key = "";
	private String nameEn = "";
	private String nameFr = "";
	private String lat = "";
	private String lon = "";
	private double dist = 0.0d;
	public City(){}
	public City(net.weather.bean.City city){
		this.key = city.key;
		this.nameEn = city.nameEn;
		this.nameFr = city.nameFr;
		this.lat = city.lat;
		this.lon = city.lon;
		this.dist = city.dist;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getNameEn() {
		return nameEn;
	}
	public void setNameEn(String nameEn) {
		this.nameEn = nameEn;
	}
	public String getNameFr() {
		return nameFr;
	}
	public void setNameFr(String nameFr) {
		this.nameFr = nameFr;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLon() {
		return lon;
	}
	public void setLon(String lon) {
		this.lon = lon;
	}
	public double getDist() {
		return dist;
	}
	public void setDist(double dist) {
		this.dist = dist;
	}
	@Override
	public String toString() {
		return nameEn;
	}
}
