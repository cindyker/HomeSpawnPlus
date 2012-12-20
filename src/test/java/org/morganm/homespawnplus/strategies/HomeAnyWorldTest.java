/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.morganm.homespawnplus.entity.Home;
import org.morganm.homespawnplus.strategy.StrategyContext;
import org.morganm.homespawnplus.strategy.StrategyMode;
import org.morganm.homespawnplus.strategy.StrategyResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author morganm
 *
 */
public class HomeAnyWorldTest extends HomeStrategyTest {
    @InjectMocks
    private HomeAnyWorld objectUnderTest;
    
    private Set<Home> homeSet;      // test home set to work with
    private Home bedHome;
    private Home defaultHome;
    private Home namedHome;

    StrategyResult mockResult;
    
    @BeforeClass
    public void beforeClass() {
        /*
         * homes and homeSet must be created in @BeforeClass so that
         * the appropriate objects are ready for @DataProvider to
         * reference for result expectations. These objects should
         * not be modified in any way by the test methods.
         */
        homeSet = new HashSet<Home>();

        // create a few homes as our test data set
        bedHome = mock(Home.class);
        when(bedHome.isBedHome()).thenReturn(true);
        when(bedHome.getName()).thenReturn("bedhome");
        homeSet.add(bedHome);

        defaultHome = mock(Home.class);
        when(defaultHome.isDefaultHome()).thenReturn(true);
        when(defaultHome.getName()).thenReturn("defaulthome");
        homeSet.add(defaultHome);

        namedHome = mock(Home.class);
        when(namedHome.getName()).thenReturn("namedhome");
        homeSet.add(namedHome);
    }
    
    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        super.beforeMethod();

        // setup mock result object
        mockResult = mock(StrategyResult.class);
        when(resultFactory.create(isA(Home.class))).thenReturn(mockResult);
        when(resultFactory.create(isNull(Home.class))).thenReturn(mockResult);
    }
    
    @Override
    public Set<Home> getHome(String player) {
        return homeSet;
    }

    @DataProvider
    private Object[][] modeDeterministicTests() {
        return new Object[][] {
                {StrategyMode.MODE_HOME_BED_ONLY, bedHome},
                {StrategyMode.MODE_HOME_DEFAULT_ONLY, defaultHome}
        };
    }

    @Test(dataProvider = "modeDeterministicTests")
    public void testModesDeterministicResults(StrategyMode mode, Home expectedResult) throws Exception {
        // Given
        StrategyContext context = mock(StrategyContext.class);
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
        StrategyContext context = mock(StrategyContext.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isDefaultModeEnabled()).thenReturn(true);

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(isA(Home.class));  // validate we chose a home
        assertEquals(mockResult, result);   // validate we got our expected mock result back
    }

    @Test
    public void testModeAny() throws Exception {
        // Given
        StrategyContext context = mock(StrategyContext.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isModeEnabled(StrategyMode.MODE_HOME_ANY)).thenReturn(true);

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(isA(Home.class));  // validate we chose a home
        assertEquals(mockResult, result);   // validate we got our expected mock result back
    }

    @Test
    public void testModeRequiresBedWithBed() throws Exception {
        // Given
        StrategyContext context = mock(StrategyContext.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isDefaultModeEnabled()).thenReturn(true);
        when(context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED)).thenReturn(true);
        
        when(bedUtil.isBedNearby(isA(Home.class))).thenReturn(true);

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(isA(Home.class));  // validate we chose a home
        assertEquals(mockResult, result);   // validate we got our expected mock result back
    }

    @Test
    public void testModeRequiresBedWithoutBed() throws Exception {
        // Given
        StrategyContext context = mock(StrategyContext.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isDefaultModeEnabled()).thenReturn(true);
        when(context.isModeEnabled(StrategyMode.MODE_HOME_REQUIRES_BED)).thenReturn(true);
        
        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(isNull(Home.class));   // validate no home was chosen
        assertEquals(mockResult, result);   // validate we got our expected mock result back
    }

    @Test
    public void testNoHomes() throws Exception {
        // Given
        StrategyContext context = mock(StrategyContext.class);
        when(context.getPlayer()).thenReturn(player);
        when(context.isDefaultModeEnabled()).thenReturn(true);

        // return no homes
        doAnswer(new Answer<Set<Home>>() {
            public Set<Home> answer(InvocationOnMock invocation) {
                return null;
            }})
            .when(homeDAO).findHomesByPlayer(any(String.class));

        // When
        StrategyResult result = objectUnderTest.evaluate(context);

        // Then
        verify(resultFactory).create(isNull(Home.class));   // validate no home was chosen
        assertEquals(mockResult, result);   // validate we got our expected mock result back
    }
}
