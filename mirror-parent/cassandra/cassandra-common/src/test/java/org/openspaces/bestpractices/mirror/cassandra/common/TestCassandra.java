package org.openspaces.bestpractices.mirror.cassandra.common;

import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.OrderedRows;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.RangeSlicesQuery;
import org.openspaces.bestpractices.mirror.common.tests.FileCallback;
import org.openspaces.bestpractices.mirror.common.tests.FileUtil;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestCassandra {
    CassandraTestUtil cassandraUtil = new CassandraTestUtil();
    FileUtil fileUtil = new FileUtil();

    @BeforeTest
    public void startup() throws IOException {
        final String ref[] = new String[1];
        FileCallback fileCallback = new FileCallback() {
            @Override
            public void found(String filename) {
                ref[0] = filename;
            }
        };
        fileUtil.findFile("cassandra\\.yaml", fileCallback);
        System.out.println(ref[0]);
        ref[0] = ref[0].substring(!ref[0].contains(":") ? 0 : ref[0].indexOf(":") + 1);
        cassandraUtil.startCassandra("file:" + ref[0]);
    }

    @AfterTest
    public void shutdown() {
        cassandraUtil.shutDownCassandra();
    }

    @Test
    public void testQuery() throws Exception {
        CassandraEDS eds = new CassandraEDS();
        eds.setClusterName("clusterName");
        eds.setColumnFamily("columnFamily");
        eds.setHost("localhost");
        eds.setPort(19160);
        eds.setKeyspaceName("keySpace");
        eds.afterPropertiesSet();
        Keyspace keySpace = eds.getKeyspace();
        Mutator<String> mutator = HFactory.createMutator(keySpace, StringSerializer.get());
        for (int i = 0; i < 30; i++) {
            String key = "fooValue:" + i;
            mutator.addInsertion(key, eds.getColumnFamily(), HFactory.createStringColumn("column1", "1:" + i))
                    .addInsertion(key, eds.getColumnFamily(), HFactory.createStringColumn("column2", "2:" + i))
                    .addInsertion(key, eds.getColumnFamily(), HFactory.createStringColumn("column3", "3:" + i))
                    .addInsertion(key, eds.getColumnFamily(), HFactory.createStringColumn("column4", "4:" + i));
        }
        mutator.execute();
        RangeSlicesQuery<String, String, String> rangeSlicesQuery =
                HFactory.createRangeSlicesQuery(eds.getKeyspace(),
                        StringSerializer.get(), StringSerializer.get(),
                        StringSerializer.get());
        rangeSlicesQuery.setColumnFamily(eds.getColumnFamily());
        rangeSlicesQuery.setKeys("", "");
        rangeSlicesQuery.setRange("", "", false, 40);
        rangeSlicesQuery.setRowCount(1);
        QueryResult<OrderedRows<String, String, String>> result = rangeSlicesQuery.execute();
        OrderedRows<String, String, String> orderedRows = result.get();


        Row<String, String, String> lastRow;
        boolean first = false;
        do {
            lastRow = orderedRows.peekLast();
            rangeSlicesQuery.setRowCount(10);
            rangeSlicesQuery.setKeys(lastRow.getKey(), "");
            orderedRows = rangeSlicesQuery.execute().get();

            for (Row<String, String, String> row : orderedRows) {
                if (first) {
                    first = false;
                } else {
                    printRow(row);
                }
            }
            first = true;
        } while (orderedRows.getCount() > 1);
        eds.shutdown();
    }

    private void printRow(Row<String, String, String> row) {
        System.out.print(row.getKey());
        for (HColumn<String, String> column : row.getColumnSlice().getColumns()) {
            System.out.print("," + column.getName() + "=" + column.getValue());
        }
        System.out.println();
    }
}
