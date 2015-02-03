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
package com.andune.minecraft.hsp.convert;

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.entity.HomeImpl;
import com.andune.minecraft.hsp.storage.StorageException;
import com.andune.minecraft.hsp.storage.dao.HomeDAO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * Class to process Commandbook home data and convert it into our storage
 * format.
 *
 * @author andune
 */
public class CommandBook extends BaseConverter {
    @Override
    public String getConverterName() {
        return "CommandBook";
    }

    @Override
    public int convert() throws IOException {
        // keep track of player names as we convert their first home, allows us to
        // set the first home we run into as the default home.
        Set<String> playerNames = new HashSet<String>();

        File folder = plugin.getDataFolder();
        String parent = folder.getParent();
        File commandBookHomeData = new File(parent + "/CommandBook/homes.csv");

        if (!commandBookHomeData.isFile()) {
            log.warn("No CommandBook homes.csv found, skipping home import");
            return 0;
        }

        HomeDAO dao = storage.getHomeDAO();

        int convertedCount = 0;
        BufferedReader br = new BufferedReader(new FileReader(commandBookHomeData));
        String line = null;
        while ((line = br.readLine()) != null) {
            line = line.replaceAll("\"", "");
            String[] arr = line.split(",");

            int i = 0;
            String homeName = arr[i++];
            String worldName = arr[i++];
            String playerName = arr[i++];
            Double x = Double.parseDouble(arr[i++]);
            Double y = Double.parseDouble(arr[i++]);
            Double z = Double.parseDouble(arr[i++]);
            Double pitch = Double.parseDouble(arr[i++]);
            Double yaw = Double.parseDouble(arr[i++]);

            World world = server.getWorld(worldName);
            if (world == null) {
                log.warn("CommandBook converter: tried to convert home from world \"{}\", but no such world exists", worldName);
                continue;
            }

            Location l = factory.newLocation(world.getName(), x, y, z,
                    yaw.floatValue(), pitch.floatValue());

            HomeImpl hspHome = new HomeImpl();
            hspHome.setLocation(l);
            hspHome.setPlayerName(playerName);
            hspHome.setName(homeName);
            hspHome.setUpdatedBy("[CommandBook_Conversion]");

            // first home we find for a player is considered the default home
            if (!playerNames.contains(playerName)) {
                hspHome.setDefaultHome(true);
                playerNames.add(playerName);
            }

            try {
                dao.saveHome(hspHome);
                convertedCount++;
            } catch (StorageException e) {
                log.warn("StorageException attempting to convert CommandBook home", e);
            }
        }
        br.close();

        return convertedCount;
    }
}
