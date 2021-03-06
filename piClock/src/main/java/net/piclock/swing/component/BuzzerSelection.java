package net.piclock.swing.component;

import net.piclock.db.entity.AlarmEntity;
import net.piclock.enums.Buzzer;

public class BuzzerSelection {

	private Buzzer buzzer;
	private int  radioId = -1;
	private int  mp3Id = -1;
	private int shutdownMin = -1;
	private int selVolume =-1;

	public BuzzerSelection(Buzzer buzzer, int min) {
		this.buzzer = buzzer;

		this.shutdownMin = min;
	}

	public BuzzerSelection(Buzzer buzzer, int id , int min) {
		this.buzzer = buzzer;
		if (buzzer == Buzzer.RADIO) {
			this.radioId = id;
		}else if (buzzer == Buzzer.MP3) {
			this.mp3Id = id;
		}	

		this.shutdownMin = min;
	}

	/**
	 * This is needed to set initial state after boot up so we won't get an npe .
	 * @param alarm
	 */
	public BuzzerSelection(AlarmEntity alarm) {

		if (alarm != null) {
			buzzer = Buzzer.valueOf(alarm.getAlarmSound());

			if (buzzer == Buzzer.RADIO) {
				this.radioId = alarm.getRadioId();
			}else if (buzzer == Buzzer.MP3) {
				this.mp3Id = alarm.getMp3Id();
			}
			this.shutdownMin = alarm.getAlarmShutdown();
		}
	}

	public Buzzer getBuzzer() {
		return buzzer;
	}
	public void setBuzzer(Buzzer buzzer) {
		this.buzzer = buzzer;
	}

	public int getRadioId() {
		return radioId;
	}

	public void setRadioId(int radioId) {
		this.radioId = radioId;
	}

	public int getMp3Id() {
		return mp3Id;
	}

	public void setMp3Id(int mp3Id) {
		this.mp3Id = mp3Id;
	}

	public int getShutdownMin() {
		return shutdownMin;
	}

	public int getSelVolume() {
		return selVolume;
	}

	public void setSelVolume(int selVolume) {
		this.selVolume = selVolume;
	}

	@Override
	public String toString() {
		return "BuzzerSelection [buzzer=" + buzzer + ", radioId=" + radioId + ", mp3Id=" + mp3Id + ", shutdownMin="
				+ shutdownMin + ", selVolume=" + selVolume + "]";
	}
}
