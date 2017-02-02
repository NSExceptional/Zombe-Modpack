package zombe.core.config;


import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.lang.*;
import java.io.*;
import java.util.logging.*;
import java.util.regex.*;

/** In this class, preferences are stored as key-value pairs. A 'key' refers to a preference. */
public final class Config {
    private static final Logger log = Logger.getLogger("zombe.core.config");
    // TODO what is this?
    private static final Map<String, Option> options = new LinkedHashMap<>();

    @Nonnull private File configFile;
    // TODO what is this?
    @Nonnull private final Properties properties;
    // TODO what is this?
    private final Config parentConfig;


    /** Adds an option to `options` */
    public static void addOption(@Nonnull String name, @Nonnull Option option) {
        if (options.containsKey(name)) {
            throw new IllegalArgumentException();
        }

        options.put(name, option);
    }

    /** @return the Option associated with the given name */
    @Nullable
    public static Option getOption(@Nonnull String name) {
        return options.get(name);
    }

    // TODO there's gotta be a better way to do this...
    private static void printNotice(@Nonnull PrintWriter out) {
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

    /** Creates the default config file */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveDefault(@Nonnull File file) throws IOException {
        if (!file.exists()) {
            log.info("Creating default config file '" + file + "'");
            file.createNewFile();
        }

        try (PrintWriter out = new PrintWriter(file, "UTF-8")) {
            // Print auto-gen warning
            out.println("# AUTO-GENERATED default config file for Zombe Mod");
            out.println("# CHANGES ARE NOT KEPT, modify config.txt instead!");
            printNotice(out);

            // TODO why is he doing if (!cat.equals(category))?
            String previousCategory = null;
            for (Map.Entry<String, Option> entry : options.entrySet()) {
                String key = entry.getKey();
                Option opt = entry.getValue();

                if (!opt.category.equals(previousCategory)) {
                    if (previousCategory == null) {
                        out.println("# ==================== " + opt.category + " ====================");
                    } else {
                        out.println("# ==================== " + opt.category + " mod ====================");
                    }
                    previousCategory = opt.category;
                }

                out.println("# " + opt.description);
                out.println(key + " = " + opt.defaultString() + "\n");
            }
        } catch (FileNotFoundException e) {
            log.info("Error writing default config: " + e.getCause());
            e.printStackTrace();
        }
    }

    @Nonnull
    public Config(@Nonnull File file, Config parent) {
        this.configFile = file;
        this.parentConfig = parent;
        this.properties = new Properties();
    }

    /** Sets this.configFile */
    public void setFile(@Nonnull File file) {
        this.configFile = file;
    }

    /** Populates `this.properties` from `this.configFile` */
    @SuppressWarnings("DuplicateThrows")
    public void load() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        if (!this.configFile.isFile()) {
            log.info("skipped loading config file '" + this.configFile + "': file not found");
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(this.configFile), "UTF-8")) {
            log.info("loading config file '" + this.configFile + "'");
            this.properties.load(reader);
        } catch (Exception e) {
            // TODO log
            e.printStackTrace();
        }
    }

    /** @return whether a given key's value came from a constrant's default value */
    public boolean isDefault(@Nonnull String key) {
        // Default because if it is inherited for everything and
        // the top level config has no parent, then the key
        // must have come from some constraint's default value
        return this.isInherited(key) && (this.parentConfig == null || this.parentConfig.isDefault(key));
    }

    /** @return whether a given key's value would come from `this.parentConfig` */
    public boolean isInherited(@Nonnull String key) {
        return !properties.containsKey(key);
    }

    /** @return the value for the preference key */
    @Nullable
    public Object getValue(@Nonnull String key) {
        if (!options.containsKey(key)) {
            return null;
        }

        String value        = this.get(key);
        Option option       = options.get(key);
        OptionConstraint tc = option.constraint;

        if (value != null && tc.canParse(value)) {
            return tc.parsedOrDefault(value);
        }

        return this.parentConfig == null ? option.defaultValue : parentConfig.getValue(key);
    }

    /** @return all option values for a given key in the config tree, with the default value last */
    @Nonnull
    public List<Object> getValues(@Nonnull String key) {
        if (!options.containsKey(key)) {
            return ImmutableList.of();
        }

        List<Object> list = new ArrayList<>();

        for (Config cfg = this; cfg != null; cfg = cfg.parentConfig) {
            String value = cfg.get(key);
            if (value != null) {
                list.add(value);
            }
        }
        list.add(options.get(key).defaultValue);

        return list;
    }

    /** @return the first preference value in the config tree
     *          for the given key, or uses the default value from `options` */
    @Nullable
    public String getInherited(@Nonnull String key) {
        String value = this.get(key);
        if (value != null) {
            return value;
        }
        if (this.parentConfig != null) {
            return this.parentConfig.getInherited(key);
        }

        if (options.containsKey(key)) {
            return options.get(key).defaultString();
        }

        return null;
    }

    /** @return the first preference value in the config tree for the given key */
    @Nullable
    public String getStringInherited(@Nonnull String key) {
        String value = this.get(key);
        if (value != null) {
            return value;
        }
        if (this.parentConfig != null) {
            return this.parentConfig.getStringInherited(key);
        }

        return null;
    }

    /** @return the preverence value for the given key */
    @Nullable
    public String get(@Nonnull String key) {
        return properties.getProperty(key);
    }

    /** Sets a preference value for the given key */
    public void set(@Nonnull String key, @Nullable String value) {
        if (this.get(key) == null) {
            properties.remove(key);
        } else {
            properties.setProperty(key, value);
        }

        try {
            this.update(key, value);
        } catch (Exception e) {
            log.warning("could not save changes to option "+key);
        }
    }

    @SuppressWarnings("DuplicateThrows")
    private void update(@Nonnull String key, @Nullable String newValue) throws IOException, FileNotFoundException, UnsupportedEncodingException {
        // Create config file if necessary
        if (!configFile.isFile()) {
            log.config("config file '" + configFile + "' not found, creating it");
            configFile.createNewFile();

            try (PrintWriter out = new PrintWriter(configFile, "UTF-8")) {
                out.println("# Zombe's user config file");
                out.println("# CHANGES ARE KEPT, you can modify it freely!");
                out.println("# check default.txt for options and default values");
                printNotice(out);
            } catch (Exception e) {
                // TODO log
                e.printStackTrace();
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), "UTF-8"))) {
            Option opt = getOption(key);
            String category = null, optComment = null;
            Pattern commentRegex  = Pattern.compile("^#.*");
            Pattern categoryRegex = null;
            Pattern optionRegex   = Pattern.compile("^" + key + "\\s*=.*");

            if (opt != null) {
                category = opt.category;
                optComment = "# " + opt.description + " (default " + opt.defaultString() + ")";
                categoryRegex = Pattern.compile("^#\\s*=+\\s*" + category + "\\s+(?:mod\\s+[-.\\w\\s]+)?=+\\s*$");
            }

            // TODO what tf is going on here???
            List<String> list = new ArrayList<>();
            String lineS;
            int lineN = -1, lineC = -1, lineO = -1;
            while ((lineS = reader.readLine()) != null) {
                if (lineC == -1 && opt != null && categoryRegex.matcher(lineS).matches()) {
                    lineC = lineN + 1;
                }

                if (optionRegex.matcher(lineS).matches()) {
                    if (lineO == -1 && newValue != null) {
                        lineO = lineN + 1;
                        lineS = key + " = " + newValue;
                    } else {
                        // This might be removing comments?
                        if (lineN >= 0 && commentRegex.matcher(list.get(lineN)).matches() && lineN != lineC) {
                            list.remove(lineN); --lineN;
                            if (lineN >= 0 && list.get(lineN).trim().equals("")) {
                                list.remove(lineN); --lineN;
                            }
                        }
                        continue;
                    }
                }

                list.add(lineS); lineN--;
            }

            // TODO what tf is going on here???
            if (newValue != null && lineO == -1) {
                if (lineC == -1) {
                    if (lineN == -1 || !list.get(lineN).trim().equals("")) {
                        lineC = lineN;
                        list.add(""); lineN++;
                    }
                    if (opt != null) {
                        list.add("# ==================== " + category + " ====================");
                        lineN++; lineC = lineN;
                        list.add("");
                    }
                }

                list.add(lineC + 1, "");
                lineO = lineC + 2;
                if (opt != null) {
                    list.add(lineO, optComment);
                    lineO++;
                }
                list.add(lineO, key + " = " + newValue);
            }

            try (PrintWriter out = new PrintWriter(configFile, "UTF-8")) {
                for (String line : list) {
                    out.println(line);
                }
            }
        }
    }
}
