
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
`source/*.java`         | modpack's source code      | ZModpack's devs, MCP

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
?       | `EntityList`, `BlockFire`

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
`avd`  | `WorldProvider`            | Cloud, Sun
`beq`  | `Minecraft`                | modpack
`bnn`  | `PlayerControllerMP`       | Dig, Ghost
`bpp`  | `MovementInputFromOptions` | Fly
`bpq`  | `EntityPlayerSP`           | Cheat, Dig, Fly, Ghost, Motion
`ly`   | `EntityPlayerMP`           | Death, Fly
`mi`   | `NetHandlerPlayServer`     | Build, Dig
`so`   | `EntityList`               | ?
`anl`  | `BlockFire`                | ?
`aax`  | `EntityPlayer`             | WIP
`bnm`  | `NetHandlerPlayClient`     | WIP

