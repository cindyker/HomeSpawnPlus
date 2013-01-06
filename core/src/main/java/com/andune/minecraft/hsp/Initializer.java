/**
 * 
 */
package com.andune.minecraft.hsp;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.andune.minecraft.hsp.config.ConfigBase;
import com.google.inject.Injector;

/** This class is used to initialize all classes within HSP that
 * implement the Initializable interface, in order of priority.
 * 
 * @author morganm
 *
 */
@Singleton
public class Initializer {
    private static final Logger log = LoggerFactory.getLogger(Initializer.class);

    private final Reflections reflections;
    private final Injector injector;
    
    @Inject
    public Initializer(Reflections reflections, Injector injector) {
        this.reflections = reflections;
        this.injector = injector;
    }

    public void initAll() throws Exception {
        long startupBegin = System.currentTimeMillis();
        for(Initializable init : getSortedInitObjects()) {
            log.debug("[Startup Timer] starting {} (t+{})", init, System.currentTimeMillis()-startupBegin);
            long startupTimer = System.currentTimeMillis();
            
            init.init();
            
            log.debug("[Startup Timer] {} finished in {}ms", init, System.currentTimeMillis()-startupTimer);
        }
    }
    
    /**
     * Called to initialize Config objects only, useful for reloading
     * configuration files.
     * 
     * @throws Exception
     */
    public void initConfigs() throws Exception {
        for(Initializable init : getSortedInitObjects()) {
            if( init instanceof ConfigBase )
                init.init();
        }
    }

    public void shutdownAll()  {
        Collection<Initializable> collection = getSortedInitObjects();
        Initializable[] objects = collection.toArray(new Initializable[] {});

        // shut them down in reverse order of startup
        for(int i = objects.length-1; i >= 0 ; i--) {
            try {
                objects[i].shutdown();
            }
            catch(Exception e) {
                log.error("Caught exception in shutdownAll()", e);
            }
        }
    }

    /**
     * Return all Initializable objects in their proper loading order.
     * @return
     */
    private Collection<Initializable> getSortedInitObjects() {
        TreeMap<Integer, List<Initializable>> sortedMap = new TreeMap<Integer, List<Initializable>>();
        
        // sort into a TreeMap which will maintain order. Items of same priority
        // are added to a List keyed by that priority
        for(Class<? extends Initializable> initClass : getInitClasses()) {
            log.debug("Initializer: getting class intance for {}", initClass);
            Initializable init = injector.getInstance(initClass);
            int priority = init.getInitPriority();
            if( priority < 0 )
                priority = 0;
            List<Initializable> list = sortedMap.get(priority);
            if( list == null ) {
                list = new ArrayList<Initializable>();
                sortedMap.put(priority, list);
            }
            list.add(init);
        }
        
        // Now iterate through the map in order of priority and add them
        // all to a single flat result array that will be all Initializable
        // objects sorted in order of priority
        List<Initializable> result = new ArrayList<Initializable>(10);
        for(List<Initializable> list : sortedMap.values()) {
            result.addAll(list);
        }

        return result;
    }
    
    /**
     * Return all Initializable classes, minus any abstract classes.
     * 
     * @return
     */
    private Set<Class<? extends Initializable>> getInitClasses() {
        Set<Class<? extends Initializable>> initClasses = reflections.getSubTypesOf(Initializable.class);
        for(Iterator<Class<? extends Initializable>> i = initClasses.iterator(); i.hasNext();) {
            Class<? extends Initializable> initClass = i.next();
            // skip any abstract classes
            if( Modifier.isAbstract(initClass.getModifiers()) )
                i.remove();
        }
        return initClasses;
    }
}
