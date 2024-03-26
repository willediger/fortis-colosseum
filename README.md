# Fortis Colosseum

Utilities and information for the Fortis Colosseum challenge gauntlet.


[![Active Installs](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/fortis-colosseum)](https://runelite.net/plugin-hub/show/fortis-colosseum)
[![Plugin Rank](http://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/fortis-colosseum)](https://runelite.net/plugin-hub/show/fortis-colosseum)

## Features

<details>
<summary>Left-Click Bank-All</summary>

Swaps the two-click bank all to a single-click bank all in the loot chest interface.
</details>

<details>
<summary>Waves Overlay</summary>

Shows the current and/or next wave spawns in an on-screen overlay.

![Waves Overlay Screenshot](docs/img/waves_overlay.png)
</details>

<details>
<summary>Splits</summary>

Shows an overlay of splits timings with either per-wave time or cumulative wave-end time.

![Splits Overlay Screenshot](docs/img/splits_overlay.png)

Alternatively, the splits can be written to a file using the "Save to File" config option.
The file is named as timestamp of the run ending, and the format is:
```
Wave 1: ticksInWave / ticksSinceRunStart
Wave 2: ticksInWave / ticksSinceRunStart
...
```

</details>

<details>
<summary>LiveSplit Integration</summary>

![img.png](docs/img/livesplit_demo.png)

The plugin can interface with LiveSplit to automatically split at the end of each wave.
This requires the [LiveSplit Server](https://github.com/LiveSplit/LiveSplit.Server) component
(which is built-in as of 1.8.29).

You can use the [layout file](/docs/livesplit/FortisColosseum.lsl) 
and [splits file](/docs/livesplit/FortisColosseum.lss) 
provided in this repository as a starting point.

You MUST select "Start Server" to receive times.
This is required EVERY TIME you begin LiveSplit.

![img.png](docs/img/livesplit_start_server.png)
</details>
