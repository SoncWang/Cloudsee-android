package com.jovision.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import neo.droid.p2r.PullToRefreshBase;
import neo.droid.p2r.PullToRefreshBase.OnLastItemVisibleListener;
import neo.droid.p2r.PullToRefreshBase.OnRefreshListener;
import neo.droid.p2r.PullToRefreshListView;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jovetech.CloudSee.temp.R;
import com.jovision.Consts;
import com.jovision.adapters.MyDeviceListAdapter;
import com.jovision.adapters.PopWindowAdapter;
import com.jovision.bean.Channel;
import com.jovision.bean.Device;
import com.jovision.commons.MyList;
import com.jovision.commons.MyLog;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.DeviceUtil;
import com.jovision.utils.PlayUtil;
import com.jovision.views.AlarmDialog;
import com.jovision.views.ImageViewPager;

/**
 * 我的设备
 */
public class JVMyDeviceFragment extends BaseFragment {

	private String TAG = "MyDeviceFragment";

	private static final int WHAT_SHOW_PRO = 0x01;// 显示dialog
	public static final int DEVICE_GETDATA_SUCCESS = 0x02;// 设备加载成功--
	public static final int DEVICE_GETDATA_FAILED = 0x03;// 设备加载失败--
	public static final int DEVICE_NO_DEVICE = 0x04;// 暂无设备--

	public static final int BROAD_DEVICE_LIST = 0x05;// 广播设备列表--
	public static final int BROAD_ADD_DEVICE = 0x06;// 添加设备的广播--
	public static final int BROAD_THREE_MINITE = 0x07;// 三分钟广播--

	public static final int AUTO_UPDATE = 0x08;// 2分钟自动刷新时间到--

	// private RefreshableView refreshableView;
	private PullToRefreshListView mPullRefreshListView;

	/** 叠加三个 布局 */
	private LinearLayout deviceLayout; // 设备列表界面
	private RelativeLayout refreshLayout; // 设备加载失败界面
	private LinearLayout quickSetSV; // 快速配置界面
	private Button quickSet;
	private Button addDevice;

	/** 广告位 */
	private LayoutInflater inflater;
	private View adView;
	private ImageViewPager imageScroll; // 图片容器
	private LinearLayout ovalLayout; // 圆点容器
	private List<View> listViews; // 图片组
	/** 弹出框 */
	private Dialog initDialog;// 显示弹出框
	private TextView dialogCancel;// 取消按钮
	private TextView dialogCompleted;// 确定按钮
	// 设备名称
	private TextView device_name;
	// 设备昵称
	private EditText device_nicket;
	// 设备昵称编辑键
	private ImageView device_niceet_cancle;
	// 设备用户名
	private EditText device_nameet;
	// 设备用户名编辑键
	private ImageView device_nameet_cancle;
	// 设备密码
	private EditText device_passwordet;
	// 设备密码编辑键
	private ImageView device_password_cancleI;
	private ImageView dialog_cancle_img;
	/** 设备列表 */
	private ListView myDeviceListView;
	private ArrayList<Device> myDeviceList = null;
	MyDeviceListAdapter myDLAdapter;
	/** 自动刷新 */
	private Timer updateTimer = null;
	private AutoUpdateTask updateTask;

	/** 3分钟广播 */
	private Timer broadTimer;
	private TimerTask broadTimerTask;

	// public boolean localFlag = false;// 本地登陆标志位
	public String devicename;

	public int broadTag = 0;
	private ArrayList<Device> broadList = new ArrayList<Device>();// 广播到的设备列表

	private PopupWindow popupWindow; // 声明PopupWindow对象；

	private ListView popListView;

	private String[] popFunArray;

	private PopWindowAdapter popWindowAdapter;

	private int[] popDrawarray = new int[] {
			R.drawable.mydevice_popwindowonse_icon,
			R.drawable.mydevice_popwindowtwo_icon,
			R.drawable.mydevice_popwindowthree_icon,
			R.drawable.mydevice_popwindowfour_icon, };
	private int[] popDrawarrayno = new int[] {
			R.drawable.mydevice_popwindowonse_icon,
			R.drawable.mydevice_popwindowtwo_icon,
			R.drawable.mydevice_popwindowthree_icon,
			R.drawable.mydevice_popwindowfour_icon,
			R.drawable.mydevice_popwindowfive_icon };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mydevice, container,
				false);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		boolean hasGot = Boolean.parseBoolean(mActivity.statusHashMap
				.get(Consts.HAG_GOT_DEVICE));
		if (!hasGot) {
			fragHandler.sendEmptyMessage(WHAT_SHOW_PRO);
		}
		mActivity = (BaseActivity) getActivity();
		mParent = getView();
		if (!Boolean.valueOf(((BaseActivity) mActivity).statusHashMap
				.get(Consts.LOCAL_LOGIN))) {
			popFunArray = mActivity.getResources().getStringArray(
					R.array.array_popno);
		} else {
			popFunArray = mActivity.getResources().getStringArray(
					R.array.array_pop);
		}
		currentMenu.setText(mActivity.getResources().getString(
				R.string.my_device));
		currentMenu.setText(R.string.my_device);

		devicename = mActivity.statusHashMap.get(Consts.KEY_USERNAME);
		inflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// refreshableView = (RefreshableView) mParent
		// .findViewById(R.id.device_refreshable_view);

		mPullRefreshListView = (PullToRefreshListView) getActivity()
				.findViewById(R.id.device_refreshable_view);

		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						String label = DateUtils.formatDateTime(getActivity(),
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);

						// Update the LastUpdatedLabel
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);

						fragHandler.sendEmptyMessage(WHAT_SHOW_PRO);

						GetDevTask task = new GetDevTask();
						String[] strParams = new String[3];
						task.execute(strParams);
					}
				});

		mPullRefreshListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						mActivity.showTextToast(R.string.end_list);
					}
				});

		adView = inflater.inflate(R.layout.ad_layout, null);

		deviceLayout = (LinearLayout) mParent.findViewById(R.id.devicelayout);
		refreshLayout = (RelativeLayout) mParent
				.findViewById(R.id.refreshlayout);
		quickSetSV = (LinearLayout) mParent
				.findViewById(R.id.quickinstalllayout);
		quickSet = (Button) mParent.findViewById(R.id.quickinstall);
		addDevice = (Button) mParent.findViewById(R.id.adddevice);
		refreshLayout.setOnClickListener(myOnClickListener);
		quickSet.setOnClickListener(myOnClickListener);
		addDevice.setOnClickListener(myOnClickListener);

		/** 广告条 */
		imageScroll = (ImageViewPager) adView.findViewById(R.id.imagescroll);
		// 防止广告图片变形
		RelativeLayout.LayoutParams reParams = new RelativeLayout.LayoutParams(
				mActivity.disMetrics.widthPixels,
				(int) (0.45 * mActivity.disMetrics.widthPixels));
		imageScroll.setLayoutParams(reParams);
		ovalLayout = (LinearLayout) adView.findViewById(R.id.dot_layout);
		InitViewPager();// 初始化图片
		// 开始滚动
		imageScroll.start(mActivity, listViews, 4000, ovalLayout,
				R.layout.dot_item, R.id.ad_item_v, R.drawable.dot_focused,
				R.drawable.dot_normal);

		myDLAdapter = new MyDeviceListAdapter(mActivity, this);
		myDeviceListView = mPullRefreshListView.getRefreshableView();
		myDeviceListView.addHeaderView(adView);
		rightBtn.setOnClickListener(myOnClickListener);

		// 非3G加广播设备
		if (!mActivity.is3G(false)) {
			broadTimer = new Timer();
			broadTimerTask = new TimerTask() {
				@Override
				public void run() {
					Log.v(TAG, "三分钟时间到--发广播");
					// while (0 != broadTag) {
					// try {
					// Thread.sleep(1000);
					// } catch (InterruptedException e) {
					// e.printStackTrace();
					// }
					// }
					broadTag = BROAD_THREE_MINITE;
					PlayUtil.broadCast(mActivity);
				}
			};
			broadTimer.schedule(broadTimerTask, 5 * 60 * 1000, 5 * 60 * 1000);
		}

		if (hasGot) {
			myDeviceList = CacheUtil.getDevList();
			refreshList();
		} else {
			fragHandler.sendEmptyMessage(WHAT_SHOW_PRO);
			GetDevTask task = new GetDevTask();
			String[] strParams = new String[3];
			task.execute(strParams);
		}

		if (!Boolean.valueOf(((BaseActivity) mActivity).statusHashMap
				.get(Consts.LOCAL_LOGIN))) {
			startAutoRefreshTimer();
		}

	}

	public void startAutoRefreshTimer() {
		// 两分钟自动刷新设备列表
		updateTask = new AutoUpdateTask();
		if (null != updateTimer) {
			updateTimer.cancel();
		}

		updateTimer = new Timer();
		if (null != updateTimer) {
			updateTimer.schedule(updateTask, 2 * 60 * 1000, 2 * 60 * 1000);
		}
	}

	public void stopRefreshWifiTimer() {
		if (null != updateTimer) {
			updateTimer.cancel();
			updateTimer = null;
		}
		if (null != updateTask) {
			updateTask.cancel();
			updateTask = null;
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	}

	OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.dialog_cancle_img:
				initDialog.dismiss();
				break;
			case R.id.btn_right:
				initPop();
				// 点击按钮时，pop显示状态，显示中就消失，否则显示
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				} else {
					// 显示在below正下方
					popupWindow.showAsDropDown(view,
							(mActivity.disMetrics.widthPixels / 2), 10);
				}
				break;
			case R.id.device_nameet_cancle:
				device_nameet.setText("");
				break;
			case R.id.device_passwrodet_cancle:
				device_passwordet.setText("");
				break;
			case R.id.device_nicket_cancle:
				device_nicket.setText("");
				break;
			case R.id.quickinstall:
				((ShakeActivity) mActivity).startSearch(false);
				break;
			case R.id.adddevice:
				Intent addIntent = new Intent();
				addIntent.setClass(mActivity, JVAddDeviceActivity.class);
				addIntent.putExtra("QR", false);
				mActivity.startActivity(addIntent);
				break;
			case R.id.refreshlayout: {
				fragHandler.sendEmptyMessage(WHAT_SHOW_PRO);

				GetDevTask task = new GetDevTask();
				String[] strParams = new String[3];
				task.execute(strParams);
				break;
			}
			default:
				break;
			}

		}

	};

	// 点击加号弹出的popWindow
	private void initPop() {
		View v = LayoutInflater.from(mActivity).inflate(R.layout.popview, null); // 将布局转化为view
		popListView = (ListView) v.findViewById(R.id.popwindowlist);
		popWindowAdapter = new PopWindowAdapter(JVMyDeviceFragment.this);
		if (!Boolean.valueOf(((BaseActivity) mActivity).statusHashMap
				.get(Consts.LOCAL_LOGIN))) {
			popWindowAdapter.setData(popFunArray, popDrawarray);
		} else {
			popWindowAdapter.setData(popFunArray, popDrawarrayno);
		}
		popListView.setAdapter(popWindowAdapter);

		if (popupWindow == null) {
			/**
			 * public PopupWindow (View contentView, int width, int height)
			 * contentView:布局view width：布局的宽 height：布局的高
			 */
			popupWindow = new PopupWindow(v,
					mActivity.disMetrics.widthPixels / 2,
					LayoutParams.WRAP_CONTENT);
		}
		popupWindow.setFocusable(true); // 获得焦点
		popupWindow.setOutsideTouchable(true);// 是否可点击

		popupWindow.getContentView().setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				popupWindow.setFocusable(false); // 失去焦点
				popupWindow.dismiss(); // pop消失
				return false;
			}
		});
		popListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				switch (position) {
				case 0: {// 云视通号
					Intent addIntent = new Intent();
					addIntent.setClass(mActivity, JVAddDeviceActivity.class);
					addIntent.putExtra("QR", false);
					mActivity.startActivity(addIntent);
					break;
				}
				case 1: {// 二维码扫描
					Intent addIntent = new Intent();
					addIntent.setClass(mActivity, JVAddDeviceActivity.class);
					addIntent.putExtra("QR", true);
					mActivity.startActivity(addIntent);
					break;
				}
				case 2: {// 无线设备
					((ShakeActivity) mActivity).startSearch(false);
					break;
				}
				case 3: {// 局域网设备
					fragHandler.sendEmptyMessage(WHAT_SHOW_PRO);
					if (!mActivity.is3G(false)) {// 3G网提示不支持
						broadTag = BROAD_ADD_DEVICE;
						broadList.clear();
						PlayUtil.broadCast(mActivity);
					} else {
						((BaseActivity) mActivity)
								.showTextToast(R.string.notwifi_forbid_func);
					}
					break;
				}
				case 4: {// IP/域名设备
					Intent intent = new Intent();
					intent.setClass(mActivity, JVAddIpDeviceActivity.class);
					mActivity.startActivity(intent);
					break;
				}

				}
				popupWindow.dismiss(); // pop消失
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		boolean hasGot = Boolean.parseBoolean(mActivity.statusHashMap
				.get(Consts.HAG_GOT_DEVICE));
		if (hasGot) {
			myDeviceList = CacheUtil.getDevList();
			refreshList();
		}
	}

	/**
	 * 刷新列表
	 */
	public void refreshList() {
		String stateStr = ((BaseActivity) mActivity).statusHashMap
				.get(Consts.DATA_LOADED_STATE);
		if (null != stateStr) {
			refreshLayout.setVisibility(View.VISIBLE);
			deviceLayout.setVisibility(View.GONE);
			quickSetSV.setVisibility(View.GONE);
		} else {
			if (null == myDeviceList) {
				refreshLayout.setVisibility(View.VISIBLE);
				deviceLayout.setVisibility(View.GONE);
				quickSetSV.setVisibility(View.GONE);
			} else if (0 == myDeviceList.size()) {
				refreshLayout.setVisibility(View.GONE);
				deviceLayout.setVisibility(View.GONE);
				quickSetSV.setVisibility(View.VISIBLE);
			} else if (myDeviceList.size() > 0) {
				refreshLayout.setVisibility(View.GONE);
				deviceLayout.setVisibility(View.VISIBLE);
				quickSetSV.setVisibility(View.GONE);
				myDLAdapter.setData(myDeviceList);
				myDeviceListView.setAdapter(myDLAdapter);
				myDLAdapter.notifyDataSetChanged();
			}
		}

		// String stateStr = ((BaseActivity) mActivity).statusHashMap
		// .get(Consts.DATA_LOADED_STATE);
		// if(null != stateStr){
		// int state = Integer.parseInt(stateStr);
		// if (-1 == state) {
		// refreshLayout.setVisibility(View.VISIBLE);
		// deviceLayout.setVisibility(View.GONE);
		// quickSetSV.setVisibility(View.GONE);
		// } else if (0 == state) {
		// refreshLayout.setVisibility(View.GONE);
		// deviceLayout.setVisibility(View.GONE);
		// quickSetSV.setVisibility(View.VISIBLE);
		// } else if (1 == state) {
		// refreshLayout.setVisibility(View.GONE);
		// deviceLayout.setVisibility(View.VISIBLE);
		// quickSetSV.setVisibility(View.GONE);
		// myDLAdapter.setData(myDeviceList);
		// myDeviceListView.setAdapter(myDLAdapter);
		// myDLAdapter.notifyDataSetChanged();
		// }
		//
		// }

	}

	@Override
	public void onDestroy() {
		stopRefreshWifiTimer();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		sortList();
		CacheUtil.saveDevList(myDeviceList);
		// imageScroll.stopTimer();
	}

	@Override
	public void onStop() {
		super.onStop();
		// imageScroll.stopTimer();
	}

	/**
	 * 初始化图片
	 */
	private void InitViewPager() {
		listViews = new ArrayList<View>();
		int[] imageResId = new int[] { R.drawable.a, R.drawable.b };
		for (int i = 0; i < imageResId.length; i++) {
			ImageView imageView = new ImageView(mActivity);
			imageView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {// 设置图片点击事件
					// ((BaseActivity) mActivity).showTextToast("点击了:"
					// + imageScroll.getCurIndex());
				}
			});
			imageView.setImageResource(imageResId[i]);
			imageView.setScaleType(ScaleType.FIT_CENTER);
			listViews.add(imageView);
		}
	}

	@Override
	public void onNotify(int what, int arg1, int arg2, Object obj) {
		fragHandler.sendMessage(fragHandler
				.obtainMessage(what, arg1, arg2, obj));
	}

	@Override
	public void onHandler(int what, int arg1, int arg2, Object obj) {
		MyLog.v("JVMyDeviceFragment", "onTabAction:what=" + what + ";arg1="
				+ arg1 + ";arg2=" + arg1);
		switch (what) {
		case AUTO_UPDATE: {
			GetDevTask task = new GetDevTask();
			String[] strParams = new String[3];
			task.execute(strParams);
			break;
		}
		case WHAT_SHOW_PRO: {
			((BaseActivity) mActivity).createDialog("");
			break;
		}
		case JVTabActivity.TAB_BACK: {// tab 返回事件，保存数据
			sortList();
			CacheUtil.saveDevList(myDeviceList);
			break;
		}

		// 广播回调
		case Consts.CALL_LAN_SEARCH: {
			Log.v("广播-----回调", "what=" + what + ";arg1=" + arg1 + ";arg2="
					+ arg1 + ";obj=" + obj.toString());
			// MyLog.v("广播回调", "onTabAction2:what=" + what + ";arg1=" + arg1
			// + ";arg2=" + arg1 + ";obj=" + obj.toString());
			// onTabAction:what=168;arg1=0;arg2=0;obj={"count":1,"curmod":0,"gid":"A","ip":"192.168.21.238","netmod":0,"no":283827713,"port":9101,"timeout":0,"type":59162,"variety":3}
			if (broadTag == BROAD_DEVICE_LIST || broadTag == BROAD_THREE_MINITE) {// 三分钟广播
																					// 或
																					// 广播设备列表
				JSONObject broadObj;
				try {
					broadObj = new JSONObject(obj.toString());
					if (0 == broadObj.optInt("timeout")) {

						String gid = broadObj.optString("gid");
						int no = broadObj.optInt("no");

						if (0 == no) {
							return;
						}
						String ip = broadObj.optString("ip");
						int port = broadObj.optInt("port");
						String broadDevNum = gid + no;

						hasDev(myDeviceList, broadDevNum, ip, port);

					} else if (1 == broadObj.optInt("timeout")) {
						broadTag = 0;
						sortList();
					}
					MyLog.v(TAG, "onTabAction1:what=" + what + ";arg1=" + arg1
							+ ";arg2=" + arg1 + ";obj=" + obj.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}

			} else if (broadTag == BROAD_ADD_DEVICE) {// 广播添加设备
				JSONObject broadObj;
				try {
					broadObj = new JSONObject(obj.toString());

					MyLog.v("广播回调--add", broadObj.optInt("timeout") + "");

					if (0 == broadObj.optInt("timeout")) {
						MyLog.v("广播回调-0-add", broadObj.optInt("timeout") + "");
						String gid = broadObj.optString("gid");
						int no = broadObj.optInt("no");
						if (0 == no) {
							return;
						}

						String ip = broadObj.optString("ip");
						int port = broadObj.optInt("port");
						int count = broadObj.optInt("count");
						String broadDevNum = gid + no;

						// 广播列表和设备列表里面都没有这个设备
						if (!hasDev(broadList, broadDevNum, ip, port)
								&& !hasDev(myDeviceList, broadDevNum, ip, port)) {
							Device broadDev = new Device(ip, port, gid, no,
									mActivity.getResources().getString(
											R.string.str_default_user),
									mActivity.getResources().getString(
											R.string.str_default_pass), false,
									count, 0);
							broadDev.setOnlineState(1);// 广播都在线
							broadList.add(broadDev);
							MyLog.v(TAG, "广播到一个设备--" + broadDevNum);
						}

					} else if (1 == broadObj.optInt("timeout")) {
						broadTag = 0;
						MyLog.v("广播回调-1-add", broadObj.optInt("timeout") + "");
						mActivity.dismissDialog();

						if (null != broadList && 0 != broadList.size()) {
							alertAddDialog();
						} else {
							mActivity.showTextToast(R.string.broad_zero);
						}
					}
					MyLog.v(TAG, "onTabAction2:what=" + what + ";arg1=" + arg1
							+ ";arg2=" + arg1 + ";obj=" + obj.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			break;
		}
		case MyDeviceListAdapter.DEVICE_ITEM_CLICK: {// 设备单击事件
			myDLAdapter.setShowDelete(false);
			myDLAdapter.notifyDataSetChanged();
			Device dev = myDeviceList.get(arg1);
			if (0 == dev.getChannelList().size()) {// 0个通道直接播放
				mActivity.showTextToast(R.string.selectone_to_connect);
			} else if (1 == dev.getChannelList().size()) {// 1个通道直接播放

				if (0 == myDeviceList.get(arg1).getOnlineState()
						&& !Boolean
								.valueOf(((BaseActivity) mActivity).statusHashMap
										.get(Consts.LOCAL_LOGIN))) {
					mActivity.showTextToast(R.string.offline_not_play);
				} else {
					// sortList(myDeviceList);
					ArrayList<Device> playList = PlayUtil
							.prepareConnect(
									myDeviceList,
									arg1,
									Boolean.valueOf(((BaseActivity) mActivity).statusHashMap
											.get(Consts.LOCAL_LOGIN)));

					if (null == playList || 0 == playList.size()) {
						mActivity.showTextToast(R.string.selectone_to_connect);
					} else {
						Intent intentPlay = new Intent(mActivity,
								JVPlayActivity.class);
						intentPlay.putExtra(Consts.KEY_PLAY_NORMAL,
								playList.toString());
						intentPlay.putExtra("PlayFlag", Consts.PLAY_NORMAL);
						intentPlay.putExtra("DeviceIndex", PlayUtil
								.getPlayIndex(playList, myDeviceList.get(arg1)
										.getFullNo()));
						intentPlay.putExtra("ChannelofChannel", dev
								.getChannelList().toList().get(0).getChannel());
						mActivity.startActivity(intentPlay);
					}

				}

			} else {// 多个通道查看通道列表
				Intent intentPlay = new Intent(mActivity,
						JVChannelsActivity.class);
				intentPlay.putExtra("DeviceIndex", arg1);
				mActivity.startActivity(intentPlay);
			}

			break;
		}
		case MyDeviceListAdapter.DEVICE_ITEM_LONG_CLICK: {// 设备长按事件
			myDLAdapter.setShowDelete(true);
			myDLAdapter.notifyDataSetChanged();
			break;
		}
		case MyDeviceListAdapter.DEVICE_ITEM_DEL_CLICK: {// 设备删除事件
			DelDevTask task = new DelDevTask();
			String[] strParams = new String[3];
			strParams[0] = String.valueOf(arg1);
			task.execute(strParams);
			break;
		}
		case MyDeviceListAdapter.DEVICE_EDIT_CLICK: {// 设备编辑事件
			myDLAdapter.setShowDelete(false);
			initSummaryDialog(myDeviceList, arg1);
		}
			break;
		case Consts.PUSH_MESSAGE:
			// 弹出对话框
			//
			// ArrayList<Device> deviceList = CacheUtil.getDevList();
			// MyLog.v("Alarm", "prepareConnect 00--" + deviceList.toString());
			if (null != mActivity) {
				new AlarmDialog(mActivity).Show(obj);
			} else {
				MyLog.e("Alarm",
						"onHandler mActivity is null ,so dont show the alarm dialog");
			}
			break;
		}
	}

	/**
	 * 判断设备是否在设备列表里
	 * 
	 * @param devNum
	 * @return
	 */
	public boolean hasDev(ArrayList<Device> devList, String devNum, String ip,
			int port) {
		boolean has = false;
		// for (int i = 0; i < size; i++) {
		// Device device = myDeviceList.get(i);
		for (Device dev : devList) {
			if (devNum.equalsIgnoreCase(dev.getFullNo())) {
				dev.setIp(ip);
				dev.setPort(port);
				dev.setOnlineState(1);// 广播都在线
				has = true;
				break;
			}
		}
		return has;
	}

	/** 弹出框初始化 */
	private void initSummaryDialog(ArrayList<Device> myDeviceList,
			final int agr1) {
		initDialog = new Dialog(mActivity, R.style.mydialog);
		View view = LayoutInflater.from(mActivity).inflate(
				R.layout.dialog_summary, null);
		initDialog.setContentView(view);
		dialog_cancle_img = (ImageView) view
				.findViewById(R.id.dialog_cancle_img);
		dialogCancel = (TextView) view.findViewById(R.id.dialog_cancel);
		dialogCompleted = (TextView) view.findViewById(R.id.dialog_completed);
		device_name = (TextView) view.findViewById(R.id.device_namew);
		device_nicket = (EditText) view.findViewById(R.id.device_nicket);
		device_niceet_cancle = (ImageView) view
				.findViewById(R.id.device_nicket_cancle);
		device_nameet = (EditText) view.findViewById(R.id.device_nameet);
		device_nameet_cancle = (ImageView) view
				.findViewById(R.id.device_nameet_cancle);
		device_passwordet = (EditText) view
				.findViewById(R.id.device_passwrodet);
		device_password_cancleI = (ImageView) view
				.findViewById(R.id.device_passwrodet_cancle);
		dialog_cancle_img.setOnClickListener(myOnClickListener);
		device_nameet_cancle.setOnClickListener(myOnClickListener);
		device_niceet_cancle.setOnClickListener(myOnClickListener);
		device_password_cancleI.setOnClickListener(myOnClickListener);
		device_name.setText(myDeviceList.get(agr1).getFullNo());
		device_nameet.setText(myDeviceList.get(agr1).getUser());
		device_passwordet.setText(myDeviceList.get(agr1).getPwd());
		if (!("").equals(myDeviceList.get(agr1).getNickName())) {
			device_nicket.setText(myDeviceList.get(agr1).getNickName());
		}
		initDialog.show();
		device_name.setFocusable(true);
		device_name.setFocusableInTouchMode(true);
		dialogCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myDLAdapter.notifyDataSetChanged();
				initDialog.dismiss();
			}
		});
		dialogCompleted.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 设备昵称不为空
				if ("".equalsIgnoreCase(device_nicket.getText().toString())) {
					mActivity.showTextToast(mActivity.getResources().getString(
							R.string.str_nikename_notnull));
				}
				// 设备昵称验证
				else if (!ConfigUtil.checkNickName(device_nicket.getText()
						.toString())) {
					mActivity.showTextToast(mActivity.getResources().getString(
							R.string.login_str_nike_name_order));
				}
				// 设备用户名不为空
				else if (""
						.equalsIgnoreCase(device_nameet.getText().toString())) {
					mActivity
							.showTextToast(R.string.login_str_device_account_notnull);
				}
				// 设备用户名验证
				else if (!ConfigUtil.checkDeviceUsername(device_nameet
						.getText().toString())) {
					mActivity.showTextToast(mActivity.getResources().getString(
							R.string.login_str_device_account_error));
				} else if (!ConfigUtil.checkDevicePwd(device_passwordet
						.getText().toString())) {
					mActivity.showTextToast(mActivity.getResources().getString(
							R.string.login_str_device_pass_error));
				} else {
					ModifyDevTask task = new ModifyDevTask();
					String[] strParams = new String[5];
					strParams[0] = agr1 + "";
					strParams[1] = device_name.getText().toString();
					strParams[2] = device_nameet.getText().toString();
					strParams[3] = device_passwordet.getText().toString();
					strParams[4] = device_nicket.getText().toString();
					task.execute(strParams);
				}
			}
		});
	}

	// 保存更改设备信息线程
	class ModifyDevTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
		// 可变长的输入参数，与AsyncTask.exucute()对应
		@Override
		protected Integer doInBackground(String... params) {
			int delRes = -1;
			try {
				int delIndex = Integer.parseInt(params[0]);
				if (Boolean.valueOf(((BaseActivity) mActivity).statusHashMap
						.get(Consts.LOCAL_LOGIN))) {// 本地保存修改信息
					delRes = 0;
				} else {
					String name = mActivity.statusHashMap
							.get(Consts.KEY_USERNAME);
					delRes = DeviceUtil.modifyDevice(name, params[1],
							params[4], params[2], params[3]);
				}
				if (0 == delRes) {
					myDeviceList.get(delIndex).setFullNo(params[1]);
					myDeviceList.get(delIndex).setUser(params[2]);
					myDeviceList.get(delIndex).setPwd(params[3]);
					myDeviceList.get(delIndex).setNickName(params[4]);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return delRes;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
			((BaseActivity) mActivity).dismissDialog();
			if (0 == result) {
				((BaseActivity) mActivity)
						.showTextToast(R.string.login_str_device_edit_success);
				myDLAdapter.setShowDelete(false);
				myDLAdapter.notifyDataSetChanged();
			} else {
				((BaseActivity) mActivity)
						.showTextToast(R.string.login_str_device_edit_failed);
			}
			initDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
			((BaseActivity) mActivity).createDialog("");
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// 更新进度,此方法在主线程执行，用于显示任务执行的进度。
		}
	}

	// 删除设备线程
	class DelDevTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
		// 可变长的输入参数，与AsyncTask.exucute()对应
		@Override
		protected Integer doInBackground(String... params) {
			int delRes = -1;
			boolean localFlag = Boolean
					.valueOf(((BaseActivity) mActivity).statusHashMap
							.get(Consts.LOCAL_LOGIN));
			try {
				int delIndex = Integer.parseInt(params[0]);
				if (localFlag) {// 本地删除
					delRes = 0;
				} else {
					delRes = DeviceUtil.unbindDevice(
							((BaseActivity) mActivity).statusHashMap
									.get("KEY_USERNAME"),
							myDeviceList.get(delIndex).getFullNo());
				}

				if (0 == delRes) {
					myDeviceList.remove(delIndex);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return delRes;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
			((BaseActivity) mActivity).dismissDialog();
			sortList();
			CacheUtil.saveDevList(myDeviceList);
			if (0 == result) {
				((BaseActivity) mActivity)
						.showTextToast(R.string.del_device_succ);
				myDLAdapter.setShowDelete(false);
				refreshList();
			} else {
				((BaseActivity) mActivity)
						.showTextToast(R.string.del_device_failed);
			}
		}

		@Override
		protected void onPreExecute() {
			// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
			((BaseActivity) mActivity).createDialog("");
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// 更新进度,此方法在主线程执行，用于显示任务执行的进度。
		}
	}

	// 获取设备列表线程
	class GetDevTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
		// 可变长的输入参数，与AsyncTask.exucute()对应
		@Override
		protected Integer doInBackground(String... params) {
			int getRes = 0;
			try {
				if (!Boolean.valueOf(((BaseActivity) mActivity).statusHashMap
						.get(Consts.LOCAL_LOGIN))) {// 非本地登录，无论是否刷新都执行
					// 获取所有设备列表和通道列表 ,如果设备请求失败，多请求一次
					if (null == myDeviceList || 0 == myDeviceList.size()) {
						myDeviceList = DeviceUtil
								.getUserDeviceList(mActivity.statusHashMap
										.get(Consts.KEY_USERNAME));
					} else {
						DeviceUtil.refreshDeviceState(mActivity.statusHashMap
								.get(Consts.KEY_USERNAME), myDeviceList);
					}
					if (null == myDeviceList || 0 == myDeviceList.size()) {
						myDeviceList = DeviceUtil
								.getUserDeviceList(mActivity.statusHashMap
										.get(Consts.KEY_USERNAME));
					}
					if (null != myDeviceList && 0 != myDeviceList.size()) {
						ArrayList<Channel> channelList = DeviceUtil
								.getUserPointList();

						if (null == channelList || 0 == channelList.size()) {
							channelList = DeviceUtil.getUserPointList();
						}

						if (null != channelList && 0 != channelList.size()) {
							for (Device dev : myDeviceList) {
								MyList<Channel> chanList = new MyList<Channel>(
										1);
								for (Channel channel : channelList) {
									if (channel.isHasFind()) {
										continue;
									}
									if (channel.getDguid().equalsIgnoreCase(
											dev.getFullNo())) {
										chanList.add(channel);
										channel.setHasFind(true);
									}
								}
								dev.setChannelList(chanList);
							}
						}

					}
					if (null != myDeviceList && 0 != myDeviceList.size()) {
						sortList();
					}
					CacheUtil.saveDevList(myDeviceList);
				} else if (Boolean
						.valueOf(((BaseActivity) mActivity).statusHashMap
								.get(Consts.LOCAL_LOGIN))) {// 本地登录
					myDeviceList = CacheUtil.getDevList();
				}

				if (null == myDeviceList) {
					((BaseActivity) mActivity).statusHashMap.put(
							Consts.DATA_LOADED_STATE, "-1");
				} else {
					((BaseActivity) mActivity).statusHashMap.put(
							Consts.DATA_LOADED_STATE, null);
				}

				mActivity.statusHashMap.put(Consts.HAG_GOT_DEVICE, "true");
				if (null != myDeviceList && 0 != myDeviceList.size()) {// 获取设备成功,去广播设备列表
					getRes = DEVICE_GETDATA_SUCCESS;
				} else if (null != myDeviceList && 0 == myDeviceList.size()) {// 无数据
					getRes = DEVICE_NO_DEVICE;
				} else {// 获取设备失败
					getRes = DEVICE_GETDATA_FAILED;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return getRes;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
			((BaseActivity) mActivity).dismissDialog();
			// refreshableView.finishRefreshing();
			mPullRefreshListView.onRefreshComplete();
			switch (result) {
			// 从服务器端获取设备成功
			case DEVICE_GETDATA_SUCCESS: {
				mActivity.statusHashMap.put(Consts.HAG_GOT_DEVICE, "true");
				// 给设备列表设置小助手
				PlayUtil.setHelperToList(myDeviceList);
				// while (0 != broadTag) {
				// try {
				// Thread.sleep(1000);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				// }
				broadTag = BROAD_DEVICE_LIST;
				PlayUtil.broadCast(mActivity);
				refreshList();
				break;
			}
			// 从服务器端获取设备成功，但是没有设备
			case DEVICE_NO_DEVICE: {
				MyLog.v(TAG, "nonedata-too");
				// while (0 != broadTag) {
				// try {
				// Thread.sleep(1000);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				// }
				// broadTag = BROAD_DEVICE_LIST;
				// PlayUtil.broadCast(mActivity);
				refreshList();
				break;
			}
			// 从服务器端获取设备失败
			case DEVICE_GETDATA_FAILED: {
				mActivity.showTextToast(R.string.get_device_failed);
				refreshList();
				break;
			}
			}
		}

		@Override
		protected void onPreExecute() {
			// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
			// ((BaseActivity) mActivity).createDialog("");
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// 更新进度,此方法在主线程执行，用于显示任务执行的进度。
		}
	}

	// 设置三种类型参数分别为String,Integer,String
	class AddDevTask extends AsyncTask<String, Integer, Integer> {
		// 可变长的输入参数，与AsyncTask.exucute()对应
		@Override
		protected Integer doInBackground(String... params) {
			int addRes = -1;
			ArrayList<Device> list = new ArrayList<Device>();// 广播到的设备列表
			list = Device.fromJsonArray(params[0]);
			try {
				for (Device addDev : list) {

					if (myDeviceList.size() >= 100
							&& !Boolean
									.valueOf(((BaseActivity) mActivity).statusHashMap
											.get(Consts.LOCAL_LOGIN))) {// 非本地多于100个设备不让再添加
						addRes = 100;
						break;
					}
					if (null != addDev) {
						if (Boolean
								.valueOf(((BaseActivity) mActivity).statusHashMap
										.get(Consts.LOCAL_LOGIN))) {// 本地添加
							addRes = 0;
						} else {
							addRes = DeviceUtil
									.addDevice(mActivity.statusHashMap
											.get("KEY_USERNAME"), addDev);
							if (0 <= addDev.getChannelList().size()) {
								if (0 == DeviceUtil.addPoint(
										addDev.getFullNo(), addDev
												.getChannelList().size())) {
									addDev.setChannelList(DeviceUtil
											.getDevicePointList(addDev,
													addDev.getFullNo()));
									addRes = 0;
								} else {
									DeviceUtil.unbindDevice(
											mActivity.statusHashMap
													.get(Consts.KEY_USERNAME),
											addDev.getFullNo());
									addRes = -1;
								}
							}
						}
					}
					if (0 == addRes) {
						myDeviceList.add(0, addDev);
					}
				}
				DeviceUtil.refreshDeviceState(
						mActivity.statusHashMap.get(Consts.KEY_USERNAME),
						myDeviceList);

			} catch (Exception e) {
				e.printStackTrace();
			}
			return addRes;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
			sortList();
			CacheUtil.saveDevList(myDeviceList);
			((BaseActivity) mActivity).dismissDialog();
			if (0 == result) {
				refreshList();
				mActivity.showTextToast(R.string.add_device_succ);
			} else if (100 == result) {
				mActivity.showTextToast(R.string.str_device_most_count);
			} else {
				refreshList();
				myDLAdapter.setData(myDeviceList);
				myDeviceListView.setAdapter(myDLAdapter);
				mActivity.showTextToast(R.string.add_device_failed);
			}
		}

		@Override
		protected void onPreExecute() {
			// 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// 更新进度,此方法在主线程执行，用于显示任务执行的进度。
		}
	}

	/**
	 * 弹搜出来几个设备界面
	 * */
	public void alertAddDialog() {
		// 提示对话框
		AlertDialog.Builder builder = new Builder(mActivity);
		builder.setTitle(R.string.tips)
				.setMessage(
						getResources().getString(R.string.add_broad_dev)
								.replaceFirst("!",
										String.valueOf(broadList.size())))
				.setPositiveButton(R.string.sure,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mActivity.createDialog("");
								AddDevTask task = new AddDevTask();
								String[] strParams = new String[3];
								strParams[0] = broadList.toString();
								task.execute(strParams);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						}).create().show();
	}

	// 根据在线状态排序
	private void sortList() {
		if (null == myDeviceList || 0 == myDeviceList.size()) {
			return;
		}
		if (!Boolean.valueOf(((BaseActivity) mActivity).statusHashMap
				.get(Consts.LOCAL_LOGIN))) {
			ArrayList<Device> onlineDevice = new ArrayList<Device>();
			ArrayList<Device> offlineDevice = new ArrayList<Device>();
			for (Device dev : myDeviceList) {
				if (0 == dev.getOnlineState()) {
					offlineDevice.add(dev);
				} else {
					onlineDevice.add(dev);
				}
			}
			myDeviceList.clear();
			myDeviceList.addAll(onlineDevice);
			myDeviceList.addAll(offlineDevice);
		}
	}

	/**
	 * 自动刷新
	 * 
	 * @author Administrator
	 * 
	 */
	class AutoUpdateTask extends TimerTask {

		@Override
		public void run() {
			CacheUtil.saveDevList(myDeviceList);
			myDeviceList = CacheUtil.getDevList();
			fragHandler.sendMessage(fragHandler.obtainMessage(AUTO_UPDATE));
		}

	}
}
