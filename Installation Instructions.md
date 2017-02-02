# Forge / LiteMod

Coming soon

# JAR Installation

*You can skip this entirely if you are using a mod-installation tool
like MCPatcher, MagicLauncher or else, and if it worked of course. But I doubt it will.*

--

If you just want to try out the jar mod, you can download a pre-packaged version by checking out the [Releases](https://github.com/ThePantsThief/Zombe-Modpack/releases) tab. Put the profile in the `versions` folder in your minecraft folder (see instructions for finding this) and put the other folder in the `mods` folder in your minecraft folder. Then, configure a profile to use the `release 1.11-zmod` version.

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
