package net.piclock.server;

public class CookieValue {

	private String mp3GenreValue = "";
	private String filler 		= "";
		
	
	public String getMp3GenreValue() {
		return mp3GenreValue;
	}

	public void setMp3GenreValue(String mp3GenreValue) {
		this.mp3GenreValue = mp3GenreValue;
	}

	public String getFiller() {
		return filler;
	}

	public void setFiller(String filler) {
		this.filler = filler;
	}

	public String toString(){
		return mp3GenreValue + "," + filler;
	}
	
	public void fromString(String string){
		
		System.out.println("COOOKIIIE: " + string);
		
		String split[] = string.split(",");
				
		this.mp3GenreValue = split[0];
		this.filler = split[1];
	}
	
	public boolean hasCookie(){
		if (mp3GenreValue != null && !mp3GenreValue.isEmpty()){
			return true;
		}else if (filler != null && !filler.isEmpty()){
			return true;
		}
		
		return false;
	}
}
