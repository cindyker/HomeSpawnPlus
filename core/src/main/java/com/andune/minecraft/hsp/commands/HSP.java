/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
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
 */
/**
 *
 */
package com.andune.minecraft.hsp.commands;

import com.andune.minecraft.commonlib.General;
import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Economy;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Scheduler;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.Initializer;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommandFallThrough;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.integration.Essentials;
import com.andune.minecraft.hsp.integration.dynmap.DynmapModule;
import com.andune.minecraft.hsp.integration.multiverse.MultiverseCore;
import com.andune.minecraft.hsp.integration.multiverse.MultiversePortals;
import com.andune.minecraft.hsp.integration.vault.Vault;
import com.andune.minecraft.hsp.integration.worldborder.WorldBorder;
import com.andune.minecraft.hsp.integration.worldguard.WorldGuard;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.HomeDAO;
import com.andune.minecraft.hsp.strategy.EventType;
import com.andune.minecraft.hsp.strategy.Strategy;
import com.andune.minecraft.hsp.strategy.StrategyConfig;
import com.andune.minecraft.hsp.util.BackupUtil;

import javax.inject.Inject;
import java.util.*;

/**
 * @author andune
 */
@UberCommand(uberCommand = "hsp", subCommand = "",
        help = "HSP Admin Commands")
public class HSP extends BaseCommand implements UberCommandFallThrough {
    @Inject
    private Initializer initializer;
    @Inject
    private MultiverseCore multiverseCore;
    @Inject
    private MultiversePortals multiversePortals;
    @Inject
    private DynmapModule dynmap;
    @Inject
    private WorldBorder worldBorder;
    @Inject
    private WorldGuard worldGuard;
    @Inject
    private Essentials essentials;
    @Inject
    private BackupUtil backupUtil;
    @Inject
    private Scheduler scheduler;
    @Inject
    private General generalUtil;
    @Inject
    private ConfigCore configCore;
    @Inject
    private StrategyConfig strategyConfig;
    @Inject
    private Vault vault;

    private final List<SubCommand> subCommands;
    private final List<String> subCommandNames;
    private final Map<String, String> subCommandAliases;

    public HSP() {
        List<SubCommand> cmds = new ArrayList<SubCommand>(10);
        cmds.add(new ReloadConfig());
        cmds.add(new ReloadEvents());
        cmds.add(new Modules());
        cmds.add(new Dedup());
        cmds.add(new LowerCase());
        cmds.add(new Backup());
        cmds.add(new Restore());
        cmds.add(new Purge());
        cmds.add(new ListEvents());
        cmds.add(new ListStrategies());
        this.subCommands = Collections.unmodifiableList(cmds);

        List<String> names = new ArrayList<String>(10);
        HashMap<String, String> aliases = new HashMap<String, String>(10);
        for (SubCommand cmd : subCommands) {
            names.add(cmd.getName());
            if (cmd.getAliases() != null)
                aliases.put(cmd.getName(), cmd.getAliases()[0]);
        }
        this.subCommandNames = Collections.unmodifiableList(names);
        this.subCommandAliases = Collections.unmodifiableMap(aliases);
    }

    private List<String> getSubCommandNames() {
        return subCommandNames;
    }

    private Map<String, String> getSubCommandAliases() {
        return subCommandAliases;
    }

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_HSP_USAGE);
    }

    public SubCommand findMatchingCommand(String arg) {
        arg = arg.toLowerCase();
        SubCommand cmdRunnable = null;

        for (SubCommand command : subCommands) {
            if (command.getName().equals(arg)) {
                cmdRunnable = command;
                break;
            }

            if (command.getAliases() != null) {
                for (String alias : command.getAliases()) {
                    if (alias.equals(arg)) {
                        cmdRunnable = command;
                        break;
                    }
                }
            }
        }

        return cmdRunnable;
    }

    @Override
    public boolean execute(final CommandSender sender, String cmd, String[] args) {
        if (!permissions.isAdmin(sender))
            return false;

        if (args.length > 0) {
            SubCommand cmdRunnable = findMatchingCommand(args[0]);
            if (cmdRunnable != null) {
                cmdRunnable.setSender(sender);
                cmdRunnable.setArgs(args);
                cmdRunnable.run();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean processUberCommandDryRun(CommandSender sender, String label, String[] args) {
        if (args != null && args.length > 0 && findMatchingCommand(args[0]) != null)
            return true;
        else
            return false;
    }

    @Override
    public String[] getExplicitSubCommandName() {
        return null;
//        return new String[]{"admin", "a"};
    }

    @Override
    public String getExplicitSubCommandHelp() {
        return null;
    }

    private Map<String, String> hspCommandHelp = null;

    @Override
    public Map<String, String> getAdditionalHelp() {
        if (hspCommandHelp != null)
            return hspCommandHelp;

        hspCommandHelp = new HashMap<String, String>();
        List<String> subCommands = getSubCommandNames();
        for (String cmdName : subCommands) {
            String help = server.getLocalizedMessage(HSPMessages.CMD_HSP_UBER_USAGE + "_" + cmdName.toUpperCase());
            if (help != null)
                hspCommandHelp.put(cmdName, help);
            else
                hspCommandHelp.put(cmdName, "(no additional help available)");
        }

        return hspCommandHelp;
    }

    public Map<String, String> getAdditionalHelpAliases() {
        return getSubCommandAliases();
    }

    private abstract class SubCommand implements Runnable {
        protected CommandSender sender = null;
        protected String[] args;

        public abstract String getName();

        public String[] getAliases() {
            return null;
        }

        public void setSender(CommandSender sender) {
            this.sender = sender;
        }

        public void setArgs(String[] args) {
            if( args != null ) {
                this.args = new String[args.length];
                System.arraycopy(args, 0, this.args, 0, args.length);
            }
        }
    }

    private class ReloadConfig extends SubCommand {
        public String getName() {
            return "reloadconfig";
        }

        public String[] getAliases() {
            return new String[]{"rc"};
        }

        public void run() {
            boolean success = false;
            try {
                initializer.initConfigs();

                // also call hookWarmups, in case admin changed the warmup settings
//				plugin.hookWarmups();

                success = true;
            } catch (Exception e) {
                log.error("Caught exception reloading config", e);
                server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_ERROR_RELOADING);
            }

            if (success)
                server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_CONFIG_RELOADED);
        }
    }

    private class ReloadEvents extends SubCommand {
        public String getName() {
            return "reloadevents";
        }

        public String[] getAliases() {
            return new String[]{"re"};
        }

        public void run() {
            boolean success = false;
            try {
                strategyConfig.loadConfig();
                success = true;
            } catch (Exception e) {
                log.error("Caught exception reloading config", e);
                server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_ERROR_RELOADING);
            }

            if (success)
                sender.sendMessage("Successfully reloaded event config");
        }
    }

    private class Modules extends SubCommand {
        private final String enabled = "%green%enabled%default_color%";
        private final String disabled = "%red%disabled%default_color%";
        private final String detectedVersion = ", detected version %blue%";
        private final String detectedNullVersion = ", detected version %red%null";

        public String getName() {
            return "modules";
        }

        public void run() {
            sender.sendMessage("Dynmap module "
                    + (dynmap.isEnabled() ? enabled : disabled)
                    + (dynmap.getVersion() != null ? detectedVersion + dynmap.getVersion() : detectedNullVersion) );

            sender.sendMessage("Multiverse-Core module "
                    + (multiverseCore.isEnabled() ? enabled : disabled)
                    + ((multiverseCore.getVersion() != null && !configCore.isMultiverseEnabled()) ?
                      " %blue%[config: core.isMultiVerseEnabled]%default_color%" : "")
                    + (multiverseCore.getVersion() != null ? detectedVersion + multiverseCore.getVersion() : detectedNullVersion) );
            sender.sendMessage("Multiverse-Portals module "
                    + (multiversePortals.isEnabled() ? enabled : disabled)
                    + ((multiversePortals.getVersion() != null && !configCore.isMultiverseEnabled()) ?
                    " %blue%[config: core.isMultiVerseEnabled]%default_color%" : "")
                    + (multiversePortals.getVersion() != null ? detectedVersion + multiversePortals.getVersion() : detectedNullVersion) );

            sender.sendMessage("WorldBorder module "
                    + (worldBorder.isEnabled() ? enabled : disabled)
                    + (worldBorder.getVersion() != null ? detectedVersion + worldBorder.getVersion() : detectedNullVersion) );

            sender.sendMessage("WorldGuard module "
                    + (worldGuard.isEnabled() ? enabled : disabled)
                    + (worldGuard.getVersion() != null ? detectedVersion + worldGuard.getVersion() : detectedNullVersion) );

            sender.sendMessage("Essentials module "
                    + (essentials.isEnabled() ? enabled : disabled)
                    + (essentials.getVersion() != null ? detectedVersion + essentials.getVersion() : detectedNullVersion) );

            sender.sendMessage("Vault module "
                    + (vault.isEnabled() ? enabled : disabled)
                    + (vault.getVersion() != null ? detectedVersion + vault.getVersion() : detectedNullVersion) );
        }
    }

    // admin command to clean up any playerName-case-caused dups
    private class Dedup extends SubCommand {
        public String getName() {
            return "dedup";
        }

        public String[] getAliases() {
            return new String[]{"dd"};
        }

        public void run() {
            sender.sendMessage("Starting async HSP database home playerName dup cleanup");
            scheduler.scheduleAsyncDelayedTask(new DeDupDatabaseRunner(sender), 0);
        }
    }

    // admin command to switch all database player names to lowercase
    private class LowerCase extends SubCommand {
        public String getName() {
            return "lowercase";
        }

        public String[] getAliases() {
            return new String[]{"lc"};
        }

        public void run() {
            sender.sendMessage("Starting async HSP database playerName-to-lowercase conversion");
            scheduler.scheduleAsyncDelayedTask(new LowerCaseDatabaseRunner(sender), 0);
        }
    }

    private class Backup extends SubCommand {
        public String getName() {
            return "backup";
        }

        public void run() {
            String errorMessage = backupUtil.backup();
            if (errorMessage == null) {
                server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_DATA_BACKED_UP,
                        "file", backupUtil.getBackupFile());
            } else {
                sender.sendMessage(errorMessage);
            }
        }
    }

    private class ListEvents extends SubCommand {
        public String getName() {
            return "listEvents";
        }

        public String[] getAliases() {
            return new String[]{"le"};
        }

        public void run() {
            sender.sendMessage("HSP event types:");
            for(EventType type : EventType.values()) {
                sender.sendMessage("  "+type);
            }
        }
    }

    private class ListStrategies extends SubCommand {
        public String getName() {
            return "listStrategies";
        }

        public String[] getAliases() {
            return new String[]{"ls"};
        }

        public void run() {
            if (args.length < 2) {
                server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_LIST_STRATEGIES_REQUIRES_ARG);
            } else {
                final String eventName = args[1].toLowerCase();
                Set<Strategy> strategies = strategyConfig.getDefaultStrategies(eventName);
                if (strategies != null) {
                    sender.sendMessage("Strategies for event %green%"+eventName);
                    for(Strategy strategy : strategies) {
                        sender.sendMessage("  - "+strategy.getStrategyConfigName());
                    }
                }

                String worldName = null;
                if( args.length > 2 )
                    worldName = args[2];
                else if( sender instanceof Player ) {
                    Player p = (Player) sender;
                    worldName = p.getWorld().getName();
                }

                if( worldName != null ) {
                    strategies = strategyConfig.getWorldStrategies(eventName, worldName);
                    if (strategies != null) {
                        sender.sendMessage("Strategies for event %green%"+eventName+"%default_color%, world %red%"+worldName);
                        for(Strategy strategy : strategies) {
                            sender.sendMessage("  - "+strategy.getStrategyConfigName());
                        }
                    }
                }
            }
        }
    }

    private class Restore extends SubCommand {
        public String getName() {
            return "restore";
        }

        public void run() {
            if (args.length < 2 || (!"OVERWRITE".equals(args[1]))) {
                server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_DATA_RESTORE_USAGE,
                        "file", backupUtil.getBackupFile());
            } else {
                String errorMessage = backupUtil.restore();
                if (errorMessage == null) {
                    server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_DATA_RESTORE_SUCCESS,
                            "file", backupUtil.getBackupFile());
                } else {
                    sender.sendMessage(errorMessage);
                }
            }
        }
    }

    private class Purge extends SubCommand {
        public String getName() {
            return "purge";
        }

        public void run() {
            if (args.length < 3) {
                server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_PURGE_WRONG_ARGUMENTS);
            } else if (args.length < 2 || (!args[1].equalsIgnoreCase("player") && !args[1].equalsIgnoreCase("world"))) {
                server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_PURGE_WRONG_ARGUMENTS);
            } else {
                if (args[1].equalsIgnoreCase("player")) {
                    final long millis = generalUtil.parseTimeInput(args[2]);
                    server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_PURGE_PLAYER_TIME,
                            "time", generalUtil.displayTimeString(millis, false, "d"));

                    if (args.length < 4 || !args[3].equals("CONFIRM")) {
                        server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_PURGE_REQUIRES_CONFIRM);
                    }
                    // DO IT
                    else {
                        server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_PURGE_STARTING_ASYNC);
                        final long purgeTime = System.currentTimeMillis() - millis;
                        scheduler.scheduleAsyncDelayedTask(new Runnable() {
                            public void run() {
                                int purged = storage.purgePlayerData(purgeTime);
                                String msg = server.getLocalizedMessage(HSPMessages.CMD_HSP_PURGE_RESULTS,
                                        "count", purged);
                                sender.sendMessage(msg);
                                log.info(msg);
                            }
                        }, 1);
                    }
                } else if (args[1].equalsIgnoreCase("world")) {
                    final String worldName = args[2];
                    server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_PURGE_PLAYER_WORLD,
                            "world", worldName);

                    if (args.length < 4 || !args[3].equals("CONFIRM")) {
                        server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_PURGE_REQUIRES_CONFIRM);
                    }
                    // DO IT
                    else {
                        server.sendLocalizedMessage(sender, HSPMessages.CMD_HSP_PURGE_STARTING_ASYNC);
                        scheduler.scheduleAsyncDelayedTask(new Runnable() {
                            public void run() {
                                int purged = storage.purgeWorldData(worldName);
                                String msg = server.getLocalizedMessage(HSPMessages.CMD_HSP_PURGE_RESULTS,
                                        "count", purged);
                                sender.sendMessage(msg);
                                log.info(msg);
                            }
                        }, 1);
                    }
                }
            }
        }
    }
    
    /*
     * ASYNC runners below here, these are not SubCommands, but rather are used
	 * by the subCommands to do certain things asynchronously.
	 */

    private class DeDupDatabaseRunner implements Runnable {
        private CommandSender sender;

        public DeDupDatabaseRunner(CommandSender sender) {
            this.sender = sender;
        }

        public void run() {
            log.debug("DeDupDatabaseRunner running");
            int dupsCleaned = 0;
            try {
                storage.setDeferredWrites(true);
                final HomeDAO homeDAO = storage.getHomeDAO();

                // we fix dups by player, so we can keep the most recent home in the
                // event of duplicates. So we keep track of player homes we have fixed
                // so we can skip any others we come across as we're iterating the
                // allHomes hash.
                HashSet<String> playersFixed = new HashSet<String>(100);

                Set<? extends com.andune.minecraft.hsp.entity.Home> allHomes = homeDAO.findAllHomes();
                for (com.andune.minecraft.hsp.entity.Home home : allHomes) {
                    final String lcPlayerName = home.getPlayerName().toLowerCase();
                    if (playersFixed.contains(lcPlayerName))
                        continue;
                    log.debug("home check for home name {}", lcPlayerName);

                    final HashMap<String, com.andune.minecraft.hsp.entity.Home> dupCheck = new HashMap<String, com.andune.minecraft.hsp.entity.Home>();
                    Set<? extends com.andune.minecraft.hsp.entity.Home> playerHomes = homeDAO.findHomesByPlayer(lcPlayerName);
                    // look for duplicates and delete all but the newest if we find any
                    for (com.andune.minecraft.hsp.entity.Home playerHome : playerHomes) {
                        final String homeName = playerHome.getName();
                        log.debug("dup check for home name \"{}\"", homeName);
                        // ignore no-name homes, they don't have to be unique
                        if (homeName == null)
                            continue;

                        // have we seen this home before?
                        com.andune.minecraft.hsp.entity.Home dup = dupCheck.get(homeName);
                        if (dup != null) {
                            log.debug("found dup for home {}", homeName);
                            // determine which one is oldest and delete the oldest one
                            if (dup.getLastModified().getTime() < playerHome.getLastModified().getTime()) {
                                // dup is oldest, delete it
                                log.info("Deleting oldest duplicate home (id " + dup.getId() + ", name " + dup.getName() + ") for player " + lcPlayerName);
                                homeDAO.deleteHome(dup);
                                dupCheck.put(homeName, playerHome); // record new record in our dup hash
                            } else {
                                // playerHome is oldest, delete it
                                log.info("Deleting oldest duplicate home (id " + playerHome.getId() + ", name " + playerHome.getName() + ") for player " + lcPlayerName);
                                homeDAO.deleteHome(playerHome);
                            }
                            dupsCleaned++;
                        } else {
                            log.debug("no dup found for home {}", homeName);
                            dupCheck.put(homeName, playerHome); // we have now, record it
                        }
                    }

                    playersFixed.add(lcPlayerName);
                }

                storage.flushAll();
            } catch (StorageException e) {
                log.error("Caught exception processing /hsp dedup", e);
            } finally {
                storage.setDeferredWrites(false);
            }

            sender.sendMessage("Database playerName dups have been cleaned up. " + dupsCleaned + " total dups found and cleaned");
        }
    }

    private class LowerCaseDatabaseRunner implements Runnable {
        private CommandSender sender;

        public LowerCaseDatabaseRunner(CommandSender sender) {
            this.sender = sender;
        }

        public void run() {
            log.debug("LowerCaseDatabaseRunner running");
            int conversions = 0;
            try {
                storage.setDeferredWrites(true);
                final HomeDAO homeDAO = storage.getHomeDAO();

                // we fix names by player, so we can keep the most recent home in the
                // event of duplicates. So we keep track of player homes we have fixed
                // so we can skip any others we come across as we're iterating the
                // allHomes hash.
                HashSet<String> playersFixed = new HashSet<String>(100);

                Set<? extends com.andune.minecraft.hsp.entity.Home> allHomes = homeDAO.findAllHomes();
                for (com.andune.minecraft.hsp.entity.Home home : allHomes) {
                    final String lcPlayerName = home.getPlayerName().toLowerCase();
                    if (playersFixed.contains(lcPlayerName))
                        continue;
                    log.debug("home check for home name {}", lcPlayerName);

                    final Set<? extends com.andune.minecraft.hsp.entity.Home> playerHomes = homeDAO.findHomesByPlayer(lcPlayerName);
                    for (com.andune.minecraft.hsp.entity.Home playerHome : playerHomes) {
                        // set home playerName to lower case if it's not already
                        if (!lcPlayerName.equals(playerHome.getPlayerName())) {
                            log.info("Fixing playerName to lowerCase for home id " + playerHome.getId() + ", home name " + playerHome.getName() + " for player " + lcPlayerName);
                            playerHome.setPlayerName(lcPlayerName);
                            homeDAO.saveHome(playerHome);
                            conversions++;
                        }
                    }

                    playersFixed.add(lcPlayerName);
                }

                storage.flushAll();
            } catch (StorageException e) {
                log.error("Caught exception processing /hsp lc conversion", e);
            } finally {
                storage.setDeferredWrites(false);
            }

            sender.sendMessage("Database playerNames converted to lowerCase complete. Processed " + conversions + " conversions");
        }
    }
}
