package com.android.welink.mu.test.videoController;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.wedrive.android.welink.control.VarHelper;

public final class LocalChannelMuController {
	private final static int WL_PORT = 6803;
	private final static int TIMEOUT = 1000;
	private boolean isRunning;
	private WeLinkRunner mWeLinkRunner;
	private String mScreenCmd = "svideo";
	private WlLocalChannelListener mLocalHelperListener;
	private Handler mHandler;
	private int mHuScreenWidth = 1280, mHuScreenHeight = 720;

//	private boolean isVedioMode = false;

	private Object mLock = new Object();
	private VarHelper mVarHelper;

	private int mWLType = 6;
	private byte[] mImgBytes;
	private int mImgIndex = 0;
	private boolean isFirstReceiveImg;
	private long mFpsDataSize;
	private int mBitmapIndex = -1;
	private int mLoseFrame;
	private static byte[] hdrDefault = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF,
			(byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46 };

	private int mcodeMode = 3;
	
	private Context mContext;

	public LocalChannelMuController(Context context,
			WlLocalChannelListener listener) {
		this.mContext = context;
		this.mLocalHelperListener = listener;
		mHandler = new Handler();
		mVarHelper = VarHelper.getInstance(context);

	}

	/**
	 * 设置录屏宽高
	 */
	private void setHuSize() {
		if (mcodeMode == 3) {
			mHuScreenWidth = 1280;
			mHuScreenHeight = 720;	
		} else {
			if(mcodeMode == 1){
				mScreenCmd = "SCREEN";
			}else{
				mScreenCmd = "SCREEN2";
			}
			mHuScreenWidth = 800;
			mHuScreenHeight = 450;
		}
	}

	public void start() {
		if (mcodeMode != 3) {
			runForCreateBitmap();
		}
		this.startWeLink();
	}

	public void stop() {
		isRunning = false;
		this.stopWeLink();
	}

	boolean checkKeyCodeStr(String json) {
		if (json.contains("keyevent")) {
			int keyCode = Integer.parseInt(json.split(" ")[1]);
			if (keyCode != 4 && (keyCode < 19 || keyCode > 23)) {
				return false;
			}
		}
		return true;
	}

	public void sendData(String json) {
		if (checkKeyCodeStr(json)) {
			if (mWeLinkRunner != null)
				mWeLinkRunner.sendData(json);
		}
	}

	void setAoaMode(boolean enable) {
	}

	private void startWeLink() {
		if (this.isRunning)
			return;
		this.isRunning = true;
		doWeLink();
	}

	private void stopWeLink() {
		this.isRunning = false;
		if (mWeLinkRunner != null)
			mWeLinkRunner.destroy();
		mWeLinkRunner = null;
	}
	

	private void doWeLink() {
		if (!this.isRunning)
			return;
		if (mWeLinkRunner != null)
			mWeLinkRunner.destroy();
		mWeLinkRunner = null;
		mWeLinkRunner = new WeLinkRunner();
		new Thread(mWeLinkRunner, "doWeLinkThread").start();
	}

	public void onScreenChanged(byte[] mImgBytes2) {
		if (mLocalHelperListener != null) {
			mLocalHelperListener.onScreenChanged(mImgBytes2,mcodeMode);
		}
	}

	/**
	 * 描述：此线程会链接6803welink通道，创建俩个线程，分别负责读取，和通知welink截屏
	 * 
	 */
	private class WeLinkRunner implements Runnable {
		private boolean isRun = false;
		private Socket mySocket = null;
		private OutputStream myOs = null;
		private InputStream myIs = null;
		private WeLinkWriter myWeLinkWriter;
		private WeLinkReader myWeLinkReader;

		public WeLinkRunner() {
			isRun = true;
		}

		public void destroy() {
			isRun = false;
			cancelWeLink();
		}

		/**
		 * 向welink驱动中发送指令
		 * 
		 * @param json
		 */
		public void sendData(String json) {
			try {
				synchronized (mLock) {
					myOs.write((json + "\n").getBytes());
					myOs.flush();
				}

			} catch (Exception e) {
				Log.e("0000", e.getMessage() + e.getCause(), e);
			}
		}

		private void cancelWeLink() {
			if (myWeLinkWriter != null) {
				myWeLinkWriter.destroy();
				myWeLinkWriter = null;
			}
			if (myWeLinkReader != null) {
				myWeLinkReader.destroy();
				myWeLinkReader = null;
			}

			if (myIs != null) {
				try {
					myIs.close();
				} catch (Exception e) {
				}
				myIs = null;
			}
			if (myOs != null) {
				try {
					myOs.close();
				} catch (Exception e) {
				}
				myOs = null;
			}
			if (mySocket != null) {
				try {
					mySocket.close();
				} catch (Exception e) {
				}
				mySocket = null;
			}
		}

		@Override
		public void run() {
			if (!isRunning)
				return;
			if (!isRun)
				return;
			try {
				mySocket = new Socket();
				mySocket.connect(
						new InetSocketAddress(mVarHelper.getWlServerIp(),
								WL_PORT), TIMEOUT);
				myIs = mySocket.getInputStream();
				myOs = mySocket.getOutputStream();

				if (myWeLinkWriter != null) {
					myWeLinkWriter.destroy();
					myWeLinkWriter = null;
				}
				if (myWeLinkReader != null) {
					myWeLinkReader.destroy();
					myWeLinkReader = null;
				}
				myWeLinkWriter = new WeLinkWriter(myOs);
				myWeLinkReader = new WeLinkReader(myIs);
				new Thread(myWeLinkWriter, "WeLinkWriterThread").start();
				new Thread(myWeLinkReader, "WeLinkReaderThread").start();
				if (mLocalHelperListener != null) {
					mLocalHelperListener.onReadyForLocal(mcodeMode);
				}
			} catch (Exception e) {
				// Utils.writeTxtToFile(Utils.formatTime(System.currentTimeMillis())
				// + ": 6803 Error.", "/Test/LocalChannel_log.txt");
				Log.e("0000", e.getMessage() + e.getCause(), e);

				cancelWeLink();

				try {
					Thread.sleep(1000);
				} catch (Exception ex) {
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (WeLinkRunner.this != null)
							new Thread(WeLinkRunner.this, "doWeLinkThread")
									.start();
					}
				});
			}
		}
	}


	/**
	 * 向welink发送开始录屏消息如果失败会重新调用dowelink();
	 *
	 */
	private class WeLinkWriter implements Runnable {
		private boolean isRun;
		private OutputStream myOs = null;

		public WeLinkWriter(OutputStream os) {
			isRun = true;
			myOs = os;
		}

		public void destroy() {
			isRun = false;
			if (myOs != null) {
				try {
					myOs.close();
				} catch (Exception e) {
				}
				myOs = null;
			}
		}

		@Override
		public void run() {
			if (myOs == null)
				return;
			if (!isRun)
				return;
			if (!isRunning)
				return;
			try {
				int min = Math.min(mHuScreenWidth, mHuScreenHeight);
				int max = Math.max(mHuScreenWidth, mHuScreenHeight);
				String strCmd = mScreenCmd + " " + min + " " + max + "\n";
				synchronized (mLock) {
					myOs.write(strCmd.getBytes());
					myOs.flush();
				}

				while (isRun && isRunning) {
					synchronized (mLock) {
						myOs.write("check\n".getBytes());
						myOs.flush();
					}
					Thread.sleep(1000);
				}
			} catch (Exception e) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						doWeLink();
					}
				});
			}
		}
	}

	private class WeLinkReader implements Runnable {
		private boolean isRun;
		private InputStream myIs = null;
		byte[] bufImg;
		private int receiveImgNum = 0;

		public WeLinkReader(InputStream is) {
			isRun = true;
			myIs = is;
			isFirstReceiveImg = true;
			receiveImgNum = 0;
		}

		public void destroy() {
			isFirstReceiveImg = true;
			isRun = false;
			receiveImgNum = 0;
			if (myIs != null) {
				try {
					myIs.close();
				} catch (Exception e) {
				}
				myIs = null;
			}
		}

		@Override
		public void run() {
			if (myIs == null)
				return;
			if (!isRun)
				return;
			if (!isRunning)
				return;
			try {
				
				while (isRun && isRunning) {
				
					byte[] bHeader = readBuffer(myIs, 4);// 车机端的6803读取数据

					if (bHeader != null && bHeader.length == 4) {
						if ((bHeader[0] == 'W') && (bHeader[1] == 'L')) {
							int iType = bHeader[2];
							int iHdrLen = bHeader[3];
							byte[] bSize = readBuffer(myIs, 4);
					
							if (iHdrLen > 0)
								readBuffer(myIs, iHdrLen);

							if (bSize != null && bSize.length == 4) {
								int nLen = toInt(bSize, 0) - iHdrLen;
								if (nLen > 0) {
									bufImg = readBuffer(myIs, nLen);
									if (bufImg != null && bufImg.length == nLen) {
										if (iType == 6) {

											mWLType = 6;
											onVedioFrameChanged(bufImg, nLen);
										} else if (iType == 0) {

											mWLType = 0;
											mImgBytes = bufImg;
											mImgIndex++;
										}
										if (iType == 0) {
											if (isFirstReceiveImg) {
												isFirstReceiveImg = false;
											}
										} else if (iType == 6) {
											if (isFirstReceiveImg) {
												if (receiveImgNum == 10) {
													isFirstReceiveImg = false;
												}
												receiveImgNum++;
											}
										}
									}
								}
								mFpsDataSize += 4 + iHdrLen + nLen;
							}
						}
					}
					
					Thread.sleep(2);

				}
			} catch (Exception e) {
				receiveImgNum = 0;
			}
		}
	}

	private byte[] readBuffer(InputStream mIs, int nSize) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			if (nSize > 0) {
				int iLen = 0;
				byte[] baBuf = new byte[nSize];
				while ((baos.size() < nSize)
						&& ((iLen = mIs.read(baBuf, 0,
								baBuf.length - baos.size())) != -1)) {
					if (iLen > 0)
						baos.write(baBuf, 0, iLen);
				}
				if (iLen == -1) {
					try {
						Thread.sleep(1000);
					} catch (Exception ex) {
					}
					mHandler.post(new Runnable() {
						@Override
						public void run() {
//							 startChannel();
						}
					});
				}
			}
		} catch (Exception e) {
		}

		return baos.toByteArray();
	}

	private int toInt(byte[] bytes, int offset) {
		return ((bytes[offset] & 0xff) << 24)
				+ ((bytes[offset + 1] & 0xff) << 16)
				+ ((bytes[offset + 2] & 0xff) << 8)
				+ (bytes[offset + 3] & 0xff);
	}

	private void onVedioFrameChanged(byte[] buffers, int len)// 视频流
	{
		// TODO 视频流
		mLocalHelperListener.onBufferChanged(buffers, len);
	}

	private void runForCreateBitmap() // 创建图片
	{
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (isRunning) {
					try {
						if (mImgBytes != null) {
							if (mBitmapIndex != mImgIndex) {
								if (mImgIndex - mBitmapIndex > 1) {
									mLoseFrame += (mImgIndex - mBitmapIndex - 1);
								}
								mBitmapIndex = mImgIndex;

								System.arraycopy(hdrDefault, 0, mImgBytes, 0,
										hdrDefault.length);

								if (mImgBytes != null) {
									onScreenChanged(mImgBytes);
								}

							}
						}
					} catch (Exception e) {
					}
					try {
						Thread.sleep(10);
					} catch (Exception e) {
					}
				}
				Log.e("0000","jiexi tu pian liu  xian cheng wancheng 0000000000000000000");
			}
		}, "CreateBitmapThread").start();
	}

	public void setCodecMode(int mode) {
		if(this.mcodeMode == mode){
			return;
		}
		this.mcodeMode = mode;
		setHuSize();
		// TODO Auto-generated method stub
		
	}

	

}
