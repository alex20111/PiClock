package net.piclock.main;

public class RadioLinks {

	private String radioName ="";
	private String streamLink = "";
	
	public RadioLinks(){};
	
	public RadioLinks(String name, String stream){
		this.radioName = name;
		this.streamLink = stream;
	}
	
	public String getRadioName() {
		return radioName;
	}
	public void setRadioName(String radioName) {
		this.radioName = radioName;
	}
	public String getStreamLink() {
		return streamLink;
	}
	public void setStreamLink(String streamLink) {
		this.streamLink = streamLink;
	}

	@Override
	public String toString() {
		return "RadioLinks [radioName=" + radioName + ", streamLink=" + streamLink + "]";
	}
	
}