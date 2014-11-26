package com.jovision;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jovetech.CloudSee.temp.R;
import com.jovision.activities.JVWelcomeActivity;
import com.jovision.bean.PushInfo;
import com.jovision.commons.JVAlarmConst;
import com.jovision.commons.MyActivityManager;
import com.jovision.utils.AlarmUtil;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

public class MessageReceiver extends XGPushBaseReceiver {
	protected NotificationManager mNotifyer;

	@Override
	public void onDeleteTagResult(Context arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNotifactionClickedResult(Context context,
			XGPushClickedResult message) {
		// TODO Auto-generated method stub
		if (context == null || message == null) {
			return;
		}
	}

	@Override
	public void onNotifactionShowedResult(Context arg0, XGPushShowedResult arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRegisterResult(Context arg0, int arg1,
			XGPushRegisterResult arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSetTagResult(Context arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextMessage(Context context, XGPushTextMessage arg1) {
		// TODO Auto-generated method stub
		Log.e("TPush", "onTextMessage:" + arg1.getContent());
		if (context == null || arg1 == null) {
			Log.e("TPush", "onTextMessage the context is null");
		} else {
			Log.e("TPush", "onTextMessage the context is not null");
		}
		String ns = Context.NOTIFICATION_SERVICE;
		mNotifyer = (NotificationManager) context.getSystemService(ns);
		// 定义通知栏展现的内容信息
		int icon = R.drawable.notification_icon;
		CharSequence tickerText = context.getResources().getString(
				R.string.str_alarm);
		long when = System.currentTimeMillis();

		Activity currentActivity = MyActivityManager.getActivityManager()
				.currentActivity();
		if (currentActivity == null) {
			Log.e("TPush", "当前程序没有在运行，在通知栏显示....");
			Notification notification = new Notification(icon, tickerText, when);
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_SOUND;// 声音
			notification.defaults |= Notification.DEFAULT_LIGHTS;// 灯
			notification.defaults |= Notification.DEFAULT_VIBRATE;// 震动

			// 定义下拉通知栏时要展现的内容信息

			// CharSequence contentText = arg1.getContent();
			// CharSequence contentTitle = arg1.getTitle();
			CharSequence contentText = "";
			CharSequence contentTitle = "";
			// 自定义消息
			String customContent = arg1.getCustomContent();
			if (customContent != null && customContent.length() != 0) {
				try {
					JSONObject object = new JSONObject(customContent);
					if (!object.isNull("key")) {
						String strJsonValueString = object.getString("key");
						Log.i("TPush", "custom content : " + strJsonValueString);
						JSONObject obj = new JSONObject(strJsonValueString);
						PushInfo pi = new PushInfo();
						String[] alarmArray = context.getResources()
								.getStringArray(R.array.alarm_type);
						pi.alarmType = obj
								.optInt(JVAlarmConst.JK_ALARM_NEW_ALARMTYPE);
						if (pi.alarmType == 7 || pi.alarmType == 4) {
							pi.deviceNickName = obj
									.optString(JVAlarmConst.JK_ALARM_NEW_CLOUDNAME);
						} else if (pi.alarmType == 11)// 第三方
						{
							pi.deviceNickName = obj
									.optString(JVAlarmConst.JK_ALARM_NEW_ALARM_THIRD_NICKNAME);
						} else {

						}
						String strTempTime = "";
						strTempTime = obj
								.optString(JVAlarmConst.JK_ALARM_NEW_ALARMTIME_STR);
						if (!strTempTime.equals("")) {
							// 有限取这个字段的时间，格式：yyyyMMddhhmmss
							pi.timestamp = "";
							pi.alarmTime = AlarmUtil.formatStrTime(strTempTime);
						} else {
							pi.timestamp = obj
									.optString(JVAlarmConst.JK_ALARM_NEW_ALARMTIME);
							pi.alarmTime = AlarmUtil.getStrTime(pi.timestamp);
						}
						contentText = pi.alarmTime
								+ "-"
								+ alarmArray[pi.alarmType].replace("%%",
										pi.deviceNickName);

						contentTitle = context.getResources().getString(
								R.string.str_alarm_info);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				String content = arg1.getContent();
				if (content == null || content.length() == 0) {
					Log.e("TPush", "the content is null");
				}
				try {
					// JSONObject object = new JSONObject(content);
					// if(!object.isNull("key")){
					// String strJsonValueString = object.getString("key");
					Log.i("TPush", "custom content : " + content);
					JSONObject obj = new JSONObject(content);
					PushInfo pi = new PushInfo();
					String[] alarmArray = context.getResources()
							.getStringArray(R.array.alarm_type);
					pi.alarmType = obj
							.optInt(JVAlarmConst.JK_ALARM_NEW_ALARMTYPE);
					if (pi.alarmType == 7 || pi.alarmType == 4) {
						pi.deviceNickName = obj
								.optString(JVAlarmConst.JK_ALARM_NEW_CLOUDNAME);
					} else if (pi.alarmType == 11)// 第三方
					{
						pi.deviceNickName = obj
								.optString(JVAlarmConst.JK_ALARM_NEW_ALARM_THIRD_NICKNAME);
					} else {

					}
					String strTempTime = "";
					strTempTime = obj
							.optString(JVAlarmConst.JK_ALARM_NEW_ALARMTIME_STR);
					if (!strTempTime.equals("")) {
						// 有限取这个字段的时间，格式：yyyyMMddhhmmss
						pi.timestamp = "";
						pi.alarmTime = AlarmUtil.formatStrTime(strTempTime);
					} else {
						pi.timestamp = obj
								.optString(JVAlarmConst.JK_ALARM_NEW_ALARMTIME);
						pi.alarmTime = AlarmUtil.getStrTime(pi.timestamp);
					}
					contentText = pi.alarmTime
							+ " "
							+ alarmArray[pi.alarmType].replace("%%",
									pi.deviceNickName);

					contentTitle = context.getResources().getString(
							R.string.str_alarm_info);
					// }
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
			notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			notificationIntent.setClass(context, JVWelcomeActivity.class);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);
			// 用mNotificationManager的notify方法通知用户生成标题栏消息通知
			mNotifyer.notify(0, notification);
		} else {
			Log.e("TPush", "当前程序正在运行，不在通知栏显示....");
			return;
			// notificationIntent.setClass(context, currentActivity.getClass());
		}

	}

	@Override
	public void onUnregisterResult(Context arg0, int arg1) {
		// TODO Auto-generated method stub

	}

}