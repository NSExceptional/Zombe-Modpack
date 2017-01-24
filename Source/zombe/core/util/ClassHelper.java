package zombe.core.util;

import java.lang.*;
import java.net.URL;
import java.security.ProtectionDomain;
import java.security.CodeSource;

public final class ClassHelper {

    public static URL getClassSourceURL(Class c) {
        CodeSource cs = c.getProtectionDomain().getCodeSource();
        if (cs == null) return null;
        return cs.getLocation();
    }

}
