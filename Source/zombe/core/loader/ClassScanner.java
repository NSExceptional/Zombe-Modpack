package zombe.core.loader;

import zombe.core.util.ClassHelper;
import java.lang.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

class ClassScanner {

    private final ClassLoader loader;
    private final URL[] urls;
    private final Logger log = Logger.getLogger("zombe.core.loader");

    ClassScanner() {
        this.loader = null;
        this.urls = new URL[]{ ClassHelper.getClassSourceURL(ClassScanner.class) };
    }

    ClassScanner(Class domainClass) {
        this.loader = domainClass.getClassLoader();
        this.urls = new URL[]{ ClassHelper.getClassSourceURL(domainClass) };
    }

    ClassScanner(URL[] roots) {
        this.loader = new URLClassLoader(roots, this.getClass().getClassLoader());
        this.urls = roots;
    }

    ClassLoader getClassLoader() {
        return this.loader;
    }

    URL[] getURLs() {
        return this.urls;
    }

    List<Class> scanForClasses(String path, Class parent) {
        List<Class> classes = new LinkedList<Class>();
        for (URL url : this.urls) {
            try {
                File file = new File(url.toURI());
                String name = file.getName();
                List<String> candidates = file.isDirectory()
                    ? ClassLister.listDirClassCandidates(url.toURI(), path)
                    : (file.isFile() && name.endsWith(".zip") || name.endsWith(".jar"))
                        ? ClassLister.listZipClassCandidates(url, path)
                        : null;
                if (candidates == null) continue;
                for (String candidate : candidates) {
                    try {
                        Class c = (this.loader == null)
                            ? Class.forName(candidate)
                            : Class.forName(candidate, true, this.loader);
                        classes.add(c);
                    } catch(Exception e) {
                        log.info("Class candidate "+candidate+" load failed.");
                    }
                }
                log.info("Classes source '"+url.toString()+"' scan complete: ("+candidates.toString()+").");
            } catch(Exception e) {
                log.warning("Classes source '"+url.toString()+"' scan error.");
            }
        }
        if (parent != null) {
            List<Class> childs = new LinkedList<Class>();
            for (Class candidate : classes) {
                if (parent.isAssignableFrom(candidate))
                    childs.add(candidate);
                else log.info("Class candidate "+candidate.getName()+" discarded: wrong superclass.");
            }
            return childs;
        }
        return classes;
    }

}
