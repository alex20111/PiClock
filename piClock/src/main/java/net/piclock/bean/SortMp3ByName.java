package net.piclock.bean;

import java.util.Comparator;

import net.piclock.db.entity.Mp3Entity;



public class SortMp3ByName implements Comparator<Mp3Entity>{ 

	// Used for sorting in ascending order of 
	// roll number 
	public int compare(Mp3Entity a, Mp3Entity b) 
	{ 			
		int r = a.getMp3Name().compareToIgnoreCase (b.getMp3Name());
		
		return r; 
	} 
}