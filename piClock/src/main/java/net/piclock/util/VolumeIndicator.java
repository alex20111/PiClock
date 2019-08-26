package net.piclock.util;

public class VolumeIndicator {

	private boolean mp3Playing = false;
	private boolean radioPlaying = false;
	
	
	public void setMp3Playing(boolean mp3Playing) {
		this.mp3Playing = mp3Playing;
	}
	public void setRadioPlaying(boolean radioPlaying) {
		this.radioPlaying = radioPlaying;
	}
	
	public boolean isMp3Playing() {
		return mp3Playing;
	}
	public boolean isRadioPlaying() {
		return radioPlaying;
	}
	public boolean displayVolumeIcon() {
		return mp3Playing || radioPlaying;
	}
	@Override
	public String toString() {
		return "VolumeIndicator [mp3Playing=" + mp3Playing + ", radioPlaying=" + radioPlaying + "]";
	}
	
	
	
	
	
}
