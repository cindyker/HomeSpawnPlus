/**
 * 
 */
package org.morganm.homespawnplus.commands;

import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.morganm.homespawnplus.command.BaseCommand;
import org.morganm.homespawnplus.i18n.HSPMessages;
import org.morganm.homespawnplus.storage.StorageException;
import org.morganm.homespawnplus.storage.dao.SpawnDAO;

/**
 * @author andune
 *
 */
public class SpawnRename extends BaseCommand {
    @Override
    public String[] getCommandAliases() { return new String[] {"spawnr", "renamespawn"}; }
    
    @Override
    public String getUsage() {
        return  util.getLocalizedMessage(HSPMessages.CMD_SPAWNRENAME_USAGE);
    }

    @Override
    public boolean execute(Player p, Command command, String[] args) {
        if( !defaultCommandChecks(p) )
            return true;
 
        org.morganm.homespawnplus.entity.Spawn spawn = null;
        
        if( args.length < 2 ) {
            return false;
        }
        String newName = args[1];

        final SpawnDAO dao = plugin.getStorage().getSpawnDAO();
        
        // try search by ID number
        int id = -1;
        try {
            id = Integer.parseInt(args[0]);
        }
        catch(NumberFormatException e) {}
        if( id != -1 )
            spawn = dao.findSpawnById(id);
        
        // if argument was not a number or not found, then search by name
        if( spawn == null )
            spawn = dao.findSpawnByName(args[0]);
        
        if( spawn == null ) {
            util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNDELETE_NO_SPAWN_FOUND,
                    "name", args[0]);
            return true;
        }

        try {
            String oldName = spawn.getName();
            if( oldName == null ) {
                oldName = "id #"+spawn.getId();
            }
            
            spawn.setName(newName);
            dao.saveSpawn(spawn);
            
            util.sendLocalizedMessage(p, HSPMessages.CMD_SPAWNRENAME_SPAWN_RENAMED,
                    "oldName", oldName, "newName", newName);
        }
        catch(StorageException e) {
            util.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
            log.log(Level.WARNING, "Error caught in /"+getCommandName()+": "+e.getMessage(), e);
        }
        return true;
    }
}
