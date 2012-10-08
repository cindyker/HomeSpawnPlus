/*******************************************************************************
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (c) 2012 Mark Morgan.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * Contributors:
 *     Mark Morgan - initial API and implementation
 ******************************************************************************/
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
	private static final String EMPTY_STRING = "%NO_MESSAGE%";
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
	
	            // strip any trailing \ since it will SIOBE the below .replaceAll()
	            while( bindVal.length() > 0 && bindVal.charAt(bindVal.length()-1) == '\\' ) {
	                bindVal = bindVal.substring(0, bindVal.length()-1);
	            }
	            
	            // escape any $ character so it can pass through the value
	            // replaceAll without being interpreted as a regex grouping
//                Debug.getInstance().devDebug("parseString() pre bindVal=",bindVal);
	            if( bindVal.indexOf('$') != 1 )
	                bindVal = bindVal.replaceAll("\\$", "\\\\\\$");
//	            Debug.getInstance().devDebug("parseString() post bindVal=",bindVal);
	            
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
