package com.newer.musicplayer;

public class Uitl {

	// 将时长转换为 分:秒
	public static String formatTime(String time) {
		int t = Integer.parseInt(time);
		t /= 1000;
		int munite = t / 60;
		int second = t % 60;

		String info = String.format("%d:%02d", munite, second);

		return info;
	}

}
