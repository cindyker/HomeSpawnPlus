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
package com.andune.minecraft.hsp.commands;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.andune.minecraft.hsp.config.ConfigCore;
import com.andune.minecraft.hsp.server.api.Location;
import com.andune.minecraft.hsp.server.api.World;
import com.andune.minecraft.hsp.util.SpawnUtil;

/**
 * @author andune
 *
 */
public class TestSetSpawn extends BaseCommandTest {
    @InjectMocks
    private SetSpawn objectUnderTest;
    
    @Mock
    private ConfigCore config;
    @Mock
    private SpawnUtil util;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        super.beforeMethod();
    }
    
    @Test
    public void testSetSpawnNoPermission() throws Exception {
        // Given
        when(permissions.hasCommandPermission(player, objectUnderTest)).thenReturn(false);
        // When
        objectUnderTest.execute(player, null, null);
        // Then
        verify(player).sendMessage(MSG_NO_PERMISSION);
    }
    
    @Test
    public void testNamedSpawn() throws Exception {
        // Given
        when(permissions.hasCommandPermission(player, objectUnderTest)).thenReturn(true);
        // When
        objectUnderTest.execute(player, null, new String[] {"dummy"});
        // Then
        verify(util).setNamedSpawn(eq("dummy"), any(Location.class), any(String.class));
    }
    
    @Test
    public void testSetDefaultSpawn() throws Exception {
        // Given
        when(permissions.hasCommandPermission(player, objectUnderTest)).thenReturn(true);
        // When
        objectUnderTest.execute(player, null, new String[] {});
        // Then
        verify(util).setDefaultWorldSpawn(any(Location.class), any(String.class));
    }
    
    @Test
    public void testSetDefaultSpawnWithWorldOverride() throws Exception {
        // Given
        when(permissions.hasCommandPermission(player, objectUnderTest)).thenReturn(true);
        when(config.isOverrideWorld()).thenReturn(true);
        final World w = mock(World.class);
        final Location l = mock(Location.class);
        when(l.getWorld()).thenReturn(w);
        when(player.getLocation()).thenReturn(l);

        // When
        objectUnderTest.execute(player, null, new String[] {});
        // Then
        verify(util).setDefaultWorldSpawn(any(Location.class), any(String.class));
        verify(w).setSpawnLocation(anyInt(), anyInt(), anyInt());
    }
}
