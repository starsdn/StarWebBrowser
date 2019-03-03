/*
 * TODO put header 
 */
package com.sdn.obd.reader;

import com.sdn.obd.reader.io.ObdCommandJob;

/**
 * TODO put description
 */
public interface IPostListener {

	void stateUpdate(ObdCommandJob job);
	
}