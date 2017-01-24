# FAQ

## Please read all of the README first.

> *Where is the `.minecraft` folder?*

See README

> *no mods are enabled! Read the readme.txt!* is shown on screen 

All mods are disabled by default and you have not enabled any of them. See README.

> *failed to load configuration from config.txt* is shown on screen

You have misplaced some the config files. Make sure you followed the installation instructions to the T and made no assumptions about anything.

> Minecraft hangs / freezes / black-screens at startup after log-in

This is nearly always caused by `META-INF` folder. See README. Worst case, the vesrion jar is probably damaged because you added a modpack version that does not match Minecraft version. Start installation over from scratch and install the correct modpack version for your Minecraft version.

> It did not hang, but there is no log.txt file  
> There are no errors nor crashes and none of the mods seem to do anything

A log file is created with every minecraft session—if you do not have the log.txt file then you have not installed the mod. Most likely you have misplaces all the .class files—they go into the root folder of the version jar archive. See README.

> *Do any of the mods make my savegames mod-dependant?*

No. Savegames will remain independent of this modpack.

> *Can I still use the savegames when I uninstall the modpack?*

Yes. Savegames will remain independent of this modpack.

> *There is an error on the screen and I do not want to see it, how do i get rid of it?*

Fix the error or press the "clear displayed error" key (default: `F9`).

> *What to do with the files in "source" folder?*

You do not NEED to do anything with them. If you do not know what they are for then just ignore them.

> *Where do I get modpack versions for older Minecraft versions?*

Download [this file](http://dl.dropbox.com/u/19090066/minecraft/readme.html) and search for "version history"

> *How do I allow "fly" mod on my server?*

Change the `allow-flight` option in your `server.properties` file to `true` and restart the server.

> *How do I disallow the "cheat" mod on my server?*

Add `no-z-cheat` to the server welcome message. Alternatively, you can disable it non-visibly too by adding `&f &f &2 &0 &4 &8` to servers wotd message. ("&" marks color codes. If your server uses a wrapper with different codes then use this for reference.)

> *Can I install only one/few of the mods?*

Yes, follow the installation instructions.

> Why does it crash when I open the inventory? 

idk

> wtf? 

idk!
