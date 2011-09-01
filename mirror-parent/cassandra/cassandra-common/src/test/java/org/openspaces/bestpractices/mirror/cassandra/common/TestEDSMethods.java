package org.openspaces.bestpractices.mirror.cassandra.common;

import com.j_spaces.core.IGSEntry;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.ITypeDescriptor;
import com.j_spaces.core.client.ExternalEntry;
import net.jini.core.entry.UnusableEntryException;
import org.mvel2.MVEL;
import org.openspaces.bestpractices.mirror.common.InvalidKeyFormatException;
import org.openspaces.bestpractices.mirror.model.Person;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class TestEDSMethods {
    @Test
    public void testKeyTypes() throws InvalidKeyFormatException {
        CassandraEDS eds = new CassandraEDS();
        IGSEntry entry = new IGSEntry() {
            @Override
            public Map.Entry getMapEntry() {
                return null;
            }

            @Override
            public short getFormat() {
                return 0;
            }

            @Override
            public String getUID() {
                return "12345";
            }

            @Override
            public String getClassName() {
                return "org.openspaces.TestIGSEntry";
            }

            @Override
            public String[] getSuperClassesNames() {
                return new String[0];
            }

            @Override
            public String getCodebase() {
                return null;
            }

            @Override
            public String[] getFieldsNames() {
                return new String[0];
            }

            @Override
            public String[] getFieldsTypes() {
                return new String[0];
            }

            @Override
            public Object[] getFieldsValues() {
                return new Object[0];
            }

            @Override
            public boolean[] getIndexIndicators() {
                return new boolean[0];
            }

            @Override
            public String getPrimaryKeyName() {
                return null;
            }

            @Override
            public boolean isFifo() {
                return false;
            }

            @Override
            public boolean isTransient() {
                return false;
            }

            @Override
            public boolean isReplicatable() {
                return false;
            }

            @Override
            public long getTimeToLive() {
                return 0;
            }

            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public int getFieldPosition(String s) {
                return 0;
            }

            @Override
            public Object getFieldValue(String s) throws IllegalArgumentException, IllegalStateException {
                return null;
            }

            @Override
            public Object getFieldValue(int i) throws IllegalArgumentException, IllegalStateException {
                return null;
            }

            @Override
            public Object setFieldValue(String s, Object o) throws IllegalArgumentException, IllegalStateException {
                return null;
            }

            @Override
            public Object setFieldValue(int i, Object o) throws IllegalArgumentException, IllegalStateException {
                return null;
            }

            @Override
            public String getFieldType(String s) throws IllegalArgumentException, IllegalStateException {
                return null;
            }

            @Override
            public boolean isIndexedField(String s) throws IllegalArgumentException, IllegalStateException {
                return false;
            }

            @Override
            public ExternalEntry getExternalEntry(IJSpace ijSpace) throws UnusableEntryException {
                return null;
            }

            @Override
            public Object getObject(IJSpace ijSpace) throws UnusableEntryException {
                return null;
            }

            @Override
            public String getRoutingFieldName() {
                return null;
            }

            @Override
            public boolean[] getPrimitiveFields() {
                return new boolean[0];
            }

            @Override
            public ITypeDescriptor.Type getEntryType() {
                return null;
            }
        };
        assertEquals(eds.getKeyValue(entry), "org_openspaces_TestIGSEntry:12345");
        assertEquals(eds.getTypeFromKey("org_openspaces_TestIGSEntry:12345"), "org.openspaces.TestIGSEntry");
        assertEquals(eds.getIdFromKey("org_openspaces_TestIGSEntry:12345"), "12345");
    }

    @Test
    public void testMVEL() {
        Person person=new Person();
        Map map=new HashMap();
        map.put("person", person);
        MVEL.eval("person.firstName=\"John\"", map);
        MVEL.eval("person.creditScore=\"5\"", map);
        System.out.println(person);
        assertEquals(person.getFirstName(), "John");
        assertEquals(5,person.getCreditScore().intValue());
    }
}
