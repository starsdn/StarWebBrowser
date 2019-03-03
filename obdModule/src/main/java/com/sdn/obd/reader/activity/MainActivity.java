package com.sdn.obd.reader.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.*;

import com.example.MyCommand;

import com.sdn.obd.commands.SpeedObdCommand;
import com.sdn.obd.commands.control.CommandEquivRatioObdCommand;
import com.sdn.obd.commands.engine.EngineRPMObdCommand;
import com.sdn.obd.commands.engine.MassAirFlowObdCommand;
import com.sdn.obd.commands.fuel.FuelEconomyObdCommand;
import com.sdn.obd.commands.fuel.FuelEconomyWithMAFObdCommand;
import com.sdn.obd.commands.fuel.FuelLevelObdCommand;
import com.sdn.obd.commands.fuel.FuelTrimObdCommand;
import com.sdn.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import com.sdn.obd.enums.AvailableCommandNames;
import com.sdn.obd.enums.FuelTrim;
import com.sdn.obd.enums.FuelType;
import com.sdn.obd.reader.IPostListener;
import com.sdn.obd.reader.R;
import com.sdn.obd.reader.io.ObdCommandJob;
import com.sdn.obd.reader.io.ObdGatewayService;
import com.sdn.obd.reader.io.ObdGatewayServiceConnection;

/**
 * The main activity.
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    /*
     * TODO 显示 描述
     */
    static final int NO_BLUETOOTH_ID = 0; //没有蓝牙
    static final int BLUETOOTH_DISABLED = 1;  //蓝牙不可用
    static final int NO_GPS_ID = 2;  //没有GPS
    static final int START_LIVE_DATA = 3;  //开始实时读取数据
    static final int STOP_LIVE_DATA = 4;   //停止实时读取数据
    static final int SETTINGS = 5;         //设置
    static final int COMMAND_ACTIVITY = 6;
    static final int TABLE_ROW_MARGIN = 7;
    static final int NO_ORIENTATION_SENSOR = 8;

    private Handler mHandler = new Handler();

    /**
     * Callback for ObdGatewayService to update UI.
     */
    private IPostListener mListener = null;
    private Intent mServiceIntent = null;
    private ObdGatewayServiceConnection mServiceConnection = null;

    private SensorManager sensorManager = null;
    private Sensor orientSensor = null;
    private SharedPreferences prefs = null;

    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;

    private boolean preRequisites = true;

    private int speed = 1;
    private double maf = 1;
    private float ltft = 0;
    private double equivRatio = 1;

    private EditText commandText;
    private TextView resultText;
    private Button sendButton;

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
            TextView compass = (TextView) findViewById(R.id.compass_text);
            updateTextView(compass, dir);
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
    };

    public void updateTextView(final TextView view, final String txt) {
        new Handler().post(new Runnable() {
            public void run() {
                view.setText(txt);
            }
        });
    }


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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        commandText=(EditText)this.findViewById(R.id.commandText);
        resultText=(TextView)this.findViewById(R.id.resultText);
        sendButton=(Button)this.findViewById(R.id.sendButton); //发送信息
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MyCommand command=new MyCommand(commandText.getText().toString());
                mServiceConnection.addJobToQueue(new ObdCommandJob(command));
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        String strResult = command.getResult();

                        resultText.setText(command.getName()+">>"+strResult);


                        try{
                            if(strResult.length()==48&&strResult !=null &&!strResult.equals("")&&!strResult.equals("NODATA")){ //strResult 不为空和null
                                strResult = strResult.trim().replace("0:", ""); //去除结果集字符串中的空格
                                String strASC2Vin = strResult.replace("\r1:", "").replace("\r2:", "");
                                resultText.setText(asciiToString(strASC2Vin));
                                // }else{
                                //  txtOBDVin.setText("获取vin失败");
                                //}
                            }else{
                                //  txtOBDVin.setText("获取vin为空");
                                strResult = strResult.trim().replace(" ", "").replace("\r", "");
                                if(strResult.length()==76){ //大众车 车辆识别代号
                                    strResult = strResult.replace("490202", "").replace("490203", "")
                                            .replace("490204", "").replace("490205", "").replace("7F0912", "").replace("000000", "");
                                    resultText.setText(asciiToString(strResult));
                                }
                            }

                        }catch(Exception ex){
                            resultText.setText("获取vin失败,请重新获取");
                        }

                    }
                },2000);
            }
        });

//**
        mListener = new IPostListener() {
            public void stateUpdate(ObdCommandJob job) {
                String cmdName = job.getCommand().getName();
                String cmdResult = job.getCommand().getFormattedResult();
                Log.d(TAG, FuelTrim.LONG_TERM_BANK_1.getBank() + " equals " + cmdName + "?");
                if (AvailableCommandNames.ENGINE_RPM.getValue().equals(cmdName)) {
                    TextView tvRpm = (TextView) findViewById(R.id.rpm_text);
                    tvRpm.setText(cmdResult);
                } else if (AvailableCommandNames.SPEED.getValue().equals(
                        cmdName)) {
                    TextView tvSpeed = (TextView) findViewById(R.id.spd_text);
                    tvSpeed.setText(cmdResult);
                    speed = ((SpeedObdCommand) job.getCommand())
                            .getMetricSpeed();
                } else if (AvailableCommandNames.MAF.getValue().equals(cmdName)) {
                    maf = ((MassAirFlowObdCommand) job.getCommand()).getMAF();
                    addTableRow(cmdName, cmdResult);
                } else if (FuelTrim.LONG_TERM_BANK_1.getBank().equals(cmdName)) {
                    ltft = ((FuelTrimObdCommand) job.getCommand()).getValue();
                } else if (AvailableCommandNames.EQUIV_RATIO.getValue().equals(cmdName)) {
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
            /*
             * TODO for testing purposes we'll not make GPS a pre-requisite.
             */
            // preRequisites = false;
            showDialog(NO_GPS_ID);
        }

        /*
         * 验证蓝牙是否存在
         */
        final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            preRequisites = false;
            showDialog(NO_BLUETOOTH_ID);
        } else {
            // Bluetooth device is enabled?
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
            mServiceIntent = new Intent(this, ObdGatewayService.class);
            mServiceConnection = new ObdGatewayServiceConnection();
            mServiceConnection.setServiceListener(mListener);

            // 绑定服务
            bindService(mServiceIntent, mServiceConnection,Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        releaseWakeLockIfHeld();
        mServiceIntent = null;
        mServiceConnection = null;
        mListener = null;
        mHandler = null;

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Pausing..");
        releaseWakeLockIfHeld();
    }

    /**
     * If lock is held, release. Lock will be held when the service is running.
     */
    private void releaseWakeLockIfHeld() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    /*
     * 开始之后执行
     * @see android.app.Activity#onResume()
     */
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(orientListener, orientSensor,SensorManager.SENSOR_DELAY_UI); //方位传感器
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE); //电源管理
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"ObdReader"); //电源保持唤醒
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
        menu.add(0, START_LIVE_DATA, 0, "开始读取数据");
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
            // case COMMAND_ACTIVITY:
            // staticCommand();
            // return true;
        }
        return false;
    }

    // private void staticCommand() {
    // Intent commandIntent = new Intent(this, ObdReaderCommandActivity.class);
    // startActivity(commandIntent);
    // }

    private void startLiveData() {
        Log.d(TAG, "开始读取实时数据..");
        if (!mServiceConnection.isRunning()) {
            Log.d(TAG, "Service is not running. Going to start it..");
            startService(mServiceIntent);
        }
        Toast.makeText(this, "开始读取实时数据", Toast.LENGTH_LONG).show();
        // start command execution
        mHandler.post(mQueueCommands);
        // screen won't turn off until wakeLock.release()
        wakeLock.acquire();
    }

    private void stopLiveData() {
        Log.d(TAG, "Stopping live data..");

        if (mServiceConnection.isRunning())
            stopService(mServiceIntent);

        // remove runnable
        mHandler.removeCallbacks(mQueueCommands);

        releaseWakeLockIfHeld();
    }

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
        MarginLayoutParams params = new MarginLayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(TABLE_ROW_MARGIN, TABLE_ROW_MARGIN, TABLE_ROW_MARGIN,
                TABLE_ROW_MARGIN);
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
     *执行实时  读取OBD数据命令
     */
    private Runnable mQueueCommands = new Runnable() {
        public void run() {
			/*  //单氐楠  2016年12月26日10:39:19 测试
			if (speed > 1 && maf > 1 && ltft != 0) {
				FuelEconomyWithMAFObdCommand fuelEconCmd = new FuelEconomyWithMAFObdCommand(
						FuelType.DIESEL, speed, maf, ltft, false );\\ TODO */
			/*
				TextView tvMpg = (TextView) findViewById(R.id.fuel_econ_text);
				String liters100km = String.format("%.2f", fuelEconCmd.getLitersPer100Km());
				tvMpg.setText("" + liters100km);
			}
		*/

            if (mServiceConnection.isRunning())
                //	queueCommands();

                // run again in 2s
                mHandler.postDelayed(mQueueCommands, 2000);
        }
    };

    /**
     * 发送获取信息指令代码
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
}
