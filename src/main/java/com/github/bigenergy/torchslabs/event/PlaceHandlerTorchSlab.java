package com.github.bigenergy.torchslabs.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

//import com.endlesnights.naturalslabsmod.blocks.FenceSlabBlock;
import com.github.bigenergy.torchslabs.TorchSlabsMod;
import com.github.bigenergy.torchslabs.SupportUtil;
import com.github.bigenergy.torchslabs.blocks.vanilla.BlockTorchSlab;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;


import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid=TorchSlabsMod.MODID)
public class PlaceHandlerTorchSlab
{
	private static final HashMap<ResourceLocation, Supplier<Block>> PLACE_ENTRIES = new HashMap<>();
	
	@SubscribeEvent
	public static void onBlockEntityPlace(RightClickBlock event)
	{	
		ItemStack held = event.getItemStack();
		ResourceLocation rl = ForgeRegistries.ITEMS.getKey(held.getItem());

		if(PLACE_ENTRIES.containsKey(rl))
		{
			placeTorch(event, held, PLACE_ENTRIES.get(rl).get());
		}
			
	}

	public static void placeTorch(RightClickBlock event, ItemStack held, Block block)
	{		
		BlockPos pos = event.getPos();
		Direction face = event.getFace();
		BlockPos placeAt = pos.relative(face);
		Level world = event.getLevel();
		SoundType soundType;

		// Top of a BOTTOM slab -> upright torch.
		// Underside of a TOP slab    -> hanging torch (sits high).
		// Underside of a BOTTOM slab -> hanging torch (sits half a block lower).
		boolean onBottomSlabTop = face == Direction.UP && SupportUtil.isBottomSupport(world.getBlockState(pos));
		boolean underTopSlab = face == Direction.DOWN && SupportUtil.isTopSupport(world.getBlockState(pos));
		boolean underBottomSlab = face == Direction.DOWN && SupportUtil.isBottomSupport(world.getBlockState(pos));
		boolean hanging = underTopSlab || underBottomSlab;

		if(
				(onBottomSlabTop || hanging)
				//|| (ModList.get().isLoaded("naturalslabsmod") && world.getBlockState(pos).getBlock() instanceof FenceSlabBlock)
				&& (world.isEmptyBlock(placeAt) || world.getFluidState(placeAt).getType() == Fluids.WATER || world.getFluidState(placeAt).getType() == Fluids.FLOWING_WATER) )
		{
			BlockState toPlace = block.defaultBlockState()
					.setValue(BlockStateProperties.HANGING, hanging)
					.setValue(BlockTorchSlab.LOWERED, underBottomSlab);
			if (block instanceof SimpleWaterloggedBlock)
				toPlace = toPlace.setValue(BlockStateProperties.WATERLOGGED, (world.getFluidState(placeAt).getType() == Fluids.WATER) );
			world.setBlockAndUpdate(placeAt, toPlace);

//			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), block.getSoundType(world.getBlockState(pos)).getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
			soundType = block.getSoundType(block.defaultBlockState(), world, pos, event.getEntity());
			world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), soundType.getPlaceSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch() - 0.2F);
			event.getEntity().swing(event.getHand());
			
			if(!event.getEntity().isCreative())
				held.shrink(1);
			event.setCanceled(true);
		}
	}
	
	public static void registerPlaceEntry(ResourceLocation itemName, Supplier<Block> torchSlabSupplier)
	{
		if(!PLACE_ENTRIES.containsKey(itemName) && torchSlabSupplier != null)
			PLACE_ENTRIES.put(itemName, torchSlabSupplier);
	}
}
