/**
 * 
 */
package org.morganm.homespawnplus.server.api;

/**
 * @author morganm
 *
 */
public interface TeleportOptions {
    public int getMinY();
    public void setMinY(int minY);
    public int getMaxY();
    public void setMaxY(int maxY);
    public int getMaxRange();
    public void setMaxRange(int range);
    
    public boolean isNoTeleportOverWater();
    public void setNoTeleportOverWater(boolean flag);
    public boolean isNoTeleportOverIce();
    public void setNoTeleportOverIce(boolean flag);
    public boolean isNoTeleportOverLeaves();
    public void setNoTeleportOverLeaves(boolean flag);
    public boolean isNoTeleportOverLilyPad();
    public void setNoTeleportOverLilyPad(boolean flag);
}
