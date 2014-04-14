package com.gigaspaces.tools.importexport.serial;

import java.io.Serializable;
import java.util.ArrayList;

public class SerialList extends ArrayList<String> implements Serializable {
	

	private static final long serialVersionUID = -6812619647418772605L;
	/**
	 * 
	 */

	public SerialList() {
		
	}

	@Override
	public boolean add(String e) {

		return super.add(e);
	}
	
}
