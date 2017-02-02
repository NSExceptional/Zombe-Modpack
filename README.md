
# Zombes Modpack

## Important

I am not the offical distributor. This is only a fork to keep it updated, as the last major release was for 1.8. From the original README:

> The one and only official place where this modpack is distributed is:
     www.minecraftforum.net/topic/91055-/
 Be warned: if you got it from elsewhere, it might not be clean.
 Similarly, people who redistribute it elsewhere sometimes put a Donate
 With Paypal button on their page. We never see this money.
 
# Installation
See [Installation Instructions](Installation Instructions.md)


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


