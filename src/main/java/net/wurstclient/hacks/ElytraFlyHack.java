/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.item.FireworkRocketItem;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.PlayerMoveListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IVec3d;
import net.wurstclient.settings.SliderSetting;

public class ElytraFlyHack extends Hack implements PlayerMoveListener
{
	public final SliderSetting horizontalSpeed =
		new SliderSetting("Horizontal Speed", 1, 0.05, 10, 0.05,
			SliderSetting.ValueDisplay.DECIMAL);
	
	public final SliderSetting verticalSpeed = new SliderSetting(
		"Vertical Speed",
		"\u00a7c\u00a7lWARNING:\u00a7r Setting this too high might cause fall damage, even with NoFall.",
		1, 0.05, 5, 0.05, SliderSetting.ValueDisplay.DECIMAL);
	
	public final SliderSetting fallMultiplier = new SliderSetting(
		"Fall Multiplier", "Controls how fast will you go down naturally.",
		0.01, 0, 1, 0.01, SliderSetting.ValueDisplay.DECIMAL);
	
	public ElytraFlyHack()
	{
		super("ElytraFly");
		setCategory(Category.MOVEMENT);
		addSetting(horizontalSpeed);
		addSetting(verticalSpeed);
		addSetting(fallMultiplier);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(PlayerMoveListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(PlayerMoveListener.class, this);
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event)
	{
		if(MC.player == null || !MC.player.isGliding())
			return;
		
		boolean a = false;
		boolean b = false;
		
		final double horizontalSpeedV = horizontalSpeed.getValue();
		final double verticalSpeedV = verticalSpeed.getValue();
		final double fallMultiplierV = fallMultiplier.getValue();
		
		Vec3d forward = Vec3d.fromPolar(0, MC.player.getYaw()).multiply(0.1);
		Vec3d right = Vec3d.fromPolar(0, MC.player.getYaw() + 90).multiply(0.1);
		
		double velX = 0, velY = event.movement.y, velZ = 0;
		
		if(velY < 0)
			velY *= fallMultiplierV;
		if(velY > 0 && !(MC.player.getMainHandStack()
			.getItem() instanceof FireworkRocketItem))
			velY = 0;
		
		if(MC.options.forwardKey.isPressed())
		{
			velX += forward.x * horizontalSpeedV * 10;
			velZ += forward.z * horizontalSpeedV * 10;
			a = true;
		}else if(MC.options.backKey.isPressed())
		{
			velX -= forward.x * horizontalSpeedV * 10;
			velZ -= forward.z * horizontalSpeedV * 10;
			a = true;
		}
		
		if(MC.options.rightKey.isPressed())
		{
			velX += right.x * horizontalSpeedV * 10;
			velZ += right.z * horizontalSpeedV * 10;
			b = true;
		}else if(MC.options.leftKey.isPressed())
		{
			velX -= right.x * horizontalSpeedV * 10;
			velZ -= right.z * horizontalSpeedV * 10;
			b = true;
		}
		
		if(a && b)
		{
			double diagonal = 1 / Math.sqrt(2);
			velX *= diagonal;
			velZ *= diagonal;
		}
		if(MC.options.jumpKey.isPressed())
			velY += 0.5 * verticalSpeedV;
		else if(MC.options.sneakKey.isPressed())
			velY -= 0.5 * verticalSpeedV;
		
		((IVec3d)event.movement).sujuk$set(velX, velY, velZ);
	}
}
