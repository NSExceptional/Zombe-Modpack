package zombe.core.loader;


import zombe.core.util.ClassHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Logger;

class ClassScanner<T> {

    @Nullable private final ClassLoader loader;
    @Nullable private final URL[] urls;
    private final Logger log = Logger.getLogger("zombe.core.loader");

    ClassScanner() {
        this.loader = null;
        this.urls = new URL[]{ ClassHelper.getClassSourceURL(ClassScanner.class) };
    }

    ClassScanner(@Nonnull Class domainClass) {
        this.loader = domainClass.getClassLoader();
        this.urls = new URL[]{ ClassHelper.getClassSourceURL(domainClass) };
    }

    ClassScanner(@Nonnull URL[] roots) {
        this.loader = new URLClassLoader(roots, this.getClass().getClassLoader());
        this.urls = roots;
    }

    @Nullable
    ClassLoader getClassLoader() {
        return this.loader;
    }

    @Nullable
    URL[] getURLs() {
        return this.urls;
    }

    /** @return A @code{ List<Class<T>> } of subclasses of parent if parent is not null. */
    @Nonnull
    List<Class<T>> scanForClasses(@Nonnull String path, @Nonnull Class<T> parent) {
        List<Class> classes = this.scanForClasses(path);
        List<Class<T>> children = new ArrayList<>();

        for (Class candidate : classes) {
            if (parent.isAssignableFrom(candidate)) {
                //noinspection unchecked
                children.add((Class<T>) candidate);
            } else {
                this.log.info("Class candidate " + candidate.getName() + " discarded: wrong superclass.");
            }
        }

        return children;
    }

    @Nonnull
    private List<Class> scanForClasses(@Nonnull String path) {
        List<Class> classes = new LinkedList<>();
        for (URL url : this.urls) {
            try {
                File file = new File(url.toURI());
                String name = file.getName();
                List<String> candidates;

                if (file.isDirectory()) {
                    candidates = ClassLister.listDirClassCandidates(url.toURI(), path);
                } else {
                    if (file.isFile() && name.endsWith(".zip") || name.endsWith(".jar")) {
                        candidates = ClassLister.listZipClassCandidates(url, path);
                    } else {
                        continue;
                    }
                }

                for (String candidate : candidates) {
                    try {
                        Class c = (this.loader == null) ? Class.forName(candidate) : Class.forName(candidate, true, this.loader);
                        classes.add(c);
                    } catch (Exception e) {
                        this.log.info("Class candidate " + candidate + " load failed.");
                    }
                }
                this.log.info("Classes source '" + url + "' scan complete: (" + candidates + ").");
            } catch (Exception e) {
                this.log.warning("Classes source '" + url + "' scan error.");
            }
        }

        return classes;
    }
}
