package zombe.core.util;

public class TimeHelper {

    public static String getTime(long time) {
        int daytime = (int)(time % 24000), h = daytime / 1000, m = (int)((daytime % 1000) * 0.06f);
        return (h<10 ? "0" : "") + h + (m<10 ? " : 0" : " : ") + m;
    }

    public static String getRealTime(long time) {
        long d = time / 1728000; time %= 1728000;
        long h = time / 72000; time %= 72000;
        long m = time / 1200; time %= 1200;
        long s = time / 20; time %= 20;
        long u = time / 2;
        return ""+d+(h<10?"\u00a7f : \u00a790":"\u00a7f : \u00a79")+h+(m<10?"\u00a7f : \u00a790":"\u00a7f : \u00a79")+m+(s<10?"\u00a7f : \u00a790":"\u00a7f : \u00a79")+s+"\u00a7f . \u00a79"+u;
    }

}
