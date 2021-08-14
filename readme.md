# Survival Invisiframes fork

This plugin enables the use of 1.16's invisible item frames for survival players
In 1.17+, glowing invisible item frames can be crafted with the glowing item frames.
The recipe center item and all messages are configurable.

Changes of this fork:
- Ability to customize messages
- Craft of glowing invisible item frames with glowing item frames instead of regular item frames.
- Code refactoring
- Changes in `config.yml` (so the old config is not supported)

## Crafting
![Recipe Screenshot1](https://i.imgur.com/c9xZMZ4.png)
![Recipe Screenshot2](https://i.imgur.com/2mXXewm.png)



## Permissions
Permission | Description
--- | ---
`survivalinvisiframes.place` | Allows the player to place an invisible item frame (enabled by default)
`survivalinvisiframes.craft`| Allows the player to craft an invisible item frame (enabled by default)
`survivalinvisiframes.cmd` | Allows the player to run commands from this plugin
`survivalinvisiframes.reload` | Permission to run `/iframe reload`
`survivalinvisiframes.forcerecheck` | Permission to run `/iframe force-recheck`
`survivalinvisiframes.get` | Permission to run `/iframe get`
`survivalinvisiframes.setitem` | Permission to run `/iframe setitem`

## Commands
Permission required for all commands: `survivalinvisiframes.cmd`

Command | Description | Permission
--- | --- | ---
`/iframe get [glow] [count]` | Gives the player an invisible item frame | `survivalinvisiframes.get`
`/iframe reload` | Reloads the config | `survivalinvisiframes.reload`
`/iframe force-recheck` | Rechecks all loaded invisible item frames to add/remove slimes manually | `survivalinvisiframes.forcerecheck`
`/iframe setitem` | Sets the recipe center item to the held item | `survivalinvisiframes.setitem`
