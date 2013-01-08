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
package com.andune.minecraft.hsp.strategies;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.andune.minecraft.hsp.entity.HomeImpl;
import com.andune.minecraft.hsp.server.api.Player;
import com.andune.minecraft.hsp.storage.dao.HomeDAO;
import com.andune.minecraft.hsp.util.BedUtils;
import com.andune.minecraft.hsp.util.HomeUtil;

/**
 * @author andune
 *
 */
public abstract class HomeStrategyTest extends BaseStrategyTest {
    @Mock
    protected HomeUtil homeUtil;
    @Mock
    protected BedUtils bedUtil;
    
    @Mock
    protected HomeDAO homeDAO;
    @Mock
    protected Player player;
    
    /** Invoke in subclass, AFTER MockitoAnnotations.initMocks
     * has been called.
     */
    protected void beforeMethod() {
        when(player.getName()).thenReturn("fooplayer");

        doAnswer(new Answer<Set<HomeImpl>>() {
            public Set<HomeImpl> answer(InvocationOnMock invocation) {
                String name = (String) invocation.getArguments()[0];
                if( name.equals("fooplayer") ) {
                    return getHome(name);
                }
                return null;
            }})
            .when(homeDAO).findHomesByPlayer(any(String.class));
    }
    
    protected abstract Set<HomeImpl> getHome(String playerName);
}
