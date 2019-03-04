package com.star.starwebbrowser.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.widget.Button;

import com.star.library.jsbridge.BridgeHandler;
import com.star.library.jsbridge.BridgeWebView;
import com.star.library.jsbridge.CallBackFunction;
import com.star.library.jsbridge.DefaultHandler;
import com.google.gson.Gson;
import com.star.starwebbrowser.model.SaveData;
import com.star.starwebbrowser.model.VideoData;
import com.star.starwebbrowser.tcp.TcpCleint;
import com.star.starwebbrowser.utils.ProgressDialog;
import com.star.starwebbrowser.save.SPUtils;
import com.star.starwebbrowser.R;
import com.star.starwebbrowser.utils.ImageUtils;

public class MainActivity extends SuperActivity implements OnClickListener{

    private final String TAG = "MainActivity";
    BridgeWebView webView;
    Button button;
    int isdnIsSame=1;//扫描VIN是否与OBD读取到的VIN一致 0:不一致 1:一致 2:未知
    String code ="";//拍照种类代码


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (BridgeWebView) findViewById(R.id.webView);

        /* ** 配置浏览器缓存*/
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setAppCacheEnabled(true);
        /* ** 配置浏览器缓存*/

        webView.setDefaultHandler(new DefaultHandler());

        webView.setWebChromeClient(new WebChromeClient() {

        });

       webView.loadUrl("file:///android_asset/demo.html");
     //  webView.loadUrl("http://192.168.1.58:8017/start.html");


        /* * ** 注册供 JS调用的 ScanQR 打开二维码扫描界面** **/
        webView.registerHandler("ScanQR", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Intent CaptureIntent;
                CaptureIntent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(CaptureIntent, 3);
                function.onCallBack("Response_sdn"); //响应JS请求
            }
        });
        /**
         * 调用OBD读取页面
         * JS 页面调用时传递车辆识别代号 clsbdh
         */
        webView.registerHandler("ReadOBD", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

                Intent obdIntent = new Intent(MainActivity.this,OBDDataCheck.class);
                obdIntent.putExtra("clsbdh", data); // 车辆识别代号
                startActivityForResult(obdIntent,4);
            }
        });
        /* * ** 注册供 JS调用的ShotCamera 打开拍照界面** **/
        webView.registerHandler("ShotCamera", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
               // new Gson().fromJson(data,)
                Gson gsonCamera = new Gson();
                GetCameraJson jsonClass = gsonCamera.fromJson(data,GetCameraJson.class);

                Intent CaptureIntent;
                CaptureIntent = new Intent(MainActivity.this, SnapShotActivity.class);
                CaptureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                code = jsonClass.code; //给全局拍照种类赋值，拍照结束时回调
                CaptureIntent.putExtra("field", jsonClass.code);
                CaptureIntent.putExtra("fieldname", jsonClass.codename);
                CaptureIntent.putExtra("clsbdh", jsonClass.clsbdh); // 车辆识别代号
                startActivityForResult(CaptureIntent, 5);
                function.onCallBack("Response_sdn_camera"); //响应JS请求
            }
        });
        /**
         * 保存string 类型的数据
         * 参数 JSON类型  {'_key':'key','_value':'value'}
         */
        webView.registerHandler("SaveStr", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {

                SaveData saveData = new SaveData();
                saveData = new Gson().fromJson(data,saveData.getClass());
                SPUtils.saveString(MainActivity.this,saveData._key,saveData._value);
                function.onCallBack("保存数据成功");
            }
        });
        /**
         * 读取保存的数据
         * 参数 key
         */
        webView.registerHandler("ReadStr", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                String strValue = SPUtils.readString(MainActivity.this,data);
                function.onCallBack(strValue);
            }
        });
        /***
         * 开始录像
         * 参数 JSON 格式数据 {}
         */
        webView.registerHandler("startVideo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Gson arrObj = new Gson();
                VideoData videodata = arrObj.fromJson(data,VideoData.class);
                Object[] arrayOfObject = new Object[6];
                arrayOfObject[0] = videodata.hphm;
                arrayOfObject[1] = videodata.hpzl;
                arrayOfObject[2] = videodata.lineNo;//线号
                arrayOfObject[3] = videodata.clsbdh; //车辆识别代码
                arrayOfObject[4] = videodata.queueId; //queueID
                arrayOfObject[5] = videodata.cllx; //车辆类型
                //arrayOfObject[6]=videodata.syxz;//使用性质
                //arrayOfObject[7]=videodata.bgys;//变更后的颜色（车辆颜色变更）
                StartVideo(arrayOfObject);//开始录像
                function.onCallBack("开启录像成功");
            }
        });
        /**
         * 结束录像
         * 参数JSON 格式 {}
         */
        webView.registerHandler("stopVideo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Gson arrObj = new Gson();
                VideoData videodata = arrObj.fromJson(data,VideoData.class);
                Object[] arrayOfObject = new Object[8];
                arrayOfObject[0] = videodata.hphm;
                arrayOfObject[1] = videodata.hpzl;
                arrayOfObject[2] = videodata.lineNo;//线号
                arrayOfObject[3] = videodata.clsbdh; //车辆识别代码
                arrayOfObject[4] = videodata.queueId; //queueID
                arrayOfObject[5] = videodata.cllx; //车辆类型
                arrayOfObject[6]=videodata.syxz;//使用性质
                arrayOfObject[7]=videodata.bgys;//变更后的颜色（车辆颜色变更）
                StopVideo(arrayOfObject);//开始录像
                function.onCallBack("开启录像成功");
            }
        });
        webView.send("start");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
      switch (requestCode){
          case 3://扫描二维码
              String strScanValue = intent.getExtras().getString("scanValue"); //得到对应的扫描结果
              webView.callHandler("showQRvalue", strScanValue, new CallBackFunction() {
                  @Override
                  public void onCallBack(String data) {
                      //得到对应的返回数据
                      Log.i(TAG,"reponse data from js"+data);
                  }
              });
              break;
          case 4://OBD对接

              int opetype_obd = intent.getExtras().getInt("optype_obd"); //得到页面返回值
              String obd_readdata = SPUtils.readString(MainActivity.this, "obd_readdata");
              isdnIsSame = intent.getExtras().getInt("sdnissame");
              if(opetype_obd==1){  //继续
                  //DealGetVechInfo();  //处理车辆信息
                  //InsertGBvData(opetype,"",9);
                 // InsertOBDdata(opetype_obd,"",9,obd_readdata);
              }else if(opetype_obd==0){ //不一致
                  //finish();//关闭
                  //InsertGBvData(opetype,"",9);
                 // InsertOBDdata(opetype_obd,"",9,obd_readdata);
                  return;
              } else if(opetype_obd==2){ //未知
                  int perType_obd = intent.getExtras().getInt("pertype_obd");
                  String strContent = intent.getExtras().getString("sdnReason_obd");
                  if(perType_obd==0){ //人工审核不通过
                      finish();//关闭
                      //	InsertGBvData(opetype,strContent,perType);
                     // InsertOBDdata(opetype_obd,strContent,perType_obd,obd_readdata);
                  }else if(perType_obd==1){ //人工审核通过
                      //	InsertGBvData(opetype,strContent,perType);
                    //  InsertOBDdata(opetype_obd,strContent,perType_obd,obd_readdata);
                      //DealGetVechInfo();  //处理车辆信息
                  }
              } else if(opetype_obd==99){ //99 为添加嫌疑车辆
                  String remark = intent.getExtras().getString("sdnReason_obd");
                  //insertBlackCar(vech.clsbdh,remark);
              }
              break;
          case 5: //拍照界面
             String strImg = intent.getExtras().getString("str_image");
            //  String strImg = bundle.getString("str_image");
              String strStrImg="";
              if(strImg.equals("1")){
                  strStrImg=  ImageUtils.getBase64Str(app.currentPhoto);
              }

              responCamera responCamera = new responCamera();
              responCamera.code = code;
              responCamera.strImg = strStrImg;

              webView.callHandler("showStrImg", strStrImg, new CallBackFunction() {
                  @Override
                  public void onCallBack(String data) {
                      //得到对应的返回数据
                      Log.i(TAG,"reponse data from js"+data);
                  }
              });
              break;
              default:
      }
    }


    /* ************ 以下是 Socket通讯******************** */

    /**
     * 开始录像
     * @param arrayOfObject
     */
    public void StartVideo(Object[] arrayOfObject) {
        if (SPUtils.readBoolean(this, "enablevideo")) {
            ProgressDialog.Show(this);
            TcpCommandTask localTcpCommandTask = new TcpCommandTask();
           /* Object[] arrayOfObject = new Object[6];
            arrayOfObject[0] = "号牌号码";
            arrayOfObject[1] = "号牌种类";
            arrayOfObject[2] = "1"; //线号
            arrayOfObject[3] = ""; //车辆识别代号
            arrayOfObject[4] =  ""; //queueId
            arrayOfObject[5] = "K33"; //车辆类型
            */
            String strParm = String.format("<?xml version=\"1.0\" encoding=\"GB2312\"?><diagram type=\"start\" hphm=\"%s\" hpzl=\"%s\" jcxxh=\"%s\" clsbdh=\"%s\" queueid=\"%s\" cllx=\"%s\" />",
                            arrayOfObject);
            localTcpCommandTask.execute(new String[] { strParm });
        }
    }

    /**
     * 结束录像
     * @param arrayOfObject
     */
    public void StopVideo(Object[] arrayOfObject) {
        if (SPUtils.readBoolean(this, "enablevideo")) {
            ProgressDialog.Show(this);
            TcpCommandTask localTcpCommandTask = new TcpCommandTask();
            localTcpCommandTask.execute(new String[] { String
                    .format("<?xml version=\"1.0\" encoding=\"GB2312\"?><diagram type=\"end\" hphm=\"%s\" hpzl=\"%s\" jcxxh=\"%s\" clsbdh=\"%s\" " +
                            " queueid=\"%s\" cllx=\"%s\"  new_use_type=\"%s\"   new_color=\"%s\" />",
                    arrayOfObject) });
        }
    }
    @SuppressLint("NewApi")
    class TcpCommandTask extends AsyncTask<String, Integer, Boolean> {
        String errorMessage = "";

        TcpCommandTask() {
        }

        protected Boolean doInBackground(String[] paramArrayOfString) {
            try {
                String str = TcpCleint.send(paramArrayOfString[0],
                        MainActivity.this);
                if (str == null) {
                    errorMessage = "服务器返回数据为空";
                    return false;
                }
                if (str.contains("state=\"2\"")) {
                    errorMessage = str;
                    return false;
                }

                return true;
            } catch (Exception localException) {
                errorMessage = localException.getLocalizedMessage();
            }
            return false;
        }

        protected void onPostExecute(Boolean paramBoolean) {
            ProgressDialog.Hide();
            if (!paramBoolean.booleanValue())
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(
                                MainActivity.this.getResources().getDrawable(
                                        R.mipmap.login_error_icon))
                        .setTitle("发送失败").setNeutralButton("确定", null)
                        .setMessage("数据异常!" + errorMessage).create().show();
        }

    }

    /* *************以上是 Socket通讯******************* */

    /**
     * 拍照参数解析类
     */
    private class GetCameraJson{
        //照片代码
        String code;
        //车辆识别代号
        String clsbdh;
        //拍照代码名称
        String codename;
    }

    /**
     * 拍照返回值
     */
    private class  responCamera{
        //代码
        String code;
        //字符串图片
        String strImg;
    }
}
