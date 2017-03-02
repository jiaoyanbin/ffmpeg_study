package com.android.welink.mu.test.hucontroller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Vector;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.android.welink.mu.test.tool.QRCodeUtil;
import com.wedrive.android.welink.control.VarHelper;

public class WlCmdController {
	/**
	 * 说明：构造方法初始化调用所调用的方法有 cancelWlCmd()和startCommon
	 * 
	 */
	private Context mcontex;
	
	private final static int WL_PORT = 6807;
	private String WL_SERVER_IP = "localhost";
	private final static int TIMEOUT = 1000;
	
	private Socket wlCmdSocket = null;//链接6807
	private OutputStream wlCmdOS = null;
	private InputStream wlCmdIs = null;

	
	
	private WLCmdReader mWLCmdReader;
	private VarHelper mVarHelper;
	
	
	private WlCmdListener mWlCmdListener;
	
	private boolean isRunning = false;
	public WlCmdController(Context context) {
		mcontex = context;
		mVarHelper = VarHelper.getInstance(mcontex);
		
	}
	public WlCmdController(Context mContext, WlCmdListener listener) {
		Log.e("0000", "WlCmdController new ");
		this.mcontex = mContext;
		mVarHelper = VarHelper.getInstance(mcontex);
		this.mWlCmdListener = listener;
		
	}
	public void performCmd(String str, boolean result) {
		
		performCmd(str, result, true);
	}

	public void start() {
		if(isRunning){
			return;
		}
		Log.e("0000"," chong lian wlCmd");
		cancelWLCmd();
		startWlCmdConnect();
		isRunning = true;
	}

	/**
	 * 开启新的线程初始化wlCmdSocket,wlCmdIs,wlCmdOs,mwlCmdReadler
	 * 开启线程读取wlCmd服务输出的内容
	 */
	private void startWlCmdConnect() {
		new Thread(new Runnable() {
			public void run() {
				try {
					wlCmdSocket = new Socket();
					wlCmdSocket.connect(
							new InetSocketAddress(WL_SERVER_IP, WL_PORT),
							TIMEOUT);
					wlCmdSocket.setReuseAddress(true);
					wlCmdSocket.setSoLinger(true, 0);
					wlCmdIs = wlCmdSocket.getInputStream();
					wlCmdOS = wlCmdSocket.getOutputStream();

					if (mWLCmdReader != null) {
						mWLCmdReader.destroy();
						mWLCmdReader = null;
					}
					Log.e("0000", "wlcmd connect success ");
					mWLCmdReader = new WLCmdReader();
					new Thread(mWLCmdReader, "WLCmdReaderThread").start();

				} catch (Exception e) {
					isRunning = false;
					e.printStackTrace();
					try {
						Thread.sleep(1000);
					} catch (Exception ex) {
					}
				}
			}
		}, "doWLCmdThread").start();
	}



	public void cancelWLCmd() {
		isRunning = false;
		if (mWLCmdReader != null) {
			mWLCmdReader.destroy();
			mWLCmdReader = null;
		}
		if (wlCmdIs != null) {
			try {
				wlCmdIs.close();
			} catch (Exception e) {
			}
			wlCmdIs = null;
		}
		if (wlCmdOS != null) {
			try {
				wlCmdOS.close();
			} catch (Exception e) {
			}
			wlCmdOS = null;
		}
		if (wlCmdSocket != null) {
			try {
				wlCmdSocket.close();
			} catch (Exception e) {
			}
			wlCmdSocket = null;
		}
	}
	
	protected void cancelCmd() {
		cancelWLCmd();
	}

	

	private class WLCmdReader implements Runnable {
		private boolean isRun;

		public WLCmdReader() {
			isRun = true;
		}

		public void destroy() {
			isRun = false;
		}

		private boolean isActivited() {
			if (!isRun)
				return false;
			return true;
		}

		public void run() {
			if (!isActivited())
				return;
			try {
				String strCmd = null;
				ByteArrayOutputStream baos = null;
				while (isActivited()) {
					byte[] bHeader = readBuffer(8);
					if (bHeader != null && bHeader.length == 8) {
						if ((bHeader[0] == 'W') && (bHeader[1] == 'L')) {

							int iType = bHeader[2];
							int iHdrLen = bHeader[3];
							if (iHdrLen > 0)
								readBuffer(iHdrLen);
							if (iType == 101) {
								baos = new ByteArrayOutputStream();
							} else if (iType == 103) {
								if (baos != null) {
									String str = new String(baos.toByteArray());
									// {
									Vector<String> results = new Vector<String>();
									String[] arrs = str.split("\n");
									int len = arrs.length;
									for (int j = 0; j < len; j++)
										results.add(arrs[j]);
									onWLResult(strCmd, results);
									// }
									baos.close();
									baos = null;
								}
								// strCmd = null;
							}
							int nLen = toInt(bHeader, 4);
							if (nLen > 0) {
								byte[] bufData = readBuffer(nLen);
								if (iType == 102 && baos != null)
									baos.write(bufData);
								else if (iType == 101) {
									strCmd = new String(bufData);

								}
							}
						}
					}

					Thread.sleep(1);
				}
			} catch (Exception e) {
				e.printStackTrace();
				cancelCmd();
			}
		}
	}

	/**
	 * 指令执行回调
	 * 
	 * @param cmd
	 *            执行的指令
	 * @param results
	 *            执行结果
	 */
	private synchronized void onWLResult(String cmd, Vector<String> results) {
		Log.e("0000", "wlcmd  cmd ==  "+cmd);
		if(cmd.equals(mVarHelper.getAdbDumpsysGetScreen() + mVarHelper.getAdbAllInfo())){

			int size = results.size();
			for(int i = 0; i < size; i++)
			{
				String str = results.get(i);
				if(TextUtils.isEmpty(str))
					continue;

				if(str.contains("cur=")){
                    str = str.substring(str.indexOf("cur=")+4, str.length());
                    String[] data = str.substring(0, str.indexOf(" ")).trim().split("x");
                    if(data.length == 2){
                        int width = Integer.parseInt(data[0])>Integer.parseInt(data[1])?Integer.parseInt(data[0]):Integer.parseInt(data[1]);
                        int height = Integer.parseInt(data[0])<Integer.parseInt(data[1])?Integer.parseInt(data[0]):Integer.parseInt(data[1]);
						QRCodeUtil.realWidth=width;
						QRCodeUtil.realHeight = height;
						break;
                    }
				}
			}
	
		}

		
		
	}


	/**
	 * 执行wlCmd命令
	 * @param cmd
	 * @param result
	 * @param isShell
	 */
	private void performCmd(String cmd, boolean result, boolean isShell) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			String newCmd = cmd;
			if (isShell) {
				if (result)
//					newCmd = "adb shell " + cmd;
					newCmd = mVarHelper.getWlMarkAdb()+" "+mVarHelper.getAdbShell()+" "+cmd;
				else
//					newCmd = "adb shell2 " + cmd;
					newCmd = mVarHelper.getWlMarkAdb()+" "+mVarHelper.getAdbShell()+"2 "+cmd;
			}

			Log.e("0000", "newCmd = "+newCmd);
			byte[] bytes = newCmd.getBytes();
			baos.write('W');
			baos.write('L');
			baos.write(109); // 109-adb shell命令, 110-写文件
			baos.write(0);
			baos.write(to4bytes(bytes.length));
			baos.write(bytes);
			writeBytes(baos.toByteArray());
		} catch (Exception e) {
		} finally {
			try {
				baos.close();
			} catch (Exception e) {
			}
			baos = null;
		}
	}

	public final static byte[] to4bytes(int intValue) {
		byte[] bytes = { (byte) ((intValue & 0xff000000) >> 24),
				(byte) ((intValue & 0x00ff0000) >> 16),
				(byte) ((intValue & 0x0000ff00) >> 8),
				(byte) (intValue & 0x000000ff) };
		return bytes;
	}

	/**
	 * 向小服务中写入字节流
	 * @param buffer
	 */
	private synchronized void writeBytes(byte[] buffer) {
		try {
			if (wlCmdOS != null) {
				wlCmdOS.write(buffer);
				wlCmdOS.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * WLCmdReader 线程中调用
	 * @param nSize
	 * @return
	 */
	private byte[] readBuffer(int nSize) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (nSize > 0) {
				int iLen = 0;
				byte[] baBuf = new byte[nSize];
				while ((baos.size() < nSize)
						&& ((iLen = wlCmdIs.read(baBuf, 0,
								baBuf.length - baos.size())) != -1)) {
					if (iLen > 0)
						baos.write(baBuf, 0, iLen);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return baos.toByteArray();
	}
	
	/**
	 * WLCmdReader 线程中调用
	 * @param nSize
	 * @return
	 */
	private int toInt(byte[] bytes, int offset) {
		return ((bytes[offset] & 0xff) << 24)
				+ ((bytes[offset + 1] & 0xff) << 16)
				+ ((bytes[offset + 2] & 0xff) << 8)
				+ (bytes[offset + 3] & 0xff);
	}

	



	public void writeTxtToFiles(String strcontent) {
		File path = Environment.getExternalStorageDirectory();
		String strFilePath = path.toString() + "/Test/case.txt";
		String strContent = strcontent + "\r\n";
		try {
			File file = new File(strFilePath);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rwd");
			raf.seek(file.length());
			raf.write(strContent.getBytes());
			raf.close();
		} catch (Exception localException) {
		}
	}

}
