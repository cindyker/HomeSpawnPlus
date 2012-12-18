/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author morganm
 *
 */
@Singleton
public class BukkitEconomy implements org.morganm.homespawnplus.server.api.Economy {
    private static final Logger log = LoggerFactory.getLogger(BukkitEconomy.class);

    private net.milkbowl.vault.economy.Economy vaultEconomy;
    
    @Inject
    public BukkitEconomy() {
        
    }
    
    public void foo() {
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

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Economy#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Economy#format(double)
     */
    @Override
    public String format(double amount) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Economy#getBalance(java.lang.String)
     */
    @Override
    public double getBalance(String playerName) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Economy#withdrawPlayer(java.lang.String, double)
     */
    @Override
    public String withdrawPlayer(String playerName, double amount) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.morganm.homespawnplus.server.api.Economy#getCommandCost(java.lang.String, java.lang.String)
     */
    @Override
    public int getCommandCost(String playerName, String command) {
        // TODO Auto-generated method stub
        return 0;
    }

}
