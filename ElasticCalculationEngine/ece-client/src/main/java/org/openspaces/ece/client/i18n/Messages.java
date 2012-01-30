package org.openspaces.ece.client.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Messages {
    ResourceBundle resources;
    static Map<Locale, Messages> map = new HashMap<Locale, Messages>();

    private Messages(Locale locale) {
        resources = ResourceBundle.getBundle("Messages", locale);
    }

    static Messages getInstance(Locale locale) {
        if (!map.containsKey(locale)) {
            map.put(locale, new Messages(locale));
        }
        return map.get(locale);
    }

    public static Messages getInstance() {
        return getInstance(Locale.getDefault());
    }

    public String getMessage(String key) {
        return resources.getString(key);
    }

    public String getMessage(String key, String defaultValue) {
        return resources.containsKey(key) ? getMessage(key) : defaultValue;
    }
}
