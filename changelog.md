## Zombe's modpack changelog starting from MC 1.4.4

v10.0.0 for MC 1.11

- Well, it compiles

v9.0.2 for MC 1.8

- ported Build and Info mods
- new mod, Motion: predicts server-side movements and tries to fix illegal moves in advance
- changes in Build mod: build sets are back, can now copy-paste from multiplayer to singleplayer, can now mark targeted block, improved box display
- changes in Dig mod: added option for checking raytraces (might conflict with Build and Info targeting)
- changes in Fly: added air-jumping option, preliminary work on new motion model
- changes in Ghost mod: added compatibility options that trigger automatically if renderer fails
- changes in Info mod: can now display infos as tags, added infos on targeted block
- changes in Radar mod: added option to show a relative compass
- a few bugfixes

v9.0.1 for MC 1.8

- ported Cloud and Sun mods
- extracted the player listing feature from Info mod, added as a new mod: Radar
- implemented the Free Fly feature from Fly mod
- fixed an event racecondition leading to unresponsive scrollbars
- updated installation instructions in the readme

v9.0.0 for MC 1.8

- COMPLETE REWRITE, much improved codebase
- compatibility: designed for Minecraft 1.8, with notes taken for backports
- the modpack is now modular
- the modpack can now load mods dynamically from outside the .jar
- much improved F7 config menu: textfields, defaults, close button, scrolling
- preliminary work done for world-specific config file support
- the modpack now outputs a default config file: default.txt
- the modpack no longer requires config.txt, it creates and fills it on the fly
- improved integration with minecraft defaults, trimmed names.txt accordingly
- removed now-unnecessary minecraft class dependencies
- countless bugfixes

v8.1.1 for MC 1.6.1

- compatibility: updated for Minecraft 1.6.1

v6.8.4 for MC 1.4.6

- added support for mouse buttons for keybindings
- added an option to the Fly mod allowing to fly toward the cursor
- improved death mod : no longer requires class EntityPlayerMP
- fixed a bug related to item damage values causing glitches in many mods

v7.1.0 for MC 1.5.1

- compatibility: updated for Minecraft 1.5.1

v7.0.0 for MC 1.5

- fixed a crash triggering when rendering damaged blocks

v7.0-pre for MC 1.5-pre

- compatibility: updated for Minecraft 1.5-pre
- slightly improved default config file

v6.8.3 for MC 1.4.6

- added support for logarithmic scale in the F7 menu
- improved feature availability detection
- added support for feature availability in the F7 menu
- build mod partly rewritten, build extension working again

v6.8.2 for MC 1.4.6

- mod toggles now available in the ingame F7 menu

v6.8.1 for MC 1.4.6

- added config options for Dig
- added missing recipes from 1.3.1 to 1.4.6
- fixed the not working death mod (tested)
- fixed a bug related to null chat lines
- fixed a bug related to Cheat:ShowOres caused by block id over 255 (untested)

v6.8.0 for MC 1.4.6

- compatibility: updated for Minecraft 1.4.6

v6.7.6 for MC 1.4.5

- fixed a bug disabling the vanilla fly when fall damages are disabled
- death mod rewritten, suposedly 100% fixed
- dig mod rewritten, 40% fixed (Reach working again, but not Speed and Harvest)

v6.7.5 for MC 1.4.5

- added a changelog in the modpack's archive
- bugfixes for the new Projection cheat view
- added missing config.txt entries for the options introduced in 6.6 and 6.7
- fixed taking damages while flying over lava in singleplayer
- fixed many cheat options not working in singleplayer
- fixed a bug where 3D overlays could fade or flicker when Rain & Snow is Off

v6.7.4 for MC 1.4.5

- NoClip 99% fixed (the 1% : still affected by water and moving pistons)
- fixed a possible crash with Ore marking with custom blocks
- added a new "Projection" cheat view acting like a ghost or like a free-cam depending on its own fly and noclip state, and uses the same keybinds as the Fly mod

v6.7.3 for MC 1.4.5

- now in the Original Post ! (updates from Nilat are now official)
- fixed a possible crash related to the Fly mod
- added config options for the Safe mod
- fixed 3D overlays (Safe, Path and Cheat's ore marks and mob bars) not showing up when using Cheat View
- added a readme file in the modpack's archive with installation instructions

Zombe-Nilat-1.4.5-2

- removed a classfile from the package which was erroneously included (xv.class) dating from 1.4.4
- fixed a possible crash when rendering clouds if the player data has not been sent to the client yet
- fixed a corner-case which made the Creative-fly-even-in-Survival bug reappear if you leave and reenter a game while Fly is on

Zombe-Nilat-1.4.5-1

- fixed the Fly mod's speed jump not working
- fixed the "Allow vanilla MC sprint/fly" options not working
- fixed the Creative-fly-even-in-Survival bug

Zombe-Nilat-1.4.5-0

- compatibility: updated for Minecraft 1.4.5
- fixed a bug causing error messages thrown by the Safe mod at altitudes below 16
- Sun mod reintroduced

Zombe-Nilat-1.4.4-1

- fixed missing crosses from the Safe mod on some blocks (notably inverted stairs and slabs)
- fixed Cheat's mobs bars and ore marks missing or obstructed
- fixed conflict between see-through cheat and 3D overlays (Safe, Path and Cheat's ore marks and mob bars)
- fixed stuck clouds and/or render distance

Zombe-Nilat-1.4.4-0

- compatibility: updated for Minecraft 1.4.4
- removed optional class files related to mods Boom, Build, Craft, Dig, Furnace, Resize and Sun

