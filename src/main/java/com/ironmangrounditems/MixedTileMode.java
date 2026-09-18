package com.ironmangrounditems;

/** What to do with a tile holding both takeable and blocked items. */
enum MixedTileMode
{
    SHOW("Leave the pile alone"),
    HIDE("Hide the whole pile");

    private final String label;

    MixedTileMode(String label) { this.label = label; }

    @Override public String toString() { return label; }
}
