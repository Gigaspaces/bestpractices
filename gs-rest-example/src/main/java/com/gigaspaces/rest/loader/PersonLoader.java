package com.gigaspaces.rest.loader;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.gigaspaces.rest.model.Person;

public class PersonLoader {

	private static int count = 10;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		UrlSpaceConfigurer urlSC = new UrlSpaceConfigurer("jini://*/*/person-space?groups=gs");

		GigaSpace gigaSpace = new GigaSpaceConfigurer(urlSC).gigaSpace();
		
		for (int i=0;i< count;i++) {
			Person p = new Person();
			p.setId("" + i);
			p.setFirstName("John " + i);
			p.setLastName("Doe");
			p.setAge(30);
	
			gigaSpace.write(p);
		}
	}

}
