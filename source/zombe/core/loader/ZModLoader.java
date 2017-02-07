package zombe.core.loader;


import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ZModLoader<T> {

    private final Logger log = Logger.getLogger("zombe.core.loader");
    private final ClassScanner<T> scanner;

    public ZModLoader(@Nonnull URL[] urls) {
        this.log.config("Initializing mod loader...");
        ClassScanner<T> scanner;
        try {
            scanner = new ClassScanner<>(urls);
        } catch (Exception e) {
            this.log.warning("Primary class scanner init failed:\n" + e + "\nDefaulting to secondary scanner...");
            try {
                scanner = new ClassScanner<>(ZModLoader.class);
            } catch (Exception f) {
                this.log.warning("Secondary class scanner init failed:\n" + f + "\nDefaulting to tertiary scanner...");
                scanner = new ClassScanner<>();
            }
        }

        this.scanner = scanner;
        this.log.config("Mod loader initialized correctly.");
    }

    @Nonnull
    public List<T> loadMods(@Nonnull String path, @Nonnull Class<T> parent) {
        List<Class<T>> classes = this.scanner.scanForClasses(path, parent);
        List<T> mods = new ArrayList<>();

        for (Class<T> c : classes) {
            try {
                Constructor constructor = c.getConstructor();
                Object instance = constructor.newInstance();
                T mod = parent.cast(instance);
                mods.add(mod);

            } catch (NoSuchMethodException e) {
                this.log.info("Mod candidate " + c.getName() + " load failed: missing constructor");
            } catch (ClassCastException e) {
                this.log.info("Mod candidate " + c.getName() + " load failed: bad cast:\n" + e);
            } catch (Exception e) {
                this.log.info("Mod candidate " + c.getName() + " load failed: instanciation error:\n" + e);
            }
        }

        return mods;
    }
}
