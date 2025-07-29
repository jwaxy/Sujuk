/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.settings;

import java.awt.Color;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.util.math.MathHelper;
import net.wurstclient.WurstClient;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.components.ColorComponent;
import net.wurstclient.keybinds.PossibleKeybind;
import net.wurstclient.util.ColorUtils;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.json.JsonException;
import net.wurstclient.util.json.JsonUtils;
import net.wurstclient.util.text.WText;

public final class ColorSetting extends Setting
{
	private final float[] rgb = new float[3]; // [red, green, blue], values 0-1
	private final Color defaultColor;
	private boolean rainbow;
	
	public ColorSetting(String name, WText description, Color color,
		boolean rainbow)
	{
		super(name, description);
		Objects.requireNonNull(color);
		setRGB(color);
		defaultColor = color;
		this.rainbow = rainbow;
	}
	
	public ColorSetting(String name, WText description, Color color)
	{
		this(name, description, color, false);
	}
	
	public ColorSetting(String name, String descriptionKey, Color color)
	{
		this(name, WText.translated(descriptionKey), color);
	}
	
	public ColorSetting(String name, Color color)
	{
		this(name, WText.empty(), color);
	}
	
	private void setRGB(Color color)
	{
		rgb[0] = color.getRed() / 255F;
		rgb[1] = color.getGreen() / 255F;
		rgb[2] = color.getBlue() / 255F;
	}
	
	private float[] getCurrentRGB()
	{
		if(rainbow)
			return RenderUtils.getRainbowColor();
		return rgb;
	}
	
	public Color getColor()
	{
		float[] c = getCurrentRGB();
		// avoids allocation if rainbow is disabled
		if(!rainbow)
			return new Color(Math.round(rgb[0] * 255), Math.round(rgb[1] * 255),
				Math.round(rgb[2] * 255));
		return new Color(c[0], c[1], c[2]); // may still allocate
	}
	
	public float[] getColorF()
	{
		float[] c = getCurrentRGB();
		return new float[]{c[0], c[1], c[2]};
	}
	
	public int getColorI()
	{
		return getColorI(1.0f);
	}
	
	public int getColorI(int alpha)
	{
		float[] c = getCurrentRGB();
		int r = Math.round(c[0] * 255);
		int g = Math.round(c[1] * 255);
		int b = Math.round(c[2] * 255);
		return (alpha << 24) | (r << 16) | (g << 8) | b;
	}
	
	public int getColorI(float alpha)
	{
		return getColorI((int)(MathHelper.clamp(alpha, 0, 1) * 255));
	}
	
	public int getRed()
	{
		return Math.round(getCurrentRGB()[0] * 255);
	}
	
	public int getGreen()
	{
		return Math.round(getCurrentRGB()[1] * 255);
	}
	
	public int getBlue()
	{
		return Math.round(getCurrentRGB()[2] * 255);
	}
	
	public Color getDefaultColor()
	{
		return defaultColor;
	}
	
	public void setColor(Color color)
	{
		Objects.requireNonNull(color);
		setRGB(color);
		WurstClient.INSTANCE.saveSettings();
	}
	
	public boolean getRainbow()
	{
		return rainbow;
	}
	
	@Override
	public Component getComponent()
	{
		return new ColorComponent(this);
	}
	
	@Override
	public void fromJson(JsonElement json)
	{
		if(!JsonUtils.isString(json))
			return;
		
		try
		{
			setColor(ColorUtils.parseHex(json.getAsString()));
		}catch(JsonException e)
		{
			e.printStackTrace();
			setColor(defaultColor);
		}
	}
	
	@Override
	public JsonElement toJson()
	{
		return new JsonPrimitive(ColorUtils.toHex(getColor()));
	}
	
	@Override
	public JsonObject exportWikiData()
	{
		JsonObject json = new JsonObject();
		json.addProperty("name", getName());
		json.addProperty("description", getDescription());
		json.addProperty("type", "Color");
		json.addProperty("defaultColor", ColorUtils.toHex(defaultColor));
		return json;
	}
	
	@Override
	public Set<PossibleKeybind> getPossibleKeybinds(String featureName)
	{
		String description = "Set " + featureName + " " + getName() + " to ";
		String command = ".setcolor " + featureName.toLowerCase() + " "
			+ getName().toLowerCase().replace(" ", "_") + " ";
		
		LinkedHashSet<PossibleKeybind> pkb = new LinkedHashSet<>();
		addPKB(pkb, command + "#FF0000", description + "red");
		addPKB(pkb, command + "#00FF00", description + "green");
		addPKB(pkb, command + "#0000FF", description + "blue");
		addPKB(pkb, command + "#FFFF00", description + "yellow");
		addPKB(pkb, command + "#00FFFF", description + "cyan");
		addPKB(pkb, command + "#FF00FF", description + "magenta");
		addPKB(pkb, command + "#FFFFFF", description + "white");
		addPKB(pkb, command + "#000000", description + "black");
		return pkb;
	}
	
	private void addPKB(LinkedHashSet<PossibleKeybind> pkb, String command,
		String description)
	{
		pkb.add(new PossibleKeybind(command, description));
	}
	
	public void setRainbow(boolean rainbow)
	{
		this.rainbow = rainbow;
	}
}
