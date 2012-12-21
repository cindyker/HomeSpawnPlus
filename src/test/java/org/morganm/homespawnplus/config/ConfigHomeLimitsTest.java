/**
 * 
 */
package org.morganm.homespawnplus.config;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.morganm.homespawnplus.config.ConfigHomeLimits.LimitsPerPermission;
import org.morganm.homespawnplus.server.api.Plugin;
import org.morganm.homespawnplus.server.api.YamlFile;
import org.morganm.mBukkitLib.JarUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
//import static org.powermock.api.mockito.PowerMockito.mock;
//import static org.powermock.api.mockito.PowerMockito.spy;
//import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
//import static org.powermock.api.mockito.PowerMockito.when;
//import static org.powermock.api.support.membermodification.MemberMatcher.method;
//import static org.powermock.api.support.membermodification.MemberModifier.suppress;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.testng.PowerMockTestCase;

/**
 * @author morganm
 *
 */
//@PrepareForTest(ConfigHomeLimits.class)
public class ConfigHomeLimitsTest { //extends PowerMockTestCase {
    private static final String BASE = "homeLimits";

    YamlFile yaml;
    @Mock Plugin plugin;
    @Mock JarUtils jarUtil;
    
    ConfigHomeLimits objectUnderTest;
    
    @BeforeMethod
    public void beforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        yaml = mock(YamlFile.class, withSettings().verboseLogging());
        
        // we manually inject dependencies instead of using @InjectMocks
        // because @InjectMocks isn't as smart as Guice to invoke @Inject
        // setters; @InjectMocks sees the @Inject constructor and considers
        // it job done.
        objectUnderTest = spy(new ConfigHomeLimits(yaml));
        objectUnderTest.setPlugin(plugin);
        objectUnderTest.setJarUtil(jarUtil);

//        System.out.println("yaml = "+System.identityHashCode(yaml));
//        System.out.println("objectUnderTest.yaml = "+System.identityHashCode(objectUnderTest.yaml));
    }
    
    @Test
    public void testLoad() throws Exception {
        objectUnderTest.load();
        verify(yaml).load(isA(File.class));
    }
    
    @Test
    public void testDefaultLimits() throws Exception {
        // given
        when(yaml.getInteger(BASE+".default.global")).thenReturn(Integer.valueOf(5));
        when(yaml.getInteger(BASE+".default.perWorld")).thenReturn(Integer.valueOf(3));
        
        // when
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
        when(yaml.getKeys(BASE+".world")).thenReturn(worldKeys);
        HashSet<String> myworldKeys = new HashSet<String>(1);
        myworldKeys.add("global");
        myworldKeys.add("perWorld");
        when(yaml.getKeys(BASE+".world.myworld")).thenReturn(myworldKeys);
        when(yaml.get(BASE+".world.myworld.global")).thenReturn(Integer.valueOf(5));
        when(yaml.get(BASE+".world.myworld.perWorld")).thenReturn(Integer.valueOf(3));

        // when
        objectUnderTest.load();
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
        when(yaml.getKeys(BASE+".permission")).thenReturn(entryKeys);
        
        HashSet<String> entry1Keys = new HashSet<String>(3);
        entry1Keys.add("permissions");
        entry1Keys.add("perWorld");
        entry1Keys.add("global");
        when(yaml.getKeys(BASE+".permission.entry1")).thenReturn(entry1Keys);
        
        ArrayList<String> perms = new ArrayList<String>(1);
        perms.add("dummyperm");
        when(yaml.getStringList(BASE+".permission.entry1.permissions")).thenReturn(perms);
        
        when(yaml.get(BASE+".permission.entry1.global")).thenReturn(Integer.valueOf(5));
        when(yaml.get(BASE+".permission.entry1.perWorld")).thenReturn(Integer.valueOf(3));

        // when
        objectUnderTest.load();
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
        when(yaml.getKeys(BASE+".permission")).thenReturn(entryKeys);
        
        HashSet<String> entry1Keys = new HashSet<String>(3);
        entry1Keys.add("perWorld");
        entry1Keys.add("global");
        when(yaml.getKeys(BASE+".permission.entry1")).thenReturn(entry1Keys);

        when(yaml.get(BASE+".permission.entry1.global")).thenReturn(Integer.valueOf(5));
        when(yaml.get(BASE+".permission.entry1.perWorld")).thenReturn(Integer.valueOf(3));

        // when
        objectUnderTest.load();
        LimitsPerPermission lpp = objectUnderTest.getPerPermissionEntries().get("entry1");
        
        // then
        assertNotNull(lpp);
        assertEquals(Integer.valueOf(5), lpp.getGlobal());
        assertEquals(Integer.valueOf(3), lpp.getPerWorld());
        assertTrue(lpp.getPermissions().contains("hsp."+BASE+".entry1"));
        assertTrue(lpp.getPermissions().contains("hsp.entry.entry1"));
        assertTrue(lpp.getPermissions().contains("group.entry1"));
    }
}
