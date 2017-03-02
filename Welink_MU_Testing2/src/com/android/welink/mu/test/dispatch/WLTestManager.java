package com.android.welink.mu.test.dispatch;

import android.content.Context;

/**
 * 
 * @author jiaoyb
 *
 */
public class WLTestManager extends WLTestController{
	
	private static String TAG = WLTestManager.class.getName();
	private static WLTestManager mWLTestManager = null;
	
	
	
	
	
	
	private WLTestManager(Context context) {
		super(context);
		
	}

	public synchronized static WLTestManager getInstance(Context context) {

		if (mWLTestManager == null) {

			mWLTestManager = new WLTestManager(context);
		}

		return mWLTestManager;
	}

	
	/**
	 * 设置监听
	 * @param listener
	 */
	
	public void setOnWLHuHelperListener(WLMuTestHelperListener listener)
	{
		super.setOnWLHuHelperListener(listener);
	
	}
	
	
	
	public void start(){
		
		super.start();
	}
	
	
	public void stop(){
		
	}
	
	

	public void destroy() {
		super.destroy();
		mWLTestManager = null;
		
		
	}

	public void kill() {
		// TODO Auto-generated method stub
		super.kill();
	}

	public void switchMode(int i) {
		super.switchMode(i);
		
	}
	
	
	
	
	

}
