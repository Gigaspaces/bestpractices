package com.gigaspaces.bestpractices.bitpage.service;

public interface BitPageService {
	/**
	 * Sets the corresponding bit for the supplied integer
	 * @param val the integer bit to be set
	 * @return "true" whether the bit was previously set or not
	 */
	Boolean set(Integer val);
	
	/**
	 * Tests whether a an integer's bit is set
	 * @param val the integer to test 
	 * @return indicates whether the bit is set
	 */
	Boolean exists(Integer val);
	
	/**
	 * Unconditionally unsets (sets to 0) the bit corresponding
	 * to the supplied integer 
	 * @param val the integer to clear
	 */
	void clear(Integer val);
}
