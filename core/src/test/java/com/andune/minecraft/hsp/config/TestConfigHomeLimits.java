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
package com.andune.minecraft.hsp.config;

import com.andune.minecraft.commonlib.server.api.ConfigurationSection;
import com.andune.minecraft.hsp.config.ConfigHomeLimits.LimitsPerPermission;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.*;

/**
 * @author andune
 */
public class TestConfigHomeLimits {
    @Mock
    ConfigLoader configLoader;
    ConfigurationSection section;

    @InjectMocks
    ConfigHomeLimits objectUnderTest;

    @BeforeMethod
    public void beforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        section = mock(ConfigurationSection.class);
        when(configLoader.load(anyString(), anyString())).thenReturn(section);
//        yaml = mock(YamlFile.class); //, withSettings().verboseLogging());

        // we manually inject dependencies instead of using @InjectMocks
        // because @InjectMocks isn't as smart as Guice to invoke @Inject
        // setters; @InjectMocks sees the @Inject constructor and considers
        // it job done.
//        objectUnderTest = spy(new ConfigHomeLimits(yaml));
//        objectUnderTest.setPlugin(plugin);
//        objectUnderTest.setJarUtil(jarUtil);

//        System.out.println("yaml = "+System.identityHashCode(yaml));
//        System.out.println("objectUnderTest.yaml = "+System.identityHashCode(objectUnderTest.yaml));
    }

    @Test
    public void testInit() throws Exception {
        objectUnderTest.init();
        verify(configLoader).load(anyString(), anyString());
    }

    @Test
    public void testDefaultLimits() throws Exception {
        // given
        when(section.getInteger("default.global")).thenReturn(Integer.valueOf(5));
        when(section.getInteger("default.perWorld")).thenReturn(Integer.valueOf(3));

        // when
        objectUnderTest.init();
        Integer global = objectUnderTest.getDefaultGlobalLimit();
        Integer perWorld = objectUnderTest.getDefaultPerWorldLimit();

        // then
        assertEquals(Integer.valueOf(5), global);
        assertEquals(Integer.valueOf(3), perWorld);
    }

    @Test
    public void testPerWorldLimits() throws Exception {
        // given
        HashSet<String> worldKeys = new HashSet<String>(5);
        worldKeys.add("myworld");
        when(section.getKeys("world")).thenReturn(worldKeys);
        HashSet<String> myworldKeys = new HashSet<String>(5);
        myworldKeys.add("global");
        myworldKeys.add("perWorld");
        when(section.getKeys("world.myworld")).thenReturn(myworldKeys);
        when(section.get("world.myworld.global")).thenReturn(Integer.valueOf(5));
        when(section.get("world.myworld.perWorld")).thenReturn(Integer.valueOf(3));

        // when
        objectUnderTest.init();
        Integer global = objectUnderTest.getPerWorldEntry("myworld").getGlobal();
        Integer perWorld = objectUnderTest.getPerWorldEntry("myworld").getPerWorld();

        // then
        assertEquals(Integer.valueOf(5), global);
        assertEquals(Integer.valueOf(3), perWorld);
    }

    @Test
    public void testPerPermissionLimitsWithPermissions() throws Exception {
        // given
        HashSet<String> entryKeys = new HashSet<String>(5);
        entryKeys.add("entry1");
        when(section.getKeys("permission")).thenReturn(entryKeys);

        HashSet<String> entry1Keys = new HashSet<String>(7);
        entry1Keys.add("permissions");
        entry1Keys.add("perWorld");
        entry1Keys.add("global");
        when(section.getKeys("permission.entry1")).thenReturn(entry1Keys);

        ArrayList<String> perms = new ArrayList<String>(5);
        perms.add("dummyperm");
        when(section.getStringList("permission.entry1.permissions")).thenReturn(perms);

        when(section.get("permission.entry1.global")).thenReturn(Integer.valueOf(5));
        when(section.get("permission.entry1.perWorld")).thenReturn(Integer.valueOf(3));

        // when
        objectUnderTest.init();
        LimitsPerPermission lpp = objectUnderTest.getPerPermissionEntries().get("entry1");

        // then
        assertNotNull(lpp);
        assertEquals(Integer.valueOf(5), lpp.getGlobal());
        assertEquals(Integer.valueOf(3), lpp.getPerWorld());
        assertEquals(perms.get(0), lpp.getPermissions().get(0));
    }

    @Test
    public void testPerPermissionLimitsDefaultPermissions() throws Exception {
        // given
        HashSet<String> entryKeys = new HashSet<String>(5);
        entryKeys.add("entry1");
        when(section.getKeys("permission")).thenReturn(entryKeys);

        HashSet<String> entry1Keys = new HashSet<String>(7);
        entry1Keys.add("perWorld");
        entry1Keys.add("global");
        when(section.getKeys("permission.entry1")).thenReturn(entry1Keys);

        when(section.get("permission.entry1.global")).thenReturn(Integer.valueOf(5));
        when(section.get("permission.entry1.perWorld")).thenReturn(Integer.valueOf(3));

        // when
        objectUnderTest.init();
        LimitsPerPermission lpp = objectUnderTest.getPerPermissionEntries().get("entry1");

        // then
        assertNotNull(lpp);
        assertEquals(Integer.valueOf(5), lpp.getGlobal());
        assertEquals(Integer.valueOf(3), lpp.getPerWorld());
        assertTrue(lpp.getPermissions().contains("hsp.entry.entry1"));
        assertTrue(lpp.getPermissions().contains("group.entry1"));

        ConfigOptions configOptions = ConfigHomeLimits.class.getAnnotation(ConfigOptions.class);
        assertTrue(lpp.getPermissions().contains("hsp." + configOptions.basePath() + ".entry1"));
    }

    @Test
    public void testPerWorldInheritedLimits() throws Exception {
        // given
        HashSet<String> worldKeys = new HashSet<String>(5);
        worldKeys.add("myworld");
        when(section.getKeys("world")).thenReturn(worldKeys);

        HashSet<String> myworldKeys = new HashSet<String>(5);
        myworldKeys.add("inherit");
        when(section.getKeys("world.myworld")).thenReturn(myworldKeys);
        when(section.get("world.myworld.inherit")).thenReturn("world");

        // when
        objectUnderTest.init();
        String inherit = objectUnderTest.getPerWorldEntry("myworld").getInherit();

        // then
        assertEquals("world", inherit);
    }
}
