# Iron Loot Filter

Other people's loot stops cluttering your screen.

On an ironman, every drop another player leaves behind is scenery. You cannot take it, but it still
piles up on the floor, still sits in your right click menu, and still eats the left click on a tile
you were trying to walk to. This plugin makes it disappear.

## What it does

* Piles you cannot pick up are not drawn.
* Their `Take` and `Examine` options are gone from the right click menu.
* Left clicking the tile does what you meant instead of failing a pickup.
* Your own drops, and your group's, are untouched.

Works on standard, ultimate, hardcore, group, hardcore group and unranked group ironman accounts.
Group ironmen keep seeing their group's drops, since those are yours to take.

There is nothing to configure to get started. On a normal account the plugin does nothing at all.

## How it knows

The game already tells your client who owns every item on the floor, and it is the same information
the server uses to allow or refuse a pickup. The plugin reads that and nothing else.

| Item | Result |
| --- | --- |
| Yours | left alone |
| Your group's | left alone |
| Another player's | hidden |
| Unowned | left alone |

Unowned means anything the server never gave to a player: world spawns, ashes from a fire that
burnt out, and so on. Supplies gathered inside a raid belong to the party rather than to whoever
picked them up, so those stay visible too.

Reading ownership directly means there is no list of item IDs to go stale. A new boss, a new raid or
a new drop table needs no update here.

## Settings

| Setting | Default | What it does |
| --- | --- | --- |
| Hide the item models | on | Stops the pile being drawn |
| Remove the take options | on | Drops `Take` entries for loot you cannot pick up |
| Remove examine too | on | Drops `Examine` for them as well |
| Group drops are takeable | on | Treats group owned loot as yours |
| Filter on a normal account | off | Runs the filter on an unrestricted account, for testing |

## Worth knowing

The client draws everything on a tile as a single object, so a pile holding one of your own items
stays visible until that item is gone. The moment it leaves, the rest of the pile disappears. Its
menu entries are filtered the whole time either way, so you can still only click what you can
actually take.

Ownership is the only reason anything is hidden. Loot you cannot pick up for some other reason, such
as a quest requirement or a full inventory, is still shown.

## Nothing is automated

The plugin does not click, walk, pick anything up, or create or invoke a menu option. It removes
options the server would have refused and hides models you were never able to use. Every action is
still yours. It makes no network requests and reads and writes no files.

Licensed under BSD 2-Clause. See [LICENSE](LICENSE), and [NOTICE](NOTICE) for attribution.
