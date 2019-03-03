
package com.sdn.obd.commands.temperature;

import com.sdn.obd.enums.AvailableCommandNames;

/**
 * Engine Coolant Temperature.
 */
public class EngineCoolantTemperatureObdCommand extends TemperatureObdCommand {

	/**
	 * 
	 */
	public EngineCoolantTemperatureObdCommand() {
		super("01 05");
	}

	/**
	 * @param other
	 */
	public EngineCoolantTemperatureObdCommand(TemperatureObdCommand other) {
		super(other);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eu.lighthouselabs.MyCommand.commands.ObdCommand#getName()
	 */
	@Override
	public String getName() {
		return AvailableCommandNames.ENGINE_COOLANT_TEMP.getValue();
	}

}