package org.openspaces.plains.datagrid;

import org.testng.annotations.Test;

public class DataGridConnectionUtilityTest {
    @Test
    public void testDataGridConnection() {
        System.out.println(DataGridConnectionUtility.getSpace("foo", 1, 0));
    }
}
