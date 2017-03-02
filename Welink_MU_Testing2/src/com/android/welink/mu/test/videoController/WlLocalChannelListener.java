package com.android.welink.mu.test.videoController;

public interface WlLocalChannelListener {
	public void onBufferChanged(byte[] buffer, int len);


	public void onReadyForLocal(int mode);

	public void onScreenChanged(byte[] mImgBytes2,int mode);
}
