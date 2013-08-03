/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
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
package com.andune.minecraft.hsp.command;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.andune.minecraft.commonlib.server.api.CommandSender;
import com.andune.minecraft.commonlib.server.api.Economy;
import com.andune.minecraft.commonlib.server.api.Player;
import com.andune.minecraft.commonlib.server.api.Plugin;
import com.andune.minecraft.hsp.HSPMessages;
import com.andune.minecraft.hsp.Permissions;
import com.andune.minecraft.hsp.PermissionsImpl;
import com.andune.minecraft.hsp.config.ConfigEconomy;
import com.andune.minecraft.hsp.manager.CooldownManager;
import com.andune.minecraft.hsp.manager.WarmupManager;
import com.andune.minecraft.hsp.manager.WarmupRunner;
import com.andune.minecraft.hsp.server.api.Command;
import com.andune.minecraft.hsp.server.api.Server;
import com.andune.minecraft.hsp.storage.Storage;
import com.google.common.base.Joiner;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;


/**
 * Abstract class that takes care of some routine tasks for commands, to keep those
 * objects as light as possible and so as to not violate the DRY principle.
 *
 * @author andune
 */
public abstract class BaseCommand implements Command {
    protected final Logger log = LoggerFactory.getLogger(BaseCommand.class);

    protected Server server;
    protected Plugin plugin;
    protected CooldownManager cooldownManager;
    protected WarmupManager warmupManager;
    protected Permissions permissions;
    // private real object, this gets injected for us by IoC container
    private PermissionsImpl permissionsRealObject;
    protected Storage storage;

    private String permissionNode;
    private String commandName;
    private Map<String, Object> commandParams;
    private Economy economy;
    private ConfigEconomy configEconomy;
    private transient boolean isConsoleCommand = false;

    public String getDescription() {
        return null;
    }

    public String getUsage() {
        return "/<command>";
    }

    /**
     * For convenience, BaseCommand detects if a command is executed by a player and
     * invokes this method if so. This allows subclasses to deal with Player class directly
     * rather than every command having to check for Console.
     * <p/>
     * If a command can respond to Console input, it should override the
     * {@link #execute(CommandSender, String, String[])} method instead.
     * <p/>
     * Note this method does nothing, it's simply a stub to be overridden by a subclass.
     */
    public boolean execute(Player player, String[] args) throws CommandException {
        return false;
    }

    /**
     * By default, commands do not respond to console input. They can override this method
     * if they wish to do so.
     *
     * @param sender the sender object (usually a Player or Console)
     * @param cmd    the actual command that was run (if a command supports aliases, this will
     *               return the exact alias that was used)
     * @param args   any arguments passed to the command
     * @return true if the command was successfully processed, false if not successful. A
     *         false status will result in usage info being displayed back to the sender.
     */
    public boolean execute(CommandSender sender, String cmd, String[] args) {
        isConsoleCommand = false;

        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (!hasPermission(p))
                return true;

            // do generic error logging for convenience
            try {
                return this.execute(p, args);
            } catch (Exception e) {
                log.warn("Caught exception in command /" + getCommandName(), e);
                server.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
            }
        }
        // From the console, if the command doesn't directly support console
        // input, we apply an assumption that the first arg is the player to
        // operate the command on. This is useful for admins that want to
        // setup command blocks or want to run command from the console.
        else {
            if (args.length < 1) {
                sender.sendMessage("From the console, command /" + cmd + " requires the first argument to be the player to run as");
                return true;
            }
            else {
                String playerName = args[0];
                Player p = server.getPlayer(playerName);
                if (p == null) {
                    sender.sendMessage("Player " + playerName + " not found online.");
                    return true;
                }

                // flag this as a console command, this allows permissions
                // checks to pass even though we're going to simulate the
                // command running as the given player
                isConsoleCommand = true;

                String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
                Joiner joiner = Joiner.on(" ").skipNulls();
                String joinedArgs = joiner.join(newArgs);
                String newCmd = cmd + " " + joinedArgs;
                sender.sendMessage("Running command as player \"" + playerName + "\": " + newCmd);

                // do generic error logging for convenience
                try {
                    return this.execute(p, newArgs);
                } catch (Exception e) {
                    log.warn("Caught exception in command /" + getCommandName(), e);
                    server.sendLocalizedMessage(p, HSPMessages.GENERIC_ERROR);
                } finally {
                    isConsoleCommand = false;
                }
            }
        }

        return false;
    }

    public void setCommandParameters(Map<String, Object> params) {
        this.commandParams = params;
    }

    protected Object getParam(String param) {
        if (commandParams != null)
            return commandParams.get(param);
        else
            return null;
    }

    /**
     * Return a given parameter as a string. If the parameter doesn't exist
     * or it is not a string, null is returned.
     *
     * @param param
     * @return
     */
    protected String getStringParam(String param) {
        Object v = getParam(param);
        if (v instanceof String)
            return (String) v;
        else if (v != null)
            return v.toString();
        else
            return null;
    }

    @Inject
    private void setServer(Server server) {
        this.server = server;
    }

    @Inject
    private void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    @Inject
    private void setCooldownManager(CooldownManager cooldownManager) {
        this.cooldownManager = cooldownManager;
    }

    @Inject
    private void setWarmupManager(WarmupManager warmupManager) {
        this.warmupManager = warmupManager;
    }

    @Inject
    private void setEconomy(Economy economy) {
        this.economy = economy;
    }

    @Inject
    private void setConfigEconomy(ConfigEconomy configEconomy) {
        this.configEconomy = configEconomy;
    }

    @Inject
    private void setStorage(Storage storage) {
        this.storage = storage;
    }

    /**
     * Check to see if player has sufficient money to pay for this command.
     *
     * @param p the player to check
     * @return true if the player has enough money or the command has no cost
     */
    protected boolean costCheck(Player p) {
        boolean returnValue = false;

        if (economy == null)
            returnValue = true;

        if (!returnValue && permissions.isCostExempt(p, getCommandName()))
            returnValue = true;

        if (!returnValue) {
            int price = getPrice(p);
            if (price > 0) {
                double balance = economy.getBalance(p.getName());
                if (balance >= price)
                    returnValue = true;
            }
            else
                returnValue = true;
        }

        return returnValue;
    }

    protected int getPrice(Player p) {
        return economy.getCommandCost(p, getCommandName());
    }

    protected void printInsufficientFundsMessage(Player p) {
        if (economy != null)
            p.sendMessage(server.getLocalizedMessage(HSPMessages.COST_INSUFFICIENT_FUNDS,
                    "price", economy.format(getPrice(p)),
                    "balance", economy.format(economy.getBalance(p.getName()))));
    }

    /**
     * @param p
     * @return true on success, false if there was an error that should prevent the action from taking place
     */
    protected boolean applyCost(Player p, boolean applyCooldown, String cooldownName) {
        boolean returnValue = false;

        if ((economy == null) || !configEconomy.isEnabled())
            returnValue = true;

        if (!returnValue && permissions.isCostExempt(p, getCommandName()))
            returnValue = true;
        log.debug("applyCost: player={}, exempt returnValue={}", p, returnValue);

        if (!costCheck(p)) {
            printInsufficientFundsMessage(p);
            returnValue = false;
        }
        else if (!returnValue) {
            int price = getPrice(p);
            if (price > 0) {
                String error = economy.withdrawPlayer(p.getName(), price);

                // SUCCESS
                if (error == null) {
                    if (configEconomy.isVerboseOnCharge()) {
                        // had an error report that might have been related to a null value
                        // being returned from economy.format(price), so let's check for that
                        // and protect against any error.
                        String priceString = economy.format(price);
                        if (priceString == null)
                            priceString = "" + price;

                        p.sendMessage(server.getLocalizedMessage(HSPMessages.COST_CHARGED,
                                "price", priceString,
                                "command", getCommandName()));
                    }

                    returnValue = true;
                }
                else {
                    p.sendMessage(server.getLocalizedMessage(HSPMessages.COST_ERROR,
                            "price", economy.format(price),
                            "errorMessage", error));
                    returnValue = false;
                }
            }
            // no cost for this command
            else
                returnValue = true;
        }

        // if applyCooldown flag is true and the returnValue is true, then apply the Cooldown now
        if (applyCooldown && returnValue)
            applyCooldown(p, cooldownName);

        return returnValue;
    }

    protected boolean applyCost(Player p, boolean applyCooldown) {
        return applyCost(p, applyCooldown, null);
    }

    protected boolean applyCost(Player p) {
        return applyCost(p, false);
    }

    protected void doWarmup(Player p, WarmupRunner wr) {
        if (!isWarmupPending(p, wr.getWarmupName())) {
            warmupManager.startWarmup(p.getName(), wr);

            p.sendMessage(server.getLocalizedMessage(HSPMessages.WARMUP_STARTED,
                    "name", wr.getWarmupName(),
                    "seconds", warmupManager.getWarmupTime(p, wr.getWarmupName()).warmupTime));
        }
        else
            p.sendMessage(server.getLocalizedMessage(HSPMessages.WARMUP_ALREADY_PENDING, "name", wr.getWarmupName()));

    }

    /**
     * Most commands for this plugin check 3 things:
     * <p/>
     * 1 - is the command enabled in the config?
     * 2 - does the player have access to run the command?
     * 3 - is the command on cooldown for the player?
     * <p/>
     * This method just implements all 3 checks.
     *
     * @param p the player object that is running the command
     * @return returns false if the checks fail and Command processing should stop, true if the command is allowed to continue
     */
    protected boolean defaultCommandChecks(Player p) {
        log.debug("defaultCommandChecks()");

        final boolean hasP = hasPermission(p);
        log.debug("defaultCommandChecks() hasPermission = {}", hasP);
        if (!hasP)
            return false;

        final boolean cd = cooldownCheck(p);
        log.debug("defaultCommandChecks() cooldownCheck = {}", cd);
        if (!cd)
            return false;

        log.debug("defaultCommandChecks() all defaultCommandChecks return true");
        return true;
    }

    protected boolean defaultCommandChecks(CommandSender sender) {
        if (sender instanceof Player) {
            return defaultCommandChecks((Player) sender);
        }
        // it's a local or remote console
        else {
            return true;
        }
    }

    @Override
    public void setCommandName(String name) {
        commandName = name;
    }

    /**
     * Can be overridden, but default implementation just applies the command name
     * as the lower case version of the class name of the implemented command.
     */
    @Override
    public String getCommandName() {
        if (commandName == null) {
            String className = this.getClass().getName();
            int index = className.lastIndexOf('.');
            commandName = className.substring(index + 1).toLowerCase();
        }

        return commandName;
    }

    /**
     * Here for convenience if the command has no aliases.  Otherwise, this should be overridden.
     */
    @Override
    public String[] getCommandAliases() {
        return null;
    }

    /**
     * check the default command cooldown for the player
     *
     * @param p
     * @param cooldownName
     * @param sendMessage  if true, a cooldown message will be sent to the player
     * @return true if cooldown is available, false if currently in cooldown period
     */
    protected boolean cooldownCheck(Player p, String cooldownName, boolean sendMessage) {
        if (cooldownName == null)
            cooldownName = getCommandName();
        return cooldownManager.cooldownCheck(p, cooldownName, sendMessage);
    }

    protected boolean cooldownCheck(Player p, String cooldownName) {
        return cooldownCheck(p, cooldownName, true);
    }

    protected boolean cooldownCheck(Player p) {
        return cooldownCheck(p, getCommandName());
    }

    protected void applyCooldown(Player p, String cooldownName) {
        if (cooldownName == null)
            cooldownName = getCommandName();
        cooldownManager.setCooldown(p, cooldownName);
    }

    protected void applyCooldown(Player p) {
        applyCooldown(p, getCommandName());
    }

    /**
     * @return true if this command & player has a warmup associated with it
     */
    protected boolean hasWarmup(Player p, String warmupName) {
        return warmupManager.hasWarmup(p, warmupName);
    }

    protected boolean hasWarmup(Player p) {
        return hasWarmup(p, getCommandName());
    }

    /**
     * check if a warmup is already pending for this command
     *
     * @param p
     * @return true if warmup is already pending, false if not
     */
    protected boolean isWarmupPending(Player p, String warmupName) {
        return warmupManager.isWarmupPending(p.getName(), warmupName);
    }

    protected boolean isWarmupPending(Player p) {
        return isWarmupPending(p, getCommandName());
    }

    protected String getCooldownName(String baseName, String homeName) {
        if (baseName == null)
            baseName = getCommandName();

        if (homeName != null && cooldownManager.isCooldownSeparationEnabled(baseName))
            return baseName + "." + homeName;
        else
            return baseName;
    }

    public final String getCommandPermissionNode() {
        if (permissionNode == null) {
            // set permission node from config params, if set
            permissionNode = getStringParam("permission");
        }

        return permissionNode;
    }

    /**
     * Check if the player has permission to run this command. If they don't
     * have permission, optionally print them a message saying so and return
     * false. This should also take into account custom command permissions
     * that might have been defined by the admin.
     *
     * @param p              the player to check
     * @param displayMessage true if a message should be displayed to the player if they
     *                       don't have permission
     * @return true if the player has permission, false if not
     */
    public boolean hasPermission(Player p, boolean displayMessage) {
        if (!permissions.hasCommandPermission(p, this)) {
            if (displayMessage)
                p.sendMessage(server.getLocalizedMessage(HSPMessages.NO_PERMISSION));
            return false;
        }
        else
            return true;
    }

    protected boolean hasPermission(Player p) {
        return hasPermission(p, true);
    }

    @Inject
    private void setPermissions(PermissionsImpl permissionsRealObject) {
        this.permissionsRealObject = permissionsRealObject;

        // create our proxy permission object
        this.permissions = (Permissions) java.lang.reflect.Proxy.newProxyInstance(
                this.permissionsRealObject.getClass().getClassLoader(),
                this.permissionsRealObject.getClass().getInterfaces(),
                new PermissionsProxy(this.permissionsRealObject));
    }

    private class PermissionsProxy implements java.lang.reflect.InvocationHandler {
        private final Object obj;

        public PermissionsProxy(Object obj) {
            this.obj = obj;
        }

        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
            Object result = null;
            try {
                log.debug("proxy method invoke {}, isConsoleCommand={}", m.getName(), isConsoleCommand);

                // if this command was executed on the console (which includes
                // running in a command block), then permissions checks are
                // always true
                if (isConsoleCommand &&
                        (m.getName().startsWith("has") || m.getName().startsWith("is"))
                        && m.getReturnType().equals(boolean.class)) {
                    result = true;
                }
                // otherwise just pass through to the real object
                else
                    result = m.invoke(obj, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } finally {
                log.debug("method result = {}", result);
            }
            return result;
        }
    }
}
