/*
 * Copyright (c) 2014-2025 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.awt.Color;
import java.util.*;
import java.util.function.BiPredicate;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.wurstclient.Category;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.PacketInputListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hacks.portalesp.PortalEspBlockGroup;
import net.wurstclient.settings.*;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.chunk.ChunkSearcherCoordinator;

public final class PortalEspHack extends Hack implements UpdateListener,
	CameraTransformViewBobbingListener, RenderListener
{
	private final EspStyleSetting style = new EspStyleSetting();
	
	enum BoxStyle
	{
		Cross,
		Solid
	}
	
	private final EnumSetting<BoxStyle> boxStyle =
		new EnumSetting<>("Box style", "Rendering style for the grouped boxes.",
			BoxStyle.values(), BoxStyle.Cross);
	
	private final PortalEspBlockGroup netherPortal =
		new PortalEspBlockGroup(Blocks.NETHER_PORTAL,
			new ColorSetting("Nether portal color",
				"Nether portals will be highlighted in this color.", Color.RED),
			new CheckboxSetting("Include nether portals", true));
	
	private final PortalEspBlockGroup endPortal =
		new PortalEspBlockGroup(Blocks.END_PORTAL,
			new ColorSetting("End portal color",
				"End portals will be highlighted in this color.", Color.GREEN),
			new CheckboxSetting("Include end portals", true));
	
	private final PortalEspBlockGroup endPortalFrame = new PortalEspBlockGroup(
		Blocks.END_PORTAL_FRAME,
		new ColorSetting("End portal frame color",
			"End portal frames will be highlighted in this color.", Color.BLUE),
		new CheckboxSetting("Include end portal frames", true));
	
	private final PortalEspBlockGroup endGateway = new PortalEspBlockGroup(
		Blocks.END_GATEWAY,
		new ColorSetting("End gateway color",
			"End gateways will be highlighted in this color.", Color.YELLOW),
		new CheckboxSetting("Include end gateways", true));
	
	private final List<PortalEspBlockGroup> groups =
		Arrays.asList(netherPortal, endPortal, endPortalFrame, endGateway);
	
	private final ChunkAreaSetting area = new ChunkAreaSetting("Area",
		"The area around the player to search in.\n"
			+ "Higher values require a faster computer.");
	
	private final BiPredicate<BlockPos, BlockState> query =
		(pos, state) -> state.getBlock() == Blocks.NETHER_PORTAL
			|| state.getBlock() == Blocks.END_PORTAL
			|| state.getBlock() == Blocks.END_PORTAL_FRAME
			|| state.getBlock() == Blocks.END_GATEWAY;
	
	private final ChunkSearcherCoordinator coordinator =
		new ChunkSearcherCoordinator(query, area);
	
	private boolean groupsUpToDate;
	
	public PortalEspHack()
	{
		super("PortalESP");
		setCategory(Category.RENDER);
		
		addSetting(style);
		addSetting(boxStyle);
		groups.stream().flatMap(PortalEspBlockGroup::getSettings)
			.forEach(this::addSetting);
		addSetting(area);
	}
	
	@Override
	protected void onEnable()
	{
		groupsUpToDate = false;
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(PacketInputListener.class, coordinator);
		EVENTS.add(CameraTransformViewBobbingListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(PacketInputListener.class, coordinator);
		EVENTS.remove(CameraTransformViewBobbingListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		coordinator.reset();
		groups.forEach(PortalEspBlockGroup::clear);
	}
	
	@Override
	public void onCameraTransformViewBobbing(
		CameraTransformViewBobbingEvent event)
	{
		if(style.getSelected().hasLines())
			event.cancel();
	}
	
	@Override
	public void onUpdate()
	{
		boolean searchersChanged = coordinator.update();
		if(searchersChanged)
			groupsUpToDate = false;
		
		if(!groupsUpToDate && coordinator.isDone())
			updateGroupBoxes();
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(style.getSelected().hasBoxes())
			renderBoxes(matrixStack);
		
		if(style.getSelected().hasLines())
			renderTracers(matrixStack, partialTicks);
	}
	
	private void renderBoxes(MatrixStack matrixStack)
	{
		for(PortalEspBlockGroup group : groups)
		{
			if(!group.isEnabled())
				continue;
			
			List<Box> boxes = group.getCombinedBoxes();
			int quadsColor = group.getColorI(0x40);
			int linesColor = group.getColorI(0x80);
			
			if(boxStyle.getSelected() == BoxStyle.Cross)
				RenderUtils.drawCrossBoxes(matrixStack, boxes, quadsColor,
					false);
			else
				RenderUtils.drawSolidBoxes(matrixStack, boxes, quadsColor,
					false);
			
			RenderUtils.drawOutlinedBoxes(matrixStack, boxes, linesColor,
				false);
		}
	}
	
	private void renderTracers(MatrixStack matrixStack, float partialTicks)
	{
		for(PortalEspBlockGroup group : groups)
		{
			if(!group.isEnabled())
				continue;
			
			List<Box> boxes = group.getCombinedBoxes();
			List<Vec3d> centers = boxes.stream().map(Box::getCenter).toList();
			int color = group.getColorI(0x80);
			
			RenderUtils.drawTracers(matrixStack, partialTicks, centers, color,
				false);
		}
	}
	
	private void updateGroupBoxes()
	{
		groups.forEach(PortalEspBlockGroup::clear);
		
		// Group results by block type
		Map<PortalEspBlockGroup, List<BlockPos>> groupedPositions =
			new HashMap<>();
		coordinator.getMatches().forEach(result -> {
			for(PortalEspBlockGroup group : groups)
			{
				if(result.state().getBlock() == group.getBlock())
				{
					groupedPositions
						.computeIfAbsent(group, k -> new ArrayList<>())
						.add(result.pos());
					break;
				}
			}
		});
		
		// For each group, find connected blocks and create combined boxes
		for(Map.Entry<PortalEspBlockGroup, List<BlockPos>> entry : groupedPositions
			.entrySet())
		{
			PortalEspBlockGroup group = entry.getKey();
			List<BlockPos> positions = entry.getValue();
			
			// Find all connected blocks
			List<Set<BlockPos>> connectedGroups =
				findConnectedBlocks(positions);
			
			// Create combined boxes for each connected group
			for(Set<BlockPos> connectedGroup : connectedGroups)
			{
				if(connectedGroup.isEmpty())
					continue;
				
				// Calculate min and max coordinates
				Box combinedBox = getCombinedBox(connectedGroup);
				
				group.addCombinedBox(combinedBox);
			}
		}
		
		groupsUpToDate = true;
	}
	
	private static Box getCombinedBox(Set<BlockPos> connectedGroup)
	{
		Box combined = null;
		for(BlockPos pos : connectedGroup)
		{
			BlockState state = MC.world.getBlockState(pos);
			VoxelShape shape = state.getOutlineShape(MC.world, pos);
			
			if(shape.isEmpty())
			{
				continue; // Skip blocks with no outline shape
			}
			
			// Get the block's bounding box in world coordinates
			Box box = shape.getBoundingBox().offset(pos);
			
			// Union with the combined box
			if(combined == null)
			{
				combined = box;
			}else
			{
				combined = combined.union(box);
			}
		}
		
		// Fallback for groups with no valid outline shapes
		if(combined == null)
		{
			int minX = Integer.MAX_VALUE;
			int minY = Integer.MAX_VALUE;
			int minZ = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int maxY = Integer.MIN_VALUE;
			int maxZ = Integer.MIN_VALUE;
			
			for(BlockPos pos : connectedGroup)
			{
				minX = Math.min(minX, pos.getX());
				minY = Math.min(minY, pos.getY());
				minZ = Math.min(minZ, pos.getZ());
				maxX = Math.max(maxX, pos.getX());
				maxY = Math.max(maxY, pos.getY());
				maxZ = Math.max(maxZ, pos.getZ());
			}
			combined = new Box(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
		}
		
		return combined;
	}
	
	private List<Set<BlockPos>> findConnectedBlocks(List<BlockPos> positions)
	{
		List<Set<BlockPos>> connectedGroups = new ArrayList<>();
		Set<BlockPos> remainingPositions = new HashSet<>(positions);
		
		while(!remainingPositions.isEmpty())
		{
			BlockPos start = remainingPositions.iterator().next();
			Set<BlockPos> currentGroup = new HashSet<>();
			Queue<BlockPos> queue = new LinkedList<>();
			queue.add(start);
			
			while(!queue.isEmpty())
			{
				BlockPos current = queue.poll();
				if(!remainingPositions.contains(current))
					continue;
				
				currentGroup.add(current);
				remainingPositions.remove(current);
				
				// Check all 6 adjacent blocks
				for(BlockPos neighbor : Arrays.asList(current.north(),
					current.east(), current.south(), current.west(),
					current.up(), current.down()))
				{
					if(remainingPositions.contains(neighbor))
					{
						queue.add(neighbor);
					}
				}
			}
			
			if(!currentGroup.isEmpty())
			{
				connectedGroups.add(currentGroup);
			}
		}
		
		return connectedGroups;
	}
}
