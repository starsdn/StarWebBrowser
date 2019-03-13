package com.star.starwebbrowser.obd;

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
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;
import com.star.starwebbrowser.R;
import com.sdn.obd.commands.ObdCommand;
import com.sdn.obd.reader.config.ObdConfig;
/**
 * 配置 activity.
 */
public class ConfigActivity extends PreferenceActivity implements
        OnPreferenceChangeListener {

    public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
    public static final String UPLOAD_URL_KEY = "upload_url_preference";
    public static final String UPLOAD_DATA_KEY = "upload_data_preference";
    public static final String UPDATE_PERIOD_KEY = "update_period_preference";
    public static final String VEHICLE_ID_KEY = "vehicle_id_preference";
    public static final String ENGINE_DISPLACEMENT_KEY = "engine_displacement_preference";
    public static final String VOLUMETRIC_EFFICIENCY_KEY = "volumetric_efficiency_preference";
    public static final String IMPERIAL_UNITS_KEY = "imperial_units_preference";
    public static final String COMMANDS_SCREEN_KEY = "obd_commands_screen";
    public static final String ENABLE_GPS_KEY = "enable_gps_preference";
    public static final String MAX_FUEL_ECON_KEY = "max_fuel_econ_preference";
    public static final String CONFIG_READER_KEY = "reader_config_preference";

    @SuppressWarnings("obd配置页面")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * 读取首选项资源 res/xml/preferences.xml
         */
        addPreferencesFromResource(R.xml.preferences);

        ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<CharSequence>();
        ArrayList<CharSequence> vals = new ArrayList<CharSequence>();
        //得到手机蓝牙设备列表
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
         * 蓝牙适配器连接OBD蓝牙
         */
        //获取手机当前所有蓝牙适配器
        final BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            listBtDevices.setEntries(pairedDeviceStrings
                    .toArray(new CharSequence[0]));
            listBtDevices.setEntryValues(vals.toArray(new CharSequence[0]));

            Toast.makeText(this, "没有找到蓝牙设备.",
                    Toast.LENGTH_LONG);

            return;
        }

        /*
         * Listen for preferences click.
         *
         */
        final Activity thisActivity = this;
        listBtDevices.setEntries(new CharSequence[1]);
        listBtDevices.setEntryValues(new CharSequence[1]);
        listBtDevices
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
                            Toast.makeText(
                                    thisActivity,
                                    "该设备不支持蓝牙或者蓝牙不可用.",
                                    Toast.LENGTH_LONG);
                            return false;
                        }
                        return true;
                    }
                });

        /*
         * 得到蓝牙设备
         */
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //列表显示名称
                pairedDeviceStrings.add(device.getName() + "\n"
                        + device.getAddress());
                //列表显示值
                vals.add(device.getAddress());
            }
        }
        listBtDevices.setEntries(pairedDeviceStrings
                .toArray(new CharSequence[0]));
        //把蓝牙设备存放到 ListPreference
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
     *
     * @param prefs
     * @return
     */
    public static int getUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs.getString(ConfigActivity.UPDATE_PERIOD_KEY,
                "4"); // 4 as in seconds
        int period = 4000; // by default 4000ms

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
     *
     * @param prefs
     * @return
     */
    public static double getVolumetricEfficieny(SharedPreferences prefs) {
        String veString = prefs.getString(
                ConfigActivity.VOLUMETRIC_EFFICIENCY_KEY, ".85");
        double ve = 0.85;
        try {
            ve = Double.parseDouble(veString);
        } catch (Exception e) {
        }
        return ve;
    }

    /**
     *
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
     *
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
     *
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
     *
     * @param prefs
     * @return
     */
    public static String[] getReaderConfigCommands(SharedPreferences prefs) {
        String cmdsStr = prefs.getString(CONFIG_READER_KEY, "atsp0\natz");
        String[] cmds = cmdsStr.split("\n");
        return cmds;
    }

}
