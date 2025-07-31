/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.BlockComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.util.BlockUtils;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public final class BlockSetting extends Setting
{
	private String userInput = "";
	private Block block;
	private final Map<String, String> properties = new HashMap<>();
	private final String defaultName;
	private final boolean allowAir;
	
	private static final Pattern BLOCK_STATE_PATTERN = Pattern.compile(
		"^(?<namespace>[a-z0-9_.-]+:)?(?<block>[a-z0-9_.-]+)(?:\\[(?<props>.+)])?$");
	private static final Pattern PROPERTY_PATTERN =
		Pattern.compile("(?<key>[a-z0-9_]+):(?<value>[a-z0-9_]+)");
	
	public BlockSetting(String name, WText description, String blockName,
		boolean allowAir)
	{
		super(name, description);
		parseInput(blockName, true);
		this.defaultName = BlockUtils.getName(block);
		this.allowAir = allowAir;
	}
	
	public BlockSetting(String name, String descriptionKey, String blockName,
		boolean allowAir)
	{
		this(name, WText.translated(descriptionKey), blockName, allowAir);
	}
	
	public BlockSetting(String name, String blockName, boolean allowAir)
	{
		this(name, WText.empty(), blockName, allowAir);
	}
	
	private void parseInput(String input, boolean allowAir)
	{
		if(input == null || input.isEmpty())
			throw new IllegalArgumentException("Block name cannot be empty");
		
		Matcher matcher = BLOCK_STATE_PATTERN.matcher(input);
		if(!matcher.matches())
			throw new IllegalArgumentException(
				"Invalid block format: " + input);
		
		String namespace = matcher.group("namespace");
		String blockName = matcher.group("block");
		String props = matcher.group("props");
		
		// Store original input
		this.userInput = input;
		
		// Get the block
		String fullBlockId = (namespace != null) ? namespace + blockName
			: "minecraft:" + blockName;
		this.block = BlockUtils.getBlockFromNameOrID(fullBlockId);
		if(this.block == null)
			throw new IllegalArgumentException("Unknown block: " + fullBlockId);
		
		if(!allowAir && this.block instanceof AirBlock)
			throw new IllegalArgumentException("Air blocks are not allowed");
		
		// Parse properties if present
		this.properties.clear();
		if(props != null && !props.trim().isEmpty())
		{
			Matcher propMatcher = PROPERTY_PATTERN.matcher(props);
			while(propMatcher.find())
			{
				properties.put(propMatcher.group("key"),
					propMatcher.group("value"));
			}
		}
	}
	
	public Block getBlock()
	{
		return block;
	}
	
	public Map<String, String> getProperties()
	{
		return properties;
	}
	
	public String getBlockName()
	{
		return getUserInput(); // legacy code
	}
	
	public String getShortBlockName()
	{
		return getBlockName().replace("minecraft:", "");
	}
	
	public String getUserInput()
	{
		return userInput;
	}
	
	public void setBlock(Block block)
	{
		if(block == null || (block instanceof AirBlock && !allowAir))
		{
			return;
		}
		
		this.block = block;
		this.properties.clear();
		this.userInput = BlockUtils.getName(block);
		WurstClient.INSTANCE.saveSettings();
	}
	
	public void setBlockName(String input)
	{
		try
		{
			parseInput(input, allowAir);
			WurstClient.INSTANCE.saveSettings();
		}catch(IllegalArgumentException e)
		{
			e.printStackTrace();
			resetToDefault();
		}
	}
	
	public void resetToDefault()
	{
		parseInput(defaultName, allowAir);
		WurstClient.INSTANCE.saveSettings();
	}
	
	@Override
	public Component getComponent()
	{
		return new BlockComponent(this);
	}
	
	@Override
	public void fromJson(JsonElement json)
	{
		try
		{
			String input = JsonUtils.getAsString(json);
			parseInput(input, allowAir);
		}catch(Exception e)
		{
			e.printStackTrace();
			resetToDefault();
		}
	}
	
	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(userInput);
	}
	
	@Override
	public JsonObject exportWikiData()
	{
		JsonObject json = new JsonObject();
		json.addProperty("name", getName());
		json.addProperty("description", getDescription());
		json.addProperty("type", "Block");
		json.addProperty("defaultValue", defaultName);
		json.addProperty("allowAir", allowAir);
		return json;
	}
	
	@Override
	public Set<PossibleKeybind> getPossibleKeybinds(String featureName)
	{
		String fullName = featureName + " " + getName();
		
		String command = ".setblock " + featureName.toLowerCase() + " ";
		command += getName().toLowerCase().replace(" ", "_") + " ";
		
		LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<>();
		// Can't just list all the blocks here. Would need to change UI to allow
		// user to choose a block after selecting this option.
		// pkb.add(new PossibleKeybind(command + "dirt", "Set " + fullName + "
		// to dirt"));
		pkb.add(new PossibleKeybind(command + "reset", "Reset " + fullName));
		
		return pkb;
	}
}
