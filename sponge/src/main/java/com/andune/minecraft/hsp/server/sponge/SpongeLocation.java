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
package com.andune.minecraft.hsp.server.sponge;

import com.andune.minecraft.commonlib.server.api.*;
import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.commonlib.server.api.impl.LocationAbstractImpl;
import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Game;
import org.spongepowered.api.world.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author andune
 *
 */
public class SpongeLocation extends LocationAbstractImpl implements Location {
    private static final Vector3d emptyViewAngle = new Vector3d(0,0,0);

    private final Game game;
    private final org.spongepowered.api.world.Location spongeLocation;
    private Vector3d viewAngle;

    public SpongeLocation(final Game game, final org.spongepowered.api.world.Location spongeLocation) {
        checkNotNull(game);
        checkNotNull(spongeLocation);

        this.game = game;
        this.spongeLocation = spongeLocation;
        viewAngle = emptyViewAngle;
    }

    public SpongeLocation(final Game game, final Location l) {
        checkNotNull(game);
        checkNotNull(l);
        checkNotNull(l.getWorld());
        checkNotNull(l.getWorld().getName());

        this.game = game;
        org.spongepowered.api.world.World w = game.getServer().getWorld(l.getWorld().getName()).get();
        spongeLocation = new org.spongepowered.api.world.Location(w, l.getX(), l.getY(), l.getZ());
        viewAngle = new Vector3d(l.getYaw(), l.getPitch(), 0);
    }

    public SpongeLocation(final Game game, final String worldName, final double x,
                          final double y, final double z, final float yaw, final float pitch)
    {
        checkNotNull(game);

        org.spongepowered.api.world.World w = game.getServer().getWorld(worldName).get();
        spongeLocation = new org.spongepowered.api.world.Location(w, x, y, z);
        viewAngle = new Vector3d(yaw, pitch, 0);
        this.game = game;
    }
    
    public org.spongepowered.api.world.Location getSpongeLocation() {
        return spongeLocation;
    }
    
    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#getBlock()
     */
    @Override
    public Block getBlock() {
        return new SpongeBlock(this);
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#setX(double)
     */
    @Override
    public void setX(double x) {
        spongeLocation.setPosition(new Vector3d(x, spongeLocation.getY(), spongeLocation.getZ()));
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#getX()
     */
    @Override
    public double getX() {
        return spongeLocation.getX();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#getBlockX()
     */
    @Override
    public int getBlockX() {
        return spongeLocation.getBlockX();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#setY(double)
     */
    @Override
    public void setY(double y) {
        spongeLocation.setPosition(new Vector3d(spongeLocation.getX(), y, spongeLocation.getZ()));
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#getY()
     */
    @Override
    public double getY() {
        return spongeLocation.getY();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#getBlockY()
     */
    @Override
    public int getBlockY() {
        return spongeLocation.getBlockY();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#setZ(double)
     */
    @Override
    public void setZ(double z) {
        spongeLocation.setPosition(new Vector3d(spongeLocation.getX(), spongeLocation.getY(), z));
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#getZ()
     */
    @Override
    public double getZ() {
        return spongeLocation.getZ();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#getBlockZ()
     */
    @Override
    public int getBlockZ() {
        return spongeLocation.getBlockZ();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#setYaw(float)
     */
    @Override
    public void setYaw(float yaw) {
        viewAngle = new Vector3d(yaw, viewAngle.getY(), viewAngle.getZ());
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#getYaw()
     */
    @Override
    public float getYaw() {
        return new Double(viewAngle.getX()).floatValue();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#setPitch(float)
     */
    @Override
    public void setPitch(float pitch) {
        viewAngle = new Vector3d(viewAngle.getX(), pitch, viewAngle.getZ());
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#getPitch()
     */
    @Override
    public float getPitch() {
        return new Double(viewAngle.getY()).floatValue();
    }

    /* (non-Javadoc)
     * @see com.andune.minecraft.hsp.server.api.Location#distance(com.andune.minecraft.hsp.server.api.Location)
     */
    @Override
    public double distance(Location o) {
        // we can only compare distance to other BukkitLocation objects.
        if( !(o instanceof SpongeLocation) )
            throw new IllegalArgumentException("invalid object class: "+o);

        return spongeLocation.getPosition().distance(((SpongeLocation) o).spongeLocation.getPosition());
    }

    @Override
    public World getWorld() {
        org.spongepowered.api.world.World w = (org.spongepowered.api.world.World) spongeLocation.getExtent();

        // TODO change to factory method
        return new SpongeWorld(w);
    }

    @Override
    public void playEffect(Effect effect, int data) {
        // TODO: implement for Sponge

        /*
        // lightning is handled special, there's no Bukkit visual lightning
        // effect, we have to trigger lightning through a direct call
        if( effect == Effect.LIGHTNING ) {
            getSpongeLocation().getWorld().strikeLightningEffect(getSpongeLocation());
        }
        else {
            org.bukkit.World w = getSpongeLocation().getWorld();
            org.bukkit.Effect bukkitEffect = BukkitEffect.getBukkitEffect(effect);
            w.playEffect(getSpongeLocation(), bukkitEffect, 0);
        }
        */
    }
    
    @Override
    public void playSound(Sound sound, float volume, float pitch) {
        // TODO: implement for Sponge

        /*
        org.bukkit.World w = getSpongeLocation().getWorld();
        org.bukkit.Sound bukkitSound = BukkitSound.getBukkitSound(sound);
        w.playSound(getSpongeLocation(), bukkitSound, volume, pitch);
        */
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof SpongeLocation)
            return spongeLocation.equals(((SpongeLocation) o).spongeLocation);
        else
            return false;
    }

    public int hashCode() {
        return spongeLocation.hashCode();
    }
}
