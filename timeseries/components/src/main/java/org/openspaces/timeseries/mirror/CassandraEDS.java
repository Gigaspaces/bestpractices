package org.openspaces.timeseries.mirror;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataProvider;
import com.gigaspaces.datasource.DataSourceException;
import com.j_spaces.core.IGSEntry;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.thoughtworks.xstream.XStream;

public class CassandraEDS implements BulkDataPersister, DataProvider {

	private String cassandraURL ;
	BoneCP connectionPool;
	HashMap<String, Boolean> schema = new HashMap<String, Boolean>();

	ConcurrentHashMap<String, String> insertSQLMap = new ConcurrentHashMap<String, String> ();
	ConcurrentHashMap<String, String> updateSQLMap = new ConcurrentHashMap<String, String> ();

	Logger log =Logger.getLogger("CassandraEDS");
	AtomicInteger operationID = new AtomicInteger();
	public void setCassandraURL(String url) {
		this.cassandraURL = url;
	}

	public void init(Properties properties) throws DataSourceException {

		try {
			Class.forName("org.apache.cassandra.cql.jdbc.CassandraDriver");

			BoneCPConfig config = new BoneCPConfig(); // create a new configuration object
			config.setJdbcUrl(cassandraURL); // set the JDBC url
			config.setMaxConnectionsPerPartition(1000);

			connectionPool = new BoneCP(config); // setup the connection pool
			log.info("Connect to Cassandra OK!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void shutdown() throws DataSourceException {
		if (connectionPool != null)
			try {
				// con.close();
				connectionPool.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public void executeBulk(List<BulkItem> bulkItems)
			throws DataSourceException {
		Connection con = null;
		try {
			con = connectionPool.getConnection();
			for (BulkItem bulkItem : bulkItems) {
				IGSEntry item = (IGSEntry) bulkItem.getItem();
				String clazzName = item.getClassName().replaceAll("\\.", "_").toUpperCase();

				String UID = item.getUID();
				//log.info("uid="+UID.toString());
				//String uid_parts[] = UID.split("\\^");
				//String ID = uid_parts[2];
				String ID=getValue(UID);

				switch (bulkItem.getOperation()) {
				case BulkItem.REMOVE:
					String deleteQL = "DELETE FROM " + clazzName
					+ " WHERE KEY = " + ID;
					try {
						executeCQL(con, deleteQL);
					} catch (Exception e) {
						e.printStackTrace();
					}

					break;
				case BulkItem.WRITE:
					String insertQL = "";
					int fldCount = item.getFieldsNames().length;
					if (insertSQLMap.containsKey(clazzName))
					{
						insertQL = insertSQLMap.get(clazzName);
					}
					else
					{
						insertQL = "INSERT INTO " + clazzName + " (KEY,";
						for (int i = 0; i < fldCount ; i++) {
							insertQL = insertQL + " '" + item.getFieldsNames()[i]+ "',";
						}
						insertQL = insertQL.substring(0, insertQL.length() - 1);

						insertSQLMap.put(clazzName,insertQL);
					}

					insertQL = insertQL + ") VALUES (" + ID + ",";
					StringBuffer insertQLBuf = new StringBuffer(insertQL);
					for (int i = 0; i < fldCount; i++) {
						if(item.getFieldsNames()[i].contains("Data")){
							//Use xstream to serialize complex children
							insertQLBuf= insertQLBuf.append(getValue(new XStream().toXML(item.getFieldValue(i))));
						}
						else{
							insertQLBuf= insertQLBuf.append(getValue(item.getFieldValue(i)));
						}
						insertQLBuf =insertQLBuf.append(",");
					}
					insertQL = insertQLBuf.toString();
					insertQL = insertQL.substring(0, insertQL.length() - 1)+ ")";
					try {
						executeCQL(con, insertQL);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case BulkItem.UPDATE:
					fldCount = item.getFieldsNames().length;
					String updateQL = "";
					if (updateSQLMap.containsKey(clazzName))
					{
						updateQL = updateSQLMap.get(clazzName);
					}
					else
					{
						updateQL = "UPDATE " + clazzName + " SET";

						for (int i = 0; i < fldCount ; i++) {
							updateQL = updateQL + " '" + item.getFieldsNames()[i]+ "'=%s,";
						}
						updateSQLMap.put(clazzName, updateQL);
					}					

					String vals [] = new String [fldCount ];
					for (int i = 0; i < fldCount ; i++) {
						vals [i] = getValue(item.getFieldValue(i));
					}
					updateQL = updateQL.format(updateQL , vals );

					updateQL = updateQL.substring(0, updateQL.length() - 1);

					updateQL = updateQL + " WHERE KEY=" + ID;
					try {
						executeCQL(con, updateQL);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	String getValue(Object val)
	{
		if (val == null)
			return "''";
		else
		{
			if(val instanceof String){
				String str = val.toString();
				if (str.indexOf("'") > 0)
				{
					return "'" +str.replaceAll("'", "''") + "'" ;
				}
				else
					return "'" + str + "'";}
			else{
				return val.toString();
			}
		}
	}

	private void executeCQL(Connection con, String cql) throws SQLException {
		log.info(operationID.incrementAndGet() + " Total Free:" + connectionPool.getStatistics().getTotalFree() +   
				"  Total Created:" +connectionPool.getStatistics().getTotalCreatedConnections() +
				" Total Leased:" + connectionPool.getStatistics().getTotalLeased() + 
				" Connection:" + con + "\n" + cql);
		Statement statement = con.createStatement();
		statement.execute(cql);
		statement.close();
	}

	@Override
	public DataIterator initialLoad() throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataIterator iterator(Object arg0) throws DataSourceException {
		// TODO Auto-generated method stub
		return new MyDataIterator();
	}

	@Override
	public Object read(Object arg0) throws DataSourceException {
		// TODO Auto-generated method stub
		return null;
	}

	static class MyDataIterator implements DataIterator {

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Object next() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub

		}

		@Override
		public void close() {
			// TODO Auto-generated method stub

		}
	}

	public String getCassandraURL() {
		return cassandraURL;
	}

}
