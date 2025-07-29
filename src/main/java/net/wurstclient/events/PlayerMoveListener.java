/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.events;

import java.util.ArrayList;

import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.event.Event;
import net.wurstclient.event.Listener;

public interface PlayerMoveListener extends Listener
{
	public void onPlayerMove(PlayerMoveEvent event);
	
	public static class PlayerMoveEvent extends Event<PlayerMoveListener>
	{
		private static final PlayerMoveEvent INSTANCE = new PlayerMoveEvent();
		
		public MovementType type;
		public Vec3d movement;
		
		public static PlayerMoveEvent get(MovementType type, Vec3d movement)
		{
			INSTANCE.type = type;
			INSTANCE.movement = movement;
			return INSTANCE;
		}
		
		@Override
		public void fire(ArrayList<PlayerMoveListener> listeners)
		{
			for(PlayerMoveListener listener : listeners)
				listener.onPlayerMove(this);
		}
		
		@Override
		public Class<PlayerMoveListener> getListenerType()
		{
			return PlayerMoveListener.class;
		}
	}
}
