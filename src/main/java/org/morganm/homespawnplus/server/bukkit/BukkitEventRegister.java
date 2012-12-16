/**
 * 
 */
package org.morganm.homespawnplus.server.bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.morganm.homespawnplus.server.api.EventRegister;
import org.morganm.homespawnplus.server.api.event.EventMethod;

/**
 * @author morganm
 *
 * @deprecated
 */
public class BukkitEventRegister implements EventRegister, Listener {
    private final Plugin plugin;
    
    public BukkitEventRegister(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerEvents(Object o) {
        Method[] methods = o.getClass().getMethods();
        
        for (final Method method : methods) {
            EventMethod annotation = method.getAnnotation(EventMethod.class);
            if( annotation == null )
                continue;
            
            switch(annotation.priority()) {
            case NORMAL:
                
            }
            
            final Class<?> checkClass = method.getParameterTypes()[0].asSubclass(Event.class);

            registerEvent()
        }
    }
    
    private class EventBind<ApiClass extends org.morganm.homespawnplus.server.api.event.Event, BukkitClass extends org.bukkit.event.Event>
    {
        Class<ApiClass> apiClass;
        Method method;
        Object listener;
        
        public EventBind(Class<ApiClass> clazz, Method method, Object listener) {
            this.apiClass = apiClass;
            this.method = method;
            this.listener = listener;
        }
        
        public void processEvent(BukkitClass event) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
            Constructor<ApiClass> constructor = apiClass.getConstructor(apiClass);
            ApiClass apiEvent = constructor.newInstance(event);
            method.invoke(listener, apiEvent);
        }
    }
    
    private void register(Class<? extends org.morganm.homespawnplus.server.api.event.Event> apiClass,
            Method method, Object listener, EventPriority priority) {
        final EventBind eb = new EventBind(apiClass, method, listener);
        
        final org.bukkit.event.Event bukkitEvent = null;
        
        EventExecutor executor = new EventExecutor() {
            public void execute(Listener listener, org.bukkit.event.Event event) throws EventException {
                try {
                    eb.processEvent(event);
                } catch (Throwable t) {
                    throw new EventException(t);
                }
            }
        };
        
        plugin.getServer().getPluginManager().registerEvent(bukkitEvent.getClass(),
                this,
                priority,
                executor,
                plugin);
        

        /*
        new EventExecutor() {
            public void execute(Listener listener, Event event) throws EventException {
                try {
                    onPlayerJoin((PlayerJoinEvent) event);
                } catch (Throwable t) {
                    throw new EventException(t);
                }
            }
        },
        */
    }

}
