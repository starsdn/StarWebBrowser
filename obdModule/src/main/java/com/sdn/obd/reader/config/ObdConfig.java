/*
 * TODO put header
 */
package com.sdn.obd.reader.config;

import java.util.ArrayList;

import com.sdn.obd.commands.ObdCommand;
import com.sdn.obd.commands.SpeedObdCommand;
import com.sdn.obd.commands.control.CommandEquivRatioObdCommand;
import com.sdn.obd.commands.control.DtcNumberObdCommand;
import com.sdn.obd.commands.control.TimingAdvanceObdCommand;
import com.sdn.obd.commands.control.TroubleCodesObdCommand;
import com.sdn.obd.commands.engine.EngineLoadObdCommand;
import com.sdn.obd.commands.engine.EngineRPMObdCommand;
import com.sdn.obd.commands.engine.EngineRuntimeObdCommand;
import com.sdn.obd.commands.engine.MassAirFlowObdCommand;
import com.sdn.obd.commands.engine.ThrottlePositionObdCommand;
import com.sdn.obd.commands.fuel.FindFuelTypeObdCommand;
import com.sdn.obd.commands.fuel.FuelLevelObdCommand;
import com.sdn.obd.commands.fuel.FuelTrimObdCommand;
import com.sdn.obd.commands.pressure.BarometricPressureObdCommand;
import com.sdn.obd.commands.pressure.FuelPressureObdCommand;
import com.sdn.obd.commands.pressure.IntakeManifoldPressureObdCommand;
import com.sdn.obd.commands.protocol.ObdResetCommand;
import com.sdn.obd.commands.temperature.AirIntakeTemperatureObdCommand;
import com.sdn.obd.commands.temperature.AmbientAirTemperatureObdCommand;
import com.sdn.obd.commands.temperature.EngineCoolantTemperatureObdCommand;
import com.sdn.obd.enums.FuelTrim;

/**
 * TODO put description
 */
public final class ObdConfig {

	public static ArrayList<ObdCommand> getCommands() {
		ArrayList<ObdCommand> cmds = new ArrayList<ObdCommand>();
		// Protocol
		cmds.add(new ObdResetCommand());

		// Control
		cmds.add(new CommandEquivRatioObdCommand());
		cmds.add(new DtcNumberObdCommand());
		cmds.add(new TimingAdvanceObdCommand());
		cmds.add(new TroubleCodesObdCommand(0));

		// Engine
		cmds.add(new EngineLoadObdCommand());
		cmds.add(new EngineRPMObdCommand());
		cmds.add(new EngineRuntimeObdCommand());
		cmds.add(new MassAirFlowObdCommand());

		// Fuel
		// cmds.add(new AverageFuelEconomyObdCommand());
		// cmds.add(new FuelEconomyObdCommand());
		// cmds.add(new FuelEconomyMAPObdCommand());
		// cmds.add(new FuelEconomyCommandedMAPObdCommand());
		cmds.add(new FindFuelTypeObdCommand());
		cmds.add(new FuelLevelObdCommand());
		cmds.add(new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_1));
		cmds.add(new FuelTrimObdCommand(FuelTrim.LONG_TERM_BANK_2));
		cmds.add(new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_1));
		cmds.add(new FuelTrimObdCommand(FuelTrim.SHORT_TERM_BANK_2));

		// Pressure
		cmds.add(new BarometricPressureObdCommand());
		cmds.add(new FuelPressureObdCommand());
		cmds.add(new IntakeManifoldPressureObdCommand());

		// Temperature
		cmds.add(new AirIntakeTemperatureObdCommand());
		cmds.add(new AmbientAirTemperatureObdCommand());
		cmds.add(new EngineCoolantTemperatureObdCommand());

		// Misc
		cmds.add(new SpeedObdCommand());
		cmds.add(new ThrottlePositionObdCommand());

		return cmds;
	}

}