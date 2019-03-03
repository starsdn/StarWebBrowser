
package com.sdn.obd.reader.activity;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import com.sdn.obd.commands.ObdCommand;
import com.sdn.obd.reader.R;
import com.sdn.obd.reader.config.ObdConfig;

/**
 * Configuration activity.
 */
public class ConfigActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference"; //蓝牙列表
	public static final String UPLOAD_URL_KEY = "upload_url_preference";  //上传url
	public static final String UPLOAD_DATA_KEY = "upload_data_preference"; //数据上传
	public static final String UPDATE_PERIOD_KEY = "update_period_preference";  //更新周期
	public static final String VEHICLE_ID_KEY = "vehicle_id_preference";  //车辆id
	public static final String ENGINE_DISPLACEMENT_KEY = "engine_displacement_preference"; //发动机排量
	public static final String VOLUMETRIC_EFFICIENCY_KEY = "volumetric_efficiency_preference"; //容积效率
	public static final String IMPERIAL_UNITS_KEY = "imperial_units_preference"; //英制单位
	public static final String COMMANDS_SCREEN_KEY = "obd_commands_screen"; //OBD命令
	public static final String ENABLE_GPS_KEY = "enable_gps_preference";  //打开GPS
	public static final String MAX_FUEL_ECON_KEY = "max_fuel_econ_preference"; //最大燃油经济性值
	public static final String CONFIG_READER_KEY = "reader_config_preference"; //读取配置命令

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Read preferences resources available at res/xml/preferences.xml
		 */
		addPreferencesFromResource(R.xml.preferences);

		ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<CharSequence>();
		ArrayList<CharSequence> vals = new ArrayList<CharSequence>();
		//蓝牙设备列表
		ListPreference listBtDevices = (ListPreference) getPreferenceScreen().findPreference(BLUETOOTH_LIST_KEY);
		String[] prefKeys = new String[] { ENGINE_DISPLACEMENT_KEY,
				VOLUMETRIC_EFFICIENCY_KEY, UPDATE_PERIOD_KEY, MAX_FUEL_ECON_KEY };
		for (String prefKey : prefKeys) {
			EditTextPreference txtPref = (EditTextPreference) getPreferenceScreen()
					.findPreference(prefKey);
			txtPref.setOnPreferenceChangeListener(this);
		}

		/*
		 * Available OBD commands
		 *
		 * TODO This should be read from preferences database
		 */
		ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
		PreferenceScreen cmdScr = (PreferenceScreen) getPreferenceScreen()
				.findPreference(COMMANDS_SCREEN_KEY);
		for (ObdCommand cmd : cmds) {
			CheckBoxPreference cpref = new CheckBoxPreference(this);
			cpref.setTitle(cmd.getName());
			cpref.setKey(cmd.getName());
			cpref.setChecked(true);
			cmdScr.addPreference(cpref);
		}

		/*
		 * 在蓝牙列表里面自动选择OBD-II 蓝牙列表中选择的蓝牙设备
		 */
		final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter == null) {
			listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
			listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));
			Toast.makeText(this, "This device does not support Bluetooth.",Toast.LENGTH_LONG);
			return;
		}

		/*
		 * 监听Preferences 点击事件
		 *
		 * TODO there are so many repeated validations :-/
		 */
		final Activity thisActivity = this;
		listBtDevices.setEntries(new CharSequence[1]);
		listBtDevices.setEntryValues(new CharSequence[1]);
		listBtDevices.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// see what I mean in the previous comment?
				if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
					Toast.makeText(thisActivity,
							"This device does not support Bluetooth or it is disabled.",
							Toast.LENGTH_LONG);
					return false;
				}
				return true;
			}
		});

		/*
		 * 获取配对设备并填充优先列表
		 * Get paired devices and populate preference list.
		 */
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				//在匹配设备字符串中添加相应的匹配设备的设备名称和设备MAC
				pairedDeviceStrings.add(device.getName() + "\n"+ device.getAddress());
				vals.add(device.getAddress()); //添加匹配设备mac
			}
		}
		listBtDevices.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
		listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));
	}

	/**
	 * OnPreferenceChangeListener method that will validate a preferencen new
	 * value when it's changed.
	 *
	 * @param preference
	 *            the changed preference
	 * @param newValue
	 *            the value to be validated and set if valid
	 */
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (UPDATE_PERIOD_KEY.equals(preference.getKey())
				|| VOLUMETRIC_EFFICIENCY_KEY.equals(preference.getKey())
				|| ENGINE_DISPLACEMENT_KEY.equals(preference.getKey())
				|| UPDATE_PERIOD_KEY.equals(preference.getKey())
				|| MAX_FUEL_ECON_KEY.equals(preference.getKey())) {
			try {
				Double.parseDouble(newValue.toString());
				return true;
			} catch (Exception e) {
				Toast.makeText(
						this,
						"Couldn't parse '" + newValue.toString()
								+ "' as a number.", Toast.LENGTH_LONG).show();
			}
		}
		return false;
	}

	/**
	 * 得到上传数据周期
	 * @param prefs
	 * @return
	 */
	public static int getUpdatePeriod(SharedPreferences prefs) {
		String periodString = prefs.getString(ConfigActivity.UPDATE_PERIOD_KEY,"4"); //4秒
		int period = 4000; // 默认 4000ms
		try {
			period = Integer.parseInt(periodString) * 1000;
		} catch (Exception e) {
		}
		if (period <= 0) {
			period = 250;
		}
		return period;
	}

	/**
	 * 体积 电  效率
	 * @param prefs
	 * @return
	 */
	public static double getVolumetricEfficieny(SharedPreferences prefs) {
		String veString = prefs.getString(ConfigActivity.VOLUMETRIC_EFFICIENCY_KEY, ".85");
		double ve = 0.85;
		try {
			ve = Double.parseDouble(veString);
		} catch (Exception e) {
		}
		return ve;
	}

	/**
	 * engine 发动机
	 * displacement 取代
	 * @param prefs
	 * @return
	 */
	public static double getEngineDisplacement(SharedPreferences prefs) {
		String edString = prefs.getString(
				ConfigActivity.ENGINE_DISPLACEMENT_KEY, "1.6");
		double ed = 1.6;
		try {
			ed = Double.parseDouble(edString);
		} catch (Exception e) {
		}
		return ed;
	}

	/**
	 * 得到OBD命令
	 * @param prefs
	 * @return
	 */
	public static ArrayList<ObdCommand> getObdCommands(SharedPreferences prefs) {
		ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
		ArrayList<ObdCommand> ucmds = new ArrayList<ObdCommand>();
		for (int i = 0; i < cmds.size(); i++) {
			ObdCommand cmd = cmds.get(i);
			boolean selected = prefs.getBoolean(cmd.getName(), true);
			if (selected) {
				ucmds.add(cmd);
			}
		}
		return ucmds;
	}

	/**
	 * 最大油耗
	 * @param prefs
	 * @return
	 */
	public static double getMaxFuelEconomy(SharedPreferences prefs) {
		String maxStr = prefs.getString(ConfigActivity.MAX_FUEL_ECON_KEY, "70");
		double max = 70;
		try {
			max = Double.parseDouble(maxStr);
		} catch (Exception e) {
		}
		return max;
	}

	/**
	 * 读取配置命令
	 * @param prefs
	 * @return
	 */
	public static String[] getReaderConfigCommands(SharedPreferences prefs) {
		String cmdsStr = prefs.getString(CONFIG_READER_KEY, "atsp0\natz");
		String[] cmds = cmdsStr.split("\n");
		return cmds;
	}

}