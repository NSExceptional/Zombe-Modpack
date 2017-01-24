package zombe.core.util;

import zombe.core.ZWrapper;
import java.lang.*;
import java.util.*;

public final class StringHelper {

    /**
        Turns uppercase the first character and lowercase the others.
    */
    public static String capitalize(String value) {
        if (value == null || value.length() == 0) return value;
        return value.substring(0,1).toUpperCase().concat(value.substring(1).toLowerCase());
    }

    /**
        Reverses a string from start to end
    */
    public static String reverse(String value) {
        String result = "";
        for (int i = 0; i < value.length(); ++i)
            result = value.charAt(i)+result;
        return result;
    }

    /**
        Compare versions numbers
        Ex: compareVersions("1.6.4", "1.7.2") == -1
        Ex: compareVersions("1.7", "1.6.4") == 1
        Versions numbers can only be comprised of alphanumeric characters
        Non numeric characters may produce strange, undefined, results
    */
    public static int compareVersions(String v1, String v2) {
        String[] a1 = v1.split("\\.");
        String[] a2 = v2.split("\\.");
        for (int i = 0; i < a1.length && i < a2.length; ++i) {
            int c = Integer.parseInt(a1[i], 36) - Integer.parseInt(a2[i], 36);
            if (c > 0) return 1;
            if (c < 0) return -1;
        }
        if (a1.length > a2.length) return 1;
        if (a1.length < a2.length) return -1;
        return 0;
    }

    public static int parseInt(String value) {
        return Integer.decode(value.trim());
    }

    public static int parseUnsigned(String str) {
        try {
            int res = Integer.decode(str.trim());
            if (res < 0) return -1;
            return res;
        } catch(Exception whatever) {
            return -1;
        }
    }

    public static Color parseColor(String value) {
        try {
            return new Color(parseInt(value));
        } catch(Exception e) {
            return null;
        }
    }

    public static Integer parseInteger(String str) {
        try {
            return Integer.decode(str.trim());
        } catch(Exception whatever) {
            return null;
        }
    }

    public static Float parseFloat(String str) {
        try {
            return Float.parseFloat(str.trim());
        } catch(Exception whatever) {
            return null;
        }
    }

    public static int parseIdMeta(String text) {
        try {
            String part[] = text.split("/");
            if (part.length > 2) return -1;
            int base = Integer.decode(part[0].trim());
            if (part.length == 2) {
                int meta = Integer.decode(part[1].trim());
                return ZWrapper.getIdMeta(base, meta);
            }
            return base;
        } catch(Exception error) {
            return -1;
        }
    }

    public static int parseBlock(String text) {
        try {
            String part[] = text.split("/");
            if (part.length > 2) return -1;
            int base = Integer.decode(part[0].trim());
            if (part.length == 2) {
                int meta = Integer.decode(part[1].trim());
                return ZWrapper.getBlockIdMeta(base, meta);
            }
            return base;
        } catch(Exception error) {
            return -1;
        }
    }

    public static int parseStack(String text) {
        return parseIdMeta(text);
    }

    public static int parseItem(String text) {
        return parseIdMeta(text);
    }

    public static Collection<String> parseCSV(String text) {
        String[] values = text.split(",");
        List<String> list = new ArrayList<String>();
        for (String value : values) {
            value = value.trim();
            if (value.length() > 0) list.add(value);
        }
        return list;
    }

    public static String blockIdMetaToString(int idmeta) {
        int base = ZWrapper.getBlockId(idmeta);
        int meta = ZWrapper.getBlockMeta(idmeta);
        return "" + base + (meta == 0 ? ""
            : "/" + (meta == ZWrapper.BLOCK_ANY ? -1 : meta));
    }

    public static String stackIdMetaToString(int idmeta) {
        int base = ZWrapper.getStackId(idmeta);
        int meta = ZWrapper.getStackMeta(idmeta);
        return "" + base + (meta == 0 ? ""
            : "/" + (meta == ZWrapper.ID_ANY ? -1 : meta));
    }

}
