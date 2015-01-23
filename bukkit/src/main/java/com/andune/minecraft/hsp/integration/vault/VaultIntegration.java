package com.andune.minecraft.hsp.integration.vault;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Integration implementation with Vault plugin.
 *
 * @author andune
 */
public class VaultIntegration {
    private static final Logger log = LoggerFactory.getLogger(VaultIntegration.class);
    private final Plugin hspPluginObject;
    private Plugin vaultPluginObject;
    protected net.milkbowl.vault.economy.Economy economy;

    public VaultIntegration(Plugin plugin) {
        this.hspPluginObject = plugin;
    }

    public void init() {
        vaultPluginObject = hspPluginObject.getServer().getPluginManager().getPlugin("Vault");
        if (vaultPluginObject != null) {
            RegisteredServiceProvider<Economy> economyProvider = hspPluginObject.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
                log.info("Vault interface found and will be used for economy-related functions (economy = {})", economy.getName());
            }
            else {
                log.info("Vault interface found but no economy plugin is installed");
            }
        }
        else {
            log.info("Vault not found, HSP economy features are disabled");
        }
    }

    public String format(double amount) {
        return economy.format(amount);
    }

    public double getBalance(String playerName) {
        return economy.getBalance(playerName);
    }

    public String withdrawPlayer(String playerName, double amount) {
        EconomyResponse response = economy.withdrawPlayer(playerName, amount);
        if (!response.transactionSuccess())
            return response.errorMessage;
        else
            return null;
    }

    public boolean isEnabled() {
        return vaultPluginObject != null;
    }

    public boolean isEconomyEnabled() {
        return economy != null;
    }

    public String getVersion() {
        if (vaultPluginObject != null)
            return vaultPluginObject.getDescription().getVersion();
        else
            return null;
    }
}
