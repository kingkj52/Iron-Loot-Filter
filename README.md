# Ironman Ground Items

Ground items an ironman account is not allowed to pick up stop being drawn, and their menu entries
go away, so a left click on the tile falls through to whatever is actually usable and a right click
never lists them.

On a normal account the plugin does nothing at all.

## How it decides

The server sends an ownership flag with every ground item, and it is the same flag the server
consults when it decides whether a pickup is allowed. The plugin reads that flag and nothing else:

| Ownership | Result |
| --- | --- |
| Yours | left alone |
| Your group's | left alone |
| Another player's | hidden |
| Unowned | left alone |

Unowned covers world spawns, ashes from a fire that burnt out, and anything else the server never
attributed to a player. Supplies gathered inside a raid are owned by the party rather than by the
player who picked them up, so they stay visible too.

Reading the flag is the whole point. The alternative is a hand written list of item ids and
locations that tries to predict what the server will allow, which is wrong the moment Jagex adds
content and is wrong in the dangerous direction: it hides something you could have taken.

Account type comes from the ironman varbit, so every ironman variant is covered without a list -
standard, ultimate, hardcore, group, hardcore group and unranked group.

## Settings

- **Hide the item models.** Stops the pile being drawn. On by default.
- **Tiles holding both.** The client draws all of a tile's items as a single object, so a tile
  holding your loot and a stranger's cannot be half hidden. By default those tiles are left visible
  and only the menu entries are filtered, which never hides your own drop. Set it to hide the whole
  pile if the clutter matters more.
- **Remove the take options.** Drops `Take` entries for blocked items. On by default.
- **Remove examine too.** Also drops `Examine` for them. On by default.
- **Group drops are takeable.** Treats group owned items as yours. Turn it off if you are not a
  group ironman and still find group owned items you cannot take.
- **Filter on a normal account.** Applies the rules on an unrestricted account, for checking the
  plugin works before taking it to an ironman. Off by default.

## What it does not do

It hides items, it does not pick anything up, click anything, or change what a click does beyond
removing entries that would have failed anyway. There is no networking, no file access, no
reflection and no background thread; every read happens on the client thread during an event the
client already raised.

Ownership is the only reason an item gets hidden. An item you cannot take for some other reason - a
quest requirement, a free to play restriction, a full inventory - is still shown, because the server
does not mark those through ownership and guessing at them would be the hand written list again.

Two stacks of the same item on one tile, one yours and one not, cannot be told apart from a menu
entry, since the entry only carries the item id. The entry is kept in that case.

Deadman, beta and seasonal worlds may report account types this does not expect. It errs towards
showing items rather than hiding them, so the worst case there is that the plugin does nothing.

## Building

```
./gradlew build
```

`./gradlew deploy` copies the jar into `~/.runelite/sideloaded-plugins`, which a client started with
`--developer-mode` will load. `./gradlew run` starts a dev client with the plugin already loaded.
