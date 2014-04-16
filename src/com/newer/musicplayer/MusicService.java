package com.newer.musicplayer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

public class MusicService extends Service implements OnCompletionListener {

	public static final String ACTION_BC_LOADED = "com.newer.musicplayer_BC_LOADED";
	public static final String ACTION_BC_PLAY_NEXT = "com.newer.musicplayer_BC_PLAY_NEXT";
	private static final int ID_PLAYING = 1;
	
	private static final String TAG = "MusicService";
	
	private LocalBinder binder;
	private MediaPlayer mediaPlayer;
	private ArrayList<Song> songList;

	private int currentSongIndex;

	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate");
		
		super.onCreate();

		binder = new LocalBinder();
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(this);

		songList = new ArrayList<Song>();

		new LoadSongList().start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		return binder;
	}

	@Override
	public void onRebind(Intent intent) {
		Log.d(TAG, "onRebind");
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "onUnbind");
		return true;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		mediaPlayer.release();
	}

	/**
	 * 循环播放
	 * 
	 * @param mp
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {
		playNext();

		sendBroadcast(new Intent(ACTION_BC_PLAY_NEXT));
	}

	/**
	 * 得到音乐列表
	 * 
	 * @return
	 */
	public ArrayList<Song> getSongList() {
		return songList;
	}

	/**
	 * 得到当前播放的歌曲索引
	 * 
	 * @return
	 */
	public int getCurrentSongIndex() {
		return currentSongIndex;
	}

	/**
	 * 获得当前播放的歌曲
	 * 
	 * @return
	 */
	public Song getCurrentSong() {
		return songList.get(currentSongIndex);
	}
	
	/**
	 * 获得时长
	 * @return
	 */
	public int getDuration() {
		return mediaPlayer.getDuration();
	}
	
	/**
	 * 获得播放的当前位置
	 * @return
	 */
	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	/**
	 * 播放当前歌曲
	 * 
	 * @return
	 */
	public boolean play() {
		return play(currentSongIndex);
	}
	
	/**
	 * 暂停播放
	 */
	public void pause() {
		mediaPlayer.pause();
	}

	/**
	 * 播放指定歌曲
	 * 
	 * @param position
	 */
	public boolean play(int position) {
		Log.d(TAG, "play");
		Song song = songList.get(position);

		if (position != currentSongIndex) {

			songList.get(currentSongIndex).setPlaying(false);
			currentSongIndex = position;

			song.setPlaying(true);

			try {
				mediaPlayer.reset();
				mediaPlayer.setDataSource(song.getPath());
				mediaPlayer.prepare();
				mediaPlayer.start();

				// 发送播放通知
				sendNotification(song);

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (mediaPlayer.isPlaying()) {

				mediaPlayer.pause();

				stopForeground(true);

				return false;
			} else {
				mediaPlayer.start();

				sendNotification(song);

				return true;
			}
		}

		return true;
	}

	/**
	 * 得到封面
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public Bitmap getCover() throws FileNotFoundException, IOException {
		int id = songList.get(currentSongIndex).getAlbumId();

		Uri uri = Uri.parse("content://media/external/audio/albumart");

		return android.provider.MediaStore.Images.Media.getBitmap(
				getContentResolver(),
				Uri.withAppendedPath(uri, String.valueOf(id)));
	}

	/**
	 * 发送播放通知
	 * 
	 * @param song
	 */
	private void sendNotification(Song song) {
		Log.d(TAG, "sendNotification");

		int icon = android.R.drawable.ic_media_play;
		CharSequence tickerText = song.getTitle();
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		Intent intent = new Intent(getApplicationContext(), SongActivity.class);

		PendingIntent contentIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(getApplicationContext(),
				song.getTitle(), song.getArtist(), contentIntent);
		
		startForeground(ID_PLAYING, notification);
	}

	/**
	 * 播放下一首
	 */
	public void playNext() {
		int position;

		if (currentSongIndex == songList.size() - 1) {
			position = 0;
		} else {
			position = currentSongIndex + 1;
		}

		play(position);
	}

	/**
	 * 播放上一首
	 */
	public void playPre() {
		int position;

		if (currentSongIndex == 0) {
			position = songList.size() - 1;
		} else {
			position = currentSongIndex - 1;
		}

		play(position);
	}

	public void seekTo(int progress) {
		mediaPlayer.seekTo(progress);
	}

	class LocalBinder extends Binder {
		public MusicService getService() {
			return MusicService.this;
		}
	}

	class LoadSongList extends Thread {

		@Override
		public void run() {
			super.run();

			Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			String[] projection = { Media._ID, Media.TITLE, Media.ARTIST,
					Media.ALBUM, Media.DURATION, Media.DATA, Media.ALBUM_ID };

			Cursor cursor = getContentResolver().query(uri, projection, null,
					null, Media.ALBUM);

			while (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				String title = cursor.getString(1);
				String artist = cursor.getString(2);
				String album = cursor.getString(3);
				String duration = cursor.getString(4);
				String path = cursor.getString(5);
				int albumId = cursor.getInt(6);

				Song song = new Song(id, title, artist, album, duration, path,
						albumId);
				song.setAlbumId(albumId);

				songList.add(song);
			}
			cursor.close();

			// 数据加载完后发送一个广播
			sendBroadcast(new Intent(ACTION_BC_LOADED));
		}
	}

}
