package com.jovision;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.test.JVACCOUNT;

import com.jovetech.CloudSee.temp.R;
import com.jovision.activities.BaseActivity;
import com.jovision.activities.JVOffLineDialogActivity;
import com.jovision.activities.JVTabActivity;
import com.jovision.bean.PushInfo;
import com.jovision.commons.JVAccountConst;
import com.jovision.commons.JVAlarmConst;
import com.jovision.commons.MyActivityManager;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.AlarmUtil;
import com.jovision.utils.DefaultExceptionHandler;

/**
 * 整个应用的入口，管理状态、活动集合，消息队列以及漏洞汇报
 * 
 * @author neo
 * 
 */
public class MainApplication extends Application implements IHandlerLikeNotify {

	public HashMap<String, String> statusHashMap;
	private ArrayList<BaseActivity> openedActivityList;
	private IHandlerLikeNotify currentNotifyer;

	protected NotificationManager mNotifyer;
	private ActivityManager activityManager;
	private String packageName;
	private Context context;

	/**
	 * 获取活动集合
	 * 
	 * @return
	 */
	public ArrayList<BaseActivity> getOpenedActivityList() {
		return openedActivityList;
	}

	/**
	 * 获取状态集合
	 * 
	 * @return
	 */
	public HashMap<String, String> getStatusHashMap() {
		return statusHashMap;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// // 开启服务
		context = this;
		// Intent intent = new Intent();
		// intent.setClass(this, MainService.class);
		// startService(intent);
		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(
				this));
		MyLog.init(Consts.LOG_PATH);
		MyLog.enableFile(false);
		MyLog.enableLogcat(false);
		statusHashMap = new HashMap<String, String>();
		openedActivityList = new ArrayList<BaseActivity>();

		currentNotifyer = null;

		String strAlarmFilePath = Consts.SD_CARD_PATH + "CSAlarmIMG"
				+ File.separator;
		File file = new File(strAlarmFilePath);
		if (!file.exists())
			file.mkdir();

		activityManager = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);
		packageName = this.getPackageName();
	}

	/**
	 * 底层所有的回调接口，将代替以下回调
	 * <p>
	 * {@link JVACCOUNT#JVOnLineCallBack(int)} <br>
	 * {@link JVACCOUNT#JVPushCallBack(int, String, String, String)}<br>
	 * {@link JVSUDT#enqueueMessage(int, int, int, int)}<br>
	 * {@link JVSUDT#saveCaptureCallBack(int, int, int)}<br>
	 * {@link JVSUDT#ConnectChange(String, byte, int)}<br>
	 * {@link JVSUDT#NormalData(byte, int, int, int, int, double, int, int, int, int, byte[], int)}
	 * <br>
	 * {@link JVSUDT#m_pfLANSData(String, int, int, int, int, int, boolean, int, int, String)}
	 * <br>
	 * {@link JVSUDT#CheckResult(int, byte[], int)}<br>
	 * {@link JVSUDT#ChatData(int, byte, byte[], int)}<br>
	 * {@link JVSUDT#TextData(int, byte, byte[], int, int)}<br>
	 * {@link JVSUDT#PlayData(int, byte, byte[], int, int, int, int, double, int, int, int)}
	 * 
	 * @param what
	 *            分类
	 * @param arg1
	 *            参数1
	 * @param arg2
	 *            参数2
	 * @param obj
	 *            附加对象
	 */
	public synchronized void onJniNotify(int what, int uchType, int channel,
			Object obj) {
		if (null != currentNotifyer) {
			currentNotifyer.onNotify(what, uchType, channel, obj);
		}
	}

	@Override
	public void onNotify(int what, int arg1, int arg2, Object obj) {
		if (null != currentNotifyer) {
			currentNotifyer.onNotify(what, arg1, arg2, obj);
		}
	}

	/**
	 * 修改当前显示的 Activity 引用
	 * 
	 * @param currentNotifyer
	 */
	public void setCurrentNotifyer(IHandlerLikeNotify currentNotifyer) {
		this.currentNotifyer = currentNotifyer;
	}

	public static int errorCount = 0;

	/**
	 * 7-保持在线的回调函数
	 * 
	 * @param res
	 */
	public void JVOnLineCallBack(int res) {
		MyLog.v("JVOnLineCallBack", "res=" + res);
		try {
			if (res == 0) {// 保持在线成功
				errorCount = 0;
			} else {// 保持在线失败
				errorCount++;
				if (4 == errorCount) {// 失败4次
					JVACCOUNT.StopHeartBeat();// 先停止心跳
					Intent intent = new Intent(getApplicationContext(),
							JVOffLineDialogActivity.class);
					intent.putExtra("ErrorCode", 4);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 8-推送的回调函数
	 * 
	 * @param res
	 * @param userName
	 * @param time
	 * @param msg
	 */

	@SuppressWarnings("deprecation")
	public void JVPushCallBack(int res, String userName, String time, String msg) {
		try {
			MyLog.v("JVPushCallBack", "res=" + res + ";time=" + time + ";msg="
					+ msg);
			if (JVAccountConst.MESSAGE_PUSH_TAG == res) {
				if (MySharedPreference.getBoolean("AlarmSwitch", false)) {
					if (null != currentNotifyer) {
						if (null != msg && !"".equalsIgnoreCase(msg)) {
							JSONObject obj = new JSONObject(msg);
							String arrayStr = statusHashMap
									.get(Consts.PUSH_JSONARRAY);
							JSONArray pushArray = null;
							if (null == arrayStr
									|| "".equalsIgnoreCase(arrayStr)) {
								pushArray = new JSONArray();
							} else {
								pushArray = new JSONArray(arrayStr);
							}
							pushArray.put(obj);
							statusHashMap.put(Consts.PUSH_JSONARRAY,
									pushArray.toString());

							String[] alarmArray = getResources()
									.getStringArray(R.array.alarm_type);

							String ns = Context.NOTIFICATION_SERVICE;
							mNotifyer = (NotificationManager) getSystemService(ns);
							// 定义通知栏展现的内容信息
							int icon = R.drawable.notification_icon;
							CharSequence tickerText = getResources().getString(
									R.string.str_alarm);
							long when = System.currentTimeMillis();
							Notification notification = new Notification(icon,
									tickerText, when);

							notification.defaults |= Notification.DEFAULT_SOUND;// 声音
							// notification.defaults |=
							// Notification.DEFAULT_LIGHTS;//灯
							// notification.defaults |=
							// Notification.DEFAULT_VIBRATE;//震动

							// 定义下拉通知栏时要展现的内容信息
							Context context = this;

							CharSequence contentText = obj
									.optString(JVAlarmConst.JK_ALARM_ALARMTIME
											+ "-"
											+ alarmArray[obj
													.optInt(JVAlarmConst.JK_ALARM_ALARMTYPE)].replace(
													"%%",
													obj.optString(JVAlarmConst.JK_ALARM_CLOUDNAME)));

							CharSequence contentTitle = getResources()
									.getString(R.string.str_alarm_info);
							// CharSequence contentText = pushMessage;

							Intent notificationIntent = new Intent(this,
									JVTabActivity.class);
							notificationIntent.putExtra("tabIndex", 1);

							PendingIntent contentIntent = PendingIntent
									.getActivity(this, 0, notificationIntent,
											PendingIntent.FLAG_UPDATE_CURRENT);
							notification.setLatestEventInfo(context,
									contentTitle, contentText, contentIntent);

							// 用mNotificationManager的notify方法通知用户生成标题栏消息通知
							mNotifyer.notify(0, notification);
						}

					}
				}
			} else if (JVAccountConst.MESSAGE_NEW_PUSH_TAG == res) {// 新报警协议推送信息
				/**
				 * message_type：4604 res----:4604;;time----:;;msg----:
				 * {"p2rmt":4604,"mt":2219,"username":"18254152812_p",
				 * "aguid":"4e01e9916109c821af63fe5a0195275a"
				 * ,"dguid":"S90252170","dname":"HD IPC",
				 * "dcn":1,"atype":7,"ats":1413035825,"amt":0,
				 * "apic":"./rec/00/20141011/A01135705.jpg"
				 * ,"avd":"./rec/00/20141011/A01135705.mp4"}
				 * 
				 */
				String strYstNumString = "";
				// Toast.makeText(getApplicationContext(),
				// "new msg push call back", Toast.LENGTH_SHORT).show();
				if (MySharedPreference.getBoolean("AlarmSwitch", false)) {
					if (null != currentNotifyer && null != msg
							&& !"".equalsIgnoreCase(msg)) {

						try {
							JSONObject obj = new JSONObject(msg);
							String arrayStr = statusHashMap
									.get(Consts.PUSH_JSONARRAY);
							JSONArray pushArray = null;
							if (null == arrayStr
									|| "".equalsIgnoreCase(arrayStr)) {
								pushArray = new JSONArray();
							} else {
								pushArray = new JSONArray(arrayStr);
							}
							pushArray.put(obj);
							statusHashMap.put(Consts.PUSH_JSONARRAY,
									pushArray.toString());
							PushInfo pi = new PushInfo();
							pi.strGUID = obj
									.optString(JVAlarmConst.JK_ALARM_NEW_GUID);
							pi.ystNum = obj
									.optString(JVAlarmConst.JK_ALARM_NEW_CLOUDNUM);
							strYstNumString = pi.ystNum;
							pi.coonNum = obj
									.optInt(JVAlarmConst.JK_ALARM_NEW_CLOUDCHN);
							//
							// pi.deviceNickName =
							// BaseApp.getNikeName(pi.ystNum);
							pi.alarmType = obj
									.optInt(JVAlarmConst.JK_ALARM_NEW_ALARMTYPE);
							if (pi.alarmType == 7) {
								pi.deviceNickName = obj
										.optString(JVAlarmConst.JK_ALARM_NEW_CLOUDNAME);
							} else if (pi.alarmType == 11)// 第三方
							{
								pi.deviceNickName = obj
										.optString(JVAlarmConst.JK_ALARM_NEW_ALARM_THIRD_NICKNAME);
							} else {

							}

							pi.timestamp = obj
									.optString(JVAlarmConst.JK_ALARM_NEW_ALARMTIME);
							pi.alarmTime = AlarmUtil.getStrTime(pi.timestamp);

							pi.deviceName = obj
									.optString(JVAlarmConst.JK_ALARM_NEW_CLOUDNAME);
							pi.newTag = true;
							pi.pic = obj
									.optString(JVAlarmConst.JK_ALARM_NEW_PICURL);
							pi.messageTag = JVAccountConst.MESSAGE_NEW_PUSH_TAG;
							pi.video = obj
									.optString(JVAlarmConst.JK_ALARM_NEW_VIDEOURL);
							// BaseApp.pushList.add(0, pi);// 新消息置顶
							// BaseApp.pushHisCount++;

							if (isAppOnForeground()) {
								MyLog.v("PushCallBack",
										"the app is OnForeground.........");
								onNotify(Consts.PUSH_MESSAGE, pi.alarmType, 0,
										pi);
								Activity currentActivity = MyActivityManager
										.getActivityManager().currentActivity();
								MyLog.v("PushCallBack", "currentActivity:"
										+ currentActivity.toString());
							} else {

								MyLog.v("PushCallBack",
										"the app is not OnForeground.........");
								onNotify(Consts.PUSH_MESSAGE, pi.alarmType, 0,
										pi);
								Activity currentActivity = MyActivityManager
										.getActivityManager().currentActivity();
								if (MyActivityManager.getActivityManager()
										.findAlarmActivity(currentActivity)) {
									Intent intentMain = new Intent(
											getApplicationContext(),
											currentActivity.getClass());
									intentMain
											.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									// statusHashMap.put(Consts.LOCAL_LOGIN,
									// "false");
									startActivity(intentMain);
									// Thread.sleep(100);
									// onNotify(Consts.PUSH_MESSAGE,
									// pi.alarmType,
									// 0, pi);
								} else {
									MyLog.v("PushCallBack",
											"this "
													+ currentActivity
															.toString()
													+ " is not need to pop alarm activity");
								}
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				} else {

				}
			} else if (JVAccountConst.MESSAGE_OFFLINE == res) {// 提掉线
				Intent intent = new Intent(getApplicationContext(),
						JVOffLineDialogActivity.class);
				intent.putExtra("ErrorCode", JVAccountConst.MESSAGE_OFFLINE);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} else if (JVAccountConst.PTCP_ERROR == res) {// TCP错误
				JVACCOUNT.StopHeartBeat();// 先停止心跳
				Intent intent = new Intent(getApplicationContext(),
						JVOffLineDialogActivity.class);
				intent.putExtra("ErrorCode", JVAccountConst.PTCP_ERROR);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			} else if (JVAccountConst.PTCP_CLOSED == res) {// TCP关闭
				JVACCOUNT.StopHeartBeat();// 先停止心跳
				Intent intent = new Intent(getApplicationContext(),
						JVOffLineDialogActivity.class);
				intent.putExtra("ErrorCode", JVAccountConst.PTCP_CLOSED);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean isAppOnForeground() {
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		if (appProcesses == null)
			return false;

		for (RunningAppProcessInfo appProcess : appProcesses) {
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}
		return false;
	}
}
