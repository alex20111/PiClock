package net.piclock.weather;

import java.util.Comparator;

public class CityNameSort implements Comparator<City>{
	@Override
	public int compare(City al0, City al1)
	{

		String temp1 = al0.getNameEn();
		String temp2 = al1.getNameEn();

		if (temp1.length() == 0)
		{
			return (temp2.length() == 0) ? 0 : -1;
		}			 
		else if (temp2.length() == 0)
		{
			return 1;
		}
		else
		{
			return temp1.compareToIgnoreCase(temp2);
		}	  
	}
}
