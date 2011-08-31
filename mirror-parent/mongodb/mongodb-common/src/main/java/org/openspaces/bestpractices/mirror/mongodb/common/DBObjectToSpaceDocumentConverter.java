package org.openspaces.bestpractices.mirror.mongodb.common;

import com.gigaspaces.document.DocumentProperties;
import com.gigaspaces.document.SpaceDocument;
import com.mongodb.DBObject;
import org.springframework.core.convert.converter.Converter;

import java.util.Set;

/**
 * @author uri
 */
public class DBObjectToSpaceDocumentConverter implements Converter<DBObject, SpaceDocument> {
    public SpaceDocument convert(DBObject source) {
        SpaceDocument document = new SpaceDocument();
        DocumentProperties documentProperties = new DocumentProperties();
        populate(source, documentProperties);
        document.addProperties(documentProperties);
        return document;
    }

    private void populate(DBObject source, DocumentProperties target) {
        Set<String> keys = source.keySet();
        for (String key : keys) {
            Object value = source.get(key);
            if (value instanceof DBObject) {
                DocumentProperties subProps = new DocumentProperties();
                populate((DBObject) value, subProps);
                target.setProperty(key, subProps);
            } else {
                target.setProperty(key, value);
            }
        }
    }
}
