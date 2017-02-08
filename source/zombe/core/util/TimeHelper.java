package zombe.core.util;


import javax.annotation.Nonnull;

public class TimeHelper {

    @Nonnull
    public static String getTime(long time) {
        int daytime = (int) (time % 24000);
        int h = daytime / 1000;
        int m = (int) ((daytime % 1000) * 0.06f);
        return (h < 10 ? "0" : "") + h + (m < 10 ? " : 0" : " : ") + m;
    }

    @Nonnull
    public static String getRealTime(long time) {
        long d = time / 1728000; time %= 1728000;
        long h = time / 72000; time %= 72000;
        long m = time / 1200; time %= 1200;
        long s = time / 20; time %= 20;
        long u = time / 2;

        String ds = (h < 10 ? "\u00a7f : \u00a790" : "\u00a7f : \u00a79");
        String hs = (m < 10 ? "\u00a7f : \u00a790" : "\u00a7f : \u00a79");
        String ms = (s < 10 ? "\u00a7f : \u00a790" : "\u00a7f : \u00a79");
        return "" + d + ds + h + hs + m + ms + s + "\u00a7f . \u00a79" + u;
    }
}
