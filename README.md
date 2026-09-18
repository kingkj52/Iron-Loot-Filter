# Ironman Ground Items

Ground items your account is not allowed to pick up stop being drawn, and their menu entries go
away, so a left click on the tile falls through to whatever is actually usable and a right click
never lists them.

It reads the ownership flag the server already sends with every item, at the moment each pile is
drawn, so a setting change or a pile changing hands takes effect on the next frame. **On a normal
account it does nothing at all.**

## How it decides

| Ownership | Result |
| --- | --- |
| Yours | left alone |
| Your group's | left alone |
| Another player's | hidden |
| Unowned | left alone |

Unowned covers world spawns, ashes from a fire that burnt out, and anything else the server never
attributed to a player. Supplies gathered inside a raid are owned by the party rather than by the
player who picked them up, so they stay visible too.

Reading the flag is the whole point. The alternative is a hand-written list of item IDs and
locations that tries to predict what the server will allow, which is wrong the moment Jagex adds
content and is wrong in the dangerous direction: it hides something you could have taken.

Account type comes from the ironman varbit, so every variant is covered without a list — standard,
ultimate, hardcore, group, hardcore group and unranked group.

## What it deliberately does not do

**It never acts for you.** No menu entry is created or invoked, no script is run, no input is
synthesised. The only menu change is removing entries the server would have refused.

**It makes no network requests.** Nothing is fetched and nothing is sent.

**It reads and writes no files.** Settings live in RuneLite's own config; there is no local store.

**It has never run in a live game.** `src/test/java` fixes the pickup rules and asserts the claims
above against the source, so the build fails if one stops being true. None of that is evidence that
the client hooks behave as expected in game.

## Known limits

- The client draws all of a tile's items as **one object**, so a tile holding your loot and a
  stranger's cannot be half hidden. Those tiles stay drawn and only their menu entries are
  filtered, which never hides your own drop. The moment your item leaves the pile the rest of it
  disappears, on the next frame, so the case resolves itself.
- A menu entry carries the item ID but not the stack, so two stacks of one ID on a tile under
  different owners cannot be told apart. The entry is kept in that case.
- Ownership is the only reason an item is hidden. Something you cannot take for another reason — a
  quest requirement, a free-to-play restriction, a full inventory — is still shown, because the
  server does not mark those through ownership and guessing at them would be the hand-written list
  again.
- Deadman, beta and seasonal worlds may report account types this does not expect. It errs towards
  showing items rather than hiding them, so the worst case there is that it does nothing.
- The render hook operates on whole item layers, which is the only granularity RuneLite exposes.
  Rebuilding the pile without the blocked items would mean drawing models by hand above the scene,
  which costs more per frame than the problem is worth.
- A layer reports the three items it is about to draw, which is all the client draws of a pile. A
  fourth stack under those three is not on screen to hide.

## Settings

- **Hide the item models.** Stops the pile being drawn. On by default. A pile holding one of your
  own items stays visible until that item is gone.
- **Remove the take options.** Drops `Take` entries for blocked items. On by default.
- **Remove examine too.** Also drops `Examine` for them. On by default.
- **Group drops are takeable.** Treats group-owned items as yours. Turn it off if you are not a
  group ironman and still find group-owned items you cannot take.
- **Filter on a normal account.** Applies the rules on an unrestricted account, for checking the
  plugin works before taking it to an ironman. Off by default.

## Before you rely on it

1. On any account, turn on **Filter on a normal account** and drop something where another player
   has left loot. Your own drop must stay visible and clickable.
2. On the ironman, stand in a busy spot and confirm other players' piles stop appearing, that left
   clicking the tile walks there instead of failing a pickup, and that right clicking lists nothing
   for them.
3. Check a tile holding both: yours must still be takeable, and the pile must vanish once you take
   it.
4. Check a fire burning out, and a raid, if you can — both should stay visible.
5. Disable the plugin and confirm everything comes back on the next frame.

If a `Take` still appears for an item you cannot pick up, the menu hook is the part to suspect; the
model hiding and the menu filtering are independent.

## Building

```
gradlew build     # compile and run the tests
gradlew run       # launch a dev client with the plugin loaded (needs developer mode)
gradlew deploy    # copy the jar into ~/.runelite/sideloaded-plugins
```

Attribution and the review boundary are in [NOTICE](NOTICE). Licensed under BSD 2-Clause; see
[LICENSE](LICENSE).
