package com.github.bigenergy.torchslabs.event;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

import com.github.bigenergy.torchslabs.TorchSlabsMod;
import com.github.bigenergy.torchslabs.SupportUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(modid=TorchSlabsMod.MODID)
public class PlaceHandlerCandleSlab {

		private static final HashMap<ResourceLocation, Supplier<Block>> PLACE_ENTRIES = new HashMap<>();
		
		@SubscribeEvent
		public static void onBlockEntityPlace(RightClickBlock event)
		{	
			ItemStack held = event.getItemStack();
			ResourceLocation rl = ForgeRegistries.ITEMS.getKey(held.getItem());

			if(PLACE_ENTRIES.containsKey(rl))
			{
				placeCandle(event, held, PLACE_ENTRIES.get(rl).get());
			}
				
		}

		public static void placeCandle(RightClickBlock event, ItemStack held, Block block)
		{		
			BlockPos pos = event.getPos();
			Direction face = event.getFace();
			BlockPos placeAt = pos.relative(face);
			Level world = event.getLevel();
			SoundType soundType;
					
			
			if(SupportUtil.isBottomSupport(world.getBlockState(pos)) && face == Direction.UP
					&& (world.isEmptyBlock(placeAt) || world.getFluidState(placeAt).getType() == Fluids.WATER || world.getFluidState(placeAt).getType() == Fluids.FLOWING_WATER) )
			{
				if(block instanceof SimpleWaterloggedBlock) {
					world.setBlockAndUpdate(placeAt, block.defaultBlockState()
							.setValue(BlockStateProperties.WATERLOGGED, (world.getFluidState(placeAt).getType() == Fluids.WATER) ));
				} else {
					world.setBlockAndUpdate(placeAt, block.defaultBlockState());
				}
				
				soundType = block.getSoundType(block.defaultBlockState(), world, pos, event.getEntity());
				world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), soundType.getPlaceSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch() - 0.2F);
				event.getEntity().swing(event.getHand());

				if(!event.getEntity().isCreative())
					held.shrink(1);
				event.setCanceled(true);			
			}
			else if((SupportUtil.isBottomSupport(world.getBlockState(pos)) && face == Direction.UP
					&& (world.getBlockState(placeAt).getBlock() == block && world.getBlockState(placeAt).getValue(CandleBlock.CANDLES) < 4))
					|| (world.getBlockState(pos).getBlock() == block && world.getBlockState(pos).getValue(CandleBlock.CANDLES) < 4 ))
			{
				if(world.getBlockState(pos).getBlock() == block) {
					placeAt = pos;
				}
				BlockState placeState = world.getBlockState(placeAt);
				
				world.setBlockAndUpdate(placeAt,placeState
						.setValue(CandleBlock.LIT, placeState.getValue(CandleBlock.LIT))
						.setValue(CandleBlock.CANDLES, placeState.getValue(CandleBlock.CANDLES) + 1)
						.setValue(CandleBlock.WATERLOGGED, placeState.getValue(CandleBlock.WATERLOGGED))
						);

				soundType = block.getSoundType(block.defaultBlockState(), world, pos, event.getEntity());
				world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), soundType.getPlaceSound(), SoundSource.BLOCKS, soundType.getVolume(), soundType.getPitch() - 0.2F);
				event.getEntity().swing(event.getHand());
				
				if(!event.getEntity().isCreative())
					held.shrink(1);
				event.setCanceled(true);		
				
			}
		}
		
		public static void registerPlaceEntry(ResourceLocation itemName, Supplier<Block> candleSlabSupplier)
		{
			if(!PLACE_ENTRIES.containsKey(itemName) && candleSlabSupplier != null)
				PLACE_ENTRIES.put(itemName, candleSlabSupplier);
		}
	}
