package zombe.core.loader;

import java.lang.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

public class ZModLoader {
    
    private final Logger log = Logger.getLogger("zombe.core.loader");
    private final ClassScanner scanner;

    public ZModLoader(URL[] urls) {
        log.config("Initializing mod loader...");
        ClassScanner scanner;
        try {
            scanner = new ClassScanner(urls);
        } catch (Exception e) {
            log.warning("Primary class scanner init failed:\n"+e+"\nDefaulting to secondary scanner...");
            try {
                scanner = new ClassScanner(ZModLoader.class);
            } catch (Exception f) {
                log.warning("Secondary class scanner init failed:\n"+f+"\nDefaulting to tertiary scanner...");
                scanner = new ClassScanner();
            }
        }
        this.scanner = scanner;
        log.config("Mod loader initialized correctly.");
    }

    public <T> List<T> loadMods(String path, Class<T> parent) {
        if (parent == null) throw new NullPointerException();
        List<Class> classes = scanner.scanForClasses(path, parent);
        List<T> mods = new LinkedList<T>();
        for (Class c : classes) {
            Constructor constructor;
            try {
                constructor = c.getConstructor();
            } catch (Exception e) {
                log.info("Mod candidate "+c.getName()+" load failed: missing constructor");
                continue;
            }
            Object instance;
            try {
                instance = constructor.newInstance();
            } catch (Exception e) {
                log.info("Mod candidate "+c.getName()+" load failed: instanciation error:\n"+e);
                continue;
            }
            T mod;
            try {
                mod = parent.cast(instance);
            } catch (Exception e) {
                log.info("Mod candidate "+c.getName()+" load failed: bad cast:\n"+e);
                continue;
            }
            mods.add(mod);
        }
        return mods;
    }

}
