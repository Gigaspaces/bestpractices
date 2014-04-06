/**
 * Aug 1, 2013
 */
package com.gigaspaces.support.export;

import java.util.ArrayList;
import java.util.List;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceInstanceRuntimeDetails;
import org.openspaces.admin.space.Spaces;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.TaskGigaSpace;

import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.logger.Constants;

import net.jini.core.discovery.LookupLocator;

/**
 * @author jb
 *
 */
public class FindSpaceClassTask implements DistributedTask<String, List<String>> {

	private static final long serialVersionUID = 5257838144063003892L;
	/**
	 * 
	 */
	public static String OBJECT = "java.lang.Object";
	
	private final static java.util.logging.Logger blogger = java.util.logging.Logger.getLogger(Constants.LOGGER_COMMON);
	
	
	private String name;
	private AdminFactory factory;
	// we don't really use this other than to get the groups and locators
	@TaskGigaSpace
	private transient GigaSpace space;
	
	public FindSpaceClassTask() { 

	}

		
	public FindSpaceClassTask(String name) {

		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.openspaces.core.executor.Task#execute()
	 */
	@Override
	public String execute() throws Exception {

		if (space == null) return "space is null!";
		this.factory = new AdminFactory();
		String result = null;
		try {
			List<String> names = new ArrayList<String>();
			LookupLocator[] locators = space.getSpace().getURL().getLookupLocators();
			if (locators != null) {
				for (LookupLocator locator : locators) {
					factory.addLocator(locator.getHost() + ":" + locator.getPort());
					blogger.info("locator:" + locator.getHost() + ":" + locator.getPort());
				}
			}
			for (String group : space.getSpace().getURL().getLookupGroups()) {
				factory.addGroup(group);
				blogger.info("group:" + group);
			}
			
			System.out.println(space.getSpace().getFinderURL().toString());
			// if this is an integrated processing unit they are invisible until we do this
			Admin admin = factory.create();
			if (admin != null) { 
				Spaces spaces = admin.getSpaces();
				int passes = 0;
				boolean discovered = false;
				while (spaces.getSpaces().length == 0 && passes < 15 && ! discovered) {
					Thread.sleep(1000L);
					passes++;
					if (spaces.getSpaces().length == 0 && passes ==15) {
						factory.discoverUnmanagedSpaces();
						spaces = admin.getSpaces();
						passes = 0;
						discovered = true;
					}
				}
				if (name != null) {
					names.addAll(getSpaceClasses(spaces.getSpaceByName(name)));
				}
				else {
					for (Space s : spaces) {
						names.addAll(getSpaceClasses(s));
						blogger.info("space:" + s.getName());
					}
				}
				result = names.toString();
			}
			else {
				result = "admin is null!";
			}
		}
		catch (Exception e) {
			result = e.getStackTrace()[0].toString();
		}
		return result.toString();
	}

	/* (non-Javadoc)
	 * @see org.openspaces.core.executor.Task#execute()
	 */
	public String executeX() throws Exception {

		if (space == null) return "space is null!";
		this.factory = new AdminFactory();
		String result = null;
		try {
			List<String> names = new ArrayList<String>();
			LookupLocator[] locators = space.getSpace().getURL().getLookupLocators();
			if (locators != null) {
				for (LookupLocator locator : locators) {
					factory.addLocator(locator.getHost() + ":" + locator.getPort());
					blogger.info("locator:" + locator.getHost() + ":" + locator.getPort());
				}
			}
			for (String group : space.getSpace().getURL().getLookupGroups()) {
				factory.addGroup(group);
				blogger.info("group:" + group);
			}
			
			System.out.println(space.getSpace().getFinderURL().toString());
			// if this is an integrated processing unit they are invisible until we do this
			Admin admin = factory.create();
			if (admin != null) { 
				Spaces spaces = admin.getSpaces();
				int passes = 0;
				boolean discovered = false;
				while (spaces.getSpaces().length == 0 && passes < 15 && ! discovered) {
					Thread.sleep(1000L);
					passes++;
					if (spaces.getSpaces().length == 0 && passes ==15) {
						factory.discoverUnmanagedSpaces();
						spaces = admin.getSpaces();
						passes = 0;
						discovered = true;
					}
				}
				if (name != null) {
					names.addAll(getSpaceClasses(spaces.getSpaceByName(name)));
				}
				else {
					for (Space s : spaces) {
						names.addAll(getSpaceClasses(s));
						blogger.info("space:" + s.getName());
					}
				}
				result = names.toString();
			}
			else {
				result = "admin is null!";
			}
		}
		catch (Exception e) {
			result = e.getStackTrace()[0].toString();
		}
		return result.toString();
	}
	
	
	/* (non-Javadoc)
	 * @see com.gigaspaces.async.AsyncResultsReducer#reduce(java.util.List)
	 */
	@Override
	public List<String> reduce(List<AsyncResult<String>> args) throws Exception {
		
		List<String> result = new ArrayList<String>();
		for (AsyncResult<String> arg : args) {
			if (arg.getException() == null) {
				String[] classes = arg.getResult().split(",");
				for (String c : classes) {
					String cn = c.trim().replaceAll("\\[", "").replaceAll("\\]", "");
					if (! result.contains(cn) && ! cn.equals(OBJECT))
						result.add(cn);
				}
			}
			else {
				result.add("exception:" + arg.getException().getMessage());
			}
		}
		
		return result;
	}
	
	public static List<String> getSpaceClasses(Space space) {

		List<String> names = new ArrayList<String>();
		if (space == null) return names;
		SpaceInstance[] instances = space.getInstances();
		while (instances.length == 0) {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) { e.printStackTrace();
			}
			instances = space.getInstances();
		}
		for (SpaceInstance instance : instances) {
			SpaceInstanceRuntimeDetails details = instance.getRuntimeDetails();
			while (details == null) {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) { e.printStackTrace();
				}
				details = instance.getRuntimeDetails();
			}
			if (details != null) {
				for (String n : details.getClassNames()) {
					if (! names.contains(n)) names.add(n);
				}
			}
		}
		return names;
	}

	public static List<String> getLocalSpaceClasses(Space space) {

		List<String> names = new ArrayList<String>();
		if (space == null) return names;
		SpaceInstance[] instances = space.getInstances();
		while (instances.length == 0) {
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) { e.printStackTrace();
			}
			instances = space.getInstances();
		}
		for (SpaceInstance instance : instances) {
			SpaceInstanceRuntimeDetails details = instance.getRuntimeDetails();
			while (details == null) {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) { e.printStackTrace();
				}
				details = instance.getRuntimeDetails();
			}
			if (details != null) {
				for (String n : details.getClassNames()) {
					if (! names.contains(n)) names.add(n);
				}
			}
		}
		return names;
	}

}


