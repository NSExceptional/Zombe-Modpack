package zombe.core.loader;

import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

class ClassLister {

    static List<String> listDirClassCandidates(URI root, String path) throws IllegalArgumentException, SecurityException {
        if (root == null) throw new NullPointerException();
        if (path == null) path = "";
        String subpath = path.replace('.', File.separatorChar);
        if (!subpath.equals("") && !subpath.endsWith(File.separator))
            subpath = subpath+File.separatorChar;
        String classprefix = path.replace(File.separatorChar, '.');
        if (!classprefix.equals("") && !classprefix.endsWith("."))
            classprefix = classprefix+".";
        File folder;
        try {
            folder = new File(new File(root), subpath);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        List<String> candidates = new LinkedList<String>();
        try {
            if (folder.isDirectory()) {
                File[] list = folder.listFiles();
                for (File f : list) {
                    String name = f.getName();
                    if (f.isFile() && name.endsWith(".class")) {
                        candidates.add(classprefix+name.substring(0, name.length()-".class".length()));
                    }
                }
            }
        } catch (SecurityException e) {
            throw e;
        }
        return candidates;
    }

    static List<String> listZipClassCandidates(URL jar, String path) throws IOException, Exception {
        if (jar == null) throw new NullPointerException();
        if (path == null) path = "";
        String subpath = path.replace('.', File.separatorChar);
        if (!subpath.equals("") && !subpath.endsWith(File.separator))
            subpath = subpath+File.separatorChar;
        String classprefix = path.replace(File.separatorChar, '.');
        if (!classprefix.equals("") && !classprefix.endsWith("."))
            classprefix = classprefix+".";
        InputStream stream = jar.openStream();
        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(stream);
        } catch(Exception e) {
            stream.close();
            throw e;
        }
        List<String> candidates = new LinkedList<String>();
        try {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String file = entry.getName();
                int last = file.lastIndexOf(File.separatorChar);
                String name = file.substring(last+1);
                if (!entry.isDirectory() && file.equals(subpath+name) && name.endsWith(".class")) {
                    candidates.add(classprefix+name.substring(0, name.length()-".class".length()));
                }
            }
        } finally {
            zip.close();
        }
        return candidates;
    }

}
