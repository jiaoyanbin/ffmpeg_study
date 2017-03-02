package com.android.welink.mu.test.bean;

public class DeviceInfo
{
	/**
	 * 值为API获取的屏幕宽高
	 */
	private int phoneHeight; 
	
	private int phoneWidth;
	
	/**
	 * 值为wlCmd获取的手机硬件屏幕的宽高
	 */
	private int curHeight;
	private int curWidth;
	
	private String imei = "";
	private String wifiMac = "";
	private String sdcardId = "";
	private int versionCode = 0;
	private String btMac = "";
	
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getWifiMac() {
		return wifiMac;
	}
	public void setWifiMac(String wifiMac) {
		this.wifiMac = wifiMac;
	}
	public String getSdcardId() {
		return sdcardId;
	}
	public void setSdcardId(String sdcardId) {
		this.sdcardId = sdcardId;
	}
	public int getVersionCode() {
		return versionCode;
	}
	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	public String getBtMac() {
		return btMac;
	}
	public void setBtMac(String btMac) {
		this.btMac = btMac;
	}
	
	public int getPhoneHeight() {
		return phoneHeight;
	}
	public void setPhoneHeight(int phoneHeight) {
		this.phoneHeight = phoneHeight;
	}
	public int getPhoneWidth() {
		return phoneWidth;
	}
	public void setPhoneWidth(int phoneWidth) {
		this.phoneWidth = phoneWidth;
	}
	public int getCurHeight() {
		return curHeight;
	}
	public void setCurHeight(int curHeight) {
		this.curHeight = curHeight;
	}
	public int getCurWidth() {
		return curWidth;
	}
	public void setCurWidth(int curWidth) {
		this.curWidth = curWidth;
	}
}
