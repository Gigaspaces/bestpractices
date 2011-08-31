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

import java.lang.reflect.Field;
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
        System.out.println(bulkItems);
        Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
        for (BulkItem bulkItem : bulkItems) {
            System.out.println(bulkItem.getItem().getClass());
            switch (bulkItem.getOperation()) {
                case BulkItem.REMOVE:
                    remove(mutator, (IGSEntry) bulkItem.getItem());
                    break;
                case BulkItem.WRITE:
                case BulkItem.PARTIAL_UPDATE:
                case BulkItem.UPDATE:
                    write(mutator, bulkItem);
                    break;
            }
        }
        mutator.execute();
    }

    Map<Class<?>, Field[]> classFields = new ConcurrentHashMap<Class<?>, Field[]>();

    private void write(Mutator<String> mutator, BulkItem item) {
        String uid = getKeyValue((IGSEntry) item.getItem());
        for (String key : item.getItemValues().keySet()) {
            if (item.getItemValues().get(key) != null) {
                System.out.printf("Adding to %s, %s: %s=%s%n", uid, getColumnFamily(), key,
                        item.getItemValues().get(key));

                mutator.addInsertion(uid, getColumnFamily(),
                        HFactory.createColumn(key, item.getItemValues().get(key).toString(), StringSerializer.get(),
                                StringSerializer.get()));
            }
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
            Set<Object> data = new HashSet<Object>();
            Iterator<Object> iterator;

            {
                RangeSlicesQuery<String, String, String> rangeSlicesQuery =
                        HFactory.createRangeSlicesQuery(getKeyspace(),
                                StringSerializer.get(), StringSerializer.get(),
                                StringSerializer.get());
                rangeSlicesQuery.setColumnFamily(getColumnFamily());
                rangeSlicesQuery.setKeys("", "");
                rangeSlicesQuery.setRange("", "", false, 40);
                rangeSlicesQuery.setRowCount(11);
                QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
                OrderedRows<String, String, String> orderedRows = result.get();

                Row<String, String, String> lastRow;
                do {
                    if (orderedRows.getCount() > 0) {
                        lastRow = orderedRows.peekLast();
                        rangeSlicesQuery.setKeys(lastRow.getKey(), "");
                        orderedRows = rangeSlicesQuery.execute().get();

                        for (Row<String, String, String> row : orderedRows) {
                            data.add(buildObjectFromRow(row));
                        }
                    }
                } while (orderedRows.getCount() > 1);
                iterator = data.iterator();
            }

            private Object buildObjectFromRow(Row<String, String, String> row) {
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
                        String expression="o." + hColumn.getName() + "=\"" + hColumn.getValue() + "\"";
                        MVEL.eval(expression, context);
                        System.out.println(o);
                    }
                    MVEL.eval("o.id=\"" + uid + "\"",context);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return o;
            }


            @Override
            public void close() {
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Object next() {
                Object o = iterator.next();
                System.out.println("loading " + o);
                return o;
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
        System.out.println("built data iterator");
        return iterator;
    }


    @Override
    public void shutdown() throws DataSourceException {
    }

    public Keyspace getKeyspace() {
        return keyspace;
    }
}
