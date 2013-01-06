/**
 * 
 */
package com.andune.minecraft.hsp.server.api.impl;

/**
 * @author morganm
 *
 */
public class TeleportOptionsImpl implements com.andune.minecraft.hsp.server.api.TeleportOptions {
    private int minY = 1;
    private int maxY = 255;
    private int maxRange = 10;
    
    private boolean isNoTeleportOverWater = false;
    private boolean isNoTeleportOverIce = false;
    private boolean isNoTeleportOverLeaves = false;
    private boolean isNoTeleportOverLilyPad = false;
    
    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public void setMinY(int minY) {
        if( minY < 0 )
            minY = 0;
        this.minY = minY;
    }

    @Override
    public int getMaxY() {
        return maxY;
    }

    @Override
    public void setMaxY(int maxY) {
        if( maxY > 255 )
            maxY = 255;
        this.maxY = maxY;
    }

    @Override
    public int getMaxRange() {
        return maxRange;
    }

    @Override
    public void setMaxRange(int range) {
        this.maxRange = range;
    }

    @Override
    public boolean isNoTeleportOverWater() {
        return isNoTeleportOverWater;
    }

    @Override
    public void setNoTeleportOverWater(boolean flag) {
        this.isNoTeleportOverWater = flag;
    }

    @Override
    public boolean isNoTeleportOverIce() {
        return isNoTeleportOverIce;
    }

    @Override
    public void setNoTeleportOverIce(boolean flag) {
        this.isNoTeleportOverIce = flag;
    }

    @Override
    public boolean isNoTeleportOverLeaves() {
        return isNoTeleportOverLeaves;
    }

    @Override
    public void setNoTeleportOverLeaves(boolean flag) {
        this.isNoTeleportOverLeaves = flag;
    }

    @Override
    public boolean isNoTeleportOverLilyPad() {
        return isNoTeleportOverLilyPad;
    }

    @Override
    public void setNoTeleportOverLilyPad(boolean flag) {
        this.isNoTeleportOverLilyPad = flag;
    }
}
