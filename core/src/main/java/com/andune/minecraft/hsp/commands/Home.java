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

import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Teleport;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.command.BaseCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommand;
import com.andune.minecraft.hsp.commands.uber.UberCommandFallThrough;
import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.manager.WarmupRunner;
import com.andune.minecraft.hsp.strategy.EventType;
import com.andune.minecraft.hsp.strategy.StrategyContext;
import com.andune.minecraft.hsp.strategy.StrategyEngine;
import com.andune.minecraft.hsp.strategy.StrategyResult;
import com.andune.minecraft.hsp.util.HomeUtil;

import javax.inject.Inject;
import java.util.Map;


/**
 * @author andune
 */
@UberCommand(uberCommand = "home", subCommand = "", help = "Teleport to your home")
public class Home extends BaseCommand implements UberCommandFallThrough {
    @Inject
    private StrategyEngine engine;
    @Inject
    private ConfigCore configCore;
    @Inject
    private Teleport teleport;
    @Inject
    private HomeUtil homeUtil;

    @Override
    public String getUsage() {
        return server.getLocalizedMessage(HSPMessages.CMD_HOME_USAGE);
    }

    @Override
    public boolean execute(final Player p, String[] args) {
        return privateExecute(p, args, false);
    }

    private boolean privateExecute(final Player p, String[] args, boolean dryRun) {
        log.debug("/home command called player={}, args={}, dryRun={}", p, args, dryRun);

        // this flag is used to determine whether the player influenced the outcome of /home
        // with an arg or whether it was purely determined by the default home strategy, so
        // that we know whether the OTHER_WORLD_PERMISSION perm needs to be checked
        boolean playerDirectedArg = false;

        final String warmupName = getWarmupName(null);
        String cooldownName = null;
        com.andune.minecraft.hsp.entity.Home theHome = null;

        StrategyResult result = null;
        Location l = null;
        if (args.length > 0) {
            playerDirectedArg = true;
            String homeName = null;

            if (args[0].startsWith("w:")) {
                if (!permissions.hasHomeOtherWorld(p)) {
                    sendLocalizedMessage(p, dryRun, HSPMessages.CMD_HOME_NO_OTHERWORLD_PERMISSION);
                    return !dryRun;
                }

                String worldName = args[0].substring(2);
                theHome = homeUtil.getDefaultHome(p.getName(), worldName);
                if (theHome == null) {
                    sendLocalizedMessage(p, dryRun, HSPMessages.CMD_HOME_NO_HOME_ON_WORLD, "world", worldName);
                    return !dryRun;
                }
            } else {
                if (!permissions.hasHomeNamed(p)) {
                    sendLocalizedMessage(p, dryRun, HSPMessages.CMD_HOME_NO_NAMED_HOME_PERMISSION);
                    return !dryRun;
                }

                result = engine.getStrategyResult(EventType.NAMED_HOME_COMMAND, p, args[0]);
                if (result != null) {
                    theHome = result.getHome();
                    l = result.getLocation();
                }
            }

            // no location yet but we have a Home object? grab it from there
            if (l == null && theHome != null) {
                l = theHome.getLocation();
                homeName = theHome.getName();
            } else
                homeName = args[0];

            cooldownName = getCooldownName("home-named", homeName);

            if (l == null) {
                sendLocalizedMessage(p, dryRun, HSPMessages.CMD_HOME_NO_NAMED_HOME_FOUND, "name", homeName);
                return !dryRun;
            }
        } else {
            result = engine.getStrategyResult(EventType.HOME_COMMAND, p);
            theHome = result.getHome();
            l = result.getLocation();
        }

		/*
		 * The return value here is explicitly not affected by dryRun, meaning
		 * if an uberCommand succeeds except for cooldown, then we will display
		 * the cooldown.
		 */
        log.debug("home command running cooldown check, cooldownName={}", cooldownName);
        if (!cooldownCheck(p, cooldownName, !dryRun))
            return true;

        final StrategyContext context;
        if (result != null)
            context = result.getContext();
        else
            context = null;

        if (l != null) {
            // make sure it's on the same world, or if not, that we have
            // cross-world home perms. We only evaluate this check if the
            // player gave input for another world; admin-directed strategies
            // always allow cross-world locations regardless of permissions.
            if (playerDirectedArg && !p.getWorld().getName().equals(l.getWorld().getName()) &&
                    !permissions.hasHomeOtherWorld(p)) {
                sendLocalizedMessage(p, dryRun, HSPMessages.CMD_HOME_NO_OTHERWORLD_PERMISSION);
                return !dryRun;
            }

            // if we get to here, the dryRun has succeeded.
            if (dryRun) {
                return true;
            }

            if (hasWarmup(p, warmupName)) {
                final Location finalL = l;
                final com.andune.minecraft.hsp.entity.Home finalHome = theHome;
                final boolean finalIsNamedHome = playerDirectedArg;
                doWarmup(p, new WarmupRunner() {
                    private boolean canceled = false;
                    private String cdName;
                    private String wuName;

                    public void run() {
                        if (!canceled) {
                            p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_WARMUP_FINISHED,
                                    "name", getWarmupName(), "place", "home"));
                            doHomeTeleport(p, finalL, cdName, context,
                                    finalHome, finalIsNamedHome);
                        }
                    }

                    public void cancel() {
                        canceled = true;
                    }

                    public void setPlayerName(String playerName) {
                    }

                    public void setWarmupId(int warmupId) {
                    }

                    public WarmupRunner setCooldownName(String cd) {
                        cdName = cd;
                        return this;
                    }

                    public WarmupRunner setWarmupName(String warmupName) {
                        wuName = warmupName;
                        return this;
                    }

                    public String getWarmupName() {
                        return wuName;
                    }
                }.setCooldownName(cooldownName).setWarmupName(warmupName));
            } else {
                doHomeTeleport(p, l, cooldownName, context, theHome, playerDirectedArg);
            }
        } else {
            sendLocalizedMessage(p, dryRun, HSPMessages.NO_HOME_FOUND);
            return !dryRun;
        }

        return true;
    }

    /**
     * Do a teleport to the home including costs, cooldowns and printing
     * departure and arrival messages. Is used from both warmups and sync /home.
     *
     * @param p
     * @param l
     */
    private void doHomeTeleport(Player p, Location l, String cooldownName,
                                StrategyContext context, com.andune.minecraft.hsp.entity.Home home,
                                boolean isNamedHome) {
        String homeName = null;
        if (home != null)
            homeName = home.getName();

        if (applyCost(p, true, cooldownName)) {
            if (configCore.isTeleportMessages()) {
                if (home != null && home.isBedHome())
                    p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_HOME_BED_TELEPORTING,
                            "home", homeName));
                else if (isNamedHome)
                    p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_HOME_NAMED_TELEPORTING,
                            "home", homeName));
                else
                    p.sendMessage(server.getLocalizedMessage(HSPMessages.CMD_HOME_TELEPORTING,
                            "home", homeName));
            }

            teleport.teleport(p, l, (context != null ? context.getTeleportOptions() : null));
        }
    }

    private void sendLocalizedMessage(Player p, boolean dryRun, HSPMessages key, Object... args) {
        if (!dryRun) {
            server.sendLocalizedMessage(p, key, args);
        }
    }

    private String getWarmupName(String homeName) {
        return getCommandName();

		/* warmup per home doesn't even make sense. Silly.
		 * 
		if( homeName != null && plugin.getHSPConfig().getBoolean(ConfigOptions.WARMUP_PER_HOME, false) )
			return getCommandName() + "." + homeName;
		else
			return getCommandName();
			*/
    }

    @Override
    public boolean processUberCommandDryRun(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            return privateExecute(((Player) sender), args, true);
        } else
            return false;
    }

    @Override
    public String[] getExplicitSubCommandName() {
        return new String[]{"teleport", "tp", "go"};
    }

    @Override
    public String getExplicitSubCommandHelp() {
        return "Teleport to your home or named home";
    }

    // These are not needed for the /home command
    public Map<String, String> getAdditionalHelp() {
        return null;
    }

    public Map<String, String> getAdditionalHelpAliases() {
        return null;
    }
}
