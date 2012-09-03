/**
 * 
 */
package org.morganm.homespawnplus.i18n;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/** Implementation of locale strings that includes Bukkit color and string
 * parameter replacement.
 * 
 * Code heavily borrowed from Hidendra's LWC.
 * 
 * @author morganm
 *
 */
public class LocaleStringReplacerImpl implements Locale {
	private static final String EMPTY_STRING = "%EMPTY%";
	private static final Pattern newLinePattern = Pattern.compile("%NL%");
	private static final Map<String, String> predefinedReplacements = new HashMap<String,String>();
	static {
		predefinedReplacements.put("%NL%", "");		// Strips out %NL%
	}
	
	private final MessageLibrary msgLib;
	private final String localeString;
	
	public LocaleStringReplacerImpl(final MessageLibrary msgLib, final String localeString) {
		this.msgLib = msgLib;
		this.localeString = localeString;
	}
	
	@Override
	public String getLocale() {
		return localeString;
	}
	
	/** Process a string and do all replacements. If processing multi-line
	 * strings with the %NL% string, be sure to split before calling this
	 * method as %NL% will be stripped out here.
	 * 
	 * Better implementation using pre-compiled Patterns exists in
	 * mBukkitLib, I will be migrating HSP to use that in the future.
	 * 
	 * @param s the string to process
	 * @param binds the bind parameters
	 * @param localizedKey the lookup key (such as "HSP_COMMAND_HOME"). Can be null,
	 * it is used only to give admin good error messages if we have a problem
	 * @return
	 */
	private String parseString(String value, Map<String, Object> binds, String localizedKey) {
        if( value == null )
        	throw new NullPointerException("localized string for key "+localizedKey+" is null");
        
        if( value.equals(EMPTY_STRING) )
        	return "";
        
        // simple optimization: if no % symbol is present at all in the value
        // string, then we can skip color/arg replacement entirely.
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
	        for (String bindKey : binds.keySet()) {
	        	if( bindKey == null )
	        		throw new NullPointerException("bindKey for localized string "+localizedKey+" is null, localized string = "+value);
	        	
	            Object object = binds.get(bindKey);
	            String bindVal = (object != null ? object.toString() : "");
	
	            value = value.replaceAll("%" + bindKey + "%", bindVal);
	        }
        }

        return value;
	}
	
	@Override
	public String getMessage(String key, final Object... args) {
        key = key.replaceAll(" ", "_");

        if (!msgLib.getResourceBundle().containsKey(key)) {
            return "UNKNOWN_LOCALE_" + key;
        }

        Map<String, Object> bind = parseBinds(args);
        String value = msgLib.getResourceBundle().getString(key);
        
        return parseString(value, bind, key);
	}
	
	@Override
	public String[] getMessages(String key, final Object... args) {
        key = key.replaceAll(" ", "_");

        if (!msgLib.getResourceBundle().containsKey(key)) {
            return new String[] {"UNKNOWN_LOCALE_" + key};
        }

        Map<String, Object> bind = parseBinds(args);
        String value = msgLib.getResourceBundle().getString(key);
        
        if( value == null )
        	throw new NullPointerException("localized string for key "+key+" is null");
        
        String[] values = newLinePattern.split(value);
        for(int i=0; i < values.length; i++) {
        	values[i] = parseString(value, bind, key);
        }
        
        return values;
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
