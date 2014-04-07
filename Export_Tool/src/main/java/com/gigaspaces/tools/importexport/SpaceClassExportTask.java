package com.gigaspaces.tools.importexport;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.jini.core.entry.UnusableEntryException;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.TaskGigaSpace;

import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.client.iterator.GSIteratorConfig;
import com.gigaspaces.client.iterator.IteratorScope;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.logger.Constants;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.index.SpaceIndex;
import com.gigaspaces.metadata.index.SpaceIndexFactory;
import com.gigaspaces.metadata.index.SpaceIndexType;
import com.gigaspaces.tools.importexport.serial.SerialAudit;
import com.gigaspaces.tools.importexport.serial.SerialList;
import com.gigaspaces.tools.importexport.serial.SerialMap;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceRuntimeInfo;
import com.j_spaces.core.client.GSIterator;

public class SpaceClassExportTask implements DistributedTask<SerialList, List<String>>, ClusterInfoAware {
	
	private static final long serialVersionUID = 5257838144063003892L;
	/**
	 * 
	 */
	public static String OBJECT = "java.lang.Object";
	public static String DOCUMENT = SpaceDocument.class.getName();
	private static String SUFFIX = ".ser.gz";
	
	private static String SPACEID = "@SpaceId";
	private static String ROUTING = "@SpaceRouting";
	private static String INDEX = "@SpaceIndex";
	
	private static String COLON = ":";
	private static String DOT = ".";
		
	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Constants.LOGGER_COMMON);
	
	private List<String> classNames;
	private Boolean export;
	private Integer batch;
	private SerialAudit audit;
	
	// we don't really use this other than to get the groups and locators
	@TaskGigaSpace
	private transient GigaSpace gigaSpace;

	// injection doesn't work
    private ClusterInfo clusterInfo;		

    public enum Type {
    	
    	DOC("document"),
    	CLASS("class");
    	
    	private String value;
    	private Type(String value) { this.value = value; }
    	public String getValue() { return value; }
    }
    
	public SpaceClassExportTask() {
		
		classNames = new ArrayList<String>();
		audit = new SerialAudit();
	}
	
	public SpaceClassExportTask(Boolean export) {

		this();
		this.export = export;
	}

	public SpaceClassExportTask(SpaceDataImportExportMain exporter) {

		this(exporter.getClasses(), exporter.getExport(), exporter.getBatch()); 
	}
	

	public SpaceClassExportTask(List<String> className, Boolean export, Integer batch) {

		this(export);
		this.classNames.addAll(classNames);
		this.batch = batch;
	}

	/* (non-Javadoc)
	 * @see org.openspaces.core.executor.Task#execute()
	 */
	@Override
	public SerialList execute() throws Exception {

		// writes the partition id with the thread id in the logs
//		audit.setPartition(clusterInfo.getInstanceId());
		
		if (export) {
			// get a list of classes and the number of entries of each class
			IRemoteJSpaceAdmin remoteAdmin = (IRemoteJSpaceAdmin) gigaSpace.getSpace().getAdmin();
			if (! classNames.isEmpty()) {
				for (String className : classNames) {
					SpaceRuntimeInfo runtimeInfo = remoteAdmin.getRuntimeInfo(className);
					if (runtimeInfo != null) {
						if (logger.isLoggable(Level.FINE)) {
							List<?> numOfEntries = runtimeInfo.m_NumOFEntries;
							for (int c = 0; c < runtimeInfo.m_ClassNames.size(); c++) 
								logger.fine(runtimeInfo.m_ClassNames.get(c) + " has " + numOfEntries.get(c).toString() + " objects"); 
						}
					}
					else {
						classNames.remove(className);
						logger.warning("space class export task - class: " + className + " was not found!");
					}
				}
				logger.info("confirmed " + classNames.size() + " classes");
				audit.add("confirmed " + classNames.size() + " classes");
			}
			else {
				Object classList[] = remoteAdmin.getRuntimeInfo().m_ClassNames.toArray();
				if (logger.isLoggable(Level.FINE)) {
					List<?> numOfEntries = remoteAdmin.getRuntimeInfo().m_NumOFEntries;
					for (int c = 0; c < classList.length; c++) 
						logger.fine(classList[c] + " has " + numOfEntries.get(c).toString() + " objects"); 
				}
				for (Object clazz : classList) {
					logger.fine(clazz.toString());
					if (! clazz.toString().equals(OBJECT)) classNames.add(clazz.toString());
				}
				logger.info("found " + classNames.size() + " classes");
				audit.add("found " + classNames.size() + " classes");
			}
			
			if (classNames.size() > 0)
				writeObjects(classNames);
		}
		else {
			File[] files = new File(DOT).listFiles(new ImportClassFileFilter(clusterInfo.getInstanceId()));
			List<String> fileNames = new ArrayList<String>();
			for (File file : files) {
				if (! classNames.isEmpty()) {
					// remove elements from the file list
					if (classNames.contains(getClassNameFromImportFile(file))) 
						fileNames.add(file.toString());
				}
				else fileNames.add(file.toString());
			}

			logger.info("importer found " + fileNames.size() + " files");
			audit.add("importer found " + fileNames.size() + " files");
			if (fileNames.size() > 0)
				readObjects(fileNames);
		}
		return audit;
	}
	
	private String getClassNameFromImportFile(File file) {
		
		StringBuffer buffer = new StringBuffer();
		String[] parts = file.getName().split("\\.");
		// class.name.#.ser.gz - we don't care about the last three
		for (int f = 0; f < parts.length - 3; f++) {
			buffer.append((buffer.length() > 0 ? "." : "") + parts[f]);
		}
		return buffer.toString();
	}
	
	/* (non-Javadoc)
	 * @see com.gigaspaces. {
	 * async.AsyncResultsReducer#reduce(java.util.List)
	 */
	@Override
	public List<String> reduce(List<AsyncResult<SerialList>> args) throws Exception {
		
		List<String> result = new ArrayList<String>();
		for (AsyncResult<SerialList> arg : args) {
			if (arg.getException() == null) {
				if (arg.getResult() != null) {
					for (String str : arg.getResult()) 
						result.add(str);
				}
			}
			else {
				result.add("exception:" + arg.getException().getMessage());
				arg.getException().printStackTrace();
			}
		}
		
		return result;
	}

	private void writeObjects(List<String> classList) {
		
		writeObjects(gigaSpace, clusterInfo, classList);
	}

	private void writeObjects(GigaSpace space, ClusterInfo clusterInfo, List<String> classList) {
		
		List<SpaceClassExportThread> threadList = new ArrayList<SpaceClassExportThread>();
		Integer partitionId = clusterInfo.getInstanceId();
		for (String className : classList) {
			File file = new File(className + DOT + partitionId + SUFFIX);
			SpaceClassExportThread operation = new SpaceClassExportThread(space, file, className, batch, partitionId);
			logger.info("starting export thread for " + className);
			audit.add("starting export thread for " + className);
			threadList.add(operation);
			operation.start();
		}

		boolean terminated = false;
		if (threadList.size() > 0)
			logger.info("waiting for " + classList.size() + " export operations to complete");
		while (! terminated) {
			boolean running = false;
			for (SpaceClassExportThread thread : threadList)
				running |= ! thread.getState().equals(Thread.State.TERMINATED);
			if (! running) terminated = true;
			else {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					logger.warning("exception during thread sleep - " + e.getMessage());
				}
			}
		}
		for (SpaceClassExportThread thread : threadList) {
			for (String line : thread.getMessage())
				((List<String>) audit).add(line);
		}
		
		audit.add("finished writing " + classList.size() + " classes");
		logger.info("finished writing " + classList.size() + " classes");
		
	}

	private void readObjects(List<String> classList) {
		
		readObjects(gigaSpace, clusterInfo, classList);
	}
	
	private void readObjects(GigaSpace space, ClusterInfo clusterInfo, List<String> classList) {
		
		List<SpaceClassImportThread> threadList = new ArrayList<SpaceClassImportThread>();

		Integer partitionId = clusterInfo.getInstanceId();
		for (String className : classList) {
			// we're being passed a file instead of a class name
			File file = new File(className);
			logger.info("importing class " + getClassNameFromImportFile(file) + " into partition " + partitionId);
			audit.add("importing class " + getClassNameFromImportFile(file) + " into partition " + partitionId);
			SpaceClassImportThread operation = new SpaceClassImportThread(space, file, 1000);
			threadList.add(operation);
			operation.start();
		}
		boolean terminated = false;
		logger.info("waiting for " + classList.size() + " import operations to complete");
		while (! terminated) {
			boolean running = false;
			for (Thread thread : threadList) {
				running |= ! thread.getState().equals(Thread.State.TERMINATED);
			}
			if (! running) terminated = true;
			else {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		for (SpaceClassImportThread thread : threadList) {
			for (String line : thread.getMessage()) {
				audit.add(line, false);
			}
		}
		logger.info("finished reading " + classList.size() + " files");
		audit.add("finished reading " + classList.size() + " files");
	}

	public class SpaceClassExportThread extends Thread {

		private GigaSpace space;
		private File file;
		private String className;
		private Integer batch = 1000;
		private Integer partitionId;
		private SerialAudit lines;

		public SpaceClassExportThread(GigaSpace space, File file, String className, Integer batch, Integer partitionId) {
			
			this.space = space;
			this.file = file;
			this.className = className;
			this.batch = batch;
			this.partitionId = partitionId;
			this.lines = new SerialAudit();
		}
		
		public SerialAudit getMessage() {
			
			return lines;
		}
		
		private Object getClassTemplate(String className) {

			Object template = null;
			
			try {
				template = Class.forName(className).newInstance();
				logger.fine("returning " + template.getClass().getSimpleName() + " (SpaceDocument)" );
			} catch (ClassNotFoundException cnfe) { 
				SpaceTypeDescriptor descriptor = space.getTypeManager().getTypeDescriptor(className);
				// right way to check for space document
				if (! descriptor.isConcreteType()) {
					template = (Object) new SpaceDocument(className);
					logger.fine("returning SpaceDocument");
				}
				else
					logger.warning(cnfe.getMessage());
			} catch (InstantiationException ie) { logger.warning(ie.getMessage());
			} catch (IllegalAccessException iae) { logger.warning(iae.getMessage());
			}

			return template;
		}
		
		private SerialMap getTypeDescriptorMap(String className) {
			
			SerialMap documentMap = new SerialMap();
			
			SpaceTypeDescriptor type = space.getTypeManager().getTypeDescriptor(className);

			if (type.getIdPropertyName() != null) 
				documentMap.put(SPACEID, type.getIdPropertyName());
			if (type.getRoutingPropertyName() != null)
				documentMap.put(ROUTING, type.getRoutingPropertyName());
			
			SerialMap indexMap = new SerialMap();
			Map<String, SpaceIndex> indexes = type.getIndexes();
			for (String key : indexes.keySet()) {

				// despite the fact that the importer won't create indexes
				// on the routing or spaceid we're not going to create the index
				if (type.getIdPropertyName() != null && type.getIdPropertyName().equals(key))
					continue;
				if (type.getRoutingPropertyName() != null && type.getRoutingPropertyName().equals(key))
					continue;
				
				// @SpaceIndex
				if (! indexMap.containsKey(indexes.get(key).getIndexType().name()))
					indexMap.put(indexes.get(key).getIndexType().name(), new SerialList());

				((SerialList) indexMap.get(indexes.get(key).getIndexType().name())).add(key);
			}
			// always write this out, even if it's empty
			documentMap.put(INDEX, indexMap);

			return documentMap;
		}
		
		@Override
		public void run() {

			GZIPOutputStream zos = null;
			ObjectOutputStream oos = null;
			try {
				Object template = getClassTemplate(className);
				if (template != null) {
					String type = (SpaceDocument.class.isInstance(template) ? Type.DOC.getValue() : Type.CLASS.getValue());
					logger.info("reading space " + type + " : " + className);
					lines.add("reading space " + type + " : " + className);
					Integer count = space.count(template);

					if (count > 0) {
						logger.info("space partition contains " + count + " objects");
						lines.add("space partition contains " + count + " objects");
						
						// create the output file stream
						zos = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
						oos = new ObjectOutputStream(zos);

						logger.info("writing to file : " + file.getAbsolutePath());
						lines.add("writing to file : " + file.getAbsolutePath());
						
						// write some header data
						oos.writeUTF(SpaceDocument.class.isInstance(template) ? DOCUMENT : className);
						oos.writeInt(count);
						// space document needs to write type descriptor
						if (Type.DOC.getValue().equals(type))
							oos.writeUTF(className);
						
						// we could serialize *all* type descriptors
						oos.writeObject(getTypeDescriptorMap(className));

						// get the objects and write them out
						GSIteratorConfig config = new GSIteratorConfig();
						config.setBufferSize(batch).setIteratorScope(IteratorScope.CURRENT);
						Collection<Object> templates = new LinkedList<Object>();
						templates.add(template);
						GSIterator iterator = null;

						try {
							iterator = new GSIterator(space.getSpace(), templates, config);

							logger.info("read " + count + " objects from space partition");
							lines.add("read " + count + " objects from space partition");

							Long start = System.currentTimeMillis();
							while (iterator.hasNext()) oos.writeObject(iterator.next());
							Long duration = (System.currentTimeMillis() - start);

							logger.info("export operation took " + duration + " millis");
							lines.add("export operation took " + duration + " millis");
						} catch (UnusableEntryException e) {
							e.printStackTrace();
						}
						// close the output file
						zos.finish();
						oos.close();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public class ImportClassFileFilter implements FilenameFilter {

		private Integer partitionId;
		
		public ImportClassFileFilter(Integer partitionId) { 
			
			this.partitionId = partitionId;
		}
		
		@Override
		public boolean accept(File dir, String name) {

			// currently there are no checks for directory - it should be automatic
			if (name.endsWith(partitionId + SUFFIX)) return true;
			return false;
		}
	}
	
	public class SpaceClassImportThread extends Thread {

		private GigaSpace space;
		private File file;
		private String className;
		private Integer batch = 1000;
		private SerialAudit lines;
		
		public SpaceClassImportThread(GigaSpace space, File file, Integer batch) {
			
			this.space = space;
			this.file = file;
			this.batch = batch;
			this.lines = new SerialAudit();
		}

		public SerialAudit getMessage() {
			
			return lines;
		}
		
		private void registerTypeDescriptor(String typeName, SerialMap typeMap) {

			SpaceTypeDescriptorBuilder typeBuilder = new SpaceTypeDescriptorBuilder(typeName);

			// is this document already registered?
			if (space.getTypeManager().getTypeDescriptor(typeName) != null) {
				logger.info("found type descriptor for " + typeName);
				lines.add("found type descriptor for " + typeName);
				return;
			}
			// create it if necessary
			logger.info("creating type descriptor for " + typeName);
			lines.add("creating type descriptor for " + typeName);

			// deal with spaceId, spaceRouting, indexes separately
			if (typeMap.keySet().contains(SPACEID)) {
				logger.fine("creating id property " + typeMap.get(SPACEID) + " for type " + typeName);
				typeBuilder.idProperty((String) typeMap.get(SPACEID));
			}
			if (typeMap.keySet().contains(ROUTING)) {
				logger.fine("creating routing property " + typeMap.get(ROUTING) + " for type " + typeName);
				typeBuilder.routingProperty((String) typeMap.get(ROUTING));
			}
			// space id is indexed, so it will show up here, too. just log it and continue
			for (String propertyName : typeMap.keySet()) {
				if (INDEX.equals(propertyName)) {
					SerialMap indexMap = (SerialMap) typeMap.get(propertyName);
					for (String indexType : indexMap.keySet()) {
						SpaceIndexType type = SpaceIndexType.valueOf(indexType);
						SerialList indexes = (SerialList) indexMap.get(indexType);
						for (String index : indexes) {
							SpaceIndex spaceIndex = SpaceIndexFactory.createPropertyIndex(index, type);
							try {
								logger.fine("creating index " + spaceIndex.toString() + " for type " + typeName);
								typeBuilder.addIndex(spaceIndex);
							} catch (IllegalArgumentException iae) {
								logger.warning("registerTypeDescriptor" + iae.getMessage());
							}
						}
					}
				}
				// this is not needed and not being written
//				typeBuilder.addFixedProperty(propertyName, (String) propertyMap.get(propertyName));
			}
		    // Register type:
		    space.getTypeManager().registerTypeDescriptor(typeBuilder.create());
		}
		
		@Override
		public void run() {

			try {
				GZIPInputStream zis = new GZIPInputStream(new BufferedInputStream(new FileInputStream(file)));
				ObjectInputStream input = new ObjectInputStream(zis);
				logger.info("opened import file " + file.toString());
				lines.add("opened import file " + file.toString());
				try {
					className = input.readUTF();
					Integer objectCount = input.readInt();
					String typeName = null;
					
					if (className.equals(DOCUMENT))
						// read in the type descriptor data
						typeName = input.readUTF();
					// we could serialize *all* type descriptors
					else 
						typeName = className;

					SerialMap propertyMap = (SerialMap) input.readObject();
					registerTypeDescriptor(typeName, propertyMap);
					// we log classname, so set it up to reflect the space document type
					className = typeName + " (" + className + ")";		

					logger.info("found " + objectCount + " instances of " + className);
					lines.add("found " + objectCount + " instances of " + className);
					
					List<Object> objectList = new ArrayList<Object>();
					Long start = System.currentTimeMillis();
					for (int o = 0; o < objectCount; o++) {
						objectList.add(input.readObject());
						if (o > 0 && (o % batch == 0 || o + 1 == objectCount)) {
							space.writeMultiple(objectList.toArray(new Object[0]));
							if (o + 1 < objectCount) objectList.clear();
						}
					}
					Long duration = (System.currentTimeMillis() - start);
					logger.info("import operation took " + duration + " millis");
					lines.add("import operation took " + duration + " millis");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void setClusterInfo(ClusterInfo clusterInfo) {
		this.clusterInfo = clusterInfo;
	}

}
