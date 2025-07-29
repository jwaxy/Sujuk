/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.mixinterface;

import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

public interface IVec3d
{
	void sujuk$set(double x, double y, double z);
	
	void sujuk$setXZ(double x, double z);
	
	void sujuk$setY(double y);
	
	default void sujuk$set(Vec3i vec)
	{
		sujuk$set(vec.getX(), vec.getY(), vec.getZ());
	}
	
	default void sujuk$set(Vector3d vec)
	{
		sujuk$set(vec.x, vec.y, vec.z);
	}
}
