package com.ironmangrounditems;

enum MixedTileMode
{
	SHOW("Leave the pile alone"),
	HIDE("Hide the whole pile");

	private final String label;

	MixedTileMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
