package org.openspaces.bestpractices.mirror.cassandra.common;

import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.document.SpaceDocument;
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
import org.openspaces.bestpractices.mirror.common.AbstractNoSQLEDS;
import org.openspaces.bestpractices.mirror.common.InvalidKeyFormatException;

import java.util.*;

public class CassandraEDS extends AbstractNoSQLEDS {
    @SuppressWarnings({"FieldCanBeLocal"})
    private Cluster cluster;
    private Integer port;
    private String clusterName;
    private String host;
    private String keyspaceName;
    private Keyspace keyspace;
    private final static StringSerializer stringSerializer = StringSerializer.get();

    @Override
    protected boolean isConnected() {
        return cluster != null;
    }

    public String getColumnFamily() {
        return columnFamily;
    }

    public void setColumnFamily(String columnFamily) {
        this.columnFamily = columnFamily;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public String getKeyspaceName() {
        return keyspaceName;
    }

    public void setKeyspaceName(String keyspaceName) {
        this.keyspaceName = keyspaceName;
    }

    private String columnFamily;

    @SuppressWarnings({"UnusedDeclaration"})
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    protected void preExecute(Map<String, Object> context) {
        context.put("mutator", HFactory.createMutator(getKeyspace(), stringSerializer));
    }

    @Override
    protected void postExecute(Map<String, Object> context) {
        Mutator<String> mutator = getMutatorFromContext(context);
        mutator.execute();
    }

    @Override
    protected void update(Map<String, Object> context, BulkItem bulkItem) {
        write(context, bulkItem);
    }

    @Override
    protected void write(Map<String, Object> context, BulkItem item) {
        Mutator<String> mutator = getMutatorFromContext(context);
        String uid = getKeyValue((IGSEntry) item.getItem());
        for (String key : item.getItemValues().keySet()) {
            if (item.getItemValues().get(key) != null) {
                mutator.addInsertion(uid, getColumnFamily(),
                        HFactory.createColumn(key, item.getItemValues().get(key).toString(), stringSerializer,
                                stringSerializer));
            }
        }
    }

    @Override
    protected void remove(Map<String, Object> context, BulkItem item) {
        IGSEntry entry = (IGSEntry) item.getItem();
        Mutator<String> mutator = getMutatorFromContext(context);
        mutator.addDeletion(getKeyValue(entry), columnFamily);
    }

    private Mutator<String> getMutatorFromContext(Map<String, Object> context) {
        //noinspection unchecked
        return (Mutator<String>) context.get("mutator");
    }

    @Override
    public void destroy() throws Exception {
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
    public DataIterator initialLoad() throws DataSourceException {
        return new DataIterator() {
            Set<Object> data = new HashSet<Object>();
            Iterator<Object> iterator;

            {
                RangeSlicesQuery<String, String, String> rangeSlicesQuery =
                        HFactory.createRangeSlicesQuery(getKeyspace(),
                                stringSerializer, stringSerializer,
                                stringSerializer);
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
                            try {
                                data.add(buildObjectFromRow(row));
                            } catch (InvalidKeyFormatException e) {
                                // bad row, discard
                                e.printStackTrace();
                            }
                        }
                    }
                } while (orderedRows.getCount() > 1);
                iterator = data.iterator();
            }

            private Object buildObjectFromRow(Row<String, String, String> row) throws InvalidKeyFormatException {
                Object o = null;
                String id = row.getKey();
                String type = getTypeFromKey(id);
                String uid = getIdFromKey(id);
                List<HColumn<String, String>> hColumns = row.getColumnSlice().getColumns();
                // okay, we need to do different things based on the underlying type.
                // if it's registered as a SpaceDocument... act like it.

                try {
                    o = Class.forName(type).newInstance();
                    Map<String, Object> context = new HashMap<String, Object>();
                    context.put("o", o);
                    for (HColumn<String, String> hColumn : hColumns) {
                        String expression = "o." + hColumn.getName() + "=\"" + hColumn.getValue() + "\"";
                        MVEL.eval(expression, context);
                    }
                    MVEL.eval("o.id=\"" + uid + "\"", context);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    // let's assume it's a SpaceDocument, and act accordingly.
                    SpaceDocument sd = new SpaceDocument(type);
                    for (HColumn<String, String> hColumn : hColumns) {
                        sd.setProperty(hColumn.getName(), hColumn.getValue());
                    }
                    sd.setProperty("id", uid);
                    o = sd;
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
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }


    @Override
    public void shutdown() throws DataSourceException {
    }

    public Keyspace getKeyspace() {
        return keyspace;
    }
}
