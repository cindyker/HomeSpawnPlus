/**
 * 
 */
package org.morganm.homespawnplus.entity;


/**
 * @author morganm
 *
 */
public interface EntityWithLocation extends BasicEntity {
	public String getWorld() ;
	public void setWorld(String world);
	public Double getX();
	public void setX(Double x);
	public Double getY();
	public void setY(Double y);
	public Double getZ();
	public void setZ(Double z);
	public Float getPitch();
	public void setPitch(Float pitch);
	public Float getYaw();
	public void setYaw(Float yaw);
}
