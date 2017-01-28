package zombe.launch;

import java.io.File;
import java.util.List;

public class ZombeCascadingTweaker extends ZombeTweaker {

    @Override
    public void acceptOptions(List<String> list, File gameDir, File assetsDir, String profile) {
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }

}
