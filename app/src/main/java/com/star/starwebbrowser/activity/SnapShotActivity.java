package com.star.starwebbrowser.activity;

import java.io.IOException;
import java.util.List;

import com.star.starwebbrowser.R;
import com.star.starwebbrowser.save.SPUtils;
import com.star.starwebbrowser.utils.ImageDeal;
import com.star.starwebbrowser.utils.ImageUtils;
import com.star.starwebbrowser.utils.ToastEx;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SnapShotActivity extends SuperActivity implements
        View.OnClickListener, SurfaceHolder.Callback {

    String[] FlashMode = {Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_OFF, Camera.Parameters.FLASH_MODE_ON};
    int[] FlashModeIcon = {R.mipmap.flash_auto, R.mipmap.flash_off, R.mipmap.flash_on};
    int curFlashMode = 0; //当前闪光灯类型

    float afterLenght;
    float beforeLenght;
    ImageButton btnFlashSwitch = null;
    Bitmap curBitMap;
    //int curFlashMode = 0;
    String fieldname = "";
    boolean isView = false;

    Camera myCamera;
    SurfaceHolder mySurfaceHolder;
    SurfaceView mySurfaceView;

    ImageView photoView;
    ImageButton resnapshot;
    ImageButton snapcancel;
    String snapfield = "";
    ImageButton snapok;
    ImageButton snapshot;
    TextView title = null;
    TextView zoomTimes = null;
    String strClsbdh = "";
    String hphm, hpzl;//号牌号码 号牌种类
    String queue_id = "0"; // 队列id

    int iCamera = 0;// 打开前置摄像头还是后置摄像头（0：后置，1：前置）

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //强制横屏
        setContentView(R.layout.activity_snap_shot);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // 强制性横屏
        InitControl();
    }

    private void InitControl() {
        try {
            mySurfaceView = ((SurfaceView) findViewById(R.id.mySurfaceView));
            mySurfaceHolder = this.mySurfaceView.getHolder();
            mySurfaceHolder.addCallback(this);
            mySurfaceHolder.setType(3);
            photoView = ((ImageView) findViewById(R.id.photoView));
            photoView.setVisibility(View.GONE);
            mySurfaceView.setVisibility(View.VISIBLE);
            btnFlashSwitch = ((ImageButton) findViewById(R.id.FlashMode));
            btnFlashSwitch.setBackgroundResource(this.FlashModeIcon[this.curFlashMode]);
            btnFlashSwitch.setOnClickListener(this);
            snapcancel = ((ImageButton) findViewById(R.id.snapcancel)); //取消camTitle
            snapshot = ((ImageButton) findViewById(R.id.snapshot)); //拍照
            snapok = ((ImageButton) findViewById(R.id.snapok));//发送
            resnapshot = ((ImageButton) findViewById(R.id.resnapshot));//重拍
            resnapshot.setEnabled(false);//重拍
            snapok.setEnabled(false);//发送
            snapcancel.setEnabled(true);//取消
            // snapshot.setEnabled(false);//拍照
            snapcancel.setBackgroundResource(R.mipmap.snapcancel); //取消
            snapshot.setBackgroundResource(R.mipmap.snapshot); //拍照
            snapok.setBackgroundResource(R.mipmap.snapok_b); //确定 发送
            resnapshot.setBackgroundResource(R.mipmap.resnapshot_b); //重拍
            snapcancel.setOnClickListener(this);
            snapshot.setOnClickListener(this);
            snapok.setOnClickListener(this);
            resnapshot.setOnClickListener(this);
            zoomTimes = ((TextView) findViewById(R.id.zoomtimes));
            snapok.setEnabled(false);
            Intent localIntent = getIntent();
            snapfield = localIntent.getStringExtra("field"); //照片种类
            fieldname = localIntent.getStringExtra("fieldname"); //照片显示名称
            strClsbdh = localIntent.getStringExtra("clsbdh"); //车辆识别代号
            hphm = localIntent.getStringExtra("hphm");//号牌号码
            hpzl = localIntent.getStringExtra("hpzl");//号牌种类
            //queue_id = localIntent.getStringExtra("queue_id");
            if (strClsbdh == null) {
                strClsbdh = "";
            }
            if (snapfield.equalsIgnoreCase("00")) { //如果是查验员照片
                iCamera = 1;
            } else {
                iCamera = 0;
            }
            title = ((TextView) findViewById(R.id.camTitle));
            title.setText(fieldname);
        } catch (Exception ex) {
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (v == resnapshot) // 重拍
            {
                photoView.setVisibility(View.GONE); // 隐藏展示的图片
                mySurfaceView.setVisibility(View.VISIBLE);
                if (isView) {
                    myCamera.stopPreview();
                }
                myCamera.startPreview(); //重新打开摄像头 开始预览
                resnapshot.setEnabled(false);//重拍
                snapok.setEnabled(false);//发送
                snapcancel.setEnabled(true);//取消
                snapshot.setEnabled(true);//拍照
                snapok.setBackgroundResource(R.mipmap.snapok_b);
                snapshot.setBackgroundResource(R.mipmap.snapshot);
                resnapshot.setBackgroundResource(R.mipmap.resnapshot_b);
                snapcancel.setBackgroundResource(R.mipmap.snapcancel);
            }
            if (v == snapok) // 确定 保存照片
            {
                resnapshot.setEnabled(true);//重拍
                snapok.setEnabled(false);//发送
                snapcancel.setEnabled(true);//取消
                snapshot.setEnabled(false);//拍照
                snapok.setBackgroundResource(R.mipmap.snapok_b);
                snapshot.setBackgroundResource(R.mipmap.snapshot_b);
                resnapshot.setBackgroundResource(R.mipmap.resnapshot);
                snapcancel.setBackgroundResource(R.mipmap.snapcancel);
                if ((snapfield != null) && (!snapfield.equals("")))
                    if (curBitMap != null) {
                        app.currentPhoto = curBitMap;
                        Intent localIntent = new Intent();
                        localIntent.putExtra("str_image", "1");
                        localIntent.putExtra("clsbdh", strClsbdh);
                        localIntent.putExtra("field", snapfield);
                        this.setResult(RESULT_OK, localIntent);
                        ToastEx.ImageToast(this, R.mipmap.smile, "正在保存……", 1);
                        if (this.isView)
                            this.myCamera.stopPreview();
                        // ================================提交图片
                        try {
                            finish();
                        } catch (Exception ex) {
                        }
                        // =================================提交图片结束
                    } else {
                        try {
                            Thread.sleep(2000L);
                            if (!this.isView) {
                                this.myCamera.stopPreview();
                            }
                            new AlertDialog.Builder(this).setTitle("存储失败")
                                    .setMessage("请检查配置")
                                    .setPositiveButton("确定", null).show();
                            return;
                        } catch (InterruptedException localInterruptedException) {
                            localInterruptedException.printStackTrace();
                        }
                    }
            }
            if (v == snapshot) { //拍照按钮
                this.myCamera.autoFocus(this.mAutoFocusCallBack);
                resnapshot.setEnabled(true);//重拍
                snapok.setEnabled(true);//发送
                snapcancel.setEnabled(true);//取消
                snapshot.setEnabled(false);//拍照
                snapok.setBackgroundResource(R.mipmap.snapok);
                snapshot.setBackgroundResource(R.mipmap.snapshot_b);
                resnapshot.setBackgroundResource(R.mipmap.resnapshot);
                snapcancel.setBackgroundResource(R.mipmap.snapcancel);
                return;
            }
            if (v == snapcancel) {
                if (this.isView)
                    this.myCamera.stopPreview();
                Intent localIntent = new Intent();
                localIntent.putExtra("str_image", "0");
                this.setResult(RESULT_OK, localIntent);
                finish();
            }
            if (v == btnFlashSwitch) {
                curFlashMode++;
                if (curFlashMode == 3) {
                    curFlashMode = 0;
                }
                btnFlashSwitch.setBackgroundResource(this.FlashModeIcon[this.curFlashMode]);

                // ==============================单氐楠 2016年3月25日15:44:04
                // =======开始=================
                Parameters localParameters = myCamera.getParameters();
                localParameters.setFlashMode(this.FlashMode[this.curFlashMode]);
                myCamera.setParameters(localParameters);
                myCamera.startPreview();
                // ==============================单氐楠 2016年3月25日15:44:04
                // =======结束=================
            }
        } catch (Exception ex) {
        }
    }

    public boolean onCreateOptionsMenu(Menu paramMenu) {
        getMenuInflater().inflate(R.menu.snap_shot, paramMenu);
        return true;
    }

    //region 摄像头控制处理方法
    private Camera.AutoFocusCallback mAutoFocusCallBack = new Camera.AutoFocusCallback() {
        @SuppressWarnings("deprecation")
        @SuppressLint("NewApi")
        public void onAutoFocus(boolean paramAnonymousBoolean, Camera paramAnonymousCamera) {
            try {
                Camera.Parameters localParameters = SnapShotActivity.this.myCamera.getParameters();
                localParameters.setPictureFormat(PixelFormat.JPEG);
                //localParameters.setFlashMode(SnapShotActivity.this.FlashMode[SnapShotActivity.this.curFlashMode]);
                SnapShotActivity.this.myCamera.setParameters(localParameters);
                SnapShotActivity.this.myCamera.takePicture(SnapShotActivity.this.mShutterCallback, null, SnapShotActivity.this.myjpegCallback);
                return;
            } catch (Exception localException) {
                Log.v("onAutoFocus", localException.getMessage());
            }
        }
    };
    Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };


    Camera.PictureCallback myjpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] paramAnonymousArrayOfByte, Camera paramAnonymousCamera) {
            try {
                Bitmap localBitmap1 = BitmapFactory.decodeByteArray(paramAnonymousArrayOfByte, 0, paramAnonymousArrayOfByte.length);
                String str = "";// 得到相应的检测项目
                // String strHPHM = SPUtils.readString(SnapShotActivity.this, "hphm");// 号牌号码
                Object localObject;
                if (!str.trim().equals("")) {
                    localObject = ImageDeal.AddTextToImage(localBitmap1, " " + str);
                } else {
                    localObject = ImageDeal.AddTextToImage(localBitmap1, hphm + " " + SnapShotActivity.this.fieldname);
                }
                curBitMap = ((Bitmap) localObject);
                myCamera.stopPreview();
                photoView.setVisibility(View.VISIBLE);
                photoView.setImageBitmap((Bitmap) localObject);
                isView = false;
                snapok.setEnabled(true);
            } catch (Exception localException) {
                Log.v("onPictureTaken", localException.getMessage());
                new AlertDialog.Builder(SnapShotActivity.this)
                        .setTitle("操作失败")
                        .setMessage("onPictureTaken " + localException.getMessage())
                        .setPositiveButton("确定", null).show();
            }
        }
    };

    /**
     * 两个手指 只能放大缩小
     **/
    void onPointerDown(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            mode = MODE.ZOOM;
            beforeLenght = getDistance(event);// 获取两点的距离
        }
    }

    /**
     * 获取两点的距离
     **/
    private float getDistance(MotionEvent event) {
        try {
            float f1 = event.getX(0) - event.getX(1);
            float f2 = event.getY(0) - event.getY(1);
            Float sqrt = (float) Math.sqrt(f1 * f1 + f2 * f2);
            return sqrt;
        } catch (Exception ex) {
            return 0.0F;
        }
    }
/*
	@SuppressLint("NewApi")
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		try {
			try {


				Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
				int cameraCount = Camera.getNumberOfCameras(); // 得到摄像头数
				//int cameraCount =2;
				if (cameraCount <= 1) // 摄像头数目小于1
				{
					new AlertDialog.Builder(this).setTitle("操作失败")
					.setMessage("PDA摄像头个数小于2")
					.setPositiveButton("确定", null).show();
					return;  //**************************************

				}

				for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
					Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
					if (iCamera == 1) { // 如果是前置摄像头
						if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
							try {
								myCamera = Camera.open(camIdx);
							} catch (RuntimeException e) {
								new AlertDialog.Builder(this).setTitle("操作失败")
								.setMessage("PDA打开前置摄像头失败")
								.setPositiveButton("确定", null).show();
							}
						}
					}else if(iCamera == 0){
						if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) { // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
							try {
								myCamera = Camera.open(camIdx);
							} catch (RuntimeException e) {
								new AlertDialog.Builder(this).setTitle("操作失败")
								.setMessage("PDA打开后置摄像头失败")
								.setPositiveButton("确定", null).show();
							}
						}
					}

				}



				this.myCamera = Camera.open();
				if (this.isView) {
					this.myCamera.stopPreview();
				}
				Camera.Parameters localParameters = this.myCamera.getParameters(); // 得到摄像机的设置参数
				localParameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
				List<Size> localList = localParameters.getSupportedPictureSizes(); // 获取受支持的图片大小
				Object localObject = null;
				if (localList != null) {
					int iWidth = 640; // 相片宽度 //640
					int iHeight = 480; // 相片高度//360

					// **********************************以下是
					// 自动设置分辨率***************************
					/*
					 * for (int num = 0; num < localList.size(); num++) {
					 * localObject = localList.get(num); if (((Camera.Size)
					 * localObject).width >= 500 && ((Camera.Size)
					 * localObject).width < 1000) { if(((Camera.Size)
					 * localObject).width/((Camera.Size)
					 * localObject).height==16/9) { iHeight = ((Camera.Size)
					 * localObject).height; // 照片高度 iWidth = ((Camera.Size)
					 * localObject).width; // 照片宽度 }
					 *
					 * break; } } if (localObject != null) { //
					 * localParameters.setPictureSize(800, 600);// 设置照片
					 * localParameters.setPictureSize(iWidth, iHeight);// 设置照片 }
					 *  /////////
					// **********************************以上是
					// 自动设置分辨率***************************
					localParameters.setPictureSize(iWidth, iHeight);// 设置照片

					if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
						// 如果是竖屏
						localParameters.set("orientation", "portrait");
						// 在2.2以上可以使用
						// camera.setDisplayOrientation(90);
					} else {
						localParameters.set("orientation", "landscape");
						// 在2.2以上可以使用
						// camera.setDisplayOrientation(0);
					}
					//******************************单氐楠  2016年6月29日11:03:19 添加字段对焦和自动曝光
					localParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //自动对焦

					localParameters.setFlashMode(this.FlashMode[this.curFlashMode]); //设置闪光灯

					myCamera.setParameters(localParameters);
					try {
						// 设置显示
						myCamera.setPreviewDisplay(holder);
						this.isView = true;

					} catch (IOException exception) {
						myCamera.release();
						myCamera = null;
					}
					// 开始预览
					myCamera.startPreview();

					myCamera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
				}
			} catch (Exception localException) {
				// Log.v("surfaceCreated", localException.getMessage());
				new AlertDialog.Builder(this).setTitle("操作失败")
						.setMessage("PDA打开摄像头异常")
						.setPositiveButton("确定", null).show();
			}

		} catch (Exception localException) {
			//Log.v("surfaceCreated", localException.getMessage());
			new AlertDialog.Builder(this)
					.setTitle("操作失败")
					.setMessage("surfaceCreated " + localException.getMessage())
					.setPositiveButton("确定", null).show();
		}
	}
*/

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        try {
            try {
                this.myCamera = Camera.open();
                if (this.isView) {
                    this.myCamera.stopPreview();
                }
                Camera.Parameters localParameters = this.myCamera.getParameters(); // 得到摄像机的设置参数
                localParameters.setPictureFormat(PixelFormat.JPEG); // 设置图片格式
                List<Size> localList = localParameters.getSupportedPictureSizes(); // 获取受支持的图片大小
                Object localObject = null;
                if (localList != null) {
                    int iWidth = 800; // 相片宽度
                    int iHeight = 600; // 相片高度
                    for (int num = 0; num < localList.size(); num++) {
                        localObject = localList.get(num);
                        if (((Camera.Size) localObject).width >= 800 && ((Camera.Size) localObject).height <= 1200) {
                            iWidth = ((Camera.Size) localObject).width;
                            iHeight = ((Camera.Size) localObject).height;
                            break;
                        }
                    }
                    if (localObject != null) {
                        localParameters.setPictureSize(iWidth, iHeight);// 设置照片
                    }
                    //  localParameters.setPictureSize(iWidth, iHeight);// 设置照片
                    if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        // 如果是竖屏
                        localParameters.set("orientation", "portrait");
                        // 在2.2以上可以使用
                        // camera.setDisplayOrientation(90);
                    } else {
                        localParameters.set("orientation", "landscape");
                        // 在2.2以上可以使用
                        // camera.setDisplayOrientation(0);
                    }
                    localParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //自动对焦
                    localParameters.setFlashMode(this.FlashMode[this.curFlashMode]); //设置闪光灯
                    myCamera.setParameters(localParameters);
                    try {
                        // 设置显示
                        myCamera.setPreviewDisplay(holder);
                        this.isView = true;

                    } catch (IOException exception) {
                        myCamera.release();
                        myCamera = null;
                    }
                    // 开始预览
                    myCamera.startPreview();
                }
            } catch (Exception localException) {
                // Log.v("surfaceCreated", localException.getMessage());
                new AlertDialog.Builder(this).setTitle("操作失败")
                        .setMessage("surfaceCreated " + localException.getMessage())
                        .setPositiveButton("确定", null).show();
            }
        } catch (Exception localException) {
            Log.v("surfaceCreated", localException.getMessage());
            new AlertDialog.Builder(this).setTitle("操作失败")
                    .setMessage("surfaceCreated " + localException.getMessage())
                    .setPositiveButton("确定", null).show();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        myCamera.stopPreview();
        isView = false;
        myCamera.release();
        myCamera = null;
    }

    @Override
    public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
        if (paramInt == 4) {
            return true;
        }
        return super.onKeyDown(paramInt, paramKeyEvent);
    }

    /**
     * 模式 NONE：无 DRAG：拖拽. ZOOM:缩放
     *
     * @author zhangjia
     */
    private enum MODE {
        NONE, DRAG, ZOOM

    }

    private int start_x, start_y, current_x, current_y;// 触摸位置
    // private float beforeLenght, afterLenght;// 两触点距离
    private float scale_temp;// 缩放比例
    private MODE mode = MODE.NONE;// 默认模式

    /***
     * touch 事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /** 处理单点、多点触摸 **/
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;
            // 多点触摸
            case MotionEvent.ACTION_POINTER_DOWN:
                onPointerDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                mode = MODE.NONE;
                break;
            // 多点松开
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE.NONE;
                /** 执行缩放还原 **/
                // if (isScaleAnim) {
                // doScaleAnim();
                // }
                break;
        }
        return true;
    }

    /**
     * 移动的处理
     **/
    void onTouchMove(MotionEvent event) {
        float f1 = getDistance(event); // 得到移动事件两点之间的距离
        if (f1 > 0.0F && (Math.abs(afterLenght - beforeLenght) > 5.0F)) {
            afterLenght = f1;// 给变动后的长度赋值
        }
        float f2 = afterLenght / beforeLenght;
        if (myCamera.getParameters().isZoomSupported()) {
            Camera.Parameters localParameters = myCamera.getParameters();
            int i = localParameters.getZoom();
            int changeValue = i + (int) f2;
            if ((f2 > 1.0F) && (changeValue < localParameters.getMaxZoom())) {
                // i++;
                i = changeValue;
            }
            int changeValue1 = i - (int) f2;
            if ((f2 < 1.0F) && (changeValue1 > 0)) {
                i--;
                // i=changeValue1;
            }
            localParameters.setZoom(i);
            myCamera.setParameters(localParameters);
            double d = 1.0D + i / 8.0D;
            Object[] arrayOfObject = new Object[1];
            arrayOfObject[0] = Double.valueOf(d);
            String str = String.format("%.1fX", arrayOfObject);
            zoomTimes.setText(str);
        }
        beforeLenght = this.afterLenght;
    }

    /**
     * 按下
     **/
    void onTouchDown(MotionEvent event) {
        mode = MODE.DRAG;

    }
    //endregion

}

