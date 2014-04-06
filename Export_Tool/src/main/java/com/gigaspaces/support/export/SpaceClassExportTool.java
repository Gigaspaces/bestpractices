/**
 * Aug 1, 2013
 */
package com.gigaspaces.support.export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.gigaspaces.async.AsyncFuture;

/**
 * @author jb
 *
 */
public class SpaceClassExportTool {

	// default parameters
	private static String LOCALHOST = "localhost";
	private static String SPACE = "space";
	private static String SEPARATE = ",";
	
	private static Logger logger = Logger.getLogger("com.gigaspaces.common");
	
	public static enum Parameters {
		
		EXPORT('e', "export", "performs space class export"),
        IMPORT('i', "import", "performs space class import"),
		GROUP('g', "groups", "the names of lookup groups - comma separated"),
		LOCATE('l', "locators", "the names of lookup services hosts - comma separated"),
		SPACE('s', "space", "the name of the space"),
		CLASS('c', "classes", "the classes whose objects to import/export - comma separated"),
		BATCH('b', "batch", "the batch size - default is 1000"),
		PART('p', "partitions", "the partition(s) to restore - comma separated"),
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
		public String getFlagParam() { return "-" + flag; }
		public String getLabel() { return label; }
		public String getLabelParam() { return "--" + label; }
		public String getDesc() { return desc; }
	}

	private String name;
	private File file;
	private Boolean export = false;
	private Boolean imp = true;
	private Boolean test = false;
	private Integer batch = 1000;
	
	private List<String> locators;
	private List<String> groups;
	private List<String> classes;
	private List<Integer> partitions;
	private GigaSpace space;
	
	public SpaceClassExportTool() {

		locators = new ArrayList<String>();
		groups = new ArrayList<String>();
		classes = new ArrayList<String>();
		partitions = new ArrayList<Integer>();
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
			if (args[a].equals(Parameters.EXPORT.getFlagParam()) || 
					args[a].equals(Parameters.EXPORT.getLabelParam())) 
				export = true; 
			if (args[a].equals(Parameters.IMPORT.getFlagParam()) || 
					args[a].equals(Parameters.IMPORT.getLabelParam())) 
				imp = true; 
			if (args[a].equals(Parameters.SPACE.getFlagParam()) || 
					args[a].equals(Parameters.SPACE.getLabelParam())) 
				name= args[++a];
			if (args[a].equals(Parameters.TEST.getFlagParam()) || 
					args[a].equals(Parameters.TEST.getLabelParam())) 
				test = true;
			if (args[a].equals(Parameters.BATCH.getFlagParam()) || 
					args[a].equals(Parameters.BATCH.getLabelParam())) 
				batch = Integer.valueOf(args[++a]);
			if (args[a].equals(Parameters.GROUP.getFlagParam()) || 
					args[a].equals(Parameters.GROUP.getLabelParam()))
				for (String group : args[++a].split(SEPARATE))  groups.add(group);
			if (args[a].equals(Parameters.CLASS.getFlagParam()) ||
					args[a].equals(Parameters.CLASS.getLabelParam()))
				for (String clazz : args[++a].split(SEPARATE))  classes.add(clazz);
			if (args[a].equals(Parameters.LOCATE.getFlagParam()) ||
					args[a].equals(Parameters.LOCATE.getLabelParam()))
				for (String locator : args[++a].split(SEPARATE))  locators.add(locator);
			if (args[a].equals(Parameters.PART.getFlagParam()) ||
					args[a].equals(Parameters.PART.getLabelParam()))
				for (String partition : args[++a].split(SEPARATE))  partitions.add(Integer.valueOf(partition));
		}
		if (locators.isEmpty()) locators.add(LOCALHOST);
		if (name == null) name = SPACE;
	}
	
	public void verify() {
		
		if (! export && ! imp) {
			System.out.println("operation required: specify export or import");
			showHelpMessage();
		}
		// perform other steps as neccessary
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SpaceClassExportTool exporter = new SpaceClassExportTool();

		exporter.init(args);
		exporter.verify();
		try {
			UrlSpaceConfigurer urlConfig = new UrlSpaceConfigurer(exporter.getSpaceUrl());
			GigaSpaceConfigurer gigaConfig = new GigaSpaceConfigurer(urlConfig.lookupTimeout(20000).space());

			exporter.setSpace(gigaConfig.gigaSpace());

			SpaceClassExportTask task = new SpaceClassExportTask(exporter);

			AsyncFuture<List<String>> results = null;
			if (exporter.getPartitions().isEmpty()) {
				results = exporter.getSpace().execute(task); 
			}
			else {
				Object[] spacePartitions = (Object[]) exporter.getPartitions().toArray(new Integer[0]);
				results = exporter.getSpace().execute(task, spacePartitions); 
			}
			System.out.print("executing tasks");
			while (! results.isDone()) {
				Thread.sleep(1000L);
				System.out.print(".");
			}
			System.out.println("");
			
			// report the results here
			for (String result : results.get()) {
//				logger.info((exporter.getExport() ? "exporter " : " importer ") + result);
				logger.info(result);
			}
			
			urlConfig.destroy();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

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
	
	public Boolean getExport() {
		return export;
	}

	public void setExport(Boolean export) {
		this.export = export;
	}

	public void setSpace(GigaSpace space) { 
		
		this.space = space; 
	}
	
	public GigaSpace getSpace() {
		
		return space;
	}

	public List<Integer> getPartitions() {
		return partitions;
	}

	public void setPartitions(List<Integer> partitions) {
		this.partitions = partitions;
	}


	public Integer getBatch() {
		return batch;
	}


	public void setBatch(Integer batch) {
		this.batch = batch;
	}
	
	
}
