# GG Client (Minecraft 26.2, Fabric)

GG Client for DonutSMP, built on the open-source **Adin** client by koifih (CC0, see LICENSE).
Adin's GUI, rendering, combat, movement and HUD modules are all still here. On top of that this
project adds a **Donut** tab and a Freecam.

## Donut tab
Ancient Debris Finder, Auto Base Hunt, Auto Dig Down, Auto Elytra Fly, Auto RTP, Auto Sell,
Base ESP, Base Finder, Donut Wheel, Farm FPS Optimizer. Freecam is in the Render tab.

Notes:
- Donut Wheel: enable the module, bind a key to it, and the key opens a command wheel.
  Edit `ACTIONS` in `ui/DonutWheelScreen.java` to change the commands.
- Auto Sell sells everything outside your hotbar (turn "Skip hotbar" off to include it).
- Auto Elytra Fly needs an elytra equipped and fireworks in your hotbar.
- Nothing here is designed to hide the client from a server's anticheat.

## Build the jar (easiest: GitHub does it for you)
1. Create a free GitHub repository and upload everything inside this folder
   (including the hidden `.github` folder).
2. Open the Actions tab, run "build".
3. Download `ggclient-jar` under Artifacts. The jar is inside.

## Build on your own PC
Needs JDK 25. Run `./gradlew build` (Windows: `gradlew.bat build`).
The jar is `build/libs/ggclient-*.jar` (not the `-sources` one).

## Install
Fabric Loader 0.19.5+ for Minecraft 26.2, Fabric API 0.159.0+26.2 or newer, and the jar in `.minecraft/mods`.

## Credits
- Adin by koifih (CC0): the base this is built on.
- timewarp: the GUI design that Adin is heavily based on.
