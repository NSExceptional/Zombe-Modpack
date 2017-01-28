package zombe.core.config;


import java.util.*;
import java.lang.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

public final class Config {
    private static final Logger log = Logger.getLogger("zombe.core.config");
    private static final Map<String,Option> options = new LinkedHashMap<String,Option>();

    public static void addOption(String name, Option option) {
        if (options.containsKey(name)) throw new IllegalArgumentException();
        if (option == null) throw new NullPointerException();
        options.put(name, option);
    }

    public static Option getOption(String name) {
        return options.get(name);
    }

    private static void printNotice(PrintWriter out) {
        out.println("#");
        out.println("# Comments: lines starting with '#' or '!' are comments.");
        out.println("#");
        out.println("# Keys: a full list of key names can be found here:");
        out.println("#   http://www.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.KEY_0");
        out.println("# - omit the 'KEY_' in front - ie: 'Q' instead of 'KEY_Q' etc");
        out.println("# - you can directly bind mouse buttons as if they were keys");
        out.println("#   named MOUSE1, MOUSE2, etc or BUTTON1, BUTTON2, etc");
        out.println("#");
        out.println("# Colors: must respect the format 0xRRGGBB");
        out.println("#   where R/G/B are the color components in hexadecimal format");
        out.println();
    }

    public static void saveDefault(File file) throws IOException, FileNotFoundException, UnsupportedEncodingException {
        if (file == null) throw new NullPointerException();
        log.info("creating default config file '"+file.toString()+"'");
        if (!file.isFile()) file.createNewFile();
        PrintWriter out = null;
        try {
            out = new PrintWriter(file, "UTF-8");
            out.println("# Zombe's AUTO-GENERATED default config file");
            out.println("# CHANGES ARE NOT KEPT, modify config.txt instead!");
            printNotice(out);

            String category = null;
            for (Map.Entry<String,Option> opt : options.entrySet()) {
                String cat = opt.getValue().getCategory();
                if (cat != category) {
                    if (category == null)
                        out.println("# ==================== "+cat+" ====================");
                    else out.println("# ==================== "+cat+" mod ====================");
                    category = cat;
                }
                out.println("# "+opt.getValue().getDescription());
                out.println(opt.getKey()+" = "+opt.getValue().getConstraint().toString(opt.getValue().getDefaultValue()));
                out.println();
            }
        } finally {
            if (out != null) out.close();
        }
    }

    private File configFile;
    private final Properties properties;
    private final Config parentConfig;

    public Config(File file, Config parent) {
        this.configFile = file;
        this.parentConfig = parent;
        this.properties = new Properties();
    }

    public void setFile(File file) {
        this.configFile = file;
    }

    public void load() throws IOException, FileNotFoundException, UnsupportedEncodingException {
        if (configFile == null) throw new NullPointerException();
        if (!configFile.isFile()) {
            log.info("skipped loading config file '"+configFile.toString()+"': file not found");
            return;
        }
        log.info("loading config file '"+configFile.toString()+"'");
        FileInputStream fs = null;
        InputStreamReader reader = null;
        try {
            fs = new FileInputStream(configFile);
            reader = new InputStreamReader(fs, "UTF-8");
            properties.load(reader);
        } finally {
            if (reader != null) reader.close();
            else if (fs != null) fs.close();
        }
    }

    public boolean isDefault(String key) {
        return isInherited(key) && (parentConfig == null || parentConfig.isDefault(key));
    }

    public boolean isInherited(String key) {
        return !properties.containsKey(key);
    }

    public Object getValue(String key) {
        if (!options.containsKey(key)) return null;
        Option opt = options.get(key);
        TypeConstraint tc = opt.getConstraint();
        String value = get(key);
        if (value != null && tc.typeMatches(value))
            return tc.fromString(value);
        if (parentConfig == null)
            return opt.getDefaultValue();
        return parentConfig.getValue(key);
    }
    
    public Collection getValues(String key) {
        LinkedList list = new LinkedList();
        if (!options.containsKey(key)) return list;
        Option opt = options.get(key);
        TypeConstraint tc = opt.getConstraint();
        for (Config cfg = this; cfg != null; cfg = cfg.parentConfig) {
            String value = cfg.get(key);
            if (value != null) list.add(value);
        }
        list.add(opt.getDefaultValue());
        return list;
    }

    public String getInherited(String key) {
        String value = get(key);
        if (value != null) return value;
        if (parentConfig != null) return parentConfig.getInherited(key);
        if (!options.containsKey(key)) return null;
        Option opt = options.get(key);
        TypeConstraint tc = opt.getConstraint();
        return tc.toString(opt.getDefaultValue());
    }

    public String getString(String key) {
        String value = get(key);
        if (value != null) return value;
        if (parentConfig != null) return parentConfig.getString(key);
        return null;
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public void set(String key, String value) {
        if (key == null) throw new NullPointerException();
        String old = get(key);
        if (value == old) return;
        if (value != null) properties.setProperty(key, value);
        else properties.remove(key);
        try {
            update(key, old, value);
        } catch (Exception e) {
            log.warning("could not save changes to option "+key);
        }
    }

    private void update(String key, String oldValue, String newValue) throws IOException, FileNotFoundException, UnsupportedEncodingException {
        if (configFile == null) throw new NullPointerException();
        if (!configFile.isFile()) {
            log.config("config file '"+configFile.toString()+"' not found, creating it");
            configFile.createNewFile();
            PrintWriter out = null;
            try {
                out = new PrintWriter(configFile, "UTF-8");
                out.println("# Zombe's user config file");
                out.println("# CHANGES ARE KEPT, you can modify it freely!");
                out.println("# check default.txt for options and default values");
                printNotice(out);
            } finally {
                if (out != null) out.close();
            }
        }

        FileInputStream fs = null;
        InputStreamReader reader = null;
        BufferedReader buffer = null;
        PrintWriter out = null;
        try {
            fs = new FileInputStream(configFile);
            reader = new InputStreamReader(fs, "UTF-8");
            buffer = new BufferedReader(reader);

            Option opt = getOption(key);
            String cat = (opt == null) ? null : opt.getCategory();
            String optComment = (opt == null) ? null : "# "+opt.getDescription()+" (default "+opt.getConstraint().toString(opt.getDefaultValue())+")";

            Pattern comPattern = Pattern.compile("^#.*");
            Pattern catPattern = (cat == null) ? null : Pattern.compile("^#\\s*=+\\s*"+cat+"\\s+(?:mod\\s+[-.\\w\\s]+)?=+\\s*$");
            Pattern optPattern = Pattern.compile("^"+key+"\\s*=.*");

            ArrayList<String> list = new ArrayList<String>();
            int lineN = -1, lineC = -1, lineO = -1;
            String lineS = null;
            while ((lineS = buffer.readLine()) != null) {
                if (lineC == -1 && opt != null && catPattern.matcher(lineS).matches()) lineC = lineN+1;
                if (optPattern.matcher(lineS).matches()) {
                    if (lineO == -1 && newValue != null) {
                        lineO = lineN+1;
                        lineS = key+" = "+newValue;
                    } else {
                        if (lineN >= 0 && comPattern.matcher(list.get(lineN)).matches() && lineN != lineC) {
                            list.remove(lineN); --lineN;
                            if (lineN >= 0 && list.get(lineN).trim().equals("")) {
                                list.remove(lineN); --lineN;
                            }
                        }
                        continue;
                    }
                }
                list.add(lineS); ++lineN;
            }
            buffer.close(); buffer = null; reader = null; fs = null;

            if (newValue != null && lineO == -1) {
                if (lineC == -1) {
                    if (lineN == -1 || !list.get(lineN).trim().equals("")) {
                        lineC = lineN;
                        list.add(""); ++lineN;
                    }
                    if (opt != null) {
                        list.add("# ==================== "+cat+" ====================");
                        ++lineN; lineC = lineN;
                        list.add(""); ++lineN;
                    }
                }
                list.add(lineC+1, ""); ++lineN;
                lineO = lineC+2;
                if (opt != null) {
                    list.add(lineO, optComment);
                    ++lineO; ++lineN;
                }
                list.add(lineO, key+" = "+newValue); ++lineN;
            }
            
            out = new PrintWriter(configFile, "UTF-8");
            for (String line : list) {
                out.println(line);
            }
        } finally {
            if (buffer != null) buffer.close();
            else if (reader != null) reader.close();
            else if (fs != null) fs.close();
            if (out != null) out.close();
        }
    }
}
