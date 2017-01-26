
# Zombes Modpack

## Important

I am not the offical distributor. This is only a fork to keep it updated, as the last major release was for 1.8. From the original README:

> The one and only official place where this modpack is distributed is:
     www.minecraftforum.net/topic/91055-/
 Be warned: if you got it from elsewhere, it might not be clean.
 Similarly, people who redistribute it elsewhere sometimes put a Donate
 With Paypal button on their page. We never see this money.

I don't plan on taking donations.

# Installation

*You can skip this part entirely if you are using a mod-installation tool
like MCPatcher, MagicLauncher or else, and if it worked of course. But I doubt it will.*

If you just want to try out the mod, you can download a pre-packaged version by checking out the [Releases](https://github.com/ThePantsThief/Zombe-Modpack/releases) tab. Put the profile in the `versions` folder in your minecraft folder (see instructions for finding this) and put the other folder in the `mods` folder in your minecraft folder. Then, configure a profile to use the `release 1.11-zmod` version.

### Notes:

- Your local Minecraft folder will hereby be referred to as `$minecraft`
- The folder called `Mod` inside the root folder will be referred to as `$modpack`
- Subfolders of either folder will be referred to as `$folder/subfolder` and nested subfolders as `$folder/subfolder/nested`, etc. You get the picture.
- `$version` refers to a minecraft version number, i.e. `1.8` or `11.1.2`

You can skip to ***New to this?*** if you need careful step-by-step instructions.

--
#### Know what you're doing?

Note: `folder/subfolder/*` means "contents of `folder `". Don't confuse it with `folder/subfolder` which refers to the folder `subfolder` itself.

- Copy `$minecraft/versions/1.11` to `$minecraft/versions/1.11-whatever`
    - Rename files inside accordingly
    - In the JSON file, change `"id": "1.11"` to `"id": "1.11-whatever"`
    - In the JSON file, delete the `downloads` entry entirely
- Open the Minecraft version jar in an archive editor, and
    - Delete the `META-INF` folder
    - Copy `$modpack/jar/*` into the jar
    - Save and close the jar
- Copy `$modpack/config/*` to `$minecraft/mods/zombe`
- Do one of the following:
    - Copy `$modpack/version/zombe/mod`into `$minecraft/mods/zombe` (only has to be done once per mod version)
    - Copy `$modpack/version/zombe` into `$minecraft/versions/1.11-whatever` alongside the JAR and JSON files (must be done for each profile and version)
- Configure a profile to use the new version you created.

--
#### New to this?

1. Locate `$minecraft`. It is in the following locations depending on your OS:

  | OS | Location |
  |---|---|
  | Windows | `%APPDATA%\.minecraft` |
  | GNU/Linux | `~/.minecraft` |
  | macOS | `~/Library/Application\ Support/minecraft` |

2. Create `$minecraft/mods/zombe`
3. Copy the contents of `$modpack/config` into the folder from step 2, so that you have `$minecraft/mods/zombe/names.txt` etc
4. In `$minecraft/versions`, make a copy of the `$version` folder you wish to mod and name it something like `$version-zombe` (I will use this name for the remainder of the instructions). Rename files inside as such:
    - `versions/$version-zombe/$version-zombe.jar`
    - `versions/$version-zombe/$version-zombe.json`
5. Open the file `$version-zombe.json` and make the following change:
  - `"id": "$version"` → `"id": "$version-zombe"`
  - Delete the entire `"downloads"` section (this prevents the launcher from overwriting the jar)
6. Locate `$version-zombe.jar` (aka "the jar"). Open it with an archive editor like 7-zip and inside,
  - Delete the folder `META-INF` (or just delete `META-INF/MOJANGCS.SF`)
  - Copy the contents of `$modpack/jar` into the root of the jar for the mods you wish to use, ~~as not all class files are required for the mod to work.~~
7. **Save and close the jar**
8. Copy contents of `$modpack/version` into `$minecraft/versions/$version-zombe`. You should be left with `$minecraft/versions/$version-zombe/zombe/mod/Fly.class` etc.
  - Alternatively, these files can be installed globally for all versions in `$minecraft/mods` as above. Careful not to overwrite the contents of `$minecraft/mods` from step 3.
9. Make a new profile in the Minecraft Launcher and use the version `release $version-zombe`

That's it.


# Configuration of the mod

You can configure the mod in-game by pressing the `F7` key.

Alternatively, you can edit the configuration file directly. It is located at `$minecraft/mods/zombe/config.txt` (If it doesn't exist, launch the game once and it will be created with default settings). Enable or disable mods by changing their values like so:

    enableSomeMod = yes
    enableSomeOtherMod = no


# Compatibility

1. This mod may or may not be compatible with modloader or Forge, 
    depending on many factors (mods versions, installation order...)

2. It is usually compatible with:
      - ModLoader
      - Optifine
      - Rei's minimap
      - Inventory Tweaks

    **It is usually incompatible with Forge.**

3. The golden rule is to **install Zombe's modpack FIRST, BEFORE ANYTHING ELSE**,
    including Modloader, Forge, and anything else.

  Class files modified by Zombe can conflict with files modified by 
Modloader, Forge and other mods. These files are often essential for 
these mods but not for Zombe which was designed to be more robust to 
missing files, that's the reason behind the previous assertions and 
more specificaly the third.
Of course, with missing files you should also expect missing features.


## Troubleshooting

- [My FAQs page](#FAQs.md)
- [Original readme](http://dl.dropbox.com/u/19090066/minecraft/readme.html) (if link is broken, open `readme_original.html` in this repo)
- [Official Zombe modpack forum thread](www.minecraftforum.net/topic/91055-/) on minecraftforum.net.

## Credits

- Zombe (aka tanzanite) as the original developer, up to 1.2.5
- md_5 for 1.3.1
- NolanSyKinsley for 1.3.2 and 1.4.2
- Nilat for 1.4.4 to 1.8
- [ThePantsThief](https://github.com/ThePantsThief) for 1.11 and 1.11.2


