package com.android.welink.mu.test.tool;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public final class JsonStringUtil
{
	public final static String toReDeviceInfo(int w, int h)
	{
		String str = "";
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink_SDK");
			jsonObject.put("version", 0);
			JSONObject command = new JSONObject();
			command.put("method", "result");
			JSONObject extData = new JSONObject();
			extData.put("methodName", "onFoundWifiDevice");
			extData.put("width", w);
			extData.put("height", h);
			command.put("extData", extData);
			jsonObject.put("command", command);
			str = jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return str;
	}
	
	public final static String toRequestWeLinkInfo()
	{
		String str = "";
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink_SDK");
			jsonObject.put("version", 0);
			JSONObject command = new JSONObject();
			command.put("method", "requestWeLink");
			JSONObject extData = new JSONObject();
			extData.put("btAddress", "asadfagfsdgasdgsdgad");
			extData.put("imei", "0000000000000000");
			command.put("extData", extData);
			jsonObject.put("command", command);
			str = jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return str;
	}
	
	public final static String toHeartbeatInfo()
	{
		String str = "";
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink_SDK");
			jsonObject.put("version", 0);
			jsonObject.put("platform", "android");
			JSONObject command = new JSONObject();
			command.put("method", "onHeartbeat");
			JSONObject extData = new JSONObject();
			command.put("extData", extData);
			jsonObject.put("command", command);
			str = jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return str;
	}
	
	public final static String toRequestWeLinkInfo(String IMEI, 
			String Bluettot_mac, String WIFI_MAC, String SDCARD_ID, int VERSION)
	{
		String str = "";
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink_SDK");
			JSONObject command = new JSONObject();
			command.put("method", "requestWeLink");
			JSONObject extData = new JSONObject();
			extData.put("bluetooth_add", Bluettot_mac);
			extData.put("imei", IMEI);
			extData.put("mac", "0000000000fc");//WIFI_MAC
			extData.put("sdcard_id", SDCARD_ID);
			extData.put("version", VERSION);
			extData.put("osVersion", android.os.Build.VERSION.RELEASE);
			command.put("extData", extData);
			jsonObject.put("command", command);
			str = jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return str;
	}
	
	/**
	 * WeLink互联响应请求
	 * @param code
	 * @param auth_time
	 * @return
	 */
	public static String toResponseWeLinkInfo(int codecMode,int state, int errCode, boolean hasNewVers,String displayCode,int responseCode)
	{
		String str = "";
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("moduleName", "WeLink_SDK");
			jsonObject.put("version", 0);
			jsonObject.put("platform", "android");
			JSONObject command = new JSONObject();
			command.put("method", "responseWeLink");
			JSONObject extData = new JSONObject();
			//授权服务器返回状态
//			if(code==200){
				extData.put("state", state);
				extData.put("errCode", errCode);
				extData.put("responseCode",responseCode);
				extData.put("displayCode",displayCode);
//			}else{
//				extData.put("state", 0);
//			}
//			if(code==3000){
//				extData.put("errCode", 3);
//			}else if(code==4000){
//				extData.put("errCode", 1);
//			}else if(code==5000||code==6000){
//				extData.put("errCode", 4);
//			}
			extData.put("codecMode", codecMode);
			//版本控制
			if(hasNewVers){
				extData.put("hasNewVers", hasNewVers);
				JSONObject verData = new JSONObject();
				JSONObject wlVerData = new JSONObject();
				wlVerData.put("version", "versions");
				wlVerData.put("path", "path");
				verData.put("wlVer", wlVerData);
				JSONObject huVerData = new JSONObject();
				huVerData.put("version", "versions");
				huVerData.put("path", "path");
				verData.put("huVer", huVerData);
				JSONObject muVerData = new JSONObject();
				muVerData.put("version", "versions");
				muVerData.put("path", "path");
				verData.put("muVer", muVerData);
				extData.put("vers", verData);
			}else{
				extData.put("hasNewVers", hasNewVers);
			}
			command.put("extData", extData);
			jsonObject.put("command", command);
			str = jsonObject.toString();
		} catch (JSONException e) {
		}
		return str;
	}
	
	public final static String toExitWeLinkInfo()
	{
		String str = "";
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink");
			jsonObject.put("version", 0);
			JSONObject command = new JSONObject();
			command.put("method", "onExitWelink");
			JSONObject extData = new JSONObject();
			command.put("extData", extData);
			jsonObject.put("command", command);
			str = jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return str;
	}
	
	public final static String toRestartWelink()
	{
		String str = "";
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink_SDK");
			jsonObject.put("version", 0);
			JSONObject command = new JSONObject();
			command.put("method", "onRestartWelink");
			JSONObject extData = new JSONObject();
			command.put("extData", extData);
			jsonObject.put("command", command);
			str = jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return str;
	}
	public final static String toLimitWeLinkInfo(String packName)
	{
		String str = "";
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink");
			jsonObject.put("version", 0);
			JSONObject command = new JSONObject();
			command.put("method", "onLimitWelink");
			JSONObject extData = new JSONObject();
			command.put("extData", extData);
			extData.put("packageName", packName);
			jsonObject.put("command", command);
			str = jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return str;
	}
	
	public final static String toResumeWeLinkInfo(String packageName)
	{
		String str = "";
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink");
			jsonObject.put("version", 0);
			JSONObject command = new JSONObject();
			command.put("method", "onResumeWelink");
			JSONObject extData = new JSONObject();
			command.put("extData", extData);
			extData.put("packageName", packageName);
			jsonObject.put("command", command);
			str = jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 手机端发起握手
	 * @return
	 */
	public static String toReadyInfo(int w, int h)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink_SDK");
			jsonObject.put("version", 0);
			jsonObject.put("platform", "android");
			JSONObject command = new JSONObject();
			command.put("method", "onMuReady");
			JSONObject extData = new JSONObject();
			extData.put("width", w);
			extData.put("height", h);
			command.put("extData", extData);
			jsonObject.put("command", command);
			return jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * 执行指令定义
	 */
	public static String toCommand(int type, int responseFlag, String key, List<String> list)
	{
		return toCommand(type, responseFlag, key, null, list);
	}
	
	/**
	 * 执行指令定义
	 */
	public static String toCommand(int type, int responseFlag, String key, String mark, List<String> list)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink_SDK");
			jsonObject.put("version", 0);
			jsonObject.put("platform", "android");
			JSONObject command = new JSONObject();
			command.put("method", "onCommand");
			JSONObject extData = new JSONObject();
			extData.put("type", type);
			extData.put("responseFlag", responseFlag);
			extData.put("key", key);
			if(!TextUtils.isEmpty(mark))
				extData.put("mark", mark);
			JSONArray commslists = new JSONArray();
			int size = list.size();
			for(int i=0;i<size;i++){
				String str = list.get(i);
				commslists.put(str);
			}
			extData.put("comms", commslists);
			command.put("extData", extData);
			jsonObject.put("command", command);

			return jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	
	public static String toMuChannelUpData(Map<String,String> map)
	{
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink");
			jsonObject.put("version", 0);
			jsonObject.put("platform", "android");
			JSONObject command = new JSONObject();
			command.put("method", "onMuUpdata");
			JSONObject extData = new JSONObject();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = it.next();
				extData.put(entry.getKey().toString(), entry.getValue());
			}
			command.put("extData", extData);
			jsonObject.put("command", command);
			return jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	public static String toMuAuthorizelData(String json)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink");
			jsonObject.put("version", 0);
			jsonObject.put("platform", "android");
			JSONObject command = new JSONObject();
			command.put("method", "onMuAuthData");
			JSONObject extData = new JSONObject();
			extData.put("result", json);
			command.put("extData", extData);
			jsonObject.put("command", command);
			return jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	public static String toMuScreenProjectionLimitData(boolean json)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink");
			jsonObject.put("version", 0);
			jsonObject.put("platform", "android");
			JSONObject command = new JSONObject();
			command.put("method", "onMuScreenData");
			JSONObject extData = new JSONObject();
			extData.put("result", json);
			command.put("extData", extData);
			jsonObject.put("command", command);
			return jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * 手机端给车机端发当前应用包名
	 * @return
	 */
	public static String toCurActivity(String packageName)
	{
		JSONObject jsonObject = new JSONObject();
		try
		{
			jsonObject.put("moduleName", "WeLink_SDK");
			jsonObject.put("version", 0);
			jsonObject.put("platform", "android");
			JSONObject command = new JSONObject();
			command.put("method", "result");
			JSONObject extData = new JSONObject();
			extData.put("methodName", "getCurActivity");
			extData.put("packageName", packageName);
			extData.put("orientation", 0);
			command.put("extData", extData);
			jsonObject.put("command", command);
			return jsonObject.toString();
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	/**
	 * WeLink更新请求
	 * @param huVersion
	 * @param sdkVersion
	 * @param index 1:发送车机apk版本信息   
	 * @return
	 */
	
	public static String toResponseWeLinkUpdate(String huVersion, String sdkVersion,int index,String packageName) {
		JSONObject jsonObject  = null;
		try {
			jsonObject = new JSONObject();
			jsonObject.put("index", index);
			jsonObject.put("huVersion", huVersion);
			jsonObject.put("sdkVersion", sdkVersion);
			jsonObject.put("packageName", packageName);
		} catch (Exception e) {
			
		}
		return getJsonString(jsonObject, "responseWeLinkUpdate");
	}
	
	/**
	 * 得到一个向手机端发送信息的json数据
	 * 
	 * @param jsonObj  附带的extData 数据
	 * @param method   
	 * @return
	 */
	public static String getJsonString(JSONObject jsonObj, String method) {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("moduleName", "WeLink");
			jsonObject.put("version", 0);
			jsonObject.put("platform", "android");
			JSONObject command = new JSONObject();
			command.put("method", method);
			command.put("extData", jsonObj);
			jsonObject.put("command", command);
			return jsonObject.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
