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
package org.morganm.homespawnplus.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.i18n.Locale;

/**
 * @author morganm
 *
 */
public final class General {
	// class version: 5
	
	private static General instance;
	
	private Logger log = Logger.getLogger(General.class.toString());
	private String logPrefix = "";
	private Locale locale;
	private final Map<String, String> timeLongHand = new HashMap<String, String>();
	private final Map<String, String> timeShortHand = new HashMap<String, String>();
	private Teleport teleportObject;
	
	private General() {
		setupTimeLocaleStrings();
	}
	
	public static General getInstance() {
		if( instance == null )
			instance = new General();
		return instance;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
		setupTimeLocaleStrings();
	}
	public void setLogger(Logger log) {
		this.log = log;
	}
	public void setLogPrefix(String logPrefix) {
		this.logPrefix = logPrefix;
	}
	
	public Teleport getTeleport() {
		if( teleportObject == null ) {
			teleportObject = Teleport.getInstance();
			teleportObject.setLogger(log);
			teleportObject.setLogPrefix(logPrefix);
		}
		
		return teleportObject;
	}
	
	private void setupTimeLocaleStrings() {
		timeLongHand.clear();
		timeShortHand.clear();
		
		// default English is hard-coded so that even without a Locale specified,
		// the time-related functions will work.
		if( locale == null || "en".equalsIgnoreCase(locale.getLocale()) ) {
			timeLongHand.put("month", "month");
			timeLongHand.put("months", "months");
			timeLongHand.put("week", "week");
			timeLongHand.put("weeks", "weeks");
			timeLongHand.put("day", "day");
			timeLongHand.put("days", "days");
			timeLongHand.put("hour", "hour");
			timeLongHand.put("hours", "hours");
			timeLongHand.put("minute", "minute");
			timeLongHand.put("minutes", "minutes");
			timeLongHand.put("second", "second");
			timeLongHand.put("seconds", "seconds");
			
			timeShortHand.put("mo", "mo");
			timeShortHand.put("w", "w");
			timeShortHand.put("d", "d");
			timeShortHand.put("h", "h");
			timeShortHand.put("m", "m");
			timeShortHand.put("s", "s");
		}
		
		// even if "en" is set and receives defaults from above, this allows
		// the actual locale specified to override the hardcoded defaults
		if( locale != null ) {
			timeLongHand.put("month", locale.getMessage("month"));
			timeLongHand.put("months", locale.getMessage("months"));
			timeLongHand.put("week", locale.getMessage("week"));
			timeLongHand.put("weeks", locale.getMessage("weeks"));
			timeLongHand.put("day", locale.getMessage("day"));
			timeLongHand.put("days", locale.getMessage("days"));
			timeLongHand.put("hour", locale.getMessage("hour"));
			timeLongHand.put("hours", locale.getMessage("hours"));
			timeLongHand.put("minute", locale.getMessage("minute"));
			timeLongHand.put("minutes", locale.getMessage("minutes"));
			timeLongHand.put("second", locale.getMessage("second"));
			timeLongHand.put("seconds", locale.getMessage("seconds"));
			
			timeShortHand.put("mo", locale.getMessage("MONTH_SHORTHAND"));
			timeShortHand.put("w", locale.getMessage("WEEK_SHORTHAND"));
			timeShortHand.put("d", locale.getMessage("DAY_SHORTHAND"));
			timeShortHand.put("h", locale.getMessage("HOUR_SHORTHAND"));
			timeShortHand.put("m", locale.getMessage("MINUTE_SHORTHAND"));
			timeShortHand.put("s", locale.getMessage("SECOND_SHORTHAND"));
		}
	}
	
	public String shortLocationString(final Location l) {
		if( l == null )
			return "null";
		else {
			World w = l.getWorld();
			String worldName = null;
			if( w != null )
				worldName = w.getName();
			else
				worldName = "(world deleted)";
			return worldName+","+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
		}
	}
	
	/** Read a string that was written by "shortLocationString" and turn it into
	 * a location object (if possible). Can return null.
	 * 
	 * @param locatinString
	 * @return
	 */
	public Location readShortLocationString(final String locationString) {
		Location location = null;
		if( locationString != null ) {
			String[] pieces = locationString.split(",");
			
			// make sure all the elements are there and it's not a deleted world 
			if( pieces.length == 4 && !pieces[0].equals("(world deleted)") ) {
				World w = Bukkit.getWorld(pieces[0]);
				int x = 0; int y = 0; int z = 0;
				try {
					x = Integer.parseInt(pieces[1]);
					y = Integer.parseInt(pieces[2]);
					z = Integer.parseInt(pieces[3]);
				} catch(NumberFormatException e) {}
				
				location = new Location(w, x, y, z);
			}
		}
		
		return location;
	}
	
	/** Return whether or not Player p is a new player (first time logged in).
	 * 
	 * Bukkit method seems wonky at times, so this is coded to check for player.dat
	 * on the default world.
	 * 
	 * @param p
	 * @return
	 */
    public boolean isNewPlayer(Player p) {
    	// Bukkit method is wonky, doesn't seem to work consistently
//    	return !p.hasPlayedBefore();
    	
		File worldContainer = Bukkit.getWorldContainer();
		
		final List<World> worlds = Bukkit.getWorlds();
		final String worldName = worlds.get(0).getName();
    	final String playerDat = p.getName() + ".dat";
    	
    	File file = new File(worldContainer, worldName+"/players/"+playerDat);
    	if( file.exists() )
    		return false;

    	// if we didn't find a player.dat file, they must be new
    	return true;
    }
    
    /** Given time input, such as of the form "1d" "1w 2d 3h", this will return
     * the number of milliseconds that time format equals. For example, "1d" is
     * 86400 seconds, so this method would return 86400000.
     * 
     * @param input
     * @return
     */
    public long parseTimeInput(final String input) throws NumberFormatException {
    	long time = 0;
    	
    	String[] args = input.split(" ");
    	for(int i=0; i < args.length; i++) {
    		long multiplier = 1000;	// milliseconds multiplier
    		int index = -1;
    		
    		if( (index = args[i].indexOf(timeShortHand.get("mo"))) != -1 ) {		// month
    			multiplier *= 86400 * 31;
    		}
    		else if( (index = args[i].indexOf(timeShortHand.get("w"))) != -1 ) {		// week
    			multiplier *= 86400 * 7;
    		}
    		else if( (index = args[i].indexOf(timeShortHand.get("d"))) != -1 ) {		// day
    			multiplier *= 86400;
    		}
    		else if( (index = args[i].indexOf(timeShortHand.get("h"))) != -1 ) {		// hours
    			multiplier *= 3600;
    		}
    		else if( (index = args[i].indexOf(timeShortHand.get("m"))) != -1 ) {		// minutes
    			multiplier *= 60;
    		}
    		
			String value = args[i].substring(0, index);
			Debug.getInstance().devDebug("parseTimeInput: value=",value,", multiplier=",multiplier);
			int v = Integer.valueOf(value);
			time += v * multiplier;
    	}
    	
		Debug.getInstance().devDebug("parseTimeInput: return time=",time);
    	return time;
    }

    /** Given milliseconds as input, this will return a string that represents
     * that time format.
     * 
     * @param seconds
     * @param useShortHand set to true to use shorthand notation. shorthand will return a string
     * of the form "4d3h2m" whereas this set to false would return "4 days 3 hours 2 minutes"
     * @param mostSignificant Most significant string to show. "mo" for month, "w" for week,
     * "d" for day, "m" for minute and null to include seconds
     * 
     * @return
     * @throws NumberFormatException
     */
    public String displayTimeString(final long millis, boolean useShortHand, String mostSignificant) throws NumberFormatException {
    	final StringBuffer sb = new StringBuffer();
    	long seconds = millis / 1000;		// chop down to seconds
    	// if mostSignificant arg is passed and we are below the threshold, this
    	// boolean allows us to print one less only. Example: lets say mostSignificant
    	// is set to "h" (hour). If we are below 1 hour, then the next mostSignificant
    	// ONLY will be shown: ie. "55m"
    	boolean mostSignificantCheck=true;
    	
    	if( seconds >= (86400 * 31) ) {
    		long months = seconds / (86400 * 31);
	    	Debug.getInstance().devDebug("months =",months);
	    	if( months > 0 ) {
	    		sb.append(months);
	    		if( useShortHand )
	    			sb.append(timeShortHand.get("mo"));
	    		else {
	    			sb.append(" ");
		    		if( months > 1 )
		        		sb.append(timeLongHand.get("months"));
		    		else
		        		sb.append(timeLongHand.get("month"));
	    		}
	    	}
	    	seconds -= months * (86400 * 31);
    	}
    	// "mostSignificant" is only passed in code (no user input) so this string
    	// is not localized.
    	if( mostSignificant != null && mostSignificant.startsWith("mo") || !mostSignificantCheck ) {
    		if( sb.length() > 0 )
    			return sb.toString();
    		else
    			mostSignificantCheck=false;
    	}

    	if( seconds >= (86400 * 7) ) {
    		long weeks = seconds / (86400 * 7);
	    	Debug.getInstance().devDebug("weeks =",weeks);
	    	if( weeks > 0 ) {
	    		if( sb.length() > 0 ) {
	    			if( !useShortHand )
	    				sb.append(",");
	    			sb.append(" ");
	    		}
	    		sb.append(weeks);
	    		if( useShortHand )
	    			sb.append(timeShortHand.get("w"));
	    		else {
	    			sb.append(" ");
		    		if( weeks > 1 )
		        		sb.append(timeLongHand.get("weeks"));
		    		else
		        		sb.append(timeLongHand.get("week"));
	    		}
	    	}
	    	seconds -= weeks * (86400 * 7);
    	}
    	Debug.getInstance().devDebug("week remaining seconds=",seconds);
    	if( mostSignificant != null && mostSignificant.startsWith("w") || !mostSignificantCheck ) {
    		if( sb.length() > 0 )
    			return sb.toString();
    		else
    			mostSignificantCheck=false;
    	}
    	
    	if( seconds >= 86400 ) {
    		long days = seconds / 86400;
	    	if( days > 0 ) {
	    		if( sb.length() > 0 ) {
	    			if( !useShortHand )
	    				sb.append(",");
	    			sb.append(" ");
	    		}
	    		sb.append(days);
	    		if( useShortHand )
	    			sb.append(timeShortHand.get("d"));
	    		else {
	    			sb.append(" ");
		    		if( days > 1 )
		        		sb.append(timeLongHand.get("days"));
		    		else
		        		sb.append(timeLongHand.get("day"));
	    		}
	    	}
	    	seconds -= days * 86400;
    	}
    	if( mostSignificant != null && mostSignificant.startsWith("d") || !mostSignificantCheck ) {
    		if( sb.length() > 0 )
    			return sb.toString();
    		else
    			mostSignificantCheck=false;
    	}
    	
    	if( seconds >= 3600 ) {
    		long hours = seconds / 3600;
	    	if( hours > 0 ) {
	    		if( sb.length() > 0 ) {
	    			if( !useShortHand )
	    				sb.append(",");
	    			sb.append(" ");
	    		}
	    		sb.append(hours);
	    		if( useShortHand )
	    			sb.append(timeShortHand.get("h"));
	    		else {
	    			sb.append(" ");
		    		if( hours > 1 )
		        		sb.append(timeLongHand.get("hours"));
		    		else
		        		sb.append(timeLongHand.get("hour"));
	    		}
	    	}    	
	    	seconds -= hours * 3600;
    	}
    	if( mostSignificant != null && mostSignificant.startsWith("h") || !mostSignificantCheck ) {
    		if( sb.length() > 0 )
    			return sb.toString();
    		else
    			mostSignificantCheck=false;
    	}
    	
    	if( seconds >= 60 ) {
    		long minutes = seconds / 60;
	    	if( minutes > 0 ) {
	    		if( sb.length() > 0 ) {
	    			if( !useShortHand )
	    				sb.append(",");
	    			sb.append(" ");
	    		}
	    		sb.append(minutes);
	    		if( useShortHand )
	    			sb.append(timeShortHand.get("m"));
	    		else {
	    			sb.append(" ");
		    		if( minutes > 1 )
		        		sb.append(timeLongHand.get("minutes"));
		    		else
		        		sb.append(timeLongHand.get("minute"));
	    		}
	    	}    	
	    	seconds -= minutes * 60;
    	}
    	if( mostSignificant != null && mostSignificant.startsWith("m") || !mostSignificantCheck ) {
    		if( sb.length() > 0 )
    			return sb.toString();
    		else
    			mostSignificantCheck=false;
    	}
    	
    	if( seconds > 0 ) {
    		if( sb.length() > 0 ) {
    			if( !useShortHand )
    				sb.append(",");
    			sb.append(" ");
    		}
    		sb.append(seconds);
    		if( useShortHand )
    			sb.append(timeShortHand.get("s"));
    		else {
    			sb.append(" ");
	    		if( seconds > 1 )
	        		sb.append(timeLongHand.get("seconds"));
	    		else
	        		sb.append(timeLongHand.get("second"));
    		}
    	}
    	
    	return sb.toString();
    }
}
