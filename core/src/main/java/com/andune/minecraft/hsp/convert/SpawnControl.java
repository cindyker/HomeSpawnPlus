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
package com.andune.minecraft.hsp.convert;

import com.andune.minecraft.commonlib.server.api.Location;
import com.andune.minecraft.commonlib.server.api.World;
import com.andune.minecraft.hsp.util.HomeUtil;

import javax.inject.Inject;
import java.sql.*;


/**
 * Converter for original SpawnControl.
 *
 * @author andune
 */
public class SpawnControl extends BaseConverter {
    @Inject
    private HomeUtil util;

    @Override
    public String getConverterName() {
        return "SpawnControl";
    }

    @Override
    public int convert() {
        int convertedCount = 0;

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            String db = "jdbc:sqlite:plugins/SpawnControl/spawncontrol.db";
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(db);
            ps = conn.prepareStatement("SELECT * FROM `players`");
            ResultSet rs = ps.executeQuery();

//            HomeSpawnUtils util = plugin.getUtil();

            int consecutiveErrors = 0;
            while (rs.next()) {
                // protect against a bunch of consecutive errors spamming the logfile
                if (consecutiveErrors > 10)
                    break;

                try {
                    String playerName = rs.getString("name");
                    String worldName = rs.getString("world");
                    World world = server.getWorld(worldName);

                    Location l = factory.newLocation(world.getName(), rs.getDouble("x"), rs.getDouble("y"),
                            rs.getDouble("z"), rs.getFloat("r"), rs.getFloat("p"));

                    util.setHome(playerName, l, "[SpawnControl_Conversion]", true, false);
                    convertedCount++;

                    consecutiveErrors = 0;    // success! reset consecutiveErrors counter
                } catch (Exception e) {
                    log.warn("error trying to process SQL row", e);
                    consecutiveErrors++;
                }
            }
            conn.close();

            if (consecutiveErrors > 10)
                log.warn("conversion process aborted, too many consecutive errors");
        } catch (SQLException e) {
            log.error("Caught exception", e);
        } catch (Exception e) {
            log.error("Caught exception", e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
            } catch (SQLException e) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
            }
        }

        return convertedCount;
    }
}
