package com.newer.musicplayer;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

public class MainActivity extends Activity implements OnItemClickListener {

	private static final String TAG = "MainActivity";

	private ListView listView;

	private ArrayList<Song> songList;
	private SongListAdapter adapter;
	private MusicService musicService;

	private boolean isBound;
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "onReceive");
			if (isBound) {
				loadSonglist();
			}

		}
	};

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

			loadSonglist();

		}
	};

	private void loadSonglist() {
		songList = musicService.getSongList();
		adapter = new SongListAdapter(getApplicationContext(), songList);
		listView.setAdapter(adapter);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "onCreate");

		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemClickListener(this);

		// 启动，创建服务
		startService(new Intent(this, MusicService.class));

	}

	@Override
	protected void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();

		bindService(new Intent(this, MusicService.class), conn,
				BIND_AUTO_CREATE);

		IntentFilter filter = new IntentFilter(MusicService.ACTION_BC_LOADED);
		registerReceiver(receiver, filter);
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();

		unbindService(conn);
		unregisterReceiver(receiver);
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (position != musicService.getCurrentSongIndex()) {
			musicService.play(position);
		}

		startActivity(new Intent(this, SongActivity.class));
		
	}

}
