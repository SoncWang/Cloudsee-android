package com.jovision.activities;


import java.io.File;
import java.io.FileOutputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jovetech.CloudSee.temp.R;
import com.jovision.Consts;
import com.jovision.utils.MobileUtil;
import com.jovision.views.popw;
import com.tencent.stat.StatService;

public class JVRebandContactActivity extends BaseActivity{

	private TextView rebandPhone;
	private TextView rebandEmail;
	private TextView rebandPhoneModify;
	private TextView rebandEmailModify;
	private ImageView rebandHeadImg;
	private LinearLayout linear;

	//����ͷ��

	private String more_name;// �û���
	private popw popupWindow; // ����PopupWindow����
	private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// ����
	private static final int PHOTO_REQUEST_GALLERY = 2;// �������ѡ��
	private static final int PHOTO_REQUEST_CUT = 3;// ���
	// ���ͷ����ļ���
	File file;
	// ��ͷ���ļ�
	File tempFile;
	// ��ͷ���ļ�
	File newFile;
	// popupWindow��������


	@Override
	public void onHandler(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNotify(int what, int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub

	}
	@Override
	protected void onResume() {
		if (tempFile.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(Consts.HEAD_PATH
					+ more_name + ".jpg");
			rebandHeadImg.setImageBitmap(bitmap);
		}
		super.onResume();
	}
	@Override
	protected void initSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initUi() {
		// TODO Auto-generated method stub
		setContentView(R.layout.rebandcontact);

		leftBtn = (Button)findViewById(R.id.btn_left);
		rightBtn = (Button)findViewById(R.id.btn_right);
		rightBtn.setVisibility(View.GONE);
		currentMenu = (TextView)findViewById(R.id.currentmenu);
		currentMenu.setText("����˺�");
		
		rebandEmail = (TextView)findViewById(R.id.reband_email_text);
		rebandPhone = (TextView)findViewById(R.id.reband_phone_text);
		rebandEmailModify = (TextView)findViewById(R.id.reband_modify_email);
		rebandPhoneModify = (TextView)findViewById(R.id.reband_modify_phone);
		rebandHeadImg = (ImageView)findViewById(R.id.reband_hand_img);
		linear = (LinearLayout)findViewById(R.id.lin);

		if (Boolean.valueOf((statusHashMap
				.get(Consts.LOCAL_LOGIN)))) {
			more_name = JVRebandContactActivity.this.getResources().getString(
					R.string.location_login);
		} else {
			more_name = (statusHashMap
					.get(Consts.KEY_USERNAME));
		}
		file = new File(Consts.HEAD_PATH);
		MobileUtil.createDirectory(file);
		tempFile = new File(Consts.HEAD_PATH + more_name + ".jpg");
		newFile = new File(Consts.HEAD_PATH + more_name + "1.jpg");

		rebandHeadImg.setOnClickListener(myOnClickListener);
		rebandEmailModify.setOnClickListener(myOnClickListener);
		rebandPhoneModify.setOnClickListener(myOnClickListener);
		leftBtn.setOnClickListener(myOnClickListener);

	}

	OnClickListener myOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_left:
				finish();
				break;
			case R.id.pop_outside:
				popupWindow.dismiss();
				break;
			case R.id.reband_hand_img:
				StatService.trackCustomEvent(
						JVRebandContactActivity.this,
						"census_moreheadimg",
						JVRebandContactActivity.this.getResources().getString(
								R.string.census_moreheadimg));
				popupWindow = new popw(JVRebandContactActivity.this, myOnClickListener);
				popupWindow.setBackgroundDrawable(null);
				popupWindow.setOutsideTouchable(true);
				popupWindow.showAtLocation(linear, Gravity.BOTTOM
						| Gravity.CENTER_HORIZONTAL, 0, 0); // ����layout��PopupWindow����ʾ��λ��
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
				// ����ϵͳ�����չ���
				popupWindow.dismiss();
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				// ָ������������պ���Ƭ�Ĵ���·��
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
				startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
				break;
			case R.id.btn_cancel:
				popupWindow.dismiss();
				break;
			case R.id.reband_modify_email:

				break;

			case R.id.reband_modify_phone:

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
		// cropΪtrue�������ڿ�����intent��������ʾ��view���Լ���
		intent.putExtra("crop", "true");

		// aspectX aspectY �ǿ�ߵı���
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		// outputX,outputY �Ǽ���ͼƬ�Ŀ��
		intent.putExtra("outputX", size);
		intent.putExtra("outputY", size);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, PHOTO_REQUEST_CUT);
	}

	// �����м��ú��ͼƬ��ʾ��UI������
	private void setPicToView(Intent picdata) {
		Bundle bundle = picdata.getExtras();
		if (bundle != null) {
			Bitmap photo = bundle.getParcelable("data");
			saveBitmap(photo);
			Drawable drawable = new BitmapDrawable(photo);
			rebandHeadImg.setBackgroundDrawable(drawable);
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

	
	@Override
	protected void saveSettings() {

	}

	@Override
	protected void freeMe() {

	}

}
