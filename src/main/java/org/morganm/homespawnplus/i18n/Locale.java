/**
 * 
 */
package org.morganm.homespawnplus.i18n;


/**
 * @author morganm
 *
 */
public interface Locale {
	/** Return a message from the Locale object, optionally doing string replacement
	 * on the given args (depending on the underlying implementation).
	 * 
	 * @param key the key value to retrieve from the resource bundle
	 * @param args key,value pairs of replacements: ie. "id",123,"playerName",player.getName()
	 * @return the localized string
	 */
	public String getMessage(String key, Object... args);
	
	/** Prefered form of message processing. This version is capable of returning
	 * multiple strings, each element represents a separate line. Thus 3 elements
	 * should be printed as 3 strings on 3 separate lines. 
	 * 
	 * @param key the key value to retrieve from the resource bundle
	 * @param args key,value pairs of replacements: ie. "id",123,"playerName",player.getName()
	 * @return the localized string(s)
	 */
	public String[] getMessages(String key, Object... args);
	
	/** Return the Locale string this object represents.
	 * 
	 * @return
	 */
	public String getLocale();
}
