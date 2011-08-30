package org.openspaces.bestpractices.mirror.cassandra.common;

import com.gigaspaces.datasource.*;
import com.j_spaces.core.IGSEntry;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ThriftKsDef;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.ddl.KeyspaceDefinition;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import org.mvel2.MVEL;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CassandraEDS implements BulkDataPersister, ManagedDataSource, DisposableBean, InitializingBean {
    Cluster cluster;
    private Integer port;
    private String clusterName;
    private String host;
    private String keyspaceName;
    private Keyspace keyspace;

    public String getColumnFamily() {
        return columnFamily;
    }

    public void setColumnFamily(String columnFamily) {
        this.columnFamily = columnFamily;
    }

    public String getKeyspaceName() {
        return keyspaceName;
    }

    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }

    private String columnFamily;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public void executeBulk(List<BulkItem> bulkItems) throws DataSourceException {
        System.out.println("in executeBulk(" + bulkItems + ")");
        Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
        for (BulkItem bulkItem : bulkItems) {
            IGSEntry item = (IGSEntry) bulkItem.getItem();
            switch (bulkItem.getOperation()) {
                case BulkItem.REMOVE:
                    remove(mutator, item);
                    break;
                case BulkItem.WRITE:
                case BulkItem.PARTIAL_UPDATE:
                case BulkItem.UPDATE:
                    write(mutator, item.getUID(), item);
                    break;
            }
        }
        mutator.execute();
    }

    private void write(Mutator<String> mutator, String uid, IGSEntry item) {
        try {
            int fldCount = item.getFieldsNames().length;
            String key = getKeyValue(item);
            for (int i = 0; i < fldCount; i++) {
                System.out.printf("insertion key:%s columnFamily %s, name %s value %s%n", key,
                        columnFamily, item.getFieldsNames()[i],
                        item.getFieldsValues()[i].toString());
                if (item.getFieldsValues()[i] != null) {
                    mutator.addInsertion(key, columnFamily,
                            HFactory.createColumn(item.getFieldsNames()[i],
                                    item.getFieldsValues()[i].toString(),
                                    StringSerializer.get(),
                                    StringSerializer.get()));
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    Map<String, String> keyPrefixes = new ConcurrentHashMap<String, String>();

    Pattern keyPattern = Pattern.compile("(.+):(.+)");

    String getTypeFromKey(String id) {
        Matcher matcher = keyPattern.matcher(id);
        if (matcher.matches()) {
            return matcher.group(1).replaceAll("_", ".");
        }
        throw new RuntimeException("Type Not Found in Cassandra row, " + id);
    }

    String getIdFromKey(String id) {
        Matcher matcher = keyPattern.matcher(id);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        throw new RuntimeException("Type Not Found in Cassandra row, " + id);
    }

    String getKeyValue(IGSEntry item) {
        String prefix = keyPrefixes.get(item.getClassName());
        if (prefix == null) {
            prefix = item.getClassName().replaceAll("\\.", "_") + ":";
            keyPrefixes.put(item.getClassName(), prefix);
        }
        return prefix + item.getUID();
    }

    private void remove(Mutator<String> mutator, IGSEntry item) {
        mutator.addDeletion(getKeyValue(item), columnFamily);
    }

    @Override
    public void destroy() throws Exception {
        //cluster.getConnectionManager().shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        cluster = HFactory.getOrCreateCluster(clusterName, host + ":" + port);
        KeyspaceDefinition keyspaceDefinition = cluster.describeKeyspace(keyspaceName);
        if (keyspaceDefinition == null) {
            createSchema(cluster);
        }
        keyspace = HFactory.createKeyspace(keyspaceName, cluster);
    }

    private void createSchema(Cluster cluster) {
        ColumnFamilyDefinition cfDef = HFactory.createColumnFamilyDefinition(keyspaceName, columnFamily,
                ComparatorType.BYTESTYPE);
        KeyspaceDefinition ksDef = HFactory.createKeyspaceDefinition(keyspaceName,
                ThriftKsDef.DEF_STRATEGY_CLASS,
                1,
                Arrays.asList(cfDef));
        cluster.addKeyspace(ksDef);
    }

    @Override
    public void init(Properties properties) throws DataSourceException {
    }

    @Override
    public DataIterator initialLoad() throws DataSourceException {
        DataIterator iterator = new DataIterator() {
            RangeSlicesQuery<String, String, String> rangeSlicesQuery;
            OrderedRows<String, String, String> orderedRows;
            Iterator iter;

            {
                rangeSlicesQuery =
                        HFactory.createRangeSlicesQuery(keyspace, StringSerializer.get(),
                                StringSerializer.get(), StringSerializer.get());
                rangeSlicesQuery.setColumnFamily(columnFamily);
                rangeSlicesQuery.setKeys("", "");
                rangeSlicesQuery.setRange("", "", false, 3);
                QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
                orderedRows = result.get();
                iter = orderedRows.iterator();
                System.out.println("data iterator static init run");
            }

            @Override
            public void close() {
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Object next() {
                Row<String, String, String> row = (Row<String, String, String>) iter.next();
                System.out.println("row: " + row);
                Object o = null;
                String id = row.getKey();
                String type = getTypeFromKey(id);
                String uid = getIdFromKey(id);
                List<HColumn<String, String>> hColumns = row.getColumnSlice().getColumns();
                try {
                    o = Class.forName(type).newInstance();
                    Map context = new HashMap();
                    context.put("o", o);
                    for (HColumn<String, String> hColumn : hColumns) {
                        MVEL.eval("o." + hColumn.getName() + "=\"" + hColumn.getValue() + "\"", context);
                        System.out.println(o);
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                System.out.println("returning " + o);
                return o;
            }

            @Override
            public void remove() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
        System.out.println("built data iterator");
        return iterator;
    }


    @Override
    public void shutdown() throws DataSourceException {

    }
}
