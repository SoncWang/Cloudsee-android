package com.jovision.activities;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.test.JVACCOUNT;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jovetech.CloudSee.temp.R;
import com.jovision.Consts;
import com.jovision.activities.ResetPwdInputAccountActivity.CheckUserInfoTask;
import com.jovision.adapters.FragmentAdapter;
import com.jovision.bean.MoreFragmentBean;
import com.jovision.commons.CheckUpdateTask;
import com.jovision.commons.JVAlarmConst;
import com.jovision.commons.MyActivityManager;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.AccountUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.ListViewUtil;
import com.jovision.utils.MobileUtil;
import com.jovision.utils.UserUtil;
import com.jovision.views.AlarmDialog;
import com.jovision.views.popw;
import com.tencent.stat.StatService;

/**
 * 更多
 */
public class JVMoreFragment extends BaseFragment {
	// Adapter 存储模块文字和图标
	private ArrayList<MoreFragmentBean> dataList;
	// 模块listView
	private ListView more_listView;
	// listView 适配器
	private FragmentAdapter adapter;
	// Fragment依附的activity
	private Activity activity;
	// 修改资料
	private TextView more_modify;
	// 找回密码
	private TextView more_findpassword;
	// 头像
	private ImageView more_head;
	// 注销按钮
	private RelativeLayout more_cancle;
	// 用户名称
	private TextView more_username;
	// 用户名
	private String more_name;
	// 最后一次登录时间
	private TextView more_lasttime;
	// 修改密码
	private TextView more_modifypwd;
	//绑定邮箱
	private TextView more_bindmail;
	// 图片数组
	private int[] Image = { R.drawable.morefragment_help_icon,
			R.drawable.morefragment_warmmessage_icon,
			R.drawable.morefragment_setting_icon, R.drawable.media_image,
			R.drawable.morefragment_update_icon, R.drawable.media_image,
			R.drawable.morefragment_aboutus_icon,
			R.drawable.morefragment_feedback_icon,
			R.drawable.morefragment_update_icon,
			R.drawable.morefragment_aboutus_icon };
	// 功能名称数组
	private String[] fragment_name;

	public static boolean localFlag = false;// 本地登陆标志位

	private popw popupWindow; // 声明PopupWindow对象；
	private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final int PHOTO_REQUEST_CUT = 3;// 结果
	// 存放头像的文件夹
	File file;
	// 旧头像文件
	File tempFile;
	// 新头像文件
	File newFile;
	// popupWindow滑出布局
	private LinearLayout linear;

	private int littlenum = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_more, container, false);
		intiUi(view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mParent = getView();
		mActivity = (BaseActivity) getActivity();
		localFlag = Boolean.valueOf(mActivity.statusHashMap
				.get(Consts.LOCAL_LOGIN));
		currentMenu.setText(R.string.more_featrue);
		rightBtn.setVisibility(View.GONE);

		if (null != mActivity.statusHashMap.get(Consts.KEY_LAST_LOGIN_TIME)) {
			more_lasttime.setText(mActivity.statusHashMap
					.get(Consts.KEY_LAST_LOGIN_TIME));
		} else {
			more_lasttime.setText("");
		}
	}

	@Override
	public void onHandler(int what, int arg1, int arg2, Object obj) {
		switch (what) {
		case Consts.WHAT_PUSH_MESSAGE:
			// 弹出对话框
			if (null != mActivity) {
				new AlarmDialog(mActivity).Show(obj);
			} else {
				MyLog.e("Alarm",
						"onHandler mActivity is null ,so dont show the alarm dialog");
			}
			break;
		case 1234:
			more_bindmail.setVisibility(View.VISIBLE);
			break;
		}

	}

	private void intiUi(View view) {
		activity = getActivity();
		if (MySharedPreference.getBoolean("PlayDeviceMode")) {
			fragment_name = activity.getResources().getStringArray(
					R.array.array_moreduo);
		} else {
			fragment_name = activity.getResources().getStringArray(
					R.array.array_more);
		}

		if (Boolean.valueOf(((BaseActivity) activity).statusHashMap
				.get(Consts.LOCAL_LOGIN))) {
			more_name = activity.getResources().getString(
					R.string.location_login);
		} else {
			more_name = ((BaseActivity) activity).statusHashMap
					.get(Consts.KEY_USERNAME);
		}
		initDatalist();
		file = new File(Consts.HEAD_PATH);
		MobileUtil.createDirectory(file);
		tempFile = new File(Consts.HEAD_PATH + more_name + ".jpg");
		newFile = new File(Consts.HEAD_PATH + more_name + "1.jpg");
		more_modifypwd = (TextView) view.findViewById(R.id.more_modifypwd);
		more_bindmail = (TextView)view.findViewById(R.id.more_bindmail);
		more_cancle = (RelativeLayout) view.findViewById(R.id.more_cancle);
		more_modify = (TextView) view.findViewById(R.id.more_modify);
		more_findpassword = (TextView) view
				.findViewById(R.id.more_findpassword);
		more_username = (TextView) view.findViewById(R.id.more_uesrname);
		more_lasttime = (TextView) view.findViewById(R.id.more_lasttime);
		linear = (LinearLayout) view.findViewById(R.id.lin);
		more_head = (ImageView) view.findViewById(R.id.more_head_img);

		more_listView = (ListView) view.findViewById(R.id.more_listView);
		adapter = new FragmentAdapter(JVMoreFragment.this, dataList);
		more_listView.setAdapter(adapter);
		ListViewUtil.setListViewHeightBasedOnChildren(more_listView);
		listViewClick();

		more_bindmail.setOnClickListener(myOnClickListener);
		more_modifypwd.setOnClickListener(myOnClickListener);
		more_cancle.setBackgroundResource(R.drawable.blue_bg);
		more_username.setText(more_name);
		more_head.setOnClickListener(myOnClickListener);
		more_cancle.setOnClickListener(myOnClickListener);
		more_modify.setOnClickListener(myOnClickListener);
		more_findpassword.setOnClickListener(myOnClickListener);
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!Boolean.valueOf(((BaseActivity) activity).statusHashMap
				.get(Consts.LOCAL_LOGIN))) {
			CheckUserInfoTask task = new CheckUserInfoTask();
			task.execute(more_name);
		}
		if (tempFile.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(Consts.HEAD_PATH
					+ more_name + ".jpg");
			more_head.setImageBitmap(bitmap);
		}
	}

	private void initDatalist() {
		dataList = new ArrayList<MoreFragmentBean>();
		for (int i = 0; i < Image.length; i++) {
			MoreFragmentBean bean = new MoreFragmentBean();
			bean.setItem_img(Image[i]);
			bean.setName(fragment_name[i]);
			dataList.add(bean);
		}
	}

	OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.pop_outside:
				popupWindow.dismiss();
				break;
			case R.id.more_head_img:
				popupWindow = new popw(mActivity, myOnClickListener);
				popupWindow.setBackgroundDrawable(null);
				popupWindow.setOutsideTouchable(true);
				popupWindow.showAtLocation(linear, Gravity.BOTTOM
						| Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
				break;
			case R.id.btn_pick_photo: {
				popupWindow.dismiss();
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
				break;
			}
			case R.id.btn_take_photo:
				// 调用系统的拍照功能
				popupWindow.dismiss();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// 指定调用相机拍照后照片的储存路径
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
				startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
				break;
			case R.id.btn_cancel:
				popupWindow.dismiss();
				break;
			case R.id.more_cancle:// 注销
				LogOutTask task = new LogOutTask();
				String[] strParams = new String[3];
				task.execute(strParams);
				break;
			case R.id.more_modify:
				
				break;
			case R.id.more_bindmail:
				startActivity(new Intent(mActivity,JVReBoundEmailActivity.class));
				break;
			case R.id.more_modifypwd:
				if (!localFlag) {
					Intent editpassintent = new Intent(mActivity,
							JVEditPassActivity.class);
					startActivity(editpassintent);
				} else {
					mActivity.showTextToast(R.string.more_nologin);
				}
				break;
			case R.id.more_findpassword:

				break;
			default:
				break;

			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PHOTO_REQUEST_TAKEPHOTO:
			if (resultCode == -1) {
				startPhotoZoom(Uri.fromFile(newFile), 300);
			}
			break;

		case PHOTO_REQUEST_GALLERY:
			if (data != null)
				startPhotoZoom(data.getData(), 300);
			break;

		case PHOTO_REQUEST_CUT:
			if (data != null)
				setPicToView(data);
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startPhotoZoom(Uri uri, int size) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// crop为true是设置在开启的intent中设置显示的view可以剪裁
		intent.putExtra("crop", "true");

		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		// outputX,outputY 是剪裁图片的宽高
		intent.putExtra("outputX", size);
		intent.putExtra("outputY", size);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

	// 将进行剪裁后的图片显示到UI界面上
	private void setPicToView(Intent picdata) {
		Bundle bundle = picdata.getExtras();
		if (bundle != null) {
			Bitmap photo = bundle.getParcelable("data");
			saveBitmap(photo);
			Drawable drawable = new BitmapDrawable(photo);
			more_head.setBackgroundDrawable(drawable);
		}
	}

	public void saveBitmap(Bitmap bm) {
		if (null == bm) {
			return;
		}
		File f = new File(Consts.HEAD_PATH + more_name + ".jpg");
		if (f.exists()) {
			f.delete();
		}
		try {
			FileOutputStream out = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void listViewClick() {
		more_listView
		.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					if (MySharedPreference.getBoolean("HELP")) {
						MySharedPreference.putBoolean("HELP", false);
						MySharedPreference.putBoolean("page1", true);
						MySharedPreference.putBoolean("page2", true);
					} else {
						MySharedPreference.putBoolean("HELP", true);
						MySharedPreference.putBoolean("page1", false);
						MySharedPreference.putBoolean("page2", false);
					}
					break;
				case 1:
					AlarmTask task = new AlarmTask();
					Integer[] params = new Integer[3];
					if (!MySharedPreference.getBoolean("AlarmSwitch",
							true)) {// 1是关
						// 0是开
						params[0] = JVAlarmConst.ALARM_ON;// 关闭状态，去打开报警
					} else {
						params[0] = JVAlarmConst.ALARM_OFF;// 已经打开了，要去关闭
					}
					task.execute(params);

					break;
				case 2:
					if (MySharedPreference.getBoolean("PlayDeviceMode")) {
						MySharedPreference.putBoolean("PlayDeviceMode",
								false);
						dataList.get(2).setName(
								mActivity.getResources().getString(
										R.string.str_video_modetwo));
					} else {
						MySharedPreference.putBoolean("PlayDeviceMode",
								true);
						dataList.get(2)
						.setName(
								mActivity
								.getResources()
								.getString(
										R.string.str_video_more_modetwo));
					}
					break;
				case 3:// 小助手
					if (MySharedPreference.getBoolean("LITTLEHELP")) {
						MySharedPreference.putBoolean("LITTLEHELP",
								false);
					} else {
						MySharedPreference.putBoolean("LITTLEHELP",
								true);
					}
					break;
				case 4:
					if (MySharedPreference.getBoolean("BROADCASTSHOW")) {
						MySharedPreference.putBoolean("BROADCASTSHOW",
								false);
					} else {
						MySharedPreference.putBoolean("BROADCASTSHOW",
								true);
					}
					break;
				case 5:// 版本号
					String itemone = mActivity.getResources()
					.getString(R.string.str_network_version)
					+ ":" + ConfigUtil.NETWORK_VERSION;
					String itemtwo = mActivity.getResources()
							.getString(R.string.str_play_version)
							+ ":"
							+ ConfigUtil.PLAY_VERSION;
					String itemthree = mActivity.getResources()
							.getString(R.string.str_appnetwork_version)
							+ ":" + ConfigUtil.GETNETWORK_VERSION;
					String itemfour = mActivity.getResources()
							.getString(R.string.str_appplay_version)
							+ ":" + ConfigUtil.GETPLAY_VERSION;
					new AlertDialog.Builder(new ContextThemeWrapper(
							mActivity, R.style.AlertDialogCustom))
					.setTitle(
							mActivity.getResources().getString(
									R.string.str_version))
									.setItems(
											new String[] { itemone, itemtwo,
													itemthree, itemfour }, null)
													.setNegativeButton(
															mActivity.getResources().getString(
																	R.string.ok), null).show();
					// TODO
					break;
				case 6:// 媒体
					StatService.trackCustomEvent(
							mActivity,
							"Media",
							mActivity.getResources().getString(
									R.string.str_media));
					Intent intentMedia = new Intent(mActivity,
							JVMediaActivity.class);
					mActivity.startActivity(intentMedia);
					break;
				case 7:
					Intent intent = new Intent(mActivity,
							JVFeedbackActivity.class);
					startActivity(intent);
					break;
				case 8:
					mActivity.createDialog("", false);
					CheckUpdateTask taskf = new CheckUpdateTask(
							mActivity);
					String[] strParams = new String[3];
					strParams[0] = "1";// 1,手动检查更新
					taskf.execute(strParams);
					break;
				case 9:
					if (!MySharedPreference.getBoolean("LITTLE")) {
						littlenum++;
						if (littlenum < 20) {
							if (littlenum >= 17) {
								mActivity
								.showTextToast((20 - littlenum)
										+ " ");
							}
						} else if (littlenum == 20) {
							MySharedPreference.putBoolean("LITTLEHELP",
									true);
							MySharedPreference.putBoolean(
									"BROADCASTSHOW", true);
							MySharedPreference.putBoolean("LITTLE",
									true);
							ListViewUtil
							.setListViewHeightBasedOnChildren(more_listView);
						}
					}
					break;
				default:
					break;
				}
				adapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onNotify(int what, int arg1, int arg2, Object obj) {
		fragHandler.sendMessage(fragHandler
				.obtainMessage(what, arg1, arg2, obj));
	}

	class CheckUserInfoTask extends AsyncTask<String, Integer, Integer> {
		String account = "";
		byte[] response = null;

		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub
			account = params[0];
			response = new byte[1024];
			int ret = -1;
			ret = JVACCOUNT.GetMailPhoneNoSession(account, response);
			return ret;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == 0)// ok
			{
				String strPhone = "";
				String strMail = "";
				try {
					JSONObject resObject = new JSONObject(new String(response));
					strPhone = resObject.optString("phone");
					strMail = resObject.optString("mail");
					if ((strMail.equals("") || null == strMail)&&(strPhone.equals("") || null == strPhone)) {
						onNotify(1234, 0, 0, null);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// 重置失败
				mActivity.showTextToast(R.string.str_video_load_failed);
			}
		}

		@Override
		protected void onPreExecute() {
			// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
		}
	}
	// 设置三种类型参数分别为String,Integer,String
	private class AlarmTask extends AsyncTask<Integer, Integer, Integer> {// A,361,2000
		// 可变长的输入参数，与AsyncTask.exucute()对应
		@Override
		protected Integer doInBackground(Integer... params) {
			int switchRes = -1;
			if (JVAlarmConst.ALARM_ON == params[0]) {// 开报警
				switchRes = JVACCOUNT.SetCurrentAlarmFlag(
						JVAlarmConst.ALARM_ON, ConfigUtil.getIMEI(mActivity));
				if (0 == switchRes) {
					MyLog.e("JVAlarmConst.ALARM--ON-", switchRes + "");
					MySharedPreference.putBoolean("AlarmSwitch", true);
				}
			} else {// 关报警
				switchRes = JVACCOUNT.SetCurrentAlarmFlag(
						JVAlarmConst.ALARM_OFF, ConfigUtil.getIMEI(mActivity));
				if (0 == switchRes) {
					MyLog.e("JVAlarmConst.ALARM--CLOSE-", switchRes + "");
					MySharedPreference.putBoolean("AlarmSwitch", false);
				}
			}

			return switchRes;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
			mActivity.dismissDialog();
			adapter.notifyDataSetChanged();
		}

		@Override
		protected void onPreExecute() {
			// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
			mActivity.createDialog("", true);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// 更新进度,此方法在主线程执行，用于显示任务执行的进度。
		}
	}

	// 注销线程
	class LogOutTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
		// 可变长的输入参数，与AsyncTask.exucute()对应
		@Override
		protected Integer doInBackground(String... params) {
			int logRes = -1;
			try {
				if (!localFlag) {
					AccountUtil.userLogout();
					MySharedPreference.putString(Consts.DEVICE_LIST, "");
				}
				ConfigUtil.logOut();
				UserUtil.resetAllUser();
				mActivity.statusHashMap.put(Consts.HAG_GOT_DEVICE, "false");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return logRes;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
			((BaseActivity) mActivity).dismissDialog();
			MyActivityManager.getActivityManager().popAllActivityExceptOne(
					JVLoginActivity.class);
			Intent intent = new Intent();
			String userName = mActivity.statusHashMap.get(Consts.KEY_USERNAME);
			intent.putExtra("UserName", userName);
			intent.setClass(mActivity, JVLoginActivity.class);
			mActivity.startActivity(intent);
			mActivity.finish();
		}

		@Override
		protected void onPreExecute() {
			// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
			((BaseActivity) mActivity).createDialog("", true);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// 更新进度,此方法在主线程执行，用于显示任务执行的进度。
		}
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		if (littlenum < 10) {
			littlenum = 0;
		}
		super.onPause();
	}
}
