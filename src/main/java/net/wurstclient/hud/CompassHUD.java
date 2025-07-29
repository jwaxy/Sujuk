/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.wurstclient.WurstClient;
import net.wurstclient.other_features.CompassOtf;

import static net.wurstclient.WurstClient.MC;

public class CompassHUD
{
	public void render(DrawContext context)
	{
		CompassOtf otf = WurstClient.INSTANCE.getOtfs().compassOtf;
		if(!otf.getEnabled())
			return;
		
		double x = context.getScaledWindowWidth() / 2.0;
		double y = context.getScaledWindowHeight() / 2.0;
		
		double pitch = Math
			.toRadians(MathHelper.clamp(MC.player.getPitch() + 30, -90, 90));
		double yaw = Math.toRadians(MathHelper.wrapDegrees(MC.player.getYaw()));
		
		double scale = otf.getScale();
		
		for(Direction direction : Direction.values())
		{
			String axis = otf.getMode() == CompassOtf.Mode.Axis
				? direction.getAxis() : direction.name();
			TextRenderer tr = MC.textRenderer;
			context.drawText(tr, axis,
				(int)(x + getX(direction, yaw, scale))
					- (tr.getWidth(axis) / 2),
				(int)(y + getY(direction, yaw, pitch, scale)) - 4,
				direction == Direction.N ? otf.getNorthColor()
					: otf.getOtherColor(),
				otf.getShadow());
		}
	}
	
	private double getX(Direction direction, double yaw, double scale)
	{
		return Math.sin(getPos(direction, yaw)) * scale * 40;
	}
	
	private double getY(Direction direction, double yaw, double pitch,
		double scale)
	{
		return Math.cos(getPos(direction, yaw)) * Math.sin(pitch) * scale * 40;
	}
	
	private double getPos(Direction direction, double yaw)
	{
		return yaw + direction.ordinal() * Math.PI / 2;
	}
	
	private enum Direction
	{
		N("Z-"),
		W("X-"),
		S("Z+"),
		E("X+");
		
		private final String axis;
		
		Direction(String axis)
		{
			this.axis = axis;
		}
		
		public String getAxis()
		{
			return axis;
		}
	}
}
