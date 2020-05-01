package net.piclock.enums;

public class TestMap {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		int val = 0;
		
		while(val < 177) {
		
		System.out.println(map(val, 0, 200, 12, 200));
		
		val ++;
		}

	}

	private static long map(long x, long in_min, long in_max, long out_min, long out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}

}
