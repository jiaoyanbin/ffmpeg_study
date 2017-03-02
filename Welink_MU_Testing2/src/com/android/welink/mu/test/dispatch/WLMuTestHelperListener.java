package com.android.welink.mu.test.dispatch;

public interface WLMuTestHelperListener {
	
	void onCheckVideoMode(int mode,Object obj);
	void onCheckTouchEvent(boolean isSuccess,Object obj);
	void checkStatus(String str);

}
