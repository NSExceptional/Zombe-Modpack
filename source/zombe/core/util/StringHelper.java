package zombe.core.util;


import zombe.core.ZWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.*;
import java.util.*;

public final class StringHelper {

    /** Turns uppercase the first character and lowercase the others. */
    @Nullable
    @SuppressWarnings("ConstantConditions")
    public static String capitalize(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        return value.substring(0, 1).toUpperCase().concat(value.substring(1).toLowerCase());
    }

    /** Reverses a string from start to end */
    @Nonnull
    public static String reverse(@Nonnull String value) {
        String result = "";
        for (int i = 0; i < value.length(); ++i) {
            result = value.charAt(i) + result;
        }
        return result;
    }

    /**
     * Compare versions numbers
     * Ex: compareVersions("1.6.4", "1.7.2") == -1
     * Ex: compareVersions("1.7", "1.6.4") == 1
     * Versions numbers can only be comprised of alphanumeric characters
     * Non numeric characters may produce strange, undefined, results
     */
    public static int compareVersions(@Nonnull String v1, @Nonnull String v2) {
        String[] a1 = v1.split("\\.");
        String[] a2 = v2.split("\\.");
        for (int i = 0; i < a1.length && i < a2.length; i++) {
            int comp = ((Integer) Integer.parseInt(a1[i], 36)).compareTo(Integer.parseInt(a2[i], 36));
            if (comp != 0) {
                return comp;
            }
        }

        if (a1.length > a2.length) {
            return 1;
        }
        if (a1.length < a2.length) {
            return -1;
        }

        return 0;
    }

    public static int parseInt(@Nonnull String value) {
        return Integer.decode(value.trim());
    }

    @Nullable
    public static Integer parseUnsigned(@Nonnull String str) {
        try {
            int res = Integer.decode(str.trim());
            return res < 0 ? null : res;
        } catch (Exception whatever) {
            return null;
        }
    }

    @Nullable
    public static Color parseColor(@Nonnull String value) {
        try {
            return new Color(parseInt(value));
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Integer parseInteger(@Nonnull String str) {
        try {
            return Integer.decode(str.trim());
        } catch (Exception whatever) {
            return null;
        }
    }

    @Nullable
    public static Float parseFloat(@Nonnull String str) {
        try {
            return Float.parseFloat(str.trim());
        } catch (Exception whatever) {
            return null;
        }
    }

    @Nullable
    public static Integer parseIdMeta(@Nonnull String text) {
        try {
            String part[] = text.split("/");
            if (part.length > 2) {
                return -1;
            }
            int base = Integer.decode(part[0].trim());
            if (part.length == 2) {
                int meta = Integer.decode(part[1].trim());
                return ZWrapper.getIdMeta(base, meta);
            }
            return base;
        } catch (Exception error) {
            return null;
        }
    }

    @Nullable
    public static Integer parseBlock(@Nonnull String text) {
        try {
            String part[] = text.split("/");
            if (part.length > 2) {
                return -1;
            }
            int base = Integer.decode(part[0].trim());
            if (part.length == 2) {
                int meta = Integer.decode(part[1].trim());
                return ZWrapper.getBlockIdMeta(base, meta);
            }
            return base;
        } catch (Exception error) {
            return null;
        }
    }

    @Nullable
    public static Integer parseStack(@Nonnull String text) {
        return parseIdMeta(text);
    }

    @Nullable
    public static Integer parseItem(@Nonnull String text) {
        return parseIdMeta(text);
    }

    @Nonnull
    public static Collection<String> parseCSV(@Nonnull String text) {
        String[] values = text.split(",");
        List<String> list = new ArrayList<>();
        for (String value : values) {
            value = value.trim();
            if (value.length() > 0) {
                list.add(value);
            }
        }
        return list;
    }

    @Nonnull
    public static String blockIdMetaToString(int idmeta) {
        int base = ZWrapper.getBlockId(idmeta);
        int meta = ZWrapper.getBlockMeta(idmeta);
        return "" + base + (meta == 0 ? "" : "/" + (meta == ZWrapper.BLOCK_ANY ? -1 : meta));
    }

    @Nonnull
    public static String stackIdMetaToString(int idmeta) {
        int base = ZWrapper.getStackId(idmeta);
        int meta = ZWrapper.getStackMeta(idmeta);
        return "" + base + (meta == 0 ? "" : "/" + (meta == ZWrapper.ID_ANY ? -1 : meta));
    }

}
