package net.piclock.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import home.mp3.decoder.Decoder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

public class Mp3Player {

	private static final Logger logger = Logger.getLogger( Mp3Player.class.getName() );

	private static Mp3Player mp3Handler;

	private SongModel songModel;
	private Iterator<String> mp3Iterator = null;
	private Thread player ;
	private Decoder decoder;
	private boolean stopRequested = false;

	private  Mp3Player() {
//		new javafx.embed.swing.JFXPanel();
//		songModel = new SongModel();
		decoder = new Decoder();
	}

	public static Mp3Player getInstance() {
		if (mp3Handler == null) {
			synchronized (Mp3Player.class) {
				if(mp3Handler == null) {
					logger.log(Level.INFO, "Mp3Handler initialized");
					mp3Handler = new Mp3Player();
				}
			}
		}
		return mp3Handler;
	}	
	
	
	public void play(final List<String> fileNames) {
		
		
		logger.config("Files names size: " + fileNames.size() + " " + fileNames);
		
		stopRequested = false;
		player = new Thread(new Runnable() {

			@Override
			public void run() {


				for(String name : fileNames) {

					//				while(mp3Iterator.hasNext()) {
					try {
						//					String file = mp3Iterator.next();
						logger.config("Playing: " + name);

						FileInputStream in = new FileInputStream(name);
						BufferedInputStream bin = new BufferedInputStream(in, 128 * 1024);


						decoder.play(name, bin);
//						System.out.println("DONEE");

						bin.close();
						in.close();
					}catch(IOException fnf) {
						logger.log(Level.CONFIG,"error", fnf);
						break;
					}

					if (stopRequested) {
						break;
					}
					//					System.out.println("mp3Iterator.hasNext(): " + mp3Iterator.hasNext());
				}
				System.out.println("end");
			}

		});
		
		player.start();
		
//		playMp3(mp3Iterator.next());
		
		
	}
	
//	private void playMp3Decode(String filename) {
//		
//	}
	private void playMp3(String fileName) {
		System.out.println("Play mp3: " + fileName);
		
		songModel.setURL(fileName);

		final MediaPlayer mediaPlayer = songModel.getMediaPlayer();
		
		mediaPlayer.play();
		mediaPlayer.setOnError(() -> {
			System.out.println("error: ");
		});
		
		mediaPlayer.setOnEndOfMedia(() -> {
			System.out.println("End of song: mp3Iterator.hasNext() : " + mp3Iterator.hasNext());
			mediaPlayer.stop();
			
			if (mp3Iterator.hasNext()) {
				playMp3(mp3Iterator.next());
			}
			
			
		});
	}
	

	public void stopMp3() {
		
		stopRequested = true;
		decoder.stop();
//		final MediaPlayer mediaPlayer = songModel.getMediaPlayer();

//		mediaPlayer.stop();
	}

	public void volume(int vol) {
		final MediaPlayer mediaPlayer = songModel.getMediaPlayer();

		mediaPlayer.setVolume(vol);
	}


	public boolean isPlaying() {
		Status status = songModel.getMediaPlayer().getStatus();

		return status == Status.PLAYING;
	}

}

final class SongModel {


	private final StringProperty album = new SimpleStringProperty(this, "album");
	private final StringProperty artist = new SimpleStringProperty(this,"artist");
	private final StringProperty title = new SimpleStringProperty(this, "title");
	private final StringProperty year = new SimpleStringProperty(this, "year");

	private final ObjectProperty<Image> albumCover = 
			new SimpleObjectProperty<Image>(this, "albumCover");

	private final ReadOnlyObjectWrapper<MediaPlayer> mediaPlayer = 
			new ReadOnlyObjectWrapper<MediaPlayer>(this, "mediaPlayer");

	public SongModel() {
		resetProperties();
	}

	public void setURL(String url) {
		if (mediaPlayer.get() != null) {
			mediaPlayer.get().stop();
		}

		initializeMedia(url);
	}

	public String getAlbum() { return album.get(); }
	public void setAlbum(String value) { album.set(value); }
	public StringProperty albumProperty() { return album; }

	public String getArtist() { return artist.get(); }
	public void setArtist(String value) { artist.set(value); }
	public StringProperty artistProperty() { return artist; }

	public String getTitle() { return title.get(); }
	public void setTitle(String value) { title.set(value); }
	public StringProperty titleProperty() { return title; }

	public String getYear() { return year.get(); }
	public void setYear(String value) { year.set(value); }
	public StringProperty yearProperty() { return year; }

	public Image getAlbumCover() { return albumCover.get(); }
	public void setAlbumCover(Image value) { albumCover.set(value); }
	public ObjectProperty<Image> albumCoverProperty() { return albumCover; }

	public MediaPlayer getMediaPlayer() { return mediaPlayer.get(); }
	public ReadOnlyObjectProperty<MediaPlayer> mediaPlayerProperty() { 
		return mediaPlayer.getReadOnlyProperty();
	}

	private void resetProperties() {
		setArtist("");
		setAlbum("");
		setTitle("");
		setYear("");

		//    setAlbumCover(DEFAULT_ALBUM_COVER);
	}

	private void initializeMedia(String url) {
		resetProperties();

		try {
			final Media media = new Media(new File(url).toURI().toString());
			media.getMetadata().addListener(new MapChangeListener<String, Object>() {
				@Override
				public void onChanged(Change<? extends String, ? extends Object> ch) {
					if (ch.wasAdded()) {
						handleMetadata(ch.getKey(), ch.getValueAdded());
					}
				}
			});

			mediaPlayer.setValue(new MediaPlayer(media));
			mediaPlayer.get().setOnError(new Runnable() {
				@Override
				public void run() {
					String errorMessage = mediaPlayer.get().getError().getMessage();
					// Handle errors during playback
					System.out.println("MediaPlayer Error: " + errorMessage);
				}
			});
		} catch (RuntimeException re) {
			// Handle construction errors
			System.out.println("Caught Exception: " + re.getMessage());
		}
	}

	private void handleMetadata(String key, Object value) {
		if (key.equals("album")) {
			setAlbum(value.toString());
		} else if (key.equals("artist")) {
			setArtist(value.toString());
		} if (key.equals("title")) {
			setTitle(value.toString());
		} if (key.equals("year")) {
			setYear(value.toString());
		} if (key.equals("image")) {
			setAlbumCover((Image)value);
		}
	}
}
