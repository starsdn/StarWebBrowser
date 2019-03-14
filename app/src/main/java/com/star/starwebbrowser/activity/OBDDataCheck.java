package com.star.starwebbrowser.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import java.util.List;

import com.star.starwebbrowser.R;
import com.star.starwebbrowser.obd.ConfigActivity;
import com.star.starwebbrowser.obd.SdnCommand;
import com.star.starwebbrowser.save.SPUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.sdn.obd.commands.SpeedObdCommand;
import com.sdn.obd.commands.control.CommandEquivRatioObdCommand;
import com.sdn.obd.commands.engine.EngineRPMObdCommand;
import com.sdn.obd.commands.engine.MassAirFlowObdCommand;
import com.sdn.obd.commands.fuel.FuelEconomyObdCommand;
import com.sdn.obd.commands.fuel.FuelLevelObdCommand;
import com.sdn.obd.commands.fuel.FuelTrimObdCommand;
import com.sdn.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import com.sdn.obd.enums.AvailableCommandNames;
import com.sdn.obd.enums.FuelTrim;
import com.sdn.obd.reader.IPostListener;
import com.sdn.obd.reader.io.ObdCommandJob;
import com.sdn.obd.reader.io.ObdGatewayService;
import com.sdn.obd.reader.io.ObdGatewayServiceConnection;

import static android.os.PowerManager.SCREEN_DIM_WAKE_LOCK;


public class OBDDataCheck extends SuperActivity implements
        View.OnClickListener {

    Button btnStartOBD;//连接OBD
    Button btnRead;//读取OBD信息
    Button btnAddBlack;//添加黑名单数据
    Button  btnNext;  //继续按钮
    Button btnCancle;//返回按钮
    Button btnPerson; //人工审核
    Button btnPass;//人工审核  通过
    Button btnNoPass;//人工审核  不通过
    EditText editContent;//编辑框  人工审核理由
    LinearLayout lineEdit; //编辑理由列
    LinearLayout lineButton; //按钮列
    TextView  txtCLSBDH;  //车辆识别代号
    TextView txtOBDVin;//OBD车辆识别代码数据
    TextView txtIsSame;//OBD vin和查验车辆vin是否一致

    EditText editBlackMark;//加入黑名单备注

    String strCLSBDH=""; //车辆识别代号
    String strObdVin=""; //OBD
    int sdnissame=2;  //obd读取vin和查验车辆信息vin比对   0:不一致 1:一致 2:未知
    int iDeptype=0;  //0:4S店  1:苏州车管所  2:县市车管所


    private int speed = 1;
    private double maf = 1;
    private float ltft = 0;
    private double equivRatio = 1;

    //***********以下为服务定义
    private static final String TAG = "OBDDataCheck";
    static final int NO_BLUETOOTH_ID = 0; //没有蓝牙
    static final int BLUETOOTH_DISABLED = 1;  //蓝牙不可用
    static final int NO_GPS_ID = 2;  //没有GPS
    static final int START_LIVE_DATA = 3;  //开始实时读取数据
    static final int STOP_LIVE_DATA = 4;   //停止实时读取数据
    static final int SETTINGS = 5;         //设置
    static final int COMMAND_ACTIVITY = 6;
    static final int TABLE_ROW_MARGIN = 7; //显示内容表格margin
    static final int NO_ORIENTATION_SENSOR = 8;

    private SensorManager sensorManager = null;
    private Sensor orientSensor = null;
    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;
    private SharedPreferences prefs = null;

    private Handler mHandler = new Handler();
    private IPostListener mListener = null;
    private Intent mServiceIntent = null;
    private ObdGatewayServiceConnection mServiceConnection = null;
    private boolean preRequisites = true;


    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_obd_check);
        InitControl();
        InitUI(0);//默认不合格

    }

    /**
     * 初始化控件
     */
    @SuppressWarnings({ "deprecation", "deprecation" })
    private void InitControl(){

        btnStartOBD =(Button)findViewById(R.id.sdnStarObd);//开始连接OBD
        btnRead = (Button)findViewById(R.id.sdnbtnRead);
        btnNext = (Button)findViewById(R.id.btnNext);
        btnAddBlack =(Button)findViewById(R.id.sdnaddblack);
        btnCancle = (Button)findViewById(R.id.btnCancle);
        btnPerson = (Button)findViewById(R.id.btnPerson);
        btnPass = (Button)findViewById(R.id.btnPass);
        btnNoPass = (Button)findViewById(R.id.btnNoPass);//人工审核 不通过
        lineEdit  = (LinearLayout)findViewById(R.id.layout40);  //编辑原因列
        lineButton = (LinearLayout)findViewById(R.id.layout50);//按钮列表
        txtCLSBDH = (TextView)findViewById(R.id.txtCLSBDH);//车辆识别代号
        txtOBDVin = (TextView)findViewById(R.id.txtOBD);  //OBDz
        txtIsSame = (TextView)findViewById(R.id.txtsame);//
        editContent = (EditText)findViewById(R.id.txtCONTENT);

        btnRead.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnStartOBD.setOnClickListener(this);
        btnCancle.setOnClickListener(this);
        btnPerson.setOnClickListener(this);
        btnPass.setOnClickListener(this);
        btnNoPass.setOnClickListener(this);
        btnAddBlack.setOnClickListener(this);

        Intent localIntent = getIntent();
        strCLSBDH = localIntent.getStringExtra("clsbdh"); //得到传递过来的车辆识别代号
        txtCLSBDH.setText(strCLSBDH);
        txtCLSBDH.setTextColor(Color.BLUE);



        mListener = new IPostListener() {
            public void stateUpdate(ObdCommandJob job) {
                String cmdName = job.getCommand().getName();
                String cmdResult = job.getCommand().getFormattedResult();
                Log.d(TAG, FuelTrim.LONG_TERM_BANK_1.getBank() + " equals " + cmdName + "?");
                if (AvailableCommandNames.ENGINE_RPM.getValue().equals(cmdName)) { //发动机
                } else if (AvailableCommandNames.SPEED.getValue().equals(cmdName)) { //速度
                    speed = ((SpeedObdCommand) job.getCommand()).getMetricSpeed();
                } else if (AvailableCommandNames.MAF.getValue().equals(cmdName)) { //空气质量流量
                    maf = ((MassAirFlowObdCommand) job.getCommand()).getMAF();
                    addTableRow(cmdName, cmdResult);
                } else if (FuelTrim.LONG_TERM_BANK_1.getBank().equals(cmdName)) { //燃料
                    ltft = ((FuelTrimObdCommand) job.getCommand()).getValue();
                } else if (AvailableCommandNames.EQUIV_RATIO.getValue().equals(cmdName)) { //当量比
                    equivRatio = ((CommandEquivRatioObdCommand) job.getCommand()).getRatio();
                    addTableRow(cmdName, cmdResult);
                } else {
                    addTableRow(cmdName, cmdResult);
                }
            }
        };

        /*
         * GPS服务
         */
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.getProvider(LocationManager.GPS_PROVIDER) == null) {
            showDialog(NO_GPS_ID);
        }

        /*
         * 验证蓝牙是否存在
         */
        final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            preRequisites = false;

        } else {
            if (!mBtAdapter.isEnabled()) {
                preRequisites = false;
                showDialog(BLUETOOTH_DISABLED);
            }
        }

        /*
         * 方位传感器
         */
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sens = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (sens.size() <= 0) {
            showDialog(NO_ORIENTATION_SENSOR);
        } else {
            orientSensor = sens.get(0);
        }

        // APP 正常运行必须服务
        if (preRequisites) {
            /*
             * 连接必须服务
             */
            showToast("开始绑定蓝牙服务……");
            mServiceIntent = new Intent(this, ObdGatewayService.class);
            mServiceConnection = new ObdGatewayServiceConnection();
            mServiceConnection.setServiceListener(mListener);

            // 绑定服务
            if(bindService(mServiceIntent, mServiceConnection,Context.BIND_AUTO_CREATE))//;
            {
                showToast("蓝牙服务绑定成功！");
            }
        }
    }


    /*
     * 显示信息
     */
    private void ShowMsg(String strMsg)
    {
        new AlertDialog.Builder(this)
                .setTitle("退出")
                .setMessage(strMsg+"是否退出检验？")
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface paramAnonymousDialogInterface,
                                    int paramAnonymousInt) {
                                Intent localIntent = new Intent();
                                localIntent.putExtra("optype_obd", 0); //取消
                                setResult(-1, localIntent);
                                finish();
                                return;

                            }
                        })
                .setNegativeButton("取消",
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    DialogInterface paramAnonymousDialogInterface,
                                    int paramAnonymousInt) {
                            }
                        }).create().show();
    }

    /*
     * 初始化UI界面
     */
    private void InitUI(int iParam){
        switch(iParam){
            case 0://不合格
            //    btnNext.setVisibility(View.GONE);//继续按钮不可用
             //   lineEdit.setVisibility(View.GONE); //编辑列布可见
             //   lineButton.setVisibility(View.GONE);
                break;
            case 1: //合格
             //   lineEdit.setVisibility(View.GONE); //编辑列布可见
              //  lineButton.setVisibility(View.GONE);
             //   btnNext.setVisibility(View.VISIBLE);
                break;
            case 2: //未知
           //   //  btnNext.setVisibility(View.GONE);//继续按钮不可用
             //   lineEdit.setVisibility(View.VISIBLE); //编辑列布可见
             //   lineButton.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }

    private void releaseWakeLockIfHeld() {
        try{
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }catch(Exception e){

        }
    }

    private final SensorEventListener orientListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            String dir = "";
            if (x >= 337.5 || x < 22.5) {
                dir = "N";
            } else if (x >= 22.5 && x < 67.5) {
                dir = "NE";
            } else if (x >= 67.5 && x < 112.5) {
                dir = "E";
            } else if (x >= 112.5 && x < 157.5) {
                dir = "SE";
            } else if (x >= 157.5 && x < 202.5) {
                dir = "S";
            } else if (x >= 202.5 && x < 247.5) {
                dir = "SW";
            } else if (x >= 247.5 && x < 292.5) {
                dir = "W";
            } else if (x >= 292.5 && x < 337.5) {
                dir = "NW";
            }
            //	TextView compass = (TextView) findViewById(R.id.compass_text);
            //updateTextView(compass, dir);
        }

        public void updateTextView(final TextView view, final String txt) {
            new Handler().post(new Runnable() {
                public void run() {
                    view.setText(txt);
                }
            });
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
    };


    @SuppressLint("InvalidWakeLockTag")
    @SuppressWarnings("deprecation")
    protected void onResume() {
        super.onResume();

        sensorManager.registerListener(orientListener, orientSensor,SensorManager.SENSOR_DELAY_UI);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(SCREEN_DIM_WAKE_LOCK,"ObdReader");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopLiveData();//关闭服务

        releaseWakeLockIfHeld();
        mServiceIntent = null;
        mServiceConnection = null;
        mListener = null;
        mHandler = null;
        Intent localIntent = new Intent();
        localIntent.putExtra("optype_obd", 0); //取消
        setResult(-1, localIntent);
        finish();

    }

    @Override
    protected void onPause() {
        super.onPause();

        try{
            releaseWakeLockIfHeld();
        }catch(Exception e){

        }
    }

    @Override
    public void onClick(View v) {
        if(v==btnNext){ //继续
            stopLiveData();
            Intent localIntent = new Intent();
            localIntent.putExtra("optype_obd", 1); //继续
            localIntent.putExtra("obdvin", strObdVin);
            setResult(-1, localIntent);
            finish();
            return;

        }else if(v==btnCancle){ //返回
            stopLiveData();
            Intent localIntent = new Intent();
            localIntent.putExtra("optype_obd", 0); //取消
            localIntent.putExtra("obdvin", "0");
            setResult(-1, localIntent);
            finish();
            return;

        }else if(v==btnPerson){ //人工审核
            InitUI(2);
            //stopLiveData();
            return;
        }else if(v==btnPass){ //人工审核 合格
            String strContent = editContent.getText().toString();
            if(strContent.equals(null) || strContent.equals("")){
                //不能为空或null

                //editContent.setText("原因不能为空！");
                editContent.setHint("原因不能为空！");
                return;
            }
            //stopLiveData();
            Intent localIntent = new Intent();
            localIntent.putExtra("optype_obd", 2); //取消
            localIntent.putExtra("pertype_obd", 1); // 人工审核 合格
            localIntent.putExtra("sdnissame", sdnissame);
            localIntent.putExtra("sdnReason_obd", strContent);
            setResult(-1, localIntent);
            finish();
            return;
        }else if(v==btnNoPass){//人工审核  不合格
            String strContent = editContent.getText().toString();
            if(strContent.equals(null) || strContent.equals("")){
                //不能为空或null

                //editContent.setText("原因不能为空！");
                editContent.setHint("原因不能为空！");
                return;
            }
            //stopLiveData();
            Intent localIntent = new Intent();
            localIntent.putExtra("optype_obd", 2); //取消
            localIntent.putExtra("pertype_obd", 0); // 人工审核  不合格
            localIntent.putExtra("sdnissame", sdnissame);
            localIntent.putExtra("sdnReason_obd", strContent);
            setResult(-1, localIntent);
            finish();
            return;
        } else if(v==btnAddBlack){  //添加黑名单
            /*
            View vAddBlack = getLayoutInflater().inflate(R.layout.activity_add_black,null);
            editBlackMark = (EditText)vAddBlack.findViewById(R.id.sdnblacemark);
            new AlertDialog.Builder(this)
                    .setTitle("添加黑名单")
                    .setView(vAddBlack)
                    .setPositiveButton("确定",new DialogInterface.OnClickListener() {
                        public void onClick(
                                DialogInterface paramAnonymousDialogInterface,
                                int paramAnonymousInt) {
                            String strBlackMark = editBlackMark.getText().toString();//得到备注
                            Intent localIntent = new Intent();
                            localIntent.putExtra("optype_obd", 99); //取消
                            localIntent.putExtra("pertype_obd", 0); // 人工审核  不合格
                            localIntent.putExtra("sdnReason_obd", strBlackMark);
                            setResult(-1, localIntent);
                            finish();
                        }
                    })
                    .setNegativeButton("取消", null).show();
                    */
            updateConfig();//打开配置按钮
        }else  if(v==btnStartOBD){ //开始连接OBD
            startLiveData();
            return;
        }
        else if(v==btnRead){  //读取OBD数据 并分析VIN吗

             //startLiveData(); //先连接 OBD
            //LZWACAGA597057988
            //txtOBDVin.setText("LZWACAGA597057988");

            //1、先打开OBD 连接  读取数据

            //2、发送读取车辆基本信息指令 读取车辆基本信息

            //3、读取并解析车辆基本信息
            if(mServiceConnection==null && !mServiceConnection.isRunning()){
                showToast("服务未连接！！！");
                return;
            }

            try{

                final SdnCommand command=new SdnCommand("0902"); //获取车辆识别代号
                mServiceConnection.addJobToQueue(new ObdCommandJob(command));
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showToast("读取车辆基本信息!!");
                        String strResult = command.getResult().toUpperCase(); //强制转换成大写 2017年8月1日13:40:26 shandinan
                        showToast(strResult);
                        try{
                            showToast("车辆识别代码原始码长度："+strResult.length());
                            if(strResult.length()==48&&strResult !=null &&!strResult.equals("")&&!strResult.equals("NODATA")){ //strResult 不为空和null
                                strResult = strResult.trim().replace("0:", ""); //去除结果集字符串中的空格
                                String strASC2Vin = strResult.replace("\r1:", "").replace("\r2:", "");
                                txtOBDVin.setText(asciiToString(strASC2Vin));
                                strObdVin = asciiToString(strASC2Vin);
                                // }else{
                                //  txtOBDVin.setText("获取vin失败");
                                //}
                            }else if(strResult.length()==52&&strResult !=null &&!strResult.equals("")&&!strResult.equals("NODATA")){
                                strResult = strResult.trim().replace("014\r0:", ""); //去除结果集字符串中的空格
                                String strASC2Vin = strResult.replace("\r1:", "").replace("\r2:", "");
                                txtOBDVin.setText(asciiToString(strASC2Vin));
                                strObdVin = asciiToString(strASC2Vin);
                            }else if(strResult.length()==59&&strResult !=null &&!strResult.equals("")&&!strResult.equals("NODATA"))
                            {
                                strResult = strResult.trim().replace("7F0922\r", "").replace("014\r0:", ""); //去除结果集字符串中的空格
                                String strASC2Vin = strResult.replace("\r1:", "").replace("\r2:", "");
                                txtOBDVin.setText(asciiToString(strASC2Vin));
                                strObdVin = asciiToString(strASC2Vin);
                            }
                            else{
                                //  txtOBDVin.setText("获取vin为空");
                                strResult = strResult.trim().replace(" ", "").replace("\r", "");
                                // showToast("车辆识别代码原始码长度："+strResult.length());
                                if(strResult.length()==76){ //大众车 车辆识别代号
                                    String strASC2Vin = strResult.replace("490202", "").replace("490203", "")
                                            .replace("490204", "").replace("490205", "").replace("7F0912", "")
                                            .replace("000000", "");
                                    showToast(strASC2Vin);
                                    txtOBDVin.setText(asciiToString(strASC2Vin));
                                    showToast(asciiToString(strASC2Vin));
                                    strObdVin = asciiToString(strASC2Vin);
                                }else if(strResult.length()==70){
                                    String strASC2Vin = strResult.replace("490202", "").replace("490203", "")
                                            .replace("490204", "").replace("490205", "")
                                            .replace("000000", "");
                                    showToast(strASC2Vin);
                                    txtOBDVin.setText(asciiToString(strASC2Vin));
                                    showToast(asciiToString(strASC2Vin));
                                    strObdVin = asciiToString(strASC2Vin);
                                }

                            }
                            SPUtils.saveString(OBDDataCheck.this, "obd_readdata", strObdVin);
                        }catch(Exception ex){
                            txtOBDVin.setText("获取vin失败,请重新获取");
                        }

                        txtOBDVin.setTextColor(Color.BLUE);


                        if(strCLSBDH.equals(strObdVin)){ //如果车辆识别代号和OBD读取到的一致
                            sdnissame =1;
                            txtIsSame.setText("一致");
                            // isSame =1;
                            txtIsSame.setTextColor(Color.GREEN); //一直背景为绿色
                            InitUI(1);//设置为合格
                        }else if(!strObdVin.equals("")){
                            sdnissame =0;
                            txtIsSame.setText("不一致");
                            txtIsSame.setTextColor(Color.RED); //一直背景为绿色
                            InitUI(0);//设置为合格
                        }
                        else{
                            sdnissame =2;
                            txtIsSame.setText("未知");
                            txtIsSame.setTextColor(Color.YELLOW); //一直背景为绿色
                            InitUI(2);//设置为未知
                        }
                    }
                },2000);


            }catch(Exception ex){

            }


        }
    }

    /*
     * ASCII 转换成string
     * 4902014C56534846464143324546373632383437
     */
    private  String asciiToString(String value)
    {
        StringBuffer sbu = new StringBuffer();

        byte[] arrByte = HexString2Bytes(value);
        int iStart=0;//是否开始
        for (int i = 0; i < arrByte.length; i++) {

            if(iStart>0){
                sbu.append((char) Integer.parseInt(arrByte[i]+""));
            }else{
                if(Integer.parseInt(arrByte[i]+"")==1)
                {
                    iStart =1;
                }
            }

        }
        return sbu.toString();
    }
    //把字符串转换成标准的16进制  例如 4C--->0x4c
    public  byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte)(_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte)(_b0 ^ _b1);
        return ret;
    }
    /*
     * 把16进制转换成byte
     */
    public  byte[] HexString2Bytes(String src){
        int iLength = src.length();
        byte[] ret = new byte[iLength/2];
        byte[] tmp = src.getBytes();
        for(int i=0; i<iLength/2; i++){
            ret[i] = uniteBytes(tmp[i*2], tmp[i*2+1]);
        }
        return ret;
    }


    /**
     * 更新配置项目
     */
    private void updateConfig() {
        Intent configIntent = new Intent(this, ConfigActivity.class);
        startActivity(configIntent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, START_LIVE_DATA, 0, "开始连接服务");
        menu.add(0, COMMAND_ACTIVITY, 0, "执行命令");
        menu.add(0, STOP_LIVE_DATA, 0, "停止");
        menu.add(0, SETTINGS, 0, "设置");
        return true;
        //return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case START_LIVE_DATA: //获取实时数据
                startLiveData();
                return true;
            case STOP_LIVE_DATA: //停止获取数据
                stopLiveData();
                return true;
            case SETTINGS:   //设置
                updateConfig();
                return true;
        }
        return false;
    }

    private void startLiveData() {
        try
        {
            Log.d(TAG, "开始读取实时数据..");
            if (!mServiceConnection.isRunning()) {
                showToast("蓝牙服务没有运行!!!");
                startService(mServiceIntent);
            }
            //	showToast("奶奶的，服务终于正常了");
            showToast("OBD读取数据……");
            // start command execution
            mHandler.post(mQueueCommands);

            // screen won't turn off until wakeLock.release()
            wakeLock.acquire();

        }catch(Exception ex){
            showToast("OBD读取数据异常……");
        }

    }

    private void stopLiveData() {
        Log.d(TAG, "Stopping live data..");
        try {

            if (mServiceConnection.isRunning())
                stopService(mServiceIntent);

            // remove runnable
            mHandler.removeCallbacks(mQueueCommands);

            releaseWakeLockIfHeld();

            Thread.sleep(1000); //关闭之后 停止1s 然后再进下面操作
        } catch (InterruptedException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    /**
     *执行实时  读取OBD数据命令
     */
    private Runnable mQueueCommands = new Runnable() {
        public void run() {
			/*
			if (speed > 1 && maf > 1 && ltft != 0) {
				FuelEconomyWithMAFObdCommand fuelEconCmd = new FuelEconomyWithMAFObdCommand(
						FuelType.DIESEL, speed, maf, ltft, false );//TODO
			//	TextView tvMpg = (TextView) findViewById(R.id.fuel_econ_text);
				String liters100km = String.format("%.2f", fuelEconCmd.getLitersPer100Km());
				//tvMpg.setText("" + liters100km);
				Log.d(TAG, "FUELECON:" + liters100km);
			}
*/
            if (mServiceConnection.isRunning())
                //	queueCommands();

                // run again in 2s
                mHandler.postDelayed(mQueueCommands, 2000);
        }
    };

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder build = new AlertDialog.Builder(this);
        switch (id) {
            case NO_BLUETOOTH_ID:
                build.setMessage("Sorry, your device doesn't support Bluetooth.");
                return build.create();
            case BLUETOOTH_DISABLED:
                build.setMessage("You have Bluetooth disabled. Please enable it!");
                return build.create();
            case NO_GPS_ID:
                build.setMessage("Sorry, your device doesn't support GPS.");
                return build.create();
            case NO_ORIENTATION_SENSOR:
                build.setMessage("Orientation sensor missing?");
                return build.create();
        }
        return null;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem startItem = menu.findItem(START_LIVE_DATA);
        MenuItem stopItem = menu.findItem(STOP_LIVE_DATA);
        MenuItem settingsItem = menu.findItem(SETTINGS);
        MenuItem commandItem = menu.findItem(COMMAND_ACTIVITY);

        // validate if preRequisites are satisfied.
        if (preRequisites) {
            if (mServiceConnection.isRunning()) {
                startItem.setEnabled(false);
                stopItem.setEnabled(true);
                settingsItem.setEnabled(false);
                commandItem.setEnabled(false);
            } else {
                stopItem.setEnabled(false);
                startItem.setEnabled(true);
                settingsItem.setEnabled(true);
                commandItem.setEnabled(false);
            }
        } else {
            startItem.setEnabled(false);
            stopItem.setEnabled(false);
            settingsItem.setEnabled(false);
            commandItem.setEnabled(false);
        }

        return true;
    }

    private void addTableRow(String key, String val) {
        TableLayout tl = (TableLayout) findViewById(R.id.data_table);
        TableRow tr = new TableRow(this);
        MarginLayoutParams params = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN,TABLE_ROW_MARGIN);
        tr.setLayoutParams(params);
        tr.setBackgroundColor(Color.BLACK);
        TextView name = new TextView(this);
        name.setGravity(Gravity.RIGHT);
        name.setText(key + ": ");
        TextView value = new TextView(this);
        value.setGravity(Gravity.LEFT);
        value.setText(val);
        tr.addView(name);
        tr.addView(value);
        tl.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        /*
         * TODO remove this hack
         *
         * let's define a limit number of rows
         */
        if (tl.getChildCount() > 10)
            tl.removeViewAt(0);
    }

    /**
     * 命令队列
     */
    private void queueCommands() {
        final ObdCommandJob airTemp = new ObdCommandJob(new AmbientAirTemperatureObdCommand());
        final ObdCommandJob speed = new ObdCommandJob(new SpeedObdCommand());
        final ObdCommandJob fuelEcon = new ObdCommandJob(new FuelEconomyObdCommand());
        final ObdCommandJob rpm = new ObdCommandJob(new EngineRPMObdCommand());
        final ObdCommandJob maf = new ObdCommandJob(new MassAirFlowObdCommand());
        final ObdCommandJob fuelLevel = new ObdCommandJob(new FuelLevelObdCommand());
        final ObdCommandJob ltft1 = new ObdCommandJob(new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_1));
        final ObdCommandJob ltft2 = new ObdCommandJob(new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_2));
        final ObdCommandJob stft1 = new ObdCommandJob(new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_1));
        final ObdCommandJob stft2 = new ObdCommandJob(new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_2));
        final ObdCommandJob equiv = new ObdCommandJob(new CommandEquivRatioObdCommand());

        // mServiceConnection.addJobToQueue(airTemp);
        mServiceConnection.addJobToQueue(speed);
        // mServiceConnection.addJobToQueue(fuelEcon);
        mServiceConnection.addJobToQueue(rpm);
        mServiceConnection.addJobToQueue(maf);
        mServiceConnection.addJobToQueue(fuelLevel);
//		mServiceConnection.addJobToQueue(equiv);
        mServiceConnection.addJobToQueue(ltft1);
        // mServiceConnection.addJobToQueue(ltft2);
        // mServiceConnection.addJobToQueue(stft1);
        // mServiceConnection.addJobToQueue(stft2);
    }

    @Override
    public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
        if (paramInt == 4)
            new AlertDialog.Builder(this)
                    .setTitle("退出")
                    .setMessage("是否退出检验？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface paramAnonymousDialogInterface,
                                        int paramAnonymousInt) {
                                    stopLiveData();
                                    Intent localIntent = new Intent();
                                    localIntent.putExtra("optype_obd", 0); //取消
                                    setResult(-1, localIntent);
                                    finish();
                                    return;

                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface paramAnonymousDialogInterface,
                                        int paramAnonymousInt) {
                                }
                            }).create().show();
        return true;
    }

}

