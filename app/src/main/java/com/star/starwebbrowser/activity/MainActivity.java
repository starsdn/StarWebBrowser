package com.star.starwebbrowser.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.star.library.jsbridge.BridgeHandler;
import com.star.library.jsbridge.BridgeWebView;
import com.star.library.jsbridge.CallBackFunction;
import com.star.library.jsbridge.DefaultHandler;
import com.star.starwebbrowser.R;
import com.star.starwebbrowser.event.MainHandler;
import com.star.starwebbrowser.model.SaveData;
import com.star.starwebbrowser.model.VideoData;
import com.star.starwebbrowser.save.SPUtils;
import com.star.starwebbrowser.service.HttpService;
import com.star.starwebbrowser.tcp.TcpCleint;
import com.star.starwebbrowser.utils.HttpPost;
import com.star.starwebbrowser.utils.ImageUtils;
import com.star.starwebbrowser.utils.ProgressDialog;
import com.star.starwebbrowser.utils.ToastEx;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends SuperActivity implements OnClickListener {

    private final String TAG = "MainActivity";
    BridgeWebView webView;
    Button button;
    Uri imageUri;
    int isdnIsSame = 1;//扫描VIN是否与OBD读取到的VIN一致 0:不一致 1:一致 2:未知
    String code = "";//拍照种类代码
    EditText ip; //ip配置界面 Ip输入框
    EditText port; //ip配置界面  端口输入框
    EditText serviceIP;//服务IP
    EditText servicePort;//服务端口
    String strIp;//socke服务ip
    String strPort;//socket服务port

    String hphm;//当前办理车辆的号牌号码
    String hpzl;//当前办理车辆的号牌种类
    String zpzl;//当前办理车辆的照片种类
    String xsnr;//当前办理车辆的照片的显示类型
    String clsbdh;//当前办理车辆的车辆识别代号
    String jylsh;//当前办理车辆的检验流水号
    String lx;//操作类型 0 照片 1 视频
    HttpService httpServer;//http服务
    Handler mainHandler; //主handle


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 设置屏幕常亮
        webView = (BridgeWebView) findViewById(R.id.webView);
        /* ** 配置浏览器缓存*/
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(appCachePath);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setTextZoom(100);
        /* ** 配置浏览器缓存*/
        webView.setDefaultHandler(new DefaultHandler());
        webView.setWebChromeClient(new WebChromeClient() {

        });
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        switch (screenDensity) {
            case DisplayMetrics.DENSITY_LOW:
                zoomDensity = WebSettings.ZoomDensity.CLOSE;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                zoomDensity = WebSettings.ZoomDensity.MEDIUM;
                break;
            case DisplayMetrics.DENSITY_HIGH:
            case DisplayMetrics.DENSITY_XHIGH:
            case DisplayMetrics.DENSITY_XXHIGH:
            default:
                zoomDensity = WebSettings.ZoomDensity.FAR;
                break;
        }
        webView.getSettings().setDefaultZoom(zoomDensity);
        // webView.loadUrl("file:///android_asset/start.html");
        webView.loadUrl("file:///android_asset/index_percent.html");
        // webView.loadUrl("http://122.193.27.194:2000/PDAInspection/AppH5/start.html");
        //webView.loadUrl("http://192.168.1.58:8017/start.html");

        //判断配置项是否配置
        strIp = SPUtils.readString(MainActivity.this, "ip");
        strPort = SPUtils.readString(MainActivity.this, "port");
        if (strIp == null || "".equals(strIp) || strPort == null || "".equals(strPort)) {
           // ShowConfig();//显示配置狂 初始化完成后会加载配置框
        }

        //region webView 与JS 相互调用
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
                Intent obdIntent = new Intent(MainActivity.this, OBDDataCheck.class);
                obdIntent.putExtra("clsbdh", data); // 车辆识别代号
                startActivityForResult(obdIntent, 4);
            }
        });
        /**
         * js调用获取手机端VERSION版本号，用于软件更新
         */
        webView.registerHandler("GetVersion", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                function.onCallBack(MainActivity.this.app.VERSION);//拿到对应的app VERSION版本号
            }
        });
        /* * ** 注册供 JS调用的ShotCamera 打开拍照界面** **/
        webView.registerHandler("ShotCamera", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                // new Gson().fromJson(data,)
                // Gson gsonCamera = new Gson();
                // GetCameraJson jsonClass = gsonCamera.fromJson(data, GetCameraJson.class);
                Intent CaptureIntent;
                CaptureIntent = new Intent(MainActivity.this, SnapShotActivity.class);
                CaptureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //  code = jsonClass.code; //给全局拍照种类赋值，拍照结束时回调
                CaptureIntent.putExtra("field", zpzl);//照片种类
                CaptureIntent.putExtra("fieldname", xsnr); //显示内容
                CaptureIntent.putExtra("clsbdh", clsbdh); // 车辆识别代号
                CaptureIntent.putExtra("hphm", hphm); //号牌号码
                CaptureIntent.putExtra("hpzl", hpzl); //号牌种类
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
                saveData = new Gson().fromJson(data, saveData.getClass());
                SPUtils.saveString(MainActivity.this, saveData._key, saveData._value);
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
                String strValue = SPUtils.readString(MainActivity.this, data);
                function.onCallBack(strValue);
            }
        });
        /**
         * 提供给JS调用 打开录像界面
         */
        webView.registerHandler("RecVideo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Intent localIntent = new Intent("android.intent.action.VIEW");
                localIntent.setComponent(new ComponentName("com.star.video.starrec", "com.star.video.starrec.RecActivity"));
                // SharedPreferences localStroage = getSharedPreferences("com.sdxk", 0);
                localIntent.putExtra("hphm", hphm);
                // localIntent.putExtra("hphm","苏E00000");
                localIntent.putExtra("hpzl", hpzl);
                //localIntent.putExtra("hpzl","02");
                localIntent.putExtra("vectype", zpzl);
                localIntent.putExtra("clsbdh", clsbdh);
                localIntent.putExtra("jylsh", jylsh);
                localIntent.putExtra("zpzl", zpzl);
                String sdnSerIp = SPUtils.readString(MainActivity.this, "serviceip");
                String sdnSerPort = SPUtils.readString(MainActivity.this, "serviceport");
                localIntent.putExtra("ip", sdnSerIp);
                localIntent.putExtra("port", sdnSerPort);
                startActivityForResult(localIntent, 1);
                function.onCallBack("Response_sdn_camera"); //响应JS请求
            }
        });

        /**
         * 打开ip配置界面
         */
        webView.registerHandler("configIp", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                ShowConfig();
                function.onCallBack(String.format("{\"ip\":\"%s\",\"port\":\"%s\"}", strIp, strPort)); //响应JS请求
            }
        });
        /**
         * 清空缓存
         */
        webView.registerHandler("cleanUp", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                hphm = "";//当前办理车辆的号牌号码
                SPUtils.saveString(MainActivity.this, "hphm", "");
                hpzl = "";//当前办理车辆的号牌种类
                SPUtils.saveString(MainActivity.this, "hpzl", "");
                zpzl = "";//当前办理车辆的照片种类
                SPUtils.saveString(MainActivity.this, "zpzl", "");
                xsnr = "";//当前办理车辆的照片的显示类型
                SPUtils.saveString(MainActivity.this, "xsnr", "");
                clsbdh = "";//当前办理车辆的车辆识别代号
                SPUtils.saveString(MainActivity.this, "clsbdh", "");
                jylsh = "";//当前办理车辆的检验流水号
                SPUtils.saveString(MainActivity.this, "jylsh", "");
                lx = "";//操作类型 0 照片 1 视频
                SPUtils.saveString(MainActivity.this, "lx", "");
                function.onCallBack(String.format("{\"ip\":\"%s\",\"port\":\"%s\"}", strIp, strPort)); //响应JS请求
            }
        });

        //endregion


        //region  app webView开放的录像开始/结束 js函数
        /***
         * 开始录像
         * 参数 JSON 格式数据 {}
         */
        webView.registerHandler("startVideo", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Gson arrObj = new Gson();
                VideoData videodata = arrObj.fromJson(data, VideoData.class);
                Object[] arrayOfObject = new Object[7];
                arrayOfObject[0] = videodata.hphm;
                arrayOfObject[1] = videodata.hpzl;
                arrayOfObject[2] = videodata.lineNo;//线号
                arrayOfObject[3] = videodata.clsbdh; //车辆识别代码
                arrayOfObject[4] = videodata.queueId; //queueID
                arrayOfObject[5] = videodata.cllx; //车辆类型
                arrayOfObject[6] = videodata.ywlx; //车辆类型
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
                VideoData videodata = arrObj.fromJson(data, VideoData.class);
                Object[] arrayOfObject = new Object[9];
                arrayOfObject[0] = videodata.hphm;
                arrayOfObject[1] = videodata.hpzl;
                arrayOfObject[2] = videodata.lineNo;//线号
                arrayOfObject[3] = videodata.clsbdh; //车辆识别代码
                arrayOfObject[4] = videodata.queueId; //queueID
                arrayOfObject[5] = videodata.cllx; //车辆类型
                arrayOfObject[6] = videodata.ywlx; //车辆类型
                arrayOfObject[7] = videodata.syxz;//使用性质
                arrayOfObject[8] = videodata.bgys;//变更后的颜色（车辆颜色变更）
                StopVideo(arrayOfObject);//开始录像
                function.onCallBack("结束录像成功");
            }
        });
        //endregion

        try {
            hphm = SPUtils.readString(this, "hphm");//号牌号码
            hpzl = SPUtils.readString(this, "hpzl");//号牌种类
            clsbdh = SPUtils.readString(this, "clsbdh");//车辆识别代号
            jylsh = SPUtils.readString(this, "jylsh");//检验流水号
            zpzl = SPUtils.readString(this, "zpzl");//照片种类
            lx = SPUtils.readString(this, "lx");//拍照类型 0 照片 1 视频
            xsnr = SPUtils.readString(this, "xsnr");//显示内容
            String strJson_init = "{\"type\":\"init\",\"data\":{\"hphm\":\"%s\",\"hpzl\":\"%s\",\"zpzl\":\"%s\",\"xsnr\":\"%s\",\"lx\":\"%s\",\"ip\":\"%s\",\"port\":\"%s\"}}";
            strJson_init = String.format(strJson_init, hphm, hpzl, zpzl, xsnr, lx, strIp, strPort);
            webView.send(strJson_init); //给前端初始化值
        } catch (Exception ex) {
            ToastEx.ImageToast(this, R.mipmap.login_error_icon, "初始化数据失败", 2);
        }
        //此处用作给定前端初始值

        //启动http服务监听请求

        if (strPort == null || strPort.equals("")) {
            strPort = "51001";
        }
        httpServer = new HttpService(Integer.parseInt(strPort));
        // httpServer = new HttpServer();
        try {
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mainHandler = new MyHandler(Looper.getMainLooper());
        MainHandler.Init(mainHandler);

    }

    @Override
    protected void onStart() {
        super.onStart();
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        switch (screenDensity) {
            case DisplayMetrics.DENSITY_LOW:
                zoomDensity = WebSettings.ZoomDensity.CLOSE;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                zoomDensity = WebSettings.ZoomDensity.MEDIUM;
                break;
            case DisplayMetrics.DENSITY_HIGH:
            case DisplayMetrics.DENSITY_XHIGH:
            case DisplayMetrics.DENSITY_XXHIGH:
            default:
                zoomDensity = WebSettings.ZoomDensity.FAR;
                break;
        }
        webView.getSettings().setDefaultZoom(zoomDensity);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        int screenDensity = getResources().getDisplayMetrics().densityDpi;
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        switch (screenDensity) {
            case DisplayMetrics.DENSITY_LOW:
                zoomDensity = WebSettings.ZoomDensity.CLOSE;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                zoomDensity = WebSettings.ZoomDensity.MEDIUM;
                break;
            case DisplayMetrics.DENSITY_HIGH:
            case DisplayMetrics.DENSITY_XHIGH:
            case DisplayMetrics.DENSITY_XXHIGH:
            default:
                zoomDensity = WebSettings.ZoomDensity.FAR;
                break;
        }
        webView.getSettings().setDefaultZoom(zoomDensity);
    }

    //region 跳转activity返回结果处理

    /**
     * Intent 跳转返回
     *
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case 1://录像
                //此处可以根据两个Code进行判断，本页面和结果页面跳过来的值
                if (resultCode == 1) {
                    boolean result = intent.getBooleanExtra("result", false);
                    if (result) {
                        //  SetUIState(UISTATE.usComplete); //完成
                        String strJson = "{\"type\":\"video\",\"data\":\"%s\"}";
                        syncPost(String.format(strJson, ""));
                    } else {
                        //  SetUIState(UISTATE.usRecode); //录像
                        String strInfo = "{\"content\":\"%s\",\"type\":\"%s\"}"; //0 一般 黑色字体，1 凸显 绿色字体，2 警告 黄色字体，3 错误 红色字体
                        htmlShow(String.format(strInfo, "取消录像："+xsnr, 2));
                        webView.loadUrl("javascript:$(\"#recvideo\").addClass(\"active\"); ");
                    }
                }
                break;
            case 3://扫描二维码
                String strScanValue = intent.getExtras().getString("scanValue"); //得到对应的扫描结果
                webView.callHandler("showQRvalue", strScanValue, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        //得到对应的返回数据
                        Log.i(TAG, "reponse data from js" + data);
                    }
                });
                break;
            case 4://OBD对接
                int opetype_obd = intent.getExtras().getInt("optype_obd"); //得到页面返回值
                String obd_readdata = SPUtils.readString(MainActivity.this, "obd_readdata");
                String strObdVin = intent.getExtras().getString("obdvin");
                webView.callHandler("xslObd", strObdVin, new CallBackFunction() {
                    @Override
                    public void onCallBack(String data) {
                        //得到对应的返回数据
                        Log.i(TAG, "reponse data from js" + data);
                    }
                });
                break;
            case 5: //拍照界面
                String strImg = intent.getExtras().getString("str_image");
                String strStrImg = "";
                if (strImg.equals("1")) {
                    strStrImg = ImageUtils.getBase64Str(app.currentPhoto);
                    //调用post上传图片
                    String strJson = "{\"type\":\"photo\",\"data\":\"%s\"}";
                    syncPost(String.format(strJson, strStrImg));
                } else { //取消拍照
                    String strInfo = "{\"content\":\"%s\",\"type\":\"%s\"}"; //0 一般 黑色字体，1 凸显 绿色字体，2 警告 黄色字体，3 错误 红色字体
                    htmlShow(String.format(strInfo, "取消拍摄："+xsnr, 2));
                    webView.loadUrl("javascript:$(\"#camera\").addClass(\"active\"); ");
                }

                break;
            default:
        }
    }
    //endregion

    /* ************ 以下是 Socket通讯******************** */

    //region  APP控制服务端是否录像 废弃

    /**
     * 开始录像
     *
     * @param arrayOfObject
     */
    public void StartVideo(Object[] arrayOfObject) {
        if (SPUtils.readBoolean(this, "enablevideo")) {
            // ProgressDialog.Show(this);
            TcpCommandTask localTcpCommandTask = new TcpCommandTask();
           /* Object[] arrayOfObject = new Object[6];
            arrayOfObject[0] = "号牌号码";
            arrayOfObject[1] = "号牌种类";
            arrayOfObject[2] = "1"; //线号
            arrayOfObject[3] = ""; //车辆识别代号
            arrayOfObject[4] =  ""; //queueId
            arrayOfObject[5] = "K33"; //车辆类型
            */
            String strParm = String.format("<?xml version=\"1.0\" encoding=\"GB2312\"?><diagram type=\"start\" hphm=\"%s\" hpzl=\"%s\" jcxxh=\"%s\" clsbdh=\"%s\" queueid=\"%s\" cllx=\"%s\" ywlx=\"%s\" />",
                    arrayOfObject);
            localTcpCommandTask.execute(
                    new String[]{strParm});
        }
    }

    /**
     * 结束录像
     *
     * @param arrayOfObject
     */
    public void StopVideo(Object[] arrayOfObject) {
        if (SPUtils.readBoolean(this, "enablevideo")) {
            // ProgressDialog.Show(this);
            TcpCommandTask localTcpCommandTask = new TcpCommandTask();
            localTcpCommandTask.execute(new String[]{String
                    .format("<?xml version=\"1.0\" encoding=\"GB2312\"?><diagram type=\"end\" hphm=\"%s\" hpzl=\"%s\" jcxxh=\"%s\" clsbdh=\"%s\" " +
                            " queueid=\"%s\" cllx=\"%s\" ywlx=\"%s\"  new_use_type=\"%s\"   new_color=\"%s\" />",
                    arrayOfObject)});
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
    //endregion 废弃

    /* *************以上是 Socket通讯******************* */

    /**
     * 拍照参数解析类
     */
    private class GetCameraJson {
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
    private class responCamera {
        //代码
        String code;
        //字符串图片
        String strImg;
    }

    /**
     * 上传照片
     */
    private void syncPost(String strjson) {
        String strUrl = "http://%s:%s";
        strUrl = String.format(strUrl, SPUtils.readString(MainActivity.this, "ip"), SPUtils.readString(MainActivity.this, "port"));
        new HttpPost().Post_json(strUrl, strjson);
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            MainHandler mainHandler = (MainHandler) msg.obj;
            String strInfo = "{\"content\":\"%s\",\"type\":\"%s\"}"; //0 一般 黑色字体，1 凸显 绿色字体，2 警告 黄色字体，3 错误 红色字体
            if(strIp==null||strIp.equals("")){ //如果IP为空，则让先配置IP
                ShowConfig();//弹出IP配置界面
                return; //阻止继续进行
            }
            switch (mainHandler.msgType) {
                case CMD: //得到指令开始处理照片
                    //编写拍照&处理照片的指令
                    JsonObject json_params = new JsonParser().parse(mainHandler.Info).getAsJsonObject();//得到对应的json字符串
                    JsonObject json_data = json_params.getAsJsonObject("data"); //得到json数据
                   // Log.i(TAG, "handleMessage: " + json_data.toString());
                    //解析json {"type":"cmd","data":{"hphm":"","hpzl":"","zpzl":"","xsnr":"","lx":"0 照片 1视频"}}
                    if (json_data.has("hphm"))
                        hphm = json_data.get("hphm").getAsString();//得到号牌号码
                    SPUtils.saveString(MainActivity.this, "hphm", hphm); //保存到本地
                    if (json_data.has("hpzl"))
                        hpzl = json_data.get("hpzl").getAsString();//得到号牌种类
                    SPUtils.saveString(MainActivity.this, "hpzl", hpzl);
                    if (json_data.has("zpzl"))
                        zpzl = json_data.get("zpzl").getAsString();//照片种类
                    SPUtils.saveString(MainActivity.this, "zpzl", zpzl);
                    if (json_data.has("clsbdh"))
                        clsbdh = json_data.get("clsbdh").getAsString();//得到车辆识别代号
                    SPUtils.saveString(MainActivity.this, "clsbdh", clsbdh);
                    if (json_data.has("jylsh"))
                        jylsh = json_data.get("jylsh").getAsString();//得到检验流水号
                    SPUtils.saveString(MainActivity.this, "jylsh", jylsh);
                    if (json_data.has("lx")) {
                        lx = json_data.get("lx").getAsString();//得到拍照类型
                        SPUtils.saveString(MainActivity.this, "lx", lx);
                    }
                    if (json_data.has("xsnr")) {
                        xsnr = json_data.get("xsnr").getAsString();//得到显示内容
                        SPUtils.saveString(MainActivity.this, "xsnr", xsnr); //显示内容
                    }
                    //<div class="logs_item">1.2020-12-9,完成设置操作</div>
                    String json_btn = "{\"xsnr\":\"%s\",\"type\":\"%s\"}";
                    if (lx.equals("0")) { //拍照类型为照片
                        htmlShow(String.format(strInfo, "收到拍照指令:" + xsnr, 1));
                        //  htmlTips( String.format(json_btn,"请拍照："+xsnr,lx));
                        htmlTips(json_data.toString());
                    } else {
                        htmlShow(String.format(strInfo, "收到录像指令:" + xsnr, 1));
                        //  htmlTips("录像："+xsnr);
                        //htmlTips( String.format(json_btn,"请录像："+xsnr,lx));
                        htmlTips(json_data.toString());
                    }
                    //控制前台按钮事件
                    break;
                case CMDEND://得到结束的命令
                    //处理收到  清空缓存数据
                    SPUtils.saveString(MainActivity.this, "hphm", ""); //保存到本地
                    SPUtils.saveString(MainActivity.this, "hpzl", "");
                    SPUtils.saveString(MainActivity.this, "zpzl", "");
                    SPUtils.saveString(MainActivity.this, "clsbdh", "");
                    SPUtils.saveString(MainActivity.this, "jylsh", "");
                    htmlShow(String.format(strInfo, "拍照完成，请点击完成检测", 1));
                    //  htmlTips("{}");
                    webView.callHandler("show_tip_only", "拍照完成，请点击完成检测", new CallBackFunction() {
                        @Override
                        public void onCallBack(String data) {
                            //调用前台js函数的返回值
                        }
                    });
                    break;
                case SEND_FAIL://发送失败
                    htmlShow(String.format(strInfo, "发送照片响应失败，请重新上传", 3));
                    //失败后控制拍照界面 再次拍照
                    break;
                case SEND_SUCCESS:
                    htmlShow(String.format(strInfo, "发送照片响应成功", 0));
                    break;
                case REV_FAIL://保存图片失败
                    htmlShow(String.format(strInfo, "发送照片保存失败，请重新上传", 3));
                    //失败后控制拍照界面 再次拍照
                    break;
                case REV_SUCCESS: //保存图片成功
                    htmlShow(String.format(strInfo, "发送照片保存成功", 0));
                    break;
                case ERROE://异常
                    htmlShow(String.format(strInfo, mainHandler.Info, 3));
                    break;
            }
        }

    }

    /**
     * 运行日志显示
     *
     * @param strInfo
     */
    private void htmlShow(String strInfo) {
        webView.callHandler("js_show_log", strInfo, new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                //调用前台js函数的返回值
            }
        });
    }

    /**
     * 显示拍照种类
     *
     * @param strInfo
     */
    private void htmlTips(String strInfo) {
        //js_show_tip
        webView.callHandler("js_show_tip", strInfo, new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                //调用前台js函数的返回值
            }
        });
    }

    /**
     * 接收到命令后 把CMD data发送到前台
     *
     * @param jsonData
     */
    private void htmlCMD(String jsonData) {
        //js_show_tip
        webView.callHandler("js_deal_cmd", jsonData, new CallBackFunction() {
            @Override
            public void onCallBack(String data) {
                //调用前台js函数的返回值
            }
        });
    }

    private void ShowConfig() {
        View localView = getLayoutInflater().inflate(R.layout.ipconfig, null);
        MainActivity.this.ip = ((EditText) localView.findViewById(R.id.ipAddress));
        MainActivity.this.port = ((EditText) localView.findViewById(R.id.port));
        MainActivity.this.serviceIP = (EditText) localView.findViewById(R.id.serviceip); //服务IP
        MainActivity.this.servicePort = (EditText) localView.findViewById(R.id.serviceport);//服务
        ip.setText(SPUtils.readString(MainActivity.this, "ip"));
        port.setText(SPUtils.readString(MainActivity.this, "port"));
        serviceIP.setText(SPUtils.readString(MainActivity.this, "serviceip"));//读取缓存的服务IP
        servicePort.setText(SPUtils.readString(MainActivity.this, "serviceport"));//读取服务端口
        new android.app.AlertDialog.Builder(MainActivity.this).setTitle("IP配置").setView(localView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface paramAnonymousDialogInterface,
                            int paramAnonymousInt) {
                        String str1 = ip.getText().toString();
                        String str2 = port.getText().toString();
                        String strServiceIP = serviceIP.getText().toString();
                        String strServicePort = servicePort.getText().toString();
                        SPUtils.saveString(MainActivity.this, "ip", str1);
                        ip=ip;
                        SPUtils.saveString(MainActivity.this, "port", str2.trim());
                        port=port;
                        SPUtils.saveString(MainActivity.this, "serviceip", strServiceIP);
                        serviceIP=serviceIP;
                        SPUtils.saveString(MainActivity.this, "serviceport", strServicePort);
                        servicePort=servicePort;
                    }
                }).setNegativeButton("取消", null).show();
    }

}
