package com.android.welink.mu.test.dispatch;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.android.welink.mu.test.bean.DeviceInfo;
import com.android.welink.mu.test.hucontroller.WlCmdController;
import com.android.welink.mu.test.hucontroller.WlCmdListener;
import com.android.welink.mu.test.tool.BitmapUtils;
import com.android.welink.mu.test.tool.QRCodeUtil;
import com.android.welink.mu.test.tool.Utils;
import com.android.welink.mu.test.videoController.LocalChannelMuController;
import com.android.welink.mu.test.videoController.WlLocalChannelListener;
import com.example.welink_mu_testing2.MainActivity;
import com.wedrive.android.welink.jni.FFmpegUtils;
import com.wedrive.android.welink.jni.FFmpegUtils.OnDecodeListener;

public class WLTestController implements WlCmdListener, WlLocalChannelListener {

	private static String TAG = WLTestController.class.getName();
	public WLMuTestHelperListener mWLMuTestHelperListener;
	private String fileName;
	private String outPath;
	Context mContext;
	Handler mHandler;
	DeviceInfo mDeviceInfo;
	FFmpegUtils ffmpegUtils;
	private WlCmdController mWlCmdController;
	private LocalChannelMuController mLocalChannelMuController;
	
	String result = "";
	String mode = "";

	public WLTestController(Context context) {
		this.mContext = context;

		fileName = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + "jiaoyb00.h264";
		outPath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + "mapbar";

		mHandler = new Handler();

		mDeviceInfo = new DeviceInfo();
		WindowManager wm = (WindowManager) context.getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();
		mDeviceInfo.setPhoneWidth(Math.min(width, height));
		mDeviceInfo.setPhoneHeight(Math.max(width, height));

		Log.e("0000", " width = " + width + "  height = " + height);
		mWlCmdController = new WlCmdController(mContext, this);
		mLocalChannelMuController = new LocalChannelMuController(context, this);
		ffmpegUtils = new FFmpegUtils();

	}

	// 子类调用
	public void start() {
		
		ffmpegUtils.decodeStreamInit(outPath,new OnDecodeListener() {
			
			@Override
			public void onImage(final byte[] imageBytes) {
				mHandler.post(new Runnable() {
					
					@Override
					public void run() {
//						onScreenChanged(imageBytes, 1);
					}
				});
				
				
			}
		});
//TODO
//TODO
//		File videofile = new File(fileName);
//		if (videofile.isFile() && videofile.exists()) {
//			videofile.delete();
//		}

		mWlCmdController.start();
		Log.e("0000", " start ");

		if (mWLServiceReader != null) {
			mWLServiceReader.destroy();
			mWLServiceReader = null;
		}
		// 重置welink服务
		killWelink();
		// 延迟两秒开启welink服务
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {

				startWelink();
				getWLServiceInfo();

			}
		}, 3000);

	}

	private void startWelink() {
		Log.e("0000", "start welink");
		mWlCmdController.performCmd("chmod 777  /data/local/tmp/welink", false);
		mWlCmdController.performCmd("/data/local/tmp/welink", false);
		mWlCmdController.performCmd("dumpsys window displays |head -n 3 2>&1", true);
	}

	private void killWelink() {
		if (mWLServiceReader != null) {
			mWLServiceReader.destroy();
			mWLServiceReader = null;
		}

		// TODO Auto-generated method stub
		Log.e("0000", "kill -9 " + myPid);
		mWlCmdController.performCmd("kill -9 " + myPid + " ", false);

	}

	private WLServiceReader mWLServiceReader;

	private void getWLServiceInfo() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (mWLServiceReader != null)
					mWLServiceReader.destroy();
				mWLServiceReader = new WLServiceReader();
				mWLServiceReader.start();
			}
		}).start();

	}

	int myPid = 0;

	private class WLServiceReader {
		private Socket mySocket;
		private InputStream myIs;
		private OutputStream myOs;
		private boolean isRun;

		public WLServiceReader() {
			isRun = true;
		}

		public void destroy() {
			isRun = false;
			cancle();
		}

		public void start() {
			if (!isRun)
				return;
			Log.e("0000", "11111111");
			try {
				mySocket = new Socket();
				mySocket.connect(new InetSocketAddress("localhost", 6803), 1000);
				myIs = mySocket.getInputStream();
				myOs = mySocket.getOutputStream();
				myOs.write("version\n".getBytes());
				myOs.flush();

				Log.e("0000", "2222222222222");
				while (isRun) {
					byte[] bHeader = readBuffer(myIs, 4);
					if (bHeader != null && bHeader.length == 4) {
						if ((bHeader[0] == 'W') && (bHeader[1] == 'L')) {
							int iType = bHeader[2] & 0xff;
							int iHdrLen = bHeader[3];

							byte[] bSize = readBuffer(myIs, 4);

							if (iHdrLen > 0)
								readBuffer(myIs, iHdrLen);
							if (bSize != null && bSize.length == 4) {
								int nLen = toInt(bSize, 0) - iHdrLen;
								if (nLen > 0) {
									if (iType == 200) {
										byte[] buff = readBuffer(myIs, nLen);
										String strVersion = new String(buff);
										String[] arrs = strVersion.split("\n");
										int len = arrs.length;
										for (int i = 0; i < len; i++) {
											String str = arrs[i];
											String[] strLines = str.split(" ");
											for (String string : strLines) {
												if (string.equals("WeLink")) {// 6803已经创建完成
																				// 准备开始录屏
													Log.e("0000",
															" WLServiceReader = 6803  11111111");
													mLocalChannelMuController
															.start();
												}
												Log.e("0000",
														"welink 服务启动 string = "
																+ string);
												if (strLines[0].equals("PID")) {
													myPid = Utils.formatInt(
															strLines[1], myPid);

													Log.e("0000",
															"welink myPid = "
																	+ myPid);
												}
											}
										}
										break;
									}
								}
							}
						}
					}
				}
				cancle();
			} catch (Exception e) {
				Log.e("0000", "get info failed ==== ", e);
				cancle();
				try {
					Thread.sleep(1000);
				} catch (Exception ex) {
				}
				if (isRun)
					start();
			}
		}

		private void cancle() {
			try {
				if (myIs != null)
					myIs.close();
				myIs = null;
			} catch (Exception e) {
			}
			try {
				if (myOs != null)
					myOs.close();
				myOs = null;
			} catch (Exception e) {
			}
			try {
				if (mySocket != null)
					mySocket.close();
				mySocket = null;
			} catch (Exception e) {
			}
		}

		private byte[] readBuffer(InputStream is, int nSize) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				if (nSize > 0) {
					int iLen = 0;
					byte[] baBuf = new byte[nSize];
					while ((baos.size() < nSize)
							&& ((iLen = is.read(baBuf, 0,
									baBuf.length - baos.size())) != -1)) {
						if (iLen > 0)
							baos.write(baBuf, 0, iLen);
					}
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
			return baos.toByteArray();
		}

		private int toInt(byte[] bytes, int offset) {
			return ((bytes[offset] & 0xff) << 24)
					+ ((bytes[offset + 1] & 0xff) << 16)
					+ ((bytes[offset + 2] & 0xff) << 8)
					+ (bytes[offset + 3] & 0xff);
		}
	}

	public void destroy() {
		mWlCmdController.cancelWLCmd();
		mLocalChannelMuController.stop();
		// TODO Auto-generated method stub

	}

	/**
	 * 调用此方法会切换视频模式
	 * 
	 * @param mode
	 *            1：screen 2:screen2 3:svideo
	 */
	private void switchVideoMode(int mode) {

		mLocalChannelMuController.stop();
		mLocalChannelMuController.setCodecMode(mode);
		start();

	}

	public void kill() {
		// Log.e("0000", ffmpegUtils.avfilterinfo());
		// ffmpegUtils.decodeStream("hellllllllllllllllllllllllllllllll".getBytes());
		// mLocalChannelMuController.start();
	}

	int frameCount = 0;
	@Override
	public void onBufferChanged(byte[] buffer, int len) {
		// TODO Auto-generated method stub
//		frameCount ++;
//		Log.e("3333", Thread.currentThread().getName() + "   buffer = "
//				+ buffer.length + "  len  = " + len+"  frame = "+frameCount);
//		appendMethodA(fileName, buffer);
		long tmp = System.currentTimeMillis();
		ffmpegUtils.decodeStream(buffer);
		Log.e("haoshi", "========= "+(System.currentTimeMillis()-tmp));

	}

	// 6803通道建立完成此时开始定时检查视频流数据是否能解析出来
	@Override
	public void onReadyForLocal(final int mode) {
		result = "";
		if(mode == 3){
			this.mode = "视频流";
			mWLMuTestHelperListener.checkStatus("检测状态：正在录制视频流文件");
		}
		if(mode == 2){
			this.mode = "图片流模式二";
			
		}
		if(mode == 1){
			this.mode = "图片流模式一";
			
		}


		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				new Thread(new Runnable() {

					@Override
					public void run() {
						// 停止
						// mLocalChannelMuController.stop();

						mLocalChannelMuController.sendData("pause");
						
						
//						
//						try {
//							Thread.sleep(1000);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
						
						ffmpegUtils.destorydecode();
//						frameCount=0;
//						if (mode == 3) {
//							File videofile = new File(fileName);
//							if (videofile.isFile() && videofile.exists()) {
//								mWLMuTestHelperListener.checkStatus("检测状态：正在解码视频流每一帧");
//								ffmpegUtils.decodeFile(fileName, outPath);
//
//								scanBitmap4file();
//
//								// TODO 检查视频信息是否能完整
//								Log.e("0000", "====  jie ma wan cheng ");
//							} else {// 不支持视频流切换到图片流检查
//								switchVideoMode(1);
//							}
//						}
//						
//						if(mode == 1){
//							
//						}
//						
//						if(mode == 2){
//							
//						}

					}

				}).start();

			}
		}, 30000);

	}

	int imageCount = 0;

	// 图片流回调
	@Override
	public void onScreenChanged(byte[] mImgBytes2, int mode) {
		// TODO Auto-generated method stub
		mWLMuTestHelperListener.checkStatus("检测状态：正在识别图片模式"+mode+"中的每一帧");
		if (imageCount > 4) {
			result += this.mode+" 图像检测结果\n 共截取"+5+"帧\n"+
					"识别成功"+5+"帧\n"+
					"识别失败"+0+"帧\n";
			checkTouchEvent();
			mLocalChannelMuController.sendData("pause");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			imageCount = 0;
			return;
		}
		Log.e("0000", Thread.currentThread().getName()
				+ "   tupian mImgBytes2 = " + mImgBytes2.length);

		Bitmap bitmap = BitmapFactory.decodeByteArray(mImgBytes2, 0,
				mImgBytes2.length);
		if (bitmap == null) {

		} else {
			QRCodeUtil.discernBitmap(mContext, bitmap);
		}

		imageCount++;
	}

	/**
	 * A方法追加文件：使用RandomAccessFile
	 */
	public static void appendMethodA(String fileName, byte[] content) {
		try {
			File file = new File(fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
			// 打开一个随机访问文件流，按读写方式
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw");
			// 文件长度，字节数
			long fileLength = randomFile.length();
			// 将写文件指针移到文件尾。
			randomFile.seek(fileLength);
			randomFile.write(content);
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void scanBitmap4file() {
		
		mWLMuTestHelperListener.checkStatus("检测状态：正在识别视频流每一帧");
		File file = new File(outPath);
		if (file.isDirectory()) {
			File[] boyfiles = file.listFiles();
			int okcount = 0;
			int errcount = 0;

			ArrayList<Boolean> errorList = new ArrayList<Boolean>();
			for (File boyfile : boyfiles) {
				Log.e("0000", " name = " + boyfile.getName());
				if (boyfile.getName().endsWith(".bmp")) {

					Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFile(
							boyfile.getAbsolutePath(), 800, 450);
					if (bitmap != null) {
						boolean isok = QRCodeUtil.discernBitmap(mContext,
								bitmap);
						if (isok) {
							okcount++;
						} else {
							errcount++;
						}
					}

//					boyfile.delete();
				}
			}

			Log.e("0000", "total = " + (okcount + errcount)
					+ "  success count = " + okcount + "  error count = "
					+ errcount);
			result += this.mode+" 图像检测结果\n 共截取"+(okcount + errcount)+"帧\n"+
					"识别成功"+okcount+"帧\n"+
					"识别失败"+errcount+"帧\n";
			checkTouchEvent();

			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			// TODO 解码失败
		}

	}

	private void checkTouchEvent() {
		
		mWLMuTestHelperListener.checkStatus("检测状态：正在检测点击事件");
		new Thread(new Runnable() {

			@Override
			public void run() {

				int tmp = 80;
				WindowManager wm = (WindowManager) mContext
						.getSystemService(Context.WINDOW_SERVICE);

				int width = wm.getDefaultDisplay().getWidth();
				int height = wm.getDefaultDisplay().getHeight();
				if (width > height) {
					width = width + height;
					height = width - height;
					width = width - height;
				}
				// TODO Auto-generated method stub
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ tmp + " down");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ tmp + " move");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ tmp + " move");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ tmp + " move");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ tmp + " move");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ tmp + " move");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ tmp + " up");

				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + tmp + " down");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + tmp + " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + tmp + " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + tmp + " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + tmp + " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + tmp + " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + tmp + " up");

				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ (height - tmp) + " down");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ (height - tmp) + " move");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ (height - tmp) + " move");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ (height - tmp) + " move");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ (height - tmp) + " move");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ (height - tmp) + " move");
				mLocalChannelMuController.sendData("motionevent " + tmp + " "
						+ (height - tmp) + " up");

				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + (height - tmp) + " down");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + (height - tmp) + " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + (height - tmp) + " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + (height - tmp) + " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + (height - tmp) + " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + (height - tmp) + " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) + " " + (height - tmp) + " up");

				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) / 2 + " " + (height - tmp) / 2
						+ " down");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) / 2 + " " + (height - tmp) / 2
						+ " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) / 2 + " " + (height - tmp) / 2
						+ " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) / 2 + " " + (height - tmp) / 2
						+ " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) / 2 + " " + (height - tmp) / 2
						+ " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) / 2 + " " + (height - tmp) / 2
						+ " move");
				mLocalChannelMuController.sendData("motionevent "
						+ (width - tmp) / 2 + " " + (height - tmp) / 2 + " up");

				mHandler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						onCheckTouchEvent(true, result);
					}
				}, 2000);
			}
		}).start();
	}

	public void switchMode(int i) {
		switchVideoMode(i);
	}

	public void setOnWLHuHelperListener(WLMuTestHelperListener listener) {
		this.mWLMuTestHelperListener = listener;
	}

	public void onCheckVideoMode(int mode, Object obj) {
		if (mWLMuTestHelperListener != null)
			mWLMuTestHelperListener.onCheckVideoMode(mode, obj);

	}

	public void onCheckTouchEvent(boolean isSuccess, Object obj) {
		if (mWLMuTestHelperListener != null)
			mWLMuTestHelperListener.onCheckTouchEvent(isSuccess, obj);
	}

}
