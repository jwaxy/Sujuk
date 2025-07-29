/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.portalesp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.BlockUtils;

public final class PortalEspBlockGroup
{
	protected final ArrayList<Box> individualBoxes = new ArrayList<>();
	
	protected final ArrayList<Box> combinedBoxes = new ArrayList<>();
	
	private final Block block;
	private final ColorSetting color;
	private final CheckboxSetting enabled;
	
	public PortalEspBlockGroup(Block block, ColorSetting color,
		CheckboxSetting enabled)
	{
		this.block = block;
		this.color = Objects.requireNonNull(color);
		this.enabled = enabled;
	}
	
	public void add(BlockPos pos)
	{
		Box box = getBox(pos);
		if(box == null)
			return;
		individualBoxes.add(box);
	}
	
	public void addCombinedBox(Box box)
	{
		combinedBoxes.add(box);
	}
	
	private Box getBox(BlockPos pos)
	{
		if(!BlockUtils.canBeClicked(pos))
			return null;
		
		return BlockUtils.getBoundingBox(pos);
	}
	
	public void clear()
	{
		individualBoxes.clear();
		combinedBoxes.clear();
	}
	
	public boolean isEnabled()
	{
		return enabled == null || enabled.isChecked();
	}
	
	public Stream<Setting> getSettings()
	{
		return Stream.of(enabled, color).filter(Objects::nonNull);
	}
	
	public Block getBlock()
	{
		return block;
	}
	
	public int getColorI(int alpha)
	{
		return color.getColorI(alpha);
	}
	
	public List<Box> getBoxes()
	{
		return Collections.unmodifiableList(
			!combinedBoxes.isEmpty() ? combinedBoxes : individualBoxes);
	}
	
	public List<Box> getIndividualBoxes()
	{
		return Collections.unmodifiableList(individualBoxes);
	}
	
	public List<Box> getCombinedBoxes()
	{
		return Collections.unmodifiableList(combinedBoxes);
	}
}
