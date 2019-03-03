package com.star.starwebbrowser.activity;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.star.starwebbrowser.activity.SuperActivity;
import com.star.starwebbrowser.save.SPUtils;
import com.star.starwebbrowser.zxing.camera.CameraManager;
import com.star.starwebbrowser.zxing.decoding.CaptureActivityHandler;
import com.star.starwebbrowser.zxing.decoding.InactivityTimer;
import com.star.starwebbrowser.zxing.view.ViewfinderView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.star.starwebbrowser.R;

public class CaptureActivity extends SuperActivity implements
		SurfaceHolder.Callback, View.OnClickListener {

	private static final float BEEP_VOLUME = 0.1F;
	private static final long VIBRATE_DURATION = 200L;
	private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
		public void onCompletion(MediaPlayer paramAnonymousMediaPlayer) {
			paramAnonymousMediaPlayer.seekTo(0);
		}
	};

	private LinearLayout btnCancle;
	private LinearLayout btnOK;
	private String carType = "-1";
	private String characterSet;
	private Vector<BarcodeFormat> decodeFormats;
	private TextView editCarType;
	private ImageView editCarTypeImage;
	private CaptureActivityHandler handler;
	private boolean hasSurface;
	private InactivityTimer inactivityTimer;
	private PopupWindow mPopupWindow;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private View popupView;
	private EditText txtCarPlate;
	private EditText txtCarType;
	private EditText txtCarLSH; //因为流水号
	private boolean vibrate;
	private ViewfinderView viewfinderView;
	private TextView sdnYWLX;
	private String strTest;

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.captureactivity);
		CameraManager.init(getApplication());
		initPopupWindow(R.layout.car_type_list); //选择车牌的弹框
		View localView = findViewById(R.id.car_type_lay);
		viewfinderView = ((ViewfinderView) findViewById(R.id.viewfinder_view));
		txtCarPlate = ((EditText) findViewById(R.id.txtCarPlate));
		txtCarLSH = ((EditText) findViewById(R.id.txtCarLsh));
		editCarType = ((TextView) findViewById(R.id.edit_car_type));
		editCarType.setVisibility(View.GONE);
		editCarTypeImage = ((ImageView) findViewById(R.id.edit_car_type_image));
		sdnYWLX = ((TextView) findViewById(R.id.title));
		String strYWLX1 = SPUtils.readString(this, "buzitype"); //业务（字母）
		String sdnYwlx = SPUtils.readString(this, "sdnywlx"); //业务类型(全称)
		if(sdnYwlx != null && sdnYwlx !="")
			sdnYWLX.setText(sdnYwlx);
		if (strYWLX1.equals("A")) { //如果业务为登记注册
			findViewById(R.id.car_type_lay).setVisibility(View.GONE); //车辆类型 不可见
			findViewById(R.id.layoutCarPlate).setVisibility(View.GONE); //号牌号码不可见
		}
		else if(strYWLX1.equals("C")){//如果业务类型为外地车转入
			findViewById(R.id.car_type_lay).setVisibility(View.GONE); //车辆类型 不可见
			findViewById(R.id.layoutCarPlate).setVisibility(View.GONE); //号牌号码不可见
		}
		else
		{
			txtCarLSH.setVisibility(View.GONE);  //扫描流水号不可见
		}
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		btnOK = ((LinearLayout) findViewById(R.id.main_bottom));
		btnOK.setOnClickListener(this);
		btnCancle=(LinearLayout)findViewById(R.id.btn_cancle);
		btnCancle.setOnClickListener(this);
		localView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View paramAnonymousView) {
				showPopupWindow();
			}
		});

	//	txtCarPlate.setTransformationMethod(new InputLowerToUpper());

	}

	public class InputLowerToUpper extends ReplacementTransformationMethod{
		@Override
		protected char[] getOriginal() {
			char[] lower = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z' };
			return lower;
		}

		@Override
		protected char[] getReplacement() {
			char[] upper = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z' };
			return upper;
		}

	}

	@SuppressLint({"MissingPermission", "WrongConstant"})
	private void playBeepSoundAndVibrate() {
		if ((this.playBeep) && (this.mediaPlayer != null))
		{this.mediaPlayer.start();}
		if (this.vibrate) {
			((Vibrator) getSystemService("vibrator")).vibrate(200L);
		}
	}

	@SuppressLint("NewApi")
	private void showPopupWindow() {
		mPopupWindow.showAsDropDown(findViewById(R.id.edit_car_type_select));
		mPopupWindow.update();
	}

	public void drawViewfinder() {
		this.viewfinderView.drawViewfinder();
	}

	public Handler getHandler() {
		return this.handler;
	}

	public ViewfinderView getViewfinderView() {
		return this.viewfinderView;
	}

	String lineNo = "1";
	RadioButton mRadio1 = null;
	RadioButton mRadio2 = null;
	RadioButton mRadio3 = null;
	RadioButton mRadio4 = null;
	RadioGroup mRadioGroup1 = null;

	private void sdnSelectLineNo()
	{

		final String str1 = "A";// 获取相应的tag信息

		final View localView = getLayoutInflater().inflate(
				R.layout.view_extinfo, null);
		mRadioGroup1 = ((RadioGroup) localView.findViewById(R.id.linegroup));
		mRadio1 = ((RadioButton) localView.findViewById(R.id.lineOne));
		mRadio1.setChecked(true);
		mRadio2 = ((RadioButton) localView.findViewById(R.id.lineTwo));
		mRadio3 = ((RadioButton) localView.findViewById(R.id.lineThree));
		mRadio4 = ((RadioButton) localView.findViewById(R.id.lineFour));
		mRadioGroup1.check(this.mRadioGroup1.getId());
		mRadioGroup1
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup paramAnonymousRadioGroup,int paramAnonymousInt) {
						if (paramAnonymousInt == CaptureActivity.this.mRadio1.getId()) //一号线
						{
							lineNo = "1";
						}
						else if(paramAnonymousInt == CaptureActivity.this.mRadio2.getId())//二号线
						{
							lineNo = "2";
						}
						else if(paramAnonymousInt == CaptureActivity.this.mRadio3.getId())
						{
							lineNo = "3";
						}
						else if(paramAnonymousInt == CaptureActivity.this.mRadio4.getId())
						{
							lineNo = "4";
						}
					}
				});
		new AlertDialog.Builder(this).setTitle("请选择").setView(localView).setPositiveButton("确定", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt)
			{
				SPUtils.saveBoolean(CaptureActivity.this, "isTwoCheck",(((CheckBox)localView.findViewById(R.id.checkDCheck)).isChecked()));//是否为复查车
				SPUtils.saveBoolean(CaptureActivity.this, "isImported",(((CheckBox)localView.findViewById(R.id.checkImported)).isChecked()));//是否进口车
				SPUtils.saveString(CaptureActivity.this, "lineno", lineNo);//线号
				SPUtils.saveString(CaptureActivity.this, "syxz", str1);

				changeActivity(MainActivity.class);
				finish();

			}
		}).show();

	}
	/**
	 * 跳转到carPurpose页面
	 *
	 * @param paramString
	 */
	public void goToCarPurposeActivity(String paramString) {
		// ((MyApp)getApplication()); /=================
		SPUtils.saveString(this, "startime", new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss").format(new Date()));// 开始时间
		//SPUtils.saveString(this, "clsbdh", paramString.toUpperCase());// 车辆识别代号
		SPUtils.saveString(this, "querytype", "CLSBDH");// querytype 车辆识别代号
		Intent localIntent = new Intent();
		localIntent.putExtra("scanValue", paramString.toUpperCase());
		setResult(-1, localIntent);
		finish();
	}

	/**
	 * 跳转到carPurpose页面
	 * 号牌号码  号牌种类
	 */
	public void goToCarPurposeActivity(String paramString1, String paramString2) {
		// ((MyApp)getApplication()); /====================
		SPUtils.saveString(this, "startime", new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss").format(new Date()));// 开始时间
		SPUtils.saveString(this, "clsbdh", "");// 车辆识别代号
		SPUtils.saveString(this, "cllx", "");// 车辆类型
		SPUtils.saveString(this, "querytype", "CLSBDH");// querytype 车辆识别代号？？
		SPUtils.saveString(this, "hphm", paramString1);// 清空处理识别代号
		SPUtils.saveString(this, "hpzl", paramString2);
		if(strTest.equals("T")) //如果是T 驾驶证照片上传
		{
			//changeActivity(sdnUploadXSZPhoto.class);// 2015年9月6日11:15:19 注释
		}else //非T 驾驶证照片上传
		{
			//changeActivity(CarPurposeActivity.class);// 2015年9月6日11:15:19 注释
		}

		finish();
	}

	public void handleDecode(Result paramResult, Bitmap paramBitmap) {
		inactivityTimer.onActivity();
		viewfinderView.drawResultBitmap(paramBitmap);
		playBeepSoundAndVibrate();
		goToCarPurposeActivity(paramResult.getText());
	}

	// 先注释 保证 zxing 编译通过
	@SuppressLint({"NewApi", "WrongConstant"})
	private void initPopupWindow(int paramInt) {
		this.popupView = ((LayoutInflater) getSystemService("layout_inflater")).inflate(paramInt, null);
		this.popupView.findViewById(R.id.car_type_yellow_car)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "01";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("大型车(黄牌黑字)");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});
		this.popupView.findViewById(R.id.car_type_blue_car_btn)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "02";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("小型车(蓝牌白字)");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});
		this.popupView.findViewById(R.id.car_type_black)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "06";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("黑牌车");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});

		this.popupView.findViewById(R.id.car_type_dxxny)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "51";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("大型新能源汽车");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});

		this.popupView.findViewById(R.id.car_type_xxny)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "52";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("小型新能源汽车");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});

		this.popupView.findViewById(R.id.car_type_lower)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "13";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("低速车");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});
		this.popupView.findViewById(R.id.car_type_tlj)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "14";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("拖拉机");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});
		this.popupView.findViewById(R.id.car_type_gc)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "15";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("挂车");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});
		this.popupView.findViewById(R.id.car_type_jlqc)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "16";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("教练汽车");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});
		this.popupView.findViewById(R.id.car_type_ptmt)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "07";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("普通摩托车");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});
		this.popupView.findViewById(R.id.car_type_qbmt)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "08";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("轻便摩托车");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});
		this.popupView.findViewById(R.id.car_type_jlmt)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "17";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("教练摩托车");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});
		this.popupView.findViewById(R.id.car_type_jymt)
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View paramAnonymousView) {
						CaptureActivity.this.carType = "24";
						CaptureActivity.this.editCarTypeImage.setVisibility(8);
						CaptureActivity.this.editCarType.setVisibility(0);
						CaptureActivity.this.editCarType.setText("警用摩托车");
						if (CaptureActivity.this.mPopupWindow.isShowing())
							CaptureActivity.this.mPopupWindow.dismiss();
					}
				});


		this.mPopupWindow = new PopupWindow(this.popupView, -2, -2);
		this.mPopupWindow.setBackgroundDrawable(getResources().getDrawable(
				R.color.transparent));
		this.mPopupWindow.setOutsideTouchable(true);
		this.mPopupWindow.setAnimationStyle(R.style.AnimTop2);
		this.mPopupWindow.setTouchable(true);
		this.mPopupWindow.setFocusable(true);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		if (v == btnOK) {
			if ((txtCarLSH.getText() != null)
					&& (!txtCarLSH.getText().toString().equals(""))) {
				//这里加验证 根据车牌
				goToCarPurposeActivity(txtCarLSH.getText().toString());
				return;
			}
			else if ((txtCarPlate.getText() != null)
					&& (!txtCarPlate.getText().toString().equals(""))
					&& (carType != null) && (!carType.equals("-1"))) {
				String buziType = SPUtils.readString(this, "buzitype");// 主菜单选项
				if(buziType.equals("T")) //如果只是上传拍照
				{
					goToCarPurposeActivity(txtCarPlate.getText().toString(),carType);
					return;
				}

				return;
			}
		}
		else if(v==btnCancle){
			Intent localIntent = new Intent();
			localIntent.putExtra("scanValue", "");
			setResult(-1, localIntent);
			finish();
			return;
		}
		new AlertDialog.Builder(this)
				.setIcon(
						getResources().getDrawable(R.mipmap.login_error_icon))
				.setTitle("提示").setNeutralButton("确定", null)
				.setMessage("请输入车牌号码和车辆类型或者流水号").create().show();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

		if (!this.hasSurface) {
			this.hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	protected void onPause() {
		super.onPause();
		if (this.handler != null) {
			this.handler.quitSynchronously();
			this.handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@SuppressLint("WrongConstant")
	protected void onResume() {
		super.onResume();
		SurfaceHolder localSurfaceHolder = ((SurfaceView) findViewById(R.id.preview_view))
				.getHolder();
		if (this.hasSurface)
			initCamera(localSurfaceHolder);
		else {
			localSurfaceHolder.addCallback(this);
			localSurfaceHolder.setType(3);
		}
		this.decodeFormats = null;
		this.characterSet = null;
		this.playBeep = true;
		if (((AudioManager) getSystemService("audio")).getRingerMode() != 2)
			this.playBeep = false;
		initBeepSound();
		this.vibrate = true;
	}

	private void initBeepSound() {
		AssetFileDescriptor localAssetFileDescriptor;
		if ((this.playBeep) && (this.mediaPlayer == null)) {
			setVolumeControlStream(3);
			this.mediaPlayer = new MediaPlayer();
			this.mediaPlayer.setAudioStreamType(3);
			this.mediaPlayer.setOnCompletionListener(this.beepListener);
			localAssetFileDescriptor = getResources().openRawResourceFd(
					R.raw.beep); // 获取声音资源

			try {
				this.mediaPlayer.setDataSource(
						localAssetFileDescriptor.getFileDescriptor(),
						localAssetFileDescriptor.getStartOffset(),
						localAssetFileDescriptor.getLength());
				localAssetFileDescriptor.close();
				this.mediaPlayer.setVolume(0.1F, 0.1F);
				this.mediaPlayer.prepare();
				return;
			} catch (IOException localIOException) {
				this.mediaPlayer = null;
			}
		}

	}

	private void initCamera(SurfaceHolder paramSurfaceHolder) {
		try {
			CameraManager.get().openDriver(paramSurfaceHolder);
			if (this.handler == null)
				this.handler = new CaptureActivityHandler(this,
						this.decodeFormats, this.characterSet);
			return;
		} catch (IOException localIOException) {
		} catch (RuntimeException localRuntimeException) {
		}
	}

}
