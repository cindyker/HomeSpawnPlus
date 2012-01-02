/**
 * 
 */
package org.morganm.homespawnplus.i18n;

import java.util.HashMap;
import java.util.Map;

/** Implementation of locale strings that includes Bukkit color and string
 * parameter replacement.
 * 
 * Code heavily borrowed from Hidendra's LWC.
 * 
 * @author morganm
 *
 */
public class LocaleStringReplacerImpl implements Locale {
	private final MessageLibrary msgLib;
	
	public LocaleStringReplacerImpl(final MessageLibrary msgLib) {
		this.msgLib = msgLib;
	}
	
	@Override
	public String getMessage(String key, final Object... args) {
        key = key.replaceAll(" ", "_");

        if (!msgLib.getResourceBundle().containsKey(key)) {
            return "UNKNOWN_LOCALE_" + key;
        }

        Map<String, Object> bind = parseBinds(args);
        String value = msgLib.getResourceBundle().getString(key);

        // apply colors
        for (String colorKey : Colors.localeColors.keySet()) {
            String color = Colors.localeColors.get(colorKey);

            if (value.contains(colorKey)) {
                value = value.replaceAll(colorKey, color);
            }
        }

        // apply binds
        for (String bindKey : bind.keySet()) {
            Object object = bind.get(bindKey);

            value = value.replaceAll("%" + bindKey + "%", object.toString());
        }

        return value;
	}

    /**
     * Convert an even-lengthed argument array to a map containing String keys
     * i.e parseBinds("Test", null, "Test2", obj) = Map().put("test", null).put("test2", obj)
     * 
     * (code borrowed from LWC)
     *
     * @author Hidendra
     * @param args
     * @return
     */
    private Map<String, Object> parseBinds(final Object... args) {
        Map<String, Object> bind = new HashMap<String, Object>(args.length/2);

        if (args == null || args.length < 2) {
            return bind;
        }

        int size = args.length;
        for (int index = 0; index < args.length; index += 2) {
            if ((index + 2) > size) {
                break;
            }

            String key = args[index].toString();
            Object object = args[index + 1];

            bind.put(key, object);
        }

        return bind;
    }
}
