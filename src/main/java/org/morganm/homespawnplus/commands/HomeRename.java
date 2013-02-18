/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.Set;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.StorageException;

/**
 * @author andune
 *
 */
public class HomeRename extends BaseCommand {
    @Override
    public String[] getCommandAliases() { return new String[] {"homer", "renamehome"}; }
    
    @Override
    public String getUsage() {
        return  util.getLocalizedMessage(HSPMessages.CMD_HOMERENAME_USAGE);
    }

    @Override
    public boolean execute(Player p, Command command, String[] args) {
        if( !defaultCommandChecks(p) )
            return true;
        
        org.morganm.homespawnplus.entity.Home home = null;
        String homeName = null;
        
        if( args.length < 2 ) {
            return false;   // print usage
        }
        homeName = args[0];
        String newName = args[1];

        int id = -1;
        try {
            id = Integer.parseInt(homeName);
        }
        catch(NumberFormatException e) {}

        if( id != -1 ) {
            home = plugin.getStorage().getHomeDAO().findHomeById(id);
            // make sure it belongs to this player
            if( home != null && !p.getName().equals(home.getPlayerName()) )
                home = null;

            // otherwise set the name according to the home that was selected
            if( home != null )
                homeName = home.getName() + " (id #"+id+")";
        }
        else if( homeName.equals("<noname>") ) {
            Set<org.morganm.homespawnplus.entity.Home> homes = plugin.getStorage()
                    .getHomeDAO().findHomesByWorldAndPlayer(p.getWorld().getName(), p.getName());
            if( homes != null ) {
                for(org.morganm.homespawnplus.entity.Home h : homes) {
                    if( h.getName() == null ) {
                        home = h;
                        break;
                    }
                }
            }
        }

        // if home is still null here, then just do a regular lookup
        if( home == null )
            home = util.getHomeByName(p.getName(), homeName);
        
        if( home != null ) {
            // safety check to be sure we aren't renaming someone else's home with this command
            // (this shouldn't be possible since all checks are keyed to this player's name, but
            // let's be paranoid anyway)
            if( !p.getName().equals(home.getPlayerName()) ) {
                util.sendLocalizedMessage(p, HSPMessages.CMD_HOMERENAME_ERROR_RENAMING_OTHER_HOME);
                log.warning(logPrefix + " ERROR: Shouldn't be possible! Player "+p.getName()+" tried to rename home for player "+home.getPlayerName());
            }
            else {
                if( home.isBedHome() || home.isDefaultHome() ) {
                    util.sendLocalizedMessage(p, HSPMessages.CMD_HOMERENAME_NAMED_HOMES_ONLY);
                    return true;
                }
                
                try {
                    home.setName(newName);
                    plugin.getStorage().getHomeDAO().saveHome(home);
                    util.sendLocalizedMessage(p, HSPMessages.CMD_HOMERENAME_HOME_RENAMED,
                            "oldName", homeName, "newName", newName);
                }
                catch(StorageException e) {
                    util.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
                    log.log(Level.WARNING, "Error caught in /"+getCommandName()+": "+e.getMessage(), e);
                }
            }
        }
        else {
            util.sendLocalizedMessage(p, HSPMessages.NO_NAMED_HOME_FOUND, "name", homeName);
        }
        
        return true;
    }
}
