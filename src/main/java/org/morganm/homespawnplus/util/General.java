/**
 * 
 */
package org.morganm.homespawnplus.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

/**
 * @author morganm
 *
 */
public final class General {
	// class version: 2
	
	private static BlockFace[] directions = new BlockFace[] {
		BlockFace.UP,
		BlockFace.NORTH,
		BlockFace.WEST,
		BlockFace.SOUTH,
		BlockFace.EAST,
		BlockFace.DOWN
	};
	private static General instance;
	
	private General() {}
	
	public static General getInstance() {
		if( instance == null )
			instance = new General();
		return instance;
	}
	
	/** Recursively look for 2 vertical safe air spots nearest the given location.
	 * 
	 *  TODO: ensure safety by also checking for lava underneath
	 * 
	 * @param base
	 */
	private Location findSafeLocation(final Set<Location> alreadyTraversed, final int level, final Location location) {
		Block base = location.getBlock();
		Block up = base.getRelative(BlockFace.UP);
		
		if( base.getTypeId() == 0 && up.getTypeId() == 0 )
			return location;
		else {
			// first try all the closest blocks before recursing further
			for(int i=0; i < directions.length; i++) {
				Block tryBlock = base.getRelative(directions[i]);
				Location tryLocation = tryBlock.getLocation();
				if( alreadyTraversed.contains(tryLocation) ) {
					continue;
				}
				alreadyTraversed.add(tryLocation);
				up = tryBlock.getRelative(BlockFace.UP);
				
				if( tryBlock.getTypeId() == 0 && up.getTypeId() == 0 )
					return location;
			}
			
			// we only recurse so far before we give up
			if( level > 10 )
				return null;
			
			// if we're here, none of them were safe, now recurse
			for(int i=0; i < directions.length; i++) {
				Location recurseLocation = base.getRelative(directions[i]).getLocation();
				if( alreadyTraversed.contains(recurseLocation) )
					continue;
				Location result = findSafeLocation(alreadyTraversed, level+1, recurseLocation);
				if( result != null )
					return result;
			}
		}
		
		return null;
	}
	
	/** Safely teleport a player to a location. Should avoid them being stuck in blocks,
	 * teleported over lava, etc.  (not fully implemented)
	 * 
	 * @param p
	 * @param l
	 */
	public void safeTeleport(final Player p, final Location l, final TeleportCause cause) {
		Location target = findSafeLocation(new HashSet<Location>(10), 0, l);

		// if we didn't find a safe location, then just teleport them to the original location
		if( target == null )
			target = l;
		
		p.teleport(target, cause);
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
    		int multiplier = 1000;	// milliseconds multiplier
    		int index = -1;
    		
    		if( (index = args[i].indexOf("w")) != -1 ) {		// week
    			multiplier *= 86400 * 7;
    		}
    		else if( (index = args[i].indexOf("d")) != -1 ) {		// day
    			multiplier *= 86400;
    		}
    		else if( (index = args[i].indexOf("h")) != -1 ) {		// hours
    			multiplier *= 3600;
    		}
    		else if( (index = args[i].indexOf("m")) != -1 ) {		// hours
    			multiplier *= 60;
    		}
    		
			String value = args[i].substring(0, index);
			int v = Integer.valueOf(value);
			time += v * multiplier;
    	}
    	
    	return time;
    }

    /** Given milliseconds as input, this will return a string that represents
     * that time format.
     * 
     * @param millis
     * @param useShortHand set to true to use shorthand notation. shorthand will return a string
     * of the form "4d3h2m" whereas this set to false would return "4 days 3 hours 2 minutes"
     * @param mostSignificantOnly if true, only the most significant time is returned. For example,
     * when false this might return "4 days 3 hours 2 minutes", when true it would return "4 days"
     * @return
     * @throws NumberFormatException
     */
    public String displayTimeString(long millis, boolean useShortHand, boolean mostSignificantOnly) throws NumberFormatException {
    	final StringBuffer sb = new StringBuffer();
    	millis /= 1000;		// chop down to seconds
    	
    	long remainder = millis % (86400 * 7);
    	if( remainder > 0 ) {
    		sb.append(remainder);
    		if( useShortHand )
    			sb.append("w");
    		else {
	    		sb.append(" week");
	    		if( remainder > 1 )
	        		sb.append("s");
    		}
    	}
    	millis -= remainder * (86400 * 7);
    	
    	if( mostSignificantOnly && sb.length() > 0 )
    		return sb.toString();
    	
    	remainder = millis % 86400;
    	if( remainder > 0 ) {
    		if( !useShortHand && sb.length() > 0 )
    			sb.append(" ");
    		sb.append(remainder);
    		if( useShortHand )
    			sb.append("d");
    		else {
	    		sb.append(" day");
	    		if( remainder > 1 )
	        		sb.append("s");
    		}
    	}
    	millis -= remainder * 86400;
    	
    	if( mostSignificantOnly && sb.length() > 0 )
    		return sb.toString();
    	
    	remainder = millis % 3600;
    	if( remainder > 0 ) {
    		if( !useShortHand && sb.length() > 0 )
    			sb.append(" ");
    		sb.append(remainder);
    		if( useShortHand )
    			sb.append("h");
    		else {
	    		sb.append(" hour");
	    		if( remainder > 1 )
	        		sb.append("s");
    		}
    	}    	
    	millis -= remainder * 3600;
    	
    	if( mostSignificantOnly && sb.length() > 0 )
    		return sb.toString();
    	
    	remainder = millis % 60;
    	if( remainder > 0 ) {
    		if( !useShortHand && sb.length() > 0 )
    			sb.append(" ");
    		sb.append(remainder);
    		if( useShortHand )
    			sb.append("m");
    		else {
	    		sb.append(" minute");
	    		if( remainder > 1 )
	        		sb.append("s");
    		}
    	}    	
    	millis -= remainder * 60;
    	
    	if( mostSignificantOnly && sb.length() > 0 )
    		return sb.toString();
    	
    	if( remainder > 0 ) {
    		if( !useShortHand && sb.length() > 0 )
    			sb.append(" ");
    		sb.append(remainder);
    		if( useShortHand )
    			sb.append("s");
    		else {
	    		sb.append(" second");
	    		if( remainder > 1 )
	        		sb.append("s");
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
