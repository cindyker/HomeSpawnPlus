package com.andune.minecraft.hsp.server.sponge;

import org.spongepowered.api.entity.player.Player;

/**
 * @author andune
 */
public interface SpongeFactoryInterface {
    public SpongePlayer newSpongePlayer(Player spongePlayer);
}
