package com.mengcraft.script;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.mengcraft.script.Main.nil;

/**
 * Created on 16-10-17.
 */
public final class EventMapping {

    private final Map<String, Mapping> mapping = new HashMap<>();

    public final static class Mapping {
        private final Class<?> clz;
        private final String name;

        private EventListener listener;

        private Mapping(Class<?> clz) {
            this.clz = clz;
            name = clz.getSimpleName().toLowerCase();
        }

        public boolean isEvent(Event event) {
            return event.getClass() == clz;
        }

        public String getName() {
            return name;
        }

        private EventListener getListener() {
            if (nil(listener)) {
                listener = new EventListener(this);
            }
            return listener;
        }
    }

    public boolean initialized(String name) {
        return mapping.containsKey(name.toLowerCase());
    }

    public EventListener getListener(String name) {
        String id = name.toLowerCase();
        if (!mapping.containsKey(id)) {
            throw new IllegalArgumentException("Not initialized");
        }
        return mapping.get(id).getListener();
    }

    public void init(Plugin plugin) {
        init(plugin.getClass().getClassLoader());
    }

    @SuppressWarnings("unchecked")
    public void init(ClassLoader loader) {
        try {
            Field field = ClassLoader.class.getDeclaredField("classes");
            field.setAccessible(true);
            List<Class<?>> list = (List) field.get(loader);
            for (Class<?> clz : list) {
                if (valid(clz)) {
                    try {
                        init(clz);
                    } catch (Exception e) {
                        Bukkit.getLogger().log(Level.WARNING, "[Script] " + e.getMessage());
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e.toString());
        }
    }

    @SuppressWarnings("unchecked")
    public void init(String plugin) {
        init(Bukkit.getPluginManager().getPlugin(plugin));
    }

    public void init(Class<?> clz) {
        Preconditions.checkArgument(valid(clz), clz.getName() + " not valid");
        String name = clz.getSimpleName().toLowerCase();
        if (!mapping.containsKey(name)) {
            mapping.put(name, new Mapping(clz));
            Bukkit.getLogger().log(Level.INFO, "[Script] Initialized " + clz.getSimpleName());
        }
    }

    private static boolean valid(Class<?> clz) {
        return Event.class.isAssignableFrom(clz) && Modifier.isPublic(clz.getModifiers()) && !Modifier.isAbstract(clz.getModifiers());
    }

    protected static HandlerList getHandler(Mapping mapping) {
        return getHandler(mapping.clz);
    }

    private static HandlerList getHandler(Class<?> clz) {
        try {
            Method e = clz.getDeclaredMethod("getHandlerList");
            e.setAccessible(true);
            return (HandlerList) e.invoke(null);
        } catch (NoSuchMethodException e) {
            Class<?> father = clz.getSuperclass();
            if (valid(father)) {
                return getHandler(father);
            }
            throw new RuntimeException(e.toString());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e.toString());
        }
    }

    public static final EventMapping INSTANCE = new EventMapping();

}
