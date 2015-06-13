package com.andune.minecraft.hsp.integration.vault;

import com.andune.minecraft.commonlib.Initializable;
import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.google.inject.Inject;

/**
 * TODO: tie into Sponge built-in capabilities
 *
 * @author andune
 */
public class VaultModule implements Vault, Initializable {
    private final Logger log = LoggerFactory.getLogger(VaultModule.class);

    @Inject
    public VaultModule() {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
    }

    @Override
    public int getInitPriority() {
        return 9;
    }

    @Override
    public boolean isEconomyEnabled() { return false; }

    @Override
    public String format(double amount) {
        return "";
    }

    @Override
    public double getBalance(String playerName) {
        return 0.0d;
    }

    @Override
    public String withdrawPlayer(String playerName, double amount) {
        return "";
    }
}
