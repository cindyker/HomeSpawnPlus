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
package com.andune.minecraft.hsp.entity;

import java.sql.Timestamp;

import com.andune.minecraft.commonlib.server.api.Location;

public interface Home extends EntityWithLocation {

    void setLocation(Location l);

    Location getLocation();

    int getId();

    void setId(int id);

    String getPlayerName();

    void setPlayerName(String playerName);

    String getWorld();

    void setWorld(String world);

    Double getX();

    void setX(Double x);

    Double getY();

    void setY(Double y);

    Double getZ();

    void setZ(Double z);

    String getUpdatedBy();

    void setUpdatedBy(String updatedBy);

    Timestamp getLastModified();

    void setLastModified(Timestamp lastModified);

    Timestamp getDateCreated();

    void setDateCreated(Timestamp dateCreated);

    Float getPitch();

    void setPitch(Float pitch);

    Float getYaw();

    void setYaw(Float yaw);

    boolean isBedHome();

    void setBedHome(boolean bedHome);

    boolean isDefaultHome();

    void setDefaultHome(boolean defaultHome);

    String getName();

    void setName(String name);

}