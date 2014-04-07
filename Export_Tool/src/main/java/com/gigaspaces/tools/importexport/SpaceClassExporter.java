/**
 * Aug 1, 2013
 */
package com.gigaspaces.tools.importexport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.client.iterator.GSIteratorConfig;
import com.gigaspaces.client.iterator.IteratorScope;

import com.j_spaces.core.client.GSIterator;

import net.jini.core.entry.UnusableEntryException;

/**
 * @author jb
 *
 */
public class SpaceClassExporter {

	// default parameters
	private static String LOCALHOST = "localhost";
	private static String SPACE = "space";
	private static String SUFFIX = ".ser.gz";
	
	public static enum Parameters {
		
		FILE('f', "file", "the name of the file to which to export"),
		SPACE('s', "space", "the name of the space"),
		GROUP('g', "groups", "the names of lookup groups"),
		LOCATE('l', "locators", "the names of lookup services hosts"),
		CLASS('c', "classes", "the classes whose objects to export"),
		HELP('h', "help", "show this message"),
		TEST('t', "test", "performs a sanity check");
		
		private Parameters(Character flag, String label, String desc) {

			this.flag = flag;
			this.label = label;
			this.desc = desc;
		}
		private Character flag;
		private String label;
		private String desc;
		
		public Character getFlag() { return flag; }
		public String getLabel() { return label; }
		public String getDesc() { return desc; }
	}

	private String name;
	private File file;
	private Boolean export = true;
	private Boolean test = false;
	
	private List<String> locators;
	private List<String> groups;
	private List<String> classes;
	private GigaSpace gigaSpace;
	
	public SpaceClassExporter() {

		locators = new ArrayList<String>();
		groups = new ArrayList<String>();
		classes = new ArrayList<String>();
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the locators
	 */
	public List<String> getLocators() {
		return locators;
	}

	/**
	 * @param locators the locators to set
	 */
	public void setLocators(List<String> locators) {
		this.locators = locators;
	}

	/**
	 * @return the groups
	 */
	public List<String> getGroups() {
		return groups;
	}

	/**
	 * @param groups the groups to set
	 */
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	/**
	 * @return the classes
	 */
	public List<String> getClasses() {
		return classes;
	}

	/**
	 * @param classes the classes to set
	 */
	public void setClasses(List<String> classes) {
		this.classes = classes;
	}
	
	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	public boolean isTest() {
		return test;
	}
	
	public void setSpace(GigaSpace space) { 
		
		this.gigaSpace = space; 
	}
	
	public GigaSpace getSpace() {
		
		return gigaSpace;
	}
	
	
	public String getSpaceUrl() {

		String spaceUrl = "jini://*/*/" + name;
		
		if (locators != null && ! locators.isEmpty()) {
			spaceUrl = spaceUrl + "?locators=";
			for (int l = 0; l < locators.size(); l++) {
				spaceUrl = spaceUrl + (l > 0 ? "," : "") + locators.get(l);
			}
			return spaceUrl;
		}
		if (groups != null && ! groups.isEmpty()) {
			spaceUrl = spaceUrl + "?locators=";
			for (int g = 0; g < groups.size(); g++) {
				spaceUrl = spaceUrl + (g > 0 ? "," : "") + groups.get(g);
			}
			return spaceUrl;
		}

		return spaceUrl;
	}
	
	public void init(String[] args) {
		
		for (int a = 0; a < args.length; a++) {
			if (args[a].equals(getFlag(Parameters.FILE))) file = new File(args[++a]); export = false;
			if (args[a].equals(getFlag(Parameters.SPACE))) name= args[++a];
			if (args[a].equals(getFlag(Parameters.TEST))) test = true;
			if (args[a].equals(getFlag(Parameters.GROUP)))
				for (String group : args[++a].split(","))  groups.add(group);
			if (args[a].equals(getFlag(Parameters.CLASS)))
				for (String clazz : args[++a].split(","))  classes.add(clazz);
			if (args[a].equals(getFlag(Parameters.LOCATE)))
				for (String locator : args[++a].split(","))  locators.add(locator);
		}
		if (locators.isEmpty()) locators.add(LOCALHOST);
		if (name == null) name = SPACE;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SpaceClassExporter exporter = new SpaceClassExporter();
		
		exporter.init(args);
		// exporter.verify();
		try {
			UrlSpaceConfigurer urlConfig = new UrlSpaceConfigurer(exporter.getSpaceUrl());
			GigaSpaceConfigurer gigaConfig = new GigaSpaceConfigurer(urlConfig.lookupTimeout(20000).space());

			exporter.setSpace(gigaConfig.gigaSpace());
			
			if (exporter.getFile() != null && exporter.getFile().exists()) {
				exporter.readObjects();
				System.exit(0);
			}
			else {
				AsyncFuture<List<String>> result = exporter.getSpace().execute(new FindSpaceClassTask(exporter.getName())); 
				
				System.out.print("sleeping");
				while (! result.isDone()) {
					Thread.sleep(100L);
					System.out.print(".");
				}
				System.out.println("");
				
				if (exporter.getClasses() != null && !exporter.getClasses().isEmpty()) {
					List<String> remove = new ArrayList<String>();
					for (String r : result.get())
						if (! exporter.getClasses().contains(r)) remove.add(r);
					for (String r : remove) result.get().remove(r);
				}
				System.out.println("> " + result.get());
				exporter.setFile(new File(exporter.getSpace().getName() + SUFFIX));
				exporter.setClasses(result.get());
				exporter.writeObjects();
			}
			
			urlConfig.destroy();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void writeObjects() {
		
		writeObjects(gigaSpace, file, classes);
	}
	
	public void writeObjects(GigaSpace space, File file, List<String> classList) {

		GZIPOutputStream zos = null;
		ObjectOutputStream oos = null;
		try {
			System.out.println("writing to file : " + file.getAbsolutePath());
			zos = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			oos = new ObjectOutputStream(zos);
			for (String className : classList) {
				System.out.println("reading space class : " + className);
				oos.writeUTF(className);
				Object template = null;
				try {
					template = Class.forName(className).newInstance();
				} catch (ClassNotFoundException e) { e.printStackTrace();
				} catch (InstantiationException e) { e.printStackTrace();
				} catch (IllegalAccessException e) { e.printStackTrace();
				}
				if (template != null) {
					Integer count = space.count(template);
					oos.writeInt(count);
					System.out.println("space reports " + count + " objects");
					GSIteratorConfig config = new GSIteratorConfig();
					config.setBufferSize(100).setIteratorScope(IteratorScope.CURRENT);
					Collection<Object> templates = new LinkedList<Object>();
					templates.add(template);
					GSIterator iterator = null;
					try {
						iterator = new GSIterator(space.getSpace(), templates, config);
						System.out.println("read " + count + " objects from space");
//						oos.writeInt(count);
						while (iterator.hasNext()) 
							oos.writeObject(iterator.next());
					} catch (UnusableEntryException e) {
						e.printStackTrace();
					}
				}
				else {
					oos.writeInt(0);
				}
			}
			zos.finish();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

/*	
	public HashMap<Class<?>, List<Object>> readObjects(ObjectInputStream input) {
		
		return readObjects(input, null, false, true);
	}
*/	

	public void readObjects() {
		
		readObjects(file, gigaSpace);
	}

	public void readObjects(File file, GigaSpace space) {
		
		readObjects(file, space, false);
	}

	public void readObjects(File file, GigaSpace space, boolean direct) {

		try {
			GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
			ObjectInputStream input = new ObjectInputStream(zis);

			if (direct) {
				readObjects(input, space, direct, false);
			}
			else {
				HashMap<Class<?>, List<Object>> objects = readObjects(input, space, false, true);
				for (Class<?> key : objects.keySet()) {
					space.writeMultiple(objects.get(key).toArray());
				}
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<Class<?>, List<Object>> readObjects(ObjectInputStream input, GigaSpace space, boolean direct, boolean print) {

		HashMap<Class<?>, List<Object>> objects = null;
		
		if (! direct) objects = new HashMap<Class<?>, List<Object>>();
		
		String objectClass;
		try {
			objectClass = input.readUTF();
			Integer objectCount = null;
			while (objectClass != null) {
				Class<?> clazz = Class.forName(objectClass);
				objectCount = input.readInt();
				objects.put(clazz, new ArrayList<Object>(objectCount));
				for (int o = 0; o < objectCount; o++) {
					Object object = input.readObject();
					if (print) System.out.println(object.toString());
					if (! direct) objects.get(clazz).add(object);
					else if (space != null) space.write(object);
				}
				if (input.available() > 0) objectClass = input.readUTF();
				else objectClass = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return objects;
	}
	public static void showHelpMessage() {
		
		System.out.println("GigaSpace Test Platform");
		System.out.println("Parameters:");
		for (Parameters params : Parameters.values()) {
			String pstr = String.format("\t-%c %-9s - %s", params.getFlag(), params.getLabel(), params.getDesc());
			System.out.println(pstr);
		}
		System.exit(0);
	}
	
	public static String getFlag(Parameters value) {
		
		return "-" + value.getFlag();
	}
}
