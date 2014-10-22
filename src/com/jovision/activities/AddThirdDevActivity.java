package com.jovision.activities;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.jovetech.CloudSee.temp.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.activities.AddThirdDeviceMenuFragment.OnDeviceClassSelectedListener;
import com.jovision.activities.BindThirdDevNicknameFragment.OnSetNickNameListener;
import com.jovision.bean.ThirdAlarmDev;
import com.jovision.commons.JVNetConst;
import com.jovision.commons.MyLog;
import com.jovision.commons.PlayWindowManager;
import com.jovision.utils.AlarmUtil;

public class AddThirdDevActivity extends BaseActivity implements
		OnClickListener, OnDeviceClassSelectedListener, OnSetNickNameListener {
	private AddThirdDeviceMenuFragment third_dev_menu_fragment;
	private Button backBtn;
	private Button rightBtn;
	public TextView titleTv;
	private String strYstNum;
	private boolean bConnectedFlag;
	private int dev_type_mark;
	private PlayWindowManager manager;
	private ProgressDialog dialog;
	private int dev_uid;
	private String nickName;
	private int fragment_tag = 0;// 0，添加界面； 1 绑定界面
	private boolean bNeedSendTextReq = true;
	private MyHandler myHandler;
	private int process_flag = 0; //0 绑定设备 1绑定昵称
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.add_third_device_main);
		Bundle extras = getIntent().getExtras();
		strYstNum = extras.getString("dev_num");
		bConnectedFlag = extras.getBoolean("conn_flag");
		bNeedSendTextReq = extras.getBoolean("text_req_flag");
		if (null == dialog) {
			dialog = new ProgressDialog(this);
			dialog.setCancelable(false);
		}
		dialog.setMessage(getResources().getString(
				R.string.waiting));		
		// Check that the activity is using the layout version with
		// the fragment_container FrameLayout
		if (findViewById(R.id.fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}
			third_dev_menu_fragment = new AddThirdDeviceMenuFragment();
			Bundle bundle1 = new Bundle();
			bundle1.putString("yst_num", strYstNum);
			bundle1.putBoolean("conn_flag", bConnectedFlag);
			third_dev_menu_fragment.setArguments(bundle1);
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.add(R.id.fragment_container, third_dev_menu_fragment)
					.commitAllowingStateLoss();
			fragment_tag = 0;
		}
		InitViews();
		myHandler = new MyHandler();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void InitViews() {
		backBtn = (Button) findViewById(R.id.btn_left);
		rightBtn = (Button) findViewById(R.id.btn_right);
		rightBtn.setVisibility(View.GONE);
		titleTv = (TextView) findViewById(R.id.currentmenu);
		backBtn.setOnClickListener(this);
		titleTv.setText(R.string.str_help1_1);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_left:
			if (fragment_tag == 0) {
				finish();
			} else {
				Intent data = new Intent();
				data.putExtra("nickname", "未命名");
				data.putExtra("dev_type_mark", dev_type_mark);
				data.putExtra("dev_uid", dev_uid);
				// 请求代码可以自己设置，这里设置成10
				setResult(10, data);
				// 关闭掉这个Activity
				finish();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void OnDeviceClassSelected(int index) {
		// TODO Auto-generated method stub
		process_flag = 0;
		switch (index) {
		case 1:
			titleTv.setText(R.string.str_door_device);
			dev_type_mark = 1;// 门禁
			dialog.show();
			if (!bConnectedFlag) {
				if (!AlarmUtil.OnlyConnect(strYstNum)) {
					showTextToast("连接失败，已经连接或者超过最大连接数");
					dialog.dismiss();
				}
			} else {
				// 首先需要发送文本聊天请求
				if (bNeedSendTextReq) {
					Jni.sendBytes(Consts.ONLY_CONNECT_INDEX, (byte) JVNetConst.JVN_REQ_TEXT,
							new byte[0], 8);
//					myHandler.sendEmptyMessageDelayed(JVNetConst.JVN_REQ_TEXT,
//							10000);// 10秒获取不到就取消Dialog
				}
				else{
					String req_data = "type=" + dev_type_mark + ";";
					Jni.sendString(Consts.ONLY_CONNECT_INDEX, (byte) JVNetConst.JVN_RSP_TEXTDATA, false, 0,
							(byte) Consts.RC_GPIN_ADD, req_data.trim());
				}
			}
			break;
		case 2:
			titleTv.setText(R.string.str_bracelet_device);
			dev_type_mark = 2;// 手环
			dialog.show();
			if (!bConnectedFlag) {
				if (!AlarmUtil.OnlyConnect(strYstNum)) {
					showTextToast("连接失败，已经连接或者超过最大连接数");
					dialog.dismiss();
				}
			} else {
				// 首先需要发送文本聊天请求
				if (bNeedSendTextReq) {
					Jni.sendBytes(Consts.ONLY_CONNECT_INDEX, (byte) JVNetConst.JVN_REQ_TEXT,
							new byte[0], 8);
//					myHandler.sendEmptyMessageDelayed(JVNetConst.JVN_REQ_TEXT,
//							10000);// 10秒获取不到就取消Dialog
				}
				else{
					String req_data = "type=" + dev_type_mark + ";";
					Jni.sendString(Consts.ONLY_CONNECT_INDEX, (byte) JVNetConst.JVN_RSP_TEXTDATA, false, 0,
							(byte) Consts.RC_GPIN_ADD, req_data.trim());
				}

			}
			break;
		default:
			break;
		}
	}

	class MyHandler extends Handler {
		@Override
		public void dispatchMessage(Message msg) {
			switch (msg.what) {
			case JVNetConst.JVN_REQ_TEXT:
			case JVNetConst.JVN_RSP_TEXTDATA:
				if (dialog != null && dialog.isShowing())
					dialog.dismiss();
				showTextToast("主控绑定设备超时");
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void onHandler(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		MyLog.e(TAG, "onHandler--what=" + what + ";arg1=" + arg1 + ";arg2="
				+ arg2 + "; obj = " + (obj == null ? "" : obj.toString()));
		switch (what) {
		// 连接结果
		case Consts.CALL_CONNECT_CHANGE:
			switch (arg2) {

			case JVNetConst.NO_RECONNECT:// 1 -- 连接成功//3 不必重新连接
			case JVNetConst.CONNECT_OK: {// 1 -- 连接成功
				MyLog.e("New alarm", "连接成功");
				bConnectedFlag = true;
				showTextToast("连接成功");
				// 首先需要发送文本聊天请求
				Jni.sendBytes(Consts.ONLY_CONNECT_INDEX, (byte) JVNetConst.JVN_REQ_TEXT, new byte[0], 8);
			}
				break;
			// 2 -- 断开连接成功
			case JVNetConst.DISCONNECT_OK: {
				if (dialog != null && dialog.isShowing())
					dialog.dismiss();
				bConnectedFlag = false;

			}
				break;
			// 4 -- 连接失败
			case JVNetConst.CONNECT_FAILED: {
				bConnectedFlag = false;
				if (dialog != null && dialog.isShowing())
					dialog.dismiss();
				try {
					JSONObject connectObj = new JSONObject(obj.toString());
					String errorMsg = connectObj.getString("msg");
					if ("password is wrong!".equalsIgnoreCase(errorMsg)
							|| "pass word is wrong!".equalsIgnoreCase(errorMsg)) {// 密码错误时提示身份验证失败
						showTextToast(R.string.connfailed_auth);
					} else if ("channel is not open!"
							.equalsIgnoreCase(errorMsg)) {// 无该通道服务
						showTextToast(R.string.connfailed_channel_notopen);
					} else if ("connect type invalid!"
							.equalsIgnoreCase(errorMsg)) {// 连接类型无效
						showTextToast(R.string.connfailed_type_invalid);
					} else if ("client count limit!".equalsIgnoreCase(errorMsg)) {// 超过主控最大连接限制
						showTextToast(R.string.connfailed_maxcount);
					} else if ("connect timeout!".equalsIgnoreCase(errorMsg)) {//
						showTextToast(R.string.connfailed_timeout);
					} else {// "Connect failed!"
						showTextToast(R.string.connect_failed);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			break;
			default:
				bConnectedFlag = false;
				break;
			}
			;
			break;
		case Consts.CALL_TEXT_DATA: {
			myHandler.removeMessages(JVNetConst.JVN_REQ_TEXT);
			myHandler.removeMessages(JVNetConst.JVN_RSP_TEXTDATA);
			switch (arg2) {
			case JVNetConst.JVN_RSP_TEXTACCEPT:// 同意文本请求后才发送请求,这里要区分出是添加还是最后的绑定昵称
				bNeedSendTextReq = false;
				if(process_flag == 0){
					String req_data = "type=" + dev_type_mark + ";";
					Jni.sendString(Consts.ONLY_CONNECT_INDEX, (byte) JVNetConst.JVN_RSP_TEXTDATA, false, 0,
							(byte) Consts.RC_GPIN_ADD, req_data.trim());				
				}
				else{
					SendBingNickName(nickName);
				}
//				myHandler.sendEmptyMessageDelayed(JVNetConst.JVN_RSP_TEXTDATA,
//						10000);// 10秒获取不到就取消Dialog
				break;
			case JVNetConst.JVN_CMD_TEXTSTOP:// 文本请求聊天终止
				if (dialog != null && dialog.isShowing())
					dialog.dismiss();
				bNeedSendTextReq = true;
				showTextToast("文本请求聊天终止");
				break;
			case JVNetConst.JVN_RSP_TEXTDATA: {
				if (dialog != null && dialog.isShowing())
					dialog.dismiss();
				JSONObject respObject = null;
				if (obj != null) {
					try {
						respObject = new JSONObject(obj.toString());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						showTextToast("TextData回调obj参数转Json异常");
						return;
					}
				} else {
					showTextToast("TextData回调obj参数is null");
					MyLog.e("Third Dev", "TextData回调obj参数is null");
					return;
				}
				int flag = respObject.optInt("flag");
				switch (flag) {
				case Consts.RC_GPIN_ADD:// 绑定设备
					if (obj != null) {
						String addStr = respObject.optString("msg");
						String addStrArray[] = addStr.split(";");
						ThirdAlarmDev addAlarm = new ThirdAlarmDev();
						int addResult = -1;
						for (int i = 0; i < addStrArray.length; i++) {
							String[] split = addStrArray[i].split("=");
							if ("res".equals(split[0])) {
								addResult = Integer.parseInt(split[1]);
								break;
							}
						}

						if (addResult == 1) {// add success
							for (int i = 0; i < addStrArray.length; i++) {
								String[] split = addStrArray[i].split("=");
								if ("guid".equals(split[0])) {
									addAlarm.dev_uid = Integer
											.parseInt(split[1]);
								} else if ("type".equals(split[0])) {
									addAlarm.dev_type_mark = Integer
											.parseInt(split[1]);
								}
							}
							showTextToast("绑定设备成功");
							dev_uid = addAlarm.dev_uid; // /////////////////////////
							BindThirdDevNicknameFragment nicknameFragment = new BindThirdDevNicknameFragment();
							FragmentTransaction transaction = getSupportFragmentManager()
									.beginTransaction();
							// Replace whatever is in the fragment_container
							// view
							// with this
							// fragment,
							// and add the transaction to the back stack so the
							// user
							// can
							// navigate back
							transaction.replace(R.id.fragment_container,
									nicknameFragment);
							transaction.addToBackStack(null);
							fragment_tag = 1;
							// Commit the transaction
							transaction.commit();
						} else if (addResult == 2) {// 超过最大数
							showTextToast("超过最大数");
						} else {
							showTextToast("绑定设备失败");
						}
					}
					break;

				case Consts.RC_GPIN_SET:// 设置昵称
					if (obj != null) {
						if (dialog != null && dialog.isShowing())
							dialog.dismiss();
						String setStr = respObject.optString("msg");
						String setStrArray[] = setStr.split(";");
						ThirdAlarmDev setalarm = new ThirdAlarmDev();
						int setResult = -1;
						for (int i = 0; i < setStrArray.length; i++) {
							String[] split = setStrArray[i].split("=");
							if ("res".equals(split[0])) {
								setResult = Integer.parseInt(split[1]);
								break;
							}
						}

						if (setResult == 1) {// SET success
							for (int i = 0; i < setStrArray.length; i++) {
								String[] split = setStrArray[i].split("=");
								if ("guid".equals(split[0])) {
									setalarm.dev_uid = Integer
											.parseInt(split[1]);
								} else if ("type".equals(split[0])) {
									setalarm.dev_type_mark = Integer
											.parseInt(split[1]);
								}
							}
							// 成功
							// 返回第三方设备列表activity
							Intent data = new Intent();
							data.putExtra("nickname", nickName);
							data.putExtra("dev_type_mark", dev_type_mark);
							data.putExtra("dev_uid", setalarm.dev_uid);
							// 请求代码可以自己设置，这里设置成10
							setResult(10, data);
							// 关闭掉这个Activity
							finish();
						} else {
							// 失败
							showTextToast("设置昵称失败");
						}
					} else {
						showTextToast("TextData回调obj参数is null");
					}
					break;
				default:
					break;
				}
			}
				break;
			default:
				if (dialog != null && dialog.isShowing())
					dialog.dismiss();
				showTextToast("TextData其他回调" + arg2);
				break;
			}
		}
			break;
		// default:
		// if (dialog != null && dialog.isShowing())
		// dialog.dismiss();
		// break;
		}
	}

	@Override
	public void onNotify(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
	}

	@Override
	protected void initSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initUi() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void freeMe() {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnSetNickName(String strNickName) {
		// TODO Auto-generated method stub
		process_flag = 1;
		nickName = strNickName;
		if (!bConnectedFlag) {
			//
			dialog.show();
			if (!AlarmUtil.OnlyConnect(strYstNum)) {
				showTextToast("连接失败，已经连接或者超过最大连接数");
				dialog.dismiss();
			}
		} else {
			dialog.show();			
			SendBingNickName(nickName);
		}
	}

	private void SendBingNickName(String nickName) {
		String strType = "type=" + dev_type_mark + ";";
		String strGuid = "guid=" + dev_uid + ";";
		String strNickName = "name=" + nickName + ";";
		String strSwitch = "enable=1;";
		String reqData = strType + strGuid + strNickName + strSwitch;
		MyLog.e("Third Dev", "bing nick name req:" + reqData);
		Jni.sendString(Consts.ONLY_CONNECT_INDEX, (byte) JVNetConst.JVN_RSP_TEXTDATA, false, 0,
				(byte) Consts.RC_GPIN_SET, reqData.trim());
	}
}
