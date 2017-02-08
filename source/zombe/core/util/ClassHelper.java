package zombe.core.util;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.security.CodeSource;

public final class ClassHelper {

    @Nullable
    public static URL getClassSourceURL(@Nonnull Class c) {
        CodeSource cs = c.getProtectionDomain().getCodeSource();
        if (cs == null) {
            return null;
        }

        return cs.getLocation();
    }
}
