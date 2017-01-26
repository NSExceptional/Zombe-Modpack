
## All included files
Files                           | Purpose                    | Used by
--------------------------------+----------------------------+-----------------------
`changelog.md`                  | changelog                  | you
`README.md`                     | install/usage instructions | you
`FAQs.md`                       | updated FAQ page           | you
`readme_original.html`          | old, original zmod readme  | you
`LICENSE`                       | source code license        | developers
(this file)                     | package files listing      | you
`Mod/jar/*.class`               | modified minecraft classes | ZModpack's core, mods
`Mod/version/zombe/mod/*.class` | zombe's modpack classes    | ZModpack's core, mods
`Mod/config/*.txt`              | configuration files        | ZModpack, you
`source/**/*.java`              | modpack's source code      | ZModpack's devs, MCP

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

