package com.gigaspaces.adminutils.common;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class AdminUtil {

	/*
	 * Converts a host name to ip address and validates if it is a valid ip
	 */
	public static String lookup(String s) throws Throwable {

		InetAddress thisComputer;
		byte[] address;

		// get the bytes of the IP address
		try {
			thisComputer = InetAddress.getByName(s);
			address = thisComputer.getAddress();
		} catch (UnknownHostException ue) {
			System.out.println("Cannot find host " + s);
			ue.printStackTrace();
			return null;
		}

		String ip = "";

		if (isHostname(s)) {
			// Print the IP address
			for (int i = 0; i < address.length; i++) {
				int unsignedByte = address[i] < 0 ? address[i] + 256
						: address[i];
				ip = ip + unsignedByte + ".";
			}
			ip = ip.substring(0, ip.length() - 1);
		} else { // this is an already an IP address
			ip = s;
		}

		// Check if the ip is valid
		try {
			InetAddress.getByName(s).toString();
		} catch (UnknownHostException e) {
			System.out.println("Could not find the address " + s);
			ip = null;
			Throwable ex = new Throwable("IP Cannot be found");
			throw ex;
		}

		return ip;
	}

	// Checks if it is a host name or ip
	public static boolean isHostname(String s) {

		char[] ca = s.toCharArray();
		// if we see a character that is neither a digit nor a period
		// then s is probably a hostname
		for (int i = 0; i < ca.length; i++) {
			if (!Character.isDigit(ca[i])) {
				if (ca[i] != '.') {
					return true;
				}
			}
		}

		// Everything was either a digit or a period
		// so s looks like an IP address in dotted quad format
		return false;

	}

}
