
## All included files
Files                   | Purpose                    | Used by
------------------------+----------------------------+-----------------------
`changelog.txt`         | changelog                  | you
`README.txt`            | install/usage instructions | you
(this file)             | package files listing      | you
`classes/*.class`       | modified minecraft classes | ZModpack's core, mods
`classes/zombe/*.class` | zombe's modpack classes    | ZModpack's core, mods
`config/*.txt`          | configuration files        | ZModpack, you
`mods/**/*.class`       | zombe's mods classes       | ZModpack's core, mods
`src/*.java`            | modpack's source code      | ZModpack's devs, MCP

## Mod dependencies
Mod     | Class dependencies
--------+-----------
modpack | `Minecraft`
Build   | `NetHandlerPlayServer`
Cheat   | `EntityPlayerSP, EntityPlayerMP`
Cloud   | `WorldProvider`
Death   | `EntityPlayerMP`
Dig     | `EntityPlayerSP`, `PlayerControllerMP`, `NetHandlerPlayServer`
Fly     | `EntityPlayerSP`, `MovementInputFromOptions`
noclip  | `EntityPlayerMP`, `NetHandlerPlayServer`
Ghost   | `EntityPlayerSP`, `PlayerControllerMP`
Info    | None
Motion  | `EntityPlayerSP`
Radar   | None
Safe    | None
Sun     | `WorldProvider`

## Optional mod dependencies
Mod     | Class dependencies
--------+-----------
Boom    | `Explosion`
Craft   | `GuiContainer`
Furnace | `TileEntityFurnace`
Resize  | `RenderLiving`

## Modified Minecraft classes
File   | Class name                 | Relevant mod(s)
-------+----------------------------+----------------
`bgd`  | `WorldProvider`            | Cloud, Sun
`bsu`  | `Minecraft`                | modpack
`cem`  | `PlayerControllerMP`       | Dig, Ghost
`cin`  | `MovementInputFromOptions` | Fly
`cio`  | `EntityPlayerSP`           | Cheat, Dig, Fly, Ghost, Motion
`qw`   | `EntityPlayerMP`           | Death, Fly
`rj`   | `NetHandlerPlayServer`     | Build, Dig

## Modified classes absent from package for now
File   | Class name             | Relevant mod(s)
-------+------------------------+----------------
`ahd`  | `EntityPlayer`         | None
`cee`  | `NetHandlerPlayClient` | None
       | `EnumGameType`         | Fly
       | `Explosion`            | Boom
       | `GuiContainer`         | Craft
       | `RenderLiving`         | Resize
       | `TileEntityFurnace`    | Furnace

