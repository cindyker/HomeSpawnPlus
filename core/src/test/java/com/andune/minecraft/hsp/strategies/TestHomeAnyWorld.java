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

import com.andune.minecraft.hsp.entity.Home;
import com.andune.minecraft.hsp.entity.HomeImpl;
import com.andune.minecraft.hsp.strategies.home.HomeAnyWorld;
import com.andune.minecraft.hsp.strategy.*;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author andune
 */
public class TestHomeAnyWorld extends HomeStrategyTest {
    @InjectMocks
    private HomeAnyWorld objectUnderTest;

    private Set<HomeImpl> homeSet;      // test home set to work with
    private HomeImpl bedHome;
    private HomeImpl defaultHome;
    private HomeImpl namedHome;

    StrategyResultImpl mockResult;

    @BeforeClass
    public void beforeClass() {
        /*
         * homes and homeSet must be created in @BeforeClass so that
         * the appropriate objects are ready for @DataProvider to
         * reference for result expectations. These objects should
         * not be modified in any way by the test methods.
         */
        homeSet = new HashSet<HomeImpl>();

        // create a few homes as our test data set
        bedHome = mock(HomeImpl.class);
        when(bedHome.isBedHome()).thenReturn(true);
        when(bedHome.getName()).thenReturn("bedhome");
        homeSet.add(bedHome);

        defaultHome = mock(HomeImpl.class);
        when(defaultHome.isDefaultHome()).thenReturn(true);
        when(defaultHome.getName()).thenReturn("defaulthome");
        homeSet.add(defaultHome);

        namedHome = mock(HomeImpl.class);
        when(namedHome.getName()).thenReturn("namedhome");
        homeSet.add(namedHome);
    }

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        super.beforeMethod();

        // setup mock result object
        mockResult = mock(StrategyResultImpl.class);
        when(resultFactory.create(isA(HomeImpl.class))).thenReturn(mockResult);
        when(resultFactory.create(isNull(HomeImpl.class))).thenReturn(mockResult);
    }

    @Override
    public Set<HomeImpl> getHome(String player) {
        return homeSet;
    }

    @DataProvider
    private Object[][] modeDeterministicTests() {
        return new Object[][]{
                {StrategyMode.MODE_HOME_BED_ONLY, bedHome},
                {StrategyMode.MODE_HOME_DEFAULT_ONLY, defaultHome}
        };
    }

    @Test(dataProvider = "modeDeterministicTests")
    public void testModesDeterministicResults(StrategyMode mode, Home expectedResult) throws Exception {
        // Given
        StrategyContext context = mock(StrategyContextImpl.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isModeEnabled(mode)).thenReturn(true);

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(expectedResult);       // validate correct home was chosen
        assertEquals(mockResult, result);                   // validate we got our expected mock result back
    }

    @Test
    public void testModeDefault() throws Exception {
        // Given
        StrategyContext context = mock(StrategyContextImpl.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isDefaultModeEnabled()).thenReturn(true);

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(isA(HomeImpl.class));  // validate we chose a home
        assertEquals(mockResult, result);   // validate we got our expected mock result back
    }

    @Test
    public void testModeAny() throws Exception {
        // Given
        StrategyContext context = mock(StrategyContextImpl.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isModeEnabled(StrategyMode.MODE_HOME_ANY)).thenReturn(true);

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(isA(HomeImpl.class));  // validate we chose a home
        assertEquals(mockResult, result);   // validate we got our expected mock result back
    }

    @Test
    public void testModeRequiresBedWithBed() throws Exception {
        // Given
        StrategyContext context = mock(StrategyContextImpl.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isDefaultModeEnabled()).thenReturn(true);
        when(context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED)).thenReturn(true);

        when(bedUtil.isBedNearby(isA(HomeImpl.class))).thenReturn(true);

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(isA(HomeImpl.class));  // validate we chose a home
        assertEquals(mockResult, result);   // validate we got our expected mock result back
    }

    @Test
    public void testModeRequiresBedWithoutBed() throws Exception {
        // Given
        StrategyContext context = mock(StrategyContextImpl.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isDefaultModeEnabled()).thenReturn(true);
        when(context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED)).thenReturn(true);

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(isNull(HomeImpl.class));   // validate no home was chosen
        assertEquals(mockResult, result);   // validate we got our expected mock result back
    }

    @Test
    public void testNoHomes() throws Exception {
        // Given
        StrategyContext context = mock(StrategyContextImpl.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isDefaultModeEnabled()).thenReturn(true);

        // return no homes
        doAnswer(new Answer<Set<HomeImpl>>() {
            public Set<HomeImpl> answer(InvocationOnMock invocation) {
                return null;
            }
        })
                .when(homeDAO).findHomesByPlayer(any(String.class));

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(isNull(HomeImpl.class));   // validate no home was chosen
        assertEquals(mockResult, result);   // validate we got our expected mock result back
    }
}
