package zombe.core.loader;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ClassLister {

    @Nonnull
    static List<String> listDirClassCandidates(@Nonnull URI root, @Nullable String path) {
        if (path == null) {
            path = "";
        }

        String subpath = path.replace('.', File.separatorChar);
        if (!subpath.equals("") && !subpath.endsWith(File.separator)) {
            subpath = subpath + File.separatorChar;
        }

        String classprefix = path.replace(File.separatorChar, '.');
        if (!classprefix.equals("") && !classprefix.endsWith(".")) {
            classprefix = classprefix + ".";
        }

        File folder = new File(new File(root), subpath);
        List<String> candidates = new LinkedList<>();
        if (folder.isDirectory()) {
            File[] list = folder.listFiles();

            if (list != null) {
                for (File f : list) {
                    String name = f.getName();
                    if (f.isFile() && name.endsWith(".class")) {
                        candidates.add(classprefix + name.substring(0, name.length() - ".class".length()));
                    }
                }
            }
        }

        return candidates;
    }

    @Nonnull
    static List<String> listZipClassCandidates(@Nonnull URL jar, @Nullable String path) throws IOException {
        if (path == null) {
            path = "";
        }

        String subpath = path.replace('.', File.separatorChar);
        if (!subpath.equals("") && !subpath.endsWith(File.separator)) {
            subpath = subpath + File.separatorChar;
        }

        String classprefix = path.replace(File.separatorChar, '.');
        if (!classprefix.equals("") && !classprefix.endsWith(".")) {
            classprefix = classprefix + ".";
        }

        try (ZipInputStream zip = new ZipInputStream(jar.openStream())) {
            List<String> candidates = new LinkedList<>();
            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                String file = entry.getName();
                int last = file.lastIndexOf(File.separatorChar);
                String name = file.substring(last + 1);
                if (!entry.isDirectory() && file.equals(subpath + name) && name.endsWith(".class")) {
                    candidates.add(classprefix + name.substring(0, name.length() - ".class".length()));
                }
            }

            return candidates;
        }
    }
}
