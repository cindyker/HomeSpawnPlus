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
	private static final Map<String, String> predefinedReplacements = new HashMap<String,String>();
	static {
		// newline didn't work like I'd hoped.  So no predefinedReplacements for now ..
//		predefinedReplacements.put("%newline%", "\n");
	}
	
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
        
        if( value == null )
        	throw new NullPointerException("localized string for key "+key+" is null");

        // simple optimization: if no % symbol is present at all in the value
        // string, then we can skip color/arg replacement entirely. -morganm
        if( value.indexOf('%') != -1 ) {
	        // apply colors
	        for (String colorKey : Colors.localeColors.keySet()) {
	            String color = Colors.localeColors.get(colorKey);
	
	            if (value.contains(colorKey)) {
	                value = value.replaceAll(colorKey, color);
	            }
	        }

	        // apply any predefined replacements
	        for (Map.Entry<String, String> entry : predefinedReplacements.entrySet()) {
	            if (value.contains(entry.getKey())) {
	                value = value.replaceAll(entry.getKey(), entry.getValue());
	            }
	        }

	        // apply binds
	        for (String bindKey : bind.keySet()) {
	        	if( bindKey == null )
	        		throw new NullPointerException("bindKey for localized string "+key+" is null, localized string = "+value);
	        	
	            Object object = bind.get(bindKey);
	            String bindVal = (object != null ? object.toString() : "null");
	
	            value = value.replaceAll("%" + bindKey + "%", bindVal);
	        }
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
