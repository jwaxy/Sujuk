/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixin;

import net.minecraft.util.math.Vec3d;
import net.wurstclient.mixinterface.IVec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Vec3d.class)
public abstract class Vec3dMixin implements IVec3d
{
	@Shadow
	@Final
	@Mutable
	public double x;
	@Shadow
	@Final
	@Mutable
	public double y;
	@Shadow
	@Final
	@Mutable
	public double z;
	
	@Override
	public void sujuk$set(double x, double y, double z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public void sujuk$setXZ(double x, double z)
	{
		this.x = x;
		this.z = z;
	}
	
	@Override
	public void sujuk$setY(double y)
	{
		this.y = y;
	}
}
