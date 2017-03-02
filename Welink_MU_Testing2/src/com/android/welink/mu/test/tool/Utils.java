package com.android.welink.mu.test.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.TextUtils;

public final class Utils {
	public static boolean isWriteFile = false;

	public final static int[] formatVersion(String version) {
		int[] arrInts = new int[] { 0, 0, 0 };
		if (!TextUtils.isEmpty(version)) {
			try {
				String tmpVer = version.toLowerCase(Locale.ENGLISH);
				if (tmpVer.indexOf("_v") != -1) {
					String[] arrs = tmpVer.split("_v");
					tmpVer = arrs[arrs.length - 1];
				} else if (tmpVer.indexOf("v") != -1) {
					String[] arrs = tmpVer.split("v");
					tmpVer = arrs[arrs.length - 1];
				}

				String[] arrs = tmpVer.split("\\.");
				int len = arrs.length;
				if (len > 2) {
					arrInts[0] = Integer.parseInt(arrs[0]);
					arrInts[1] = Integer.parseInt(arrs[1]);
					arrInts[2] = Integer.parseInt(arrs[2]);
				} else if (len > 1) {
					arrInts[0] = Integer.parseInt(arrs[0]);
					arrInts[1] = Integer.parseInt(arrs[1]);
				} else {
					arrInts[0] = Integer.parseInt(arrs[0]);
				}
			} catch (Exception e) {
			}
		}
		return arrInts;
	}

	public final static boolean checkVersion(String oldVer, String newVer) {
		int[] oldArrInts = formatVersion(oldVer);
		int[] newArrInts = formatVersion(newVer);
		if (newArrInts[0] > oldArrInts[0])
			return true;
		else if (newArrInts[0] < oldArrInts[0])
			return false;
		if (newArrInts[1] > oldArrInts[1])
			return true;
		else if (newArrInts[1] < oldArrInts[1])
			return false;
		if (newArrInts[2] > oldArrInts[2])
			return true;
		return false;
	}

	/*
	public final static void writeTxtToFile(String strcontent) {
		writeTxtToFile(strcontent, "/Test/log.txt");
	}

	public final static void writeTxtToFile(String strcontent, String logPath) {
		writeTxtToFile(strcontent, logPath, isWriteFile);
	}

	public final static void writeTxtToFile(String strcontent, String logPath,
			boolean isDebug) {
		String strContent = strcontent + "\r\n";
		writeTxtToFile(strContent.getBytes(), logPath, isDebug);
	}

	public final static void writeTxtToFile(byte[] buffer) {
		writeTxtToFile(buffer, "/Test/log.txt");
	}

	public final static void writeTxtToFile(byte[] buffer, String logPath) {
		writeTxtToFile(buffer, logPath, isWriteFile);
	}

	public final static void writeTxtToFile(byte[] buffer, String logPath,
			boolean isDebug) {
		if (!isDebug) {
			return;
		}
		File path = Environment.getExternalStorageDirectory();
		String strFilePath = path.toString() + logPath;

		try {
			File file = new File(strFilePath);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rwd");
			raf.seek(file.length());
			raf.write(buffer);
			raf.close();
		} catch (Exception localException) {
		}
	}
	*/

	public final static byte[] to4bytes(int intValue) {
		byte[] bytes = { (byte) ((intValue & 0xff000000) >> 24),
				(byte) ((intValue & 0x00ff0000) >> 16),
				(byte) ((intValue & 0x0000ff00) >> 8),
				(byte) (intValue & 0x000000ff) };
		return bytes;
	}

	public final static int toInt(byte[] bytes, int offset) {
		return ((bytes[offset] & 0xff) << 24)
				+ ((bytes[offset + 1] & 0xff) << 16)
				+ ((bytes[offset + 2] & 0xff) << 8)
				+ (bytes[offset + 3] & 0xff);
	}

	public final static int formatInt(String str, int def) {
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
		}
		return def;
	}

	public final static int formatCodecModec(int sdkCode)
	{
		if(sdkCode == 18 || sdkCode == 19 || sdkCode == 21 || sdkCode == 22)
		{
			return 0x02;
		}
		return 0x01;
	}

	/**
	 * 获取android版本
	 * @return
	 */
	public final static int getAndroidOSVersion() {
		int osVersion;
		try {
			osVersion = android.os.Build.VERSION.SDK_INT;
		} catch (NumberFormatException e) {
			osVersion = 0;
		}
		return osVersion;
	}

	/**
	 * 获取手机品牌
	 * @return
	 */
	public final static String getPhoneManufacturer() {
		String Manufacturer = android.os.Build.BRAND;
		return Manufacturer;
	}
	
	/**
	 * 获取手机型号
	 * @return
	 */
	public final static String getPhoneModel() {
		String Model = android.os.Build.MODEL;
		return Model;
	}
	
	public final static String formatSdkCode(int sdkCode) {
		//
		// if
		// (android.os.Build.MODEL.equals("HM NOTE 1TD")&&android.os.Build.MANUFACTURER.equals("Xiaomi"))
		// {
		// return "hm_" + sdkCode;
		// }
		if (sdkCode == 19) {
			String codeName = android.os.Build.VERSION.RELEASE;
			if (codeName.equals("4.4.2"))
				return sdkCode + "_" + codeName.replaceAll("\\.", "_");
		}
		return "" + sdkCode;
	}

	// 缓存
	private static final int BUFF_SIZE = 1024 * 10; // 10 Byte

	// 解压文件
	public final static boolean upZipFile(File zipFile, String folderPath,
			String targetName) throws ZipException, IOException {
		boolean bSuccess = true;
		File desDir = new File(folderPath);
		if (!desDir.exists()) {
			desDir.mkdirs();
		}
		ZipFile zf = new ZipFile(zipFile);

		for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements();) {
			ZipEntry entry = ((ZipEntry) entries.nextElement());

			if (!entry.getName().equals(targetName))
				continue;

			InputStream in = zf.getInputStream(entry);
			in = new CheckedInputStream(in, new CRC32());

			long zipCrc = entry.getCrc();

			String str = folderPath + File.separator + entry.getName();
			{

				File desFile = new File(str);
				if (desFile.exists()) {
					int len = in.available();
					long fileLen = desFile.length();
					if (len == fileLen) {
						CheckedInputStream cis = null;
						try {
							cis = new CheckedInputStream(new FileInputStream(
									desFile), new CRC32());
							byte[] buf = new byte[BUFF_SIZE];
							while (cis.read(buf) >= 0) {
							}

							long resCrc = cis.getChecksum().getValue();
							if (zipCrc == resCrc) {
								return true;
							}
						} catch (IOException e) {
						} finally {
							if (cis != null) {
								try {
									cis.close();
								} catch (Exception e) {
								}
								cis = null;
							}
						}
					} else {
						try {
							desFile.delete();
						} catch (Exception ex) {
							return false;
						}
					}
				}

				File tempFile = new File(str + ".tmp");

				if (tempFile.exists()) {
					try {
						tempFile.delete();
					} catch (Exception ex) {
					}
				} else {
					File fileParentDir = tempFile.getParentFile();
					if (!fileParentDir.exists()) {
						fileParentDir.mkdirs();
					}
					tempFile.createNewFile();
				}
				OutputStream out = new FileOutputStream(tempFile);
				byte buffer[] = new byte[BUFF_SIZE];
				int realLength;
				while ((realLength = in.read(buffer)) > 0) {
					out.write(buffer, 0, realLength);
				}
				long strCrc = ((CheckedInputStream) in).getChecksum()
						.getValue();
				in.close();
				out.close();

				// crc32校验失败
				if (strCrc != zipCrc) {
					// 删除
					tempFile.delete();
					bSuccess = false;
				} else {
					// 重命名
					tempFile.renameTo(desFile);
				}
			}
		}
		return bSuccess;
	}

	public final static String formatTime(long time) {
		Date date = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
		return dateFormat.format(date);
	}

	/**
	 * 判断是否有网
	 * 
	 * @return
	 */
	public final static boolean isNetwork(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
		for (int i = 0; i < networkInfo.length; i++) {
			if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
				return true;
			}
		}
		return false;
	}

	public final static boolean isWiFi(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getApplicationContext().getSystemService(
						Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				return true;
			}
		} else {
			return false;
		}
		return false;
	}

	/**
	 * 把一个字符串中的小写转换为大写
	 * 
	 */
	public static String strToUpperCase(String str){  
		StringBuffer sb = new StringBuffer();  
		if(str!=null){  
			for(int i=0;i<str.length();i++){  
				char c = str.charAt(i);  
			    if(Character.isLowerCase(c)){  
			    	sb.append(Character.toUpperCase(c));   
			    }else {
			        sb.append(c);
				} 
			}  
		}  
		return sb.toString();  
	}
	
	/**
	 * 把一个字符串中的大写转换为小写
	 * 
	 */
	public static String strToLowerCase(String str){  
		StringBuffer sb = new StringBuffer();  
		if(str!=null){  
			for(int i=0;i<str.length();i++){  
				char c = str.charAt(i);  
			    if(Character.isUpperCase(c)){  
			    	sb.append(Character.toLowerCase(c));   
			    }else {
			        sb.append(c);
				} 
			}  
		}  
		return sb.toString();  
	}
	
	/** 
	* 写入内容到SD卡文件中
	* str 为内容 
	* filePath 文件全路径
	* append 是否以追加形式写文件
	*/ 
	public static void writeSDFile(String str, String filePath, boolean append) {
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file,append);
			fw.write(str);
			fw.close();
		} catch (Exception e) {
		}
		
	}

	/** 
	* 读取SD卡中文本文件 
	* 
	* @param filePath 
	* @return 
	*/ 
	public static String readSDFile(String filePath) {
		String str = "";
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			return "";
		}
		try {  
            FileInputStream fis = new FileInputStream(file);  
            int len = fis.available();  
            byte[] buffer = new byte[len];  
            fis.read(buffer);  
            fis.close();  
            str=new String(buffer,"UTF-8") ;  
        } catch (Exception e) {  
        }  
  
		return str;
	}
	
}
