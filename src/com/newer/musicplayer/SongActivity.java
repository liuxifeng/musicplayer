package com.newer.musicplayer;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SongActivity extends Activity implements OnSeekBarChangeListener {

	private static final String TAG = "SongActivity";
	
	private TextView textViewTitle;
	private ImageView imageViewAlbum;
	private SeekBar seekBar;
	private TextView textViewPlusTime;
	private TextView textViewDuration;
	private Button buttonPlay;

	private MusicService musicService;
	private boolean isBound;

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected");
			
			musicService = ((MusicService.LocalBinder) service).getService();

			isBound = true;
			updateSong(musicService.getCurrentSong());
		}
	};

	private Runnable actionUpdate = new Runnable() {

		@Override
		public void run() {
			if (isBound) {
				seekBar.setMax(musicService.getDuration());

				int position = musicService.getCurrentPosition();
				seekBar.setProgress(position);

				textViewPlusTime.setText(Uitl.formatTime(String
						.valueOf(position)));

			}
			seekBar.postDelayed(this, 1000);
		}

	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive");
			
			String action = intent.getAction();

			if (action == MusicService.ACTION_BC_PLAY_NEXT) {
				updateSong(musicService.getCurrentSong());
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song);
		Log.d(TAG, "onCreate");

		startService(new Intent(this, MusicService.class));

		initView();
	}

	private void initView() {
		textViewTitle = (TextView) findViewById(R.id.textView_title);
		imageViewAlbum = (ImageView) findViewById(R.id.imageView);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		textViewPlusTime = (TextView) findViewById(R.id.textView_plusTime);
		textViewDuration = (TextView) findViewById(R.id.textView_duration);
		buttonPlay = (Button) findViewById(R.id.button_play);

		seekBar.setOnSeekBarChangeListener(this);

	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
		bindService(new Intent(this, MusicService.class), conn,
				BIND_AUTO_CREATE);
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();

		seekBar.post(actionUpdate);

		IntentFilter filter = new IntentFilter();
		filter.addAction(MusicService.ACTION_BC_PLAY_NEXT);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();

		seekBar.removeCallbacks(actionUpdate);
		unregisterReceiver(receiver);
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		unbindService(conn);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	public void doClick(View v) {
		switch (v.getId()) {
		case R.id.button_play:

			if (musicService.play()) {
				buttonPlay.setText("ÔÝÍ£");
			} else {
				buttonPlay.setText("²¥·Å");
			}

			break;
		case R.id.button_pre:
			musicService.playPre();
			updateSong(musicService.getCurrentSong());
			break;
		case R.id.button_next:
			musicService.playNext();
			updateSong(musicService.getCurrentSong());
			break;
		}
	}

	private void updateSong(Song currentSong) {
		textViewTitle.setText(currentSong.getTitle());
		textViewDuration.setText(Uitl.formatTime(currentSong.getDuration()));
		buttonPlay.setText("ÔÝÍ£");

		try {
			imageViewAlbum.setImageBitmap(musicService.getCover());
		} catch (FileNotFoundException e) {
			imageViewAlbum.setImageResource(R.drawable.albumart_mp_unknown);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (fromUser) {
			musicService.seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		musicService.pause();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		musicService.play();
	}

}
