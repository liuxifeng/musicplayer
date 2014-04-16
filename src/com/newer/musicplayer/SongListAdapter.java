package com.newer.musicplayer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SongListAdapter extends BaseAdapter {

	private ArrayList<Song> songList;
	private LayoutInflater inflater;

	public SongListAdapter(Context context, ArrayList<Song> songList) {
		this.songList = songList;
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return songList.size();
	}

	@Override
	public Song getItem(int position) {
		return songList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {
		TextView title;
		TextView artist;
		TextView duration;
		ImageView isPlaying;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.song_item, null);
			holder = new ViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.artist = (TextView) convertView.findViewById(R.id.artist);
			holder.duration = (TextView) convertView
					.findViewById(R.id.duration);
			holder.isPlaying = (ImageView) convertView
					.findViewById(R.id.is_playing);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Song song = songList.get(position);
		holder.title.setText(song.getTitle());
		holder.artist.setText(song.getArtist());
		holder.duration.setText(Uitl.formatTime(song.getDuration()));

		if (song.isPlaying()) {
			holder.isPlaying.setVisibility(View.VISIBLE);
		} else {
			holder.isPlaying.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

}