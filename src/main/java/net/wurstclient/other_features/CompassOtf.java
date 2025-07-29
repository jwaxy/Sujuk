/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.other_features;

import net.wurstclient.other_feature.OtherFeature;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.SliderSetting;

import java.awt.*;

public class CompassOtf extends OtherFeature
{
	private final CheckboxSetting enabled =
		new CheckboxSetting("Enabled", false);
	
	private final ColorSetting northColor =
		new ColorSetting("North Color", "Color of north.", Color.RED);
	
	private final ColorSetting otherColor = new ColorSetting("Other Color",
		"Color of other directions.", Color.WHITE);
	
	private final SliderSetting scale = new SliderSetting("Size",
		"Size of the compass (Spacing between the directions)", 1, 0.5, 3, 0.1,
		SliderSetting.ValueDisplay.DECIMAL);
	
	private final EnumSetting<Mode> mode = new EnumSetting<>("Type",
		"Which type of direction information to show.", Mode.values(),
		Mode.Axis);
	
	private final CheckboxSetting shadow =
		new CheckboxSetting("Shadow", "Text shadow.", true);
	
	public CompassOtf()
	{
		super("Compass", "Displays a compass.");
		addSetting(enabled);
		addSetting(northColor);
		addSetting(otherColor);
		addSetting(scale);
	}
	
	public boolean getEnabled()
	{
		return enabled.isChecked();
	}
	
	public int getNorthColor()
	{
		return northColor.getColorI();
	}
	
	public int getOtherColor()
	{
		return otherColor.getColorI();
	}
	
	public double getScale()
	{
		return scale.getValue();
	}
	
	public boolean getShadow()
	{
		return shadow.isChecked();
	}
	
	public Mode getMode()
	{
		return mode.getSelected();
	}
	
	public enum Mode
	{
		Direction,
		Axis
	}
}
