/**
 * 
 */
package com.andune.minecraft.hsp.server.bukkit;

import javax.inject.Singleton;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andune.minecraft.hsp.Initializable;
import com.andune.minecraft.hsp.server.api.impl.EconomyAbstractImpl;

/**
 * @author morganm
 *
 */
@Singleton
public class BukkitEconomy extends EconomyAbstractImpl implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(BukkitEconomy.class);

    private net.milkbowl.vault.economy.Economy vaultEconomy;
    
    public BukkitEconomy() {
    }
    
    @Override
    public void init() throws Exception {
        if( vaultEconomy == null ) {
            Plugin vault = Bukkit.getServer().getPluginManager().getPlugin("Vault");
            if( vault != null ) {
                RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                if (economyProvider != null) {
                    vaultEconomy = economyProvider.getProvider();
                    log.info("Vault interface found and will be used for economy-related functions");
                }
            }
            else
                log.info("Vault not found, HSP economy features are disabled");
        }
    }

    @Override
    public int getInitPriority() {
        return 6;
    }

    @Override
    public boolean isEnabled() {
        return vaultEconomy != null;
    }

    @Override
    public String format(double amount) {
        return vaultEconomy.format(amount);
    }

    @Override
    public double getBalance(String playerName) {
        return vaultEconomy.getBalance(playerName);
    }

    @Override
    public String withdrawPlayer(String playerName, double amount) {
        EconomyResponse response = vaultEconomy.withdrawPlayer(playerName, amount);
        if( !response.transactionSuccess() )
            return response.errorMessage;
        else
            return null;
    }

    @Override
    public void shutdown() throws Exception {
    }
}
