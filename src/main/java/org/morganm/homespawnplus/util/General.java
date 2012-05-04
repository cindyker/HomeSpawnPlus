/**
 * 
 */
package org.morganm.homespawnplus.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.morganm.homespawnplus.i18n.Locale;

/**
 * @author morganm
 *
 */
public final class General {
	// class version: 3
	
	private static BlockFace[] directions = new BlockFace[] {
		BlockFace.UP,
		BlockFace.NORTH,
		BlockFace.WEST,
		BlockFace.SOUTH,
		BlockFace.EAST,
		BlockFace.DOWN
	};
	private static General instance;
	
	private Logger log = Logger.getLogger(General.class.toString());
	private String logPrefix = "";
	private Locale locale;
	private final Map<String, String> timeLongHand = new HashMap<String, String>();
	private final Map<String, String> timeShortHand = new HashMap<String, String>();
	
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
	
	private boolean isSafeBlock(Block b) {
		final Block up = b.getRelative(BlockFace.UP);
		final Block down = b.getRelative(BlockFace.DOWN);
		if( b.getTypeId() == 0 && up.getTypeId() == 0
				&& (down.getTypeId() != 10 && down.getTypeId() != 11)	// no lava underneath
				&& (down.getTypeId() != 0)								// no air underneath
		){
			return true;
		}
		else
			return false;
		
	}
	
	/** Recursively look for 2 vertical safe air spots nearest the given location.
	 * 
	 * @param base
	 */
	private Location findSafeLocation(final Set<Location> alreadyTraversed, final int level, final Location location) {
		if( location == null )
			return null;
		final Block base = location.getBlock();
		
		if( isSafeBlock(base) )
			return location;
		else {
			// first try all the closest blocks before recursing further
			for(int i=0; i < directions.length; i++) {
				Block tryBlock = base.getRelative(directions[i]);
				Location tryLocation = tryBlock.getLocation();
				if( alreadyTraversed.contains(tryLocation) ) {
					continue;
				}
				
				if( isSafeBlock(tryBlock) ) {
					return tryLocation;
				}
			}
			
			// we only recurse so far before we give up
			if( level > 10 )
				return null;
			
			// if we're here, none of them were safe, now recurse
			for(int i=0; i < directions.length; i++) {
				Location recurseLocation = base.getRelative(directions[i]).getLocation();
				if( alreadyTraversed.contains(recurseLocation) )
					continue;
				alreadyTraversed.add(recurseLocation);
				
				Location result = findSafeLocation(alreadyTraversed, level+1, recurseLocation);
				if( result != null )
					return result;
			}
		}
		
		return null;
	}
	
	/** Safely teleport a player to a location. Should avoid them being stuck in blocks,
	 * teleported over lava, etc.
	 * 
	 * @param p
	 * @param l
	 */
	public void safeTeleport(final Player p, final Location l, final TeleportCause cause) {
		p.teleport(safeLocation(l), cause);
	}
	
	/** Given a location, find the nearest "safe" location, ie. that won't suffocate a
	 * player, spawn them over lava, etc.
	 * 
	 * @param l
	 */
	public Location safeLocation(final Location l) {
		Location target = findSafeLocation(new HashSet<Location>(10), 0, l);
		
		// if we didn't find a safe location, then just use the original location
		if( target == null ) {
			log.info(logPrefix+" safeLocation: couldn't find nearby safe location, using original location "+l);
			target = l;
		}
		else {
			// preserve pitch/yaw
			target.setPitch(l.getPitch());
			target.setYaw(l.getYaw());
		}
		
		return target;
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
    	if( mostSignificant != null && mostSignificant.startsWith("mo") )
    		return sb.toString();

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
    	if( mostSignificant != null && mostSignificant.startsWith("w") )
    		return sb.toString();
    	
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
    	if( mostSignificant != null && mostSignificant.startsWith("d") )
    		return sb.toString();
    	
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
    	if( mostSignificant != null && mostSignificant.startsWith("h") )
    		return sb.toString();
    	
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
    	if( mostSignificant != null && mostSignificant.startsWith("m") )
    		return sb.toString();
    	
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

	/** Code borrowed from @Diddiz's LogBlock
	 * 
	 * @param items1
	 * @param items2
	 * @return
	 */
	public ItemStack[] compareInventories(ItemStack[] items1, ItemStack[] items2) {
		final ItemStackComparator comperator = new ItemStackComparator();
		final ArrayList<ItemStack> diff = new ArrayList<ItemStack>();
		final int l1 = items1.length, l2 = items2.length;
		int c1 = 0, c2 = 0;
		while (c1 < l1 || c2 < l2) {
			if (c1 >= l1) {
				diff.add(items2[c2]);
				c2++;
				continue;
			}
			if (c2 >= l2) {
				items1[c1].setAmount(items1[c1].getAmount() * -1);
				diff.add(items1[c1]);
				c1++;
				continue;
			}
			final int comp = comperator.compare(items1[c1], items2[c2]);
			if (comp < 0) {
				items1[c1].setAmount(items1[c1].getAmount() * -1);
				diff.add(items1[c1]);
				c1++;
			} else if (comp > 0) {
				diff.add(items2[c2]);
				c2++;
			} else {
				final int amount = items2[c2].getAmount() - items1[c1].getAmount();
				if (amount != 0) {
					items1[c1].setAmount(amount);
					diff.add(items1[c1]);
				}
				c1++;
				c2++;
			}
		}
		return diff.toArray(new ItemStack[diff.size()]);
	}

	/** Code borrowed from @Diddiz's LogBlock
	 * 
	 * @param items
	 * @return
	 */
	public ItemStack[] compressInventory(ItemStack[] items) {
		final ArrayList<ItemStack> compressed = new ArrayList<ItemStack>();
		for (final ItemStack item : items)
			if (item != null) {
				final int type = item.getTypeId();
				final byte data = rawData(item);
				boolean found = false;
				for (final ItemStack item2 : compressed)
					if (type == item2.getTypeId() && data == rawData(item2)) {
						item2.setAmount(item2.getAmount() + item.getAmount());
						found = true;
						break;
					}
				if (!found)
					compressed.add(new ItemStack(type, item.getAmount(), (short)0, data));
			}
		Collections.sort(compressed, new ItemStackComparator());
		return compressed.toArray(new ItemStack[compressed.size()]);
	}

	/** Code borrowed from @Diddiz's LogBlock 
	 * 
	 * @param item
	 * @return
	 */
	public byte rawData(ItemStack item) {
		return item.getType() != null ? item.getData() != null ? item.getData().getData() : 0 : 0;
	}
	
	/** Code borrowed from @Diddiz's LogBlock 
	 * 
	 * @param item
	 * @return
	 */
	public class ItemStackComparator implements Comparator<ItemStack>
	{
		@Override
		public int compare(ItemStack a, ItemStack b) {
			final int aType = a.getTypeId(), bType = b.getTypeId();
			if (aType < bType)
				return -1;
			if (aType > bType)
				return 1;
			final byte aData = rawData(a), bData = rawData(b);
			if (aData < bData)
				return -1;
			if (aData > bData)
				return 1;
			return 0;
		}
	}
}
