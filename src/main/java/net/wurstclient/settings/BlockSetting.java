/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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
	private BlockState blockState;
	private final String defaultName;
	private final boolean allowAir;
	private boolean isDefaultState;
	
	// Regex to parse block states like
	// "minecraft:block[prop1:value1;prop2:value2]"
	private static final Pattern BLOCK_STATE_PATTERN = Pattern.compile(
		"^(?<namespace>[a-z0-9_.-]+:)?(?<block>[a-z0-9_.-]+)(?:\\[(?<props>.+)])?$");
	
	public BlockSetting(String name, WText description, String blockName,
		boolean allowAir)
	{
		super(name, description);
		
		this.blockState = parseBlockInput(blockName, true);
		this.userInput = blockName;
		this.defaultName = BlockUtils.getName(blockState.getBlock());
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
	
	/**
	 * Parses both simple block names and block state strings
	 */
	private BlockState parseBlockInput(String input, boolean allowAir)
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
		
		// Construct full block ID
		String fullBlockId = (namespace != null) ? namespace + blockName
			: "minecraft:" + blockName;
		
		// Get the block
		Block block = BlockUtils.getBlockFromNameOrID(fullBlockId);
		if(block == null)
			throw new IllegalArgumentException("Unknown block: " + fullBlockId);
		
		if(!allowAir && block instanceof AirBlock)
			throw new IllegalArgumentException("Air blocks are not allowed");
		
		// If no properties, return default state
		if(props == null || props.trim().isEmpty())
		{
			this.isDefaultState = true;
			return block.getDefaultState();
		}
		this.isDefaultState = false;
		
		// Parse properties
		return BlockUtils.parseBlockState(block, props);
	}
	
	/**
	 * @return this setting's {@link Block}. Cannot be null.
	 */
	public Block getBlock()
	{
		return blockState.getBlock();
	}
	
	/**
	 * @return the full block state including properties
	 */
	public BlockState getBlockState()
	{
		return blockState;
	}
	
	public String getBlockName()
	{
		return userInput; // this what they actually want
		// return BlockUtils.getName(blockState.getBlock());
	}
	
	public String getShortBlockName()
	{
		return getBlockName().replace("minecraft:", "");
	}
	
	/**
	 * @return the original user input (either simple name or full state string)
	 */
	public String getUserInput()
	{
		return userInput;
	}
	
	public boolean isDefaultState()
	{
		return isDefaultState;
	}
	
	public void setBlock(Block block)
	{
		setBlockState(block.getDefaultState());
	}
	
	public void setBlockState(BlockState state)
	{
		if(state == null)
			return;
		
		if(!allowAir && state.getBlock() instanceof AirBlock)
			return;
		
		if(state.equals(blockState))
			return;
		
		blockState = state;
		userInput = BlockUtils.getName(state.getBlock()); // Default to simple
															// name
		WurstClient.INSTANCE.saveSettings();
	}
	
	public void setBlockName(String input)
	{
		try
		{
			blockState = parseBlockInput(input, allowAir);
			userInput = input;
			WurstClient.INSTANCE.saveSettings();
		}catch(IllegalArgumentException e)
		{
			resetToDefault();
		}
	}
	
	public void resetToDefault()
	{
		blockState =
			BlockUtils.getBlockFromNameOrID(defaultName).getDefaultState();
		userInput = defaultName;
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
			
			blockState = parseBlockInput(input, allowAir);
			userInput = input;
			
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
		pkb.add(new PossibleKeybind(command + "reset", "Reset " + fullName));
		
		return pkb;
	}
}
