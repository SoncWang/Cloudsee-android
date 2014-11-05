package com.jovision.activities;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jovetech.CloudSee.temp.R;
import com.jovision.Consts;
import com.jovision.bean.Device;
import com.jovision.commons.JVConst;
import com.jovision.commons.MyLog;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.GetPhoneNumber;
import com.jovision.utils.mails.MailSenderInfo;
import com.jovision.views.AlarmDialog;

public class JVFeedbackActivity extends BaseActivity {

	private Button back; // 后退
	private TextView title; // 标题
	private Button commit; // 提交按钮
	private EditText content; // 意见反馈内容
	private TextView wordsNum; // 文字数量统计
	private GetPhoneNumber phone;// 验证手机号码

	private EditText connection; // 意见反馈联系方式
	private int number = 256;

	@Override
	public void onHandler(int what, int arg1, int arg2, Object obj) {
		switch (what) {
		case JVConst.FEEDBACK_SUCCESS:// 反馈成功
			AlertDialog.Builder builder = new AlertDialog.Builder(
					JVFeedbackActivity.this);
			builder.setMessage(R.string.str_commit_success);
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							JVFeedbackActivity.this.finish();
							dialog.dismiss();
						}
					});
			builder.create().show();
			break;

		case JVConst.FEEDBACK_FAILED:// 反馈失败
			// 提交建议失败，什么也不做
			break;
		case Consts.PUSH_MESSAGE:
			// 弹出对话框
			new AlarmDialog(this).Show(obj);
			break;
		}
	}

	@Override
	public void onNotify(int what, int arg1, int arg2, Object obj) {
		handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
	}

	@Override
	protected void initSettings() {

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void initUi() {
		setContentView(R.layout.feedback_layout);
		back = (Button) findViewById(R.id.btn_left);
		title = (TextView) findViewById(R.id.currentmenu);
		commit = (Button) findViewById(R.id.btn_right);
		content = (EditText) findViewById(R.id.content);
		wordsNum = (TextView) findViewById(R.id.wordsnum);
		back.setVisibility(View.VISIBLE);
		commit.setVisibility(View.VISIBLE);

		// back.setTextColor(Color.WHITE);
		// back.setBackgroundDrawable(getResources().getDrawable(
		// R.drawable.setting_save));
		//
		// commit.setTextColor(Color.WHITE);
		// commit.setBackgroundDrawable(getResources().getDrawable(
		// R.drawable.setting_save));

		// commit.setTextColor(getResources().getColor(R.color.white));
		back.setText(getResources().getString(R.string.cancel));
		commit.setText(getResources().getString(R.string.commit));

		connection = (EditText) findViewById(R.id.connectway);

		// back.setBackgroundDrawable(getResources().getDrawable(
		// R.drawable.send_btn_selector_1));
		// commit.setBackgroundDrawable(getResources().getDrawable(
		// R.drawable.send_btn_selector_2));
		title.setText(getResources().getString(R.string.str_idea_and_feedback));
		back.setOnClickListener(myOnClickListener);
		commit.setOnClickListener(myOnClickListener);
		wordsNum.setOnClickListener(myOnClickListener);
		content.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				temp = s;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				// 设置内容不得超过字数限制，超过时删除多余文字
				int num = number - s.length();
				wordsNum.setText(num + "");
				selectionStart = content.getSelectionStart();
				selectionEnd = content.getSelectionEnd();
				if (temp.length() > number) {
					s.delete(selectionStart - (temp.length() - number),
							selectionEnd);
					int tempSelection = selectionEnd;
					content.setText(s);
					content.setSelection(tempSelection);// 设置光标在最后
				}
			}
		});

	}

	OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_left:
				finish();
				break;
			case R.id.btn_right:
				String connectStr = connection.getText().toString();// 联系方式
				String contentStr = content.getText().toString();// 反馈内容
				phone = new GetPhoneNumber(connectStr);
				if (phone.matchNum() == 4 || phone.matchNum() == 5) {
					showTextToast(R.string.str_notice_connection);
				} else {
					if (0 == content.getText().toString().length()
							|| ("").equals(connectStr)) {
						if (0 == content.getText().toString().length()) {
							showTextToast(R.string.str_notice_content);
						} else {
							showTextToast(R.string.str_notice_connection);
						}

					} else {
						createDialog("");
						if (0 != connectStr.length()) {
							contentStr += "\n\r 联系方式" + connectStr;
						}

						FeedbackThread feedbackThread = new FeedbackThread(
								contentStr);
						feedbackThread.start();
					}
				}
				break;
			case R.id.wordsnum:
				content.setText("");
			default:
				break;
			}
		}

	};

	class FeedbackThread extends Thread {

		String content;

		public FeedbackThread(String contents) {
			content = contents;
		}

		@Override
		public void run() {
			super.run();

			try {
				Calendar rightNow = Calendar.getInstance();
				String str = rightNow.get(Calendar.YEAR)
						+ "_"
						+ (rightNow.get(Calendar.MONTH) + 1 + "_" + rightNow
								.get(Calendar.DATE));
				MailSenderInfo mailInfo = new MailSenderInfo();
				mailInfo.setMailServerHost("smtp.qq.com");
				mailInfo.setMailServerPort("25");
				mailInfo.setValidate(true);
				mailInfo.setUserName("741376209@qq.com"); // 你的邮箱地址
				mailInfo.setPassword("mfq_zsw");// 您的邮箱密码
				mailInfo.setFromAddress("741376209@qq.com");
				mailInfo.setToAddress("suifupeng@jovision.com");// jovetech1203**
				// mailInfo.setToAddress("jy0329@163.com");
				mailInfo.setSubject("["
						+ getResources().getString(R.string.app_name)
						+ "]"
						+ getResources().getString(R.string.str_feedback)
						+ getResources()
								.getString(R.string.str_current_version));
				mailInfo.setContent(content + mobileInfo() + encryptInfo1());
				MyLog.e("feedback=", content + mobileInfo() + encryptInfo1());
				String[] receivers = new String[] { "juyang@jovision.com",
						"mfq@jovision.com" };
				String[] ccs = receivers;
				mailInfo.setReceivers(receivers);
				mailInfo.setCcs(ccs);
				// 这个类主要来发送邮件
				// BaseApp.sendMailtoMultiReceiver(mailInfo);
				// SimpleMailSender sms = new SimpleMailSender();
				boolean flag = ConfigUtil.sendMailtoMultiReceiver(mailInfo);// 发送文体格式
				// sms.sendHtmlMail(mailInfo);//发送html格式
				if (flag) {
					handler.sendMessage(handler
							.obtainMessage(JVConst.FEEDBACK_SUCCESS));// 反馈成功
				} else {
					handler.sendMessage(handler
							.obtainMessage(JVConst.FEEDBACK_FAILED)); // 反馈失败
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			JVFeedbackActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void saveSettings() {

	}

	@Override
	protected void freeMe() {

	}

	/**
	 * 收集手机信息
	 */
	private String mobileInfo() {
		String mobileInfo = "";
		try {
			String model = android.os.Build.MODEL;
			String version = android.os.Build.VERSION.RELEASE;
			String fingerprint = android.os.Build.FINGERPRINT;
			String country = ConfigUtil.getCountry();
			String cpu = Build.CPU_ABI;
			String softwareVersion = this.getResources().getString(
					R.string.app_name)
					+ this.getResources().getString(
							R.string.str_current_version);
			mobileInfo = "\n\r"+ "[1-MODEL]=" + model + "\n\r" + "[2-VERSION]="
					+ version + "\n\r" + "[3-FINGERPRINT]=" + fingerprint + "\n\r"
					+ "[4-country]=" + country + "\n\r" + "[5-CPU]=" + cpu + "\n\r"
					+ "[6-SOFTVERSION]=" + softwareVersion+"\n\r";

		} catch (Exception e) {
			e.printStackTrace();
		}

		return mobileInfo;

	}

	private String encryptInfo1() {
		String info = statusHashMap.get(Consts.KEY_USERNAME) + " , "
				+ statusHashMap.get(Consts.KEY_PASSWORD);
		ArrayList<Device> deviceList = CacheUtil.getDevList();

		if (null != deviceList && 0 != deviceList.size()) {
			for (int i = 0; i < deviceList.size(); i++) {
				info += " , " + deviceList.get(i).getFullNo() + " , "
						+ deviceList.get(i).getUser() + " , "
						+ deviceList.get(i).getPwd();
			}
		}
		return ConfigUtil.getBase64(info);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
}
