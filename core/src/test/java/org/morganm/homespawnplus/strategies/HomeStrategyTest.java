/**
 * 
 */
package org.morganm.homespawnplus.strategies;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.morganm.homespawnplus.entity.HomeImpl;
import org.morganm.homespawnplus.server.api.Player;
import org.morganm.homespawnplus.storage.dao.HomeDAO;
import org.morganm.homespawnplus.util.BedUtils;
import org.morganm.homespawnplus.util.HomeUtil;

/**
 * @author morganm
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
