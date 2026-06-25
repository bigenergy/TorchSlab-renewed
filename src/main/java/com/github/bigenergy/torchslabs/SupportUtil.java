package com.github.bigenergy.torchslabs;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Shared "is this block a valid support" checks for the floor-style lights.
 * A bottom slab and a bottom stair both expose a flat surface you can stand a light on top of;
 * a top slab and a top stair both expose an underside you can hang a light from.
 */
public final class SupportUtil
{
	private SupportUtil() {}

	/** Top surface you can stand a light on: a BOTTOM slab or a BOTTOM-half stair. */
	public static boolean isBottomSupport(BlockState state)
	{
		return (state.getBlock() instanceof SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM)
				|| (state.getBlock() instanceof StairBlock && state.getValue(StairBlock.HALF) == Half.BOTTOM);
	}

	/** Underside you can hang a light from: a TOP slab or a TOP-half stair. */
	public static boolean isTopSupport(BlockState state)
	{
		return (state.getBlock() instanceof SlabBlock && state.getValue(SlabBlock.TYPE) == SlabType.TOP)
				|| (state.getBlock() instanceof StairBlock && state.getValue(StairBlock.HALF) == Half.TOP);
	}
}
