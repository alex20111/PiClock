package net.piclock.server;

public class Mp3GenreFilter {

	public static final String OTHER = "Other";
	public static final String ALL = "All";
	
	private int id 	  = -1;
	private int genre = -1;
	private String description = "";
	
	
	public Mp3GenreFilter(int id, int g, String d){
		this.id = id;
		genre = g;
		
		if (d == null || d.trim().length() == 0){
			description = OTHER;
		}
		else
		{
			description = d;
		}
	}
	
	public String getDisplayName(){
		return description;
	}
	
	public int getKey(){
		return genre;
	}
	
	public String getSearchName(){
		if (OTHER.equals(description)){
			return "";
		}
		return description;
	}
	
	//tell the sql to search by description or genre.
	//if the description is OTHER, do not search by genre. 
	//this means that the description is invalid.
	public boolean searchByGenre(){
		if (!OTHER.equals(description) && genre > 0){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + genre;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mp3GenreFilter other = (Mp3GenreFilter) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (genre != other.genre)
			return false;
		return true;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Mp3GenreFilter [genre=" + genre + ", description=" + description + "]";
	}
	
}