/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (c) 2015 Andune (andune.alleria@gmail.com)
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
package com.andune.minecraft.hsp.integration.multiverse;

import com.andune.minecraft.commonlib.Logger;
import com.andune.minecraft.commonlib.LoggerFactory;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.event.MVTeleportEvent;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.destination.PortalDestination;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import org.bukkit.event.Listener;

import javax.inject.Singleton;

/**
 * Class incomplete, may not be necessary.
 *
 * @author andune
 */
@Singleton
public class MultiverseListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(MultiverseListener.class);

    private MultiverseCore multiverseCoreModule;
    private MultiversePortals multiversePortalsModule;

    public MultiverseListener() {
    }

    public void setMultiverseCoreModule(MultiverseCore multiverseCoreModule) {
        this.multiverseCoreModule = multiverseCoreModule;
    }

    public void setMultiversePortalsModule(MultiversePortals multiversePortalsModule) {
        this.multiversePortalsModule = multiversePortalsModule;
    }

    public void onMultiverseTeleport(MVTeleportEvent event) {
        if (event.isCancelled())
            return;

        if (multiverseCoreModule != null) {
            log.debug("onMultiverseTeleport(): setting entity to {}", event.getTeleportee());
            multiverseCoreModule.setCurrentTeleporter(event.getTeleportee().getName());
        }
    }

    public void onMultiversePortalEvent(MVPortalEvent event) {
        if (event.isCancelled())
            return;

        if (multiversePortalsModule != null) {
            log.debug("onMultiversePortalEvent(): setting entity to {}", event.getTeleportee());
            MVPortal portal = event.getSendingPortal();
            if (portal != null)
                multiversePortalsModule.setSourcePortalName(portal.getName());

            MVDestination destination = event.getDestination();
            if (destination != null && destination instanceof PortalDestination) {
                PortalDestination portalDestination = (PortalDestination) destination;
                multiversePortalsModule.setDestinationPortalName(portalDestination.getName());
            }
        }
    }
}
