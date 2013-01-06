/**
 * 
 */
package org.morganm.homespawnplus.config;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.andune.minecraft.hsp.config.ConfigHomeLimits;
import com.andune.minecraft.hsp.config.ConfigLoader;
import com.andune.minecraft.hsp.config.ConfigOptions;
import com.andune.minecraft.hsp.config.ConfigHomeLimits.LimitsPerPermission;
import com.andune.minecraft.hsp.server.api.ConfigurationSection;

/**
 * @author morganm
 *
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
        HashSet<String> worldKeys = new HashSet<String>(1);
        worldKeys.add("myworld");
        when(section.getKeys("world")).thenReturn(worldKeys);
        HashSet<String> myworldKeys = new HashSet<String>(1);
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
        HashSet<String> entryKeys = new HashSet<String>(1);
        entryKeys.add("entry1");
        when(section.getKeys("permission")).thenReturn(entryKeys);
        
        HashSet<String> entry1Keys = new HashSet<String>(3);
        entry1Keys.add("permissions");
        entry1Keys.add("perWorld");
        entry1Keys.add("global");
        when(section.getKeys("permission.entry1")).thenReturn(entry1Keys);
        
        ArrayList<String> perms = new ArrayList<String>(1);
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
        HashSet<String> entryKeys = new HashSet<String>(1);
        entryKeys.add("entry1");
        when(section.getKeys("permission")).thenReturn(entryKeys);
        
        HashSet<String> entry1Keys = new HashSet<String>(3);
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
        assertTrue(lpp.getPermissions().contains("hsp."+configOptions.basePath()+".entry1"));
    }
}
