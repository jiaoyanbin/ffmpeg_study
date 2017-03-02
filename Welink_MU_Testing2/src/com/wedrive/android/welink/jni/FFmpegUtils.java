package com.wedrive.android.welink.jni;

import java.io.File;


public class FFmpegUtils {

	private OnDecodeListener listener;
	static {
		System.loadLibrary("ffmpeg");
		System.loadLibrary("ffmpegutils");
	}

	public boolean decodeFile(String resPath, String outPath) {

		if (!outPath.endsWith(File.separator)) {
			outPath = outPath + File.separator;
		}
		File file = new File(outPath);
		if (!file.exists()) {

			file.mkdirs();
		}
		return decodeFile4Jni(resPath, outPath);
	}

	public native boolean decodeFile4Jni(String resPath, String outPath);
	
	
	
	public void decodeStreamInit(String outPath,OnDecodeListener listener){
		this.listener = listener;
		if (!outPath.endsWith(File.separator)) {
			outPath = outPath + File.separator;
		}
		File file = new File(outPath);
		if (!file.exists()) {

			file.mkdirs();
		}
		initdecode(outPath);
	}
	public native void initdecode(String outpath);
	public native void destorydecode();
	public native byte[] decodeStream(byte[] buffer);
	private void onImage(byte[] data){
		System.out.println("999999999==============="+(data == null ? 0:data.length));
		if(listener != null){
			listener.onImage(data);
		}
	}

	public interface OnDecodeListener{
		
		void onImage(byte[] imageBytes);
	}
}
