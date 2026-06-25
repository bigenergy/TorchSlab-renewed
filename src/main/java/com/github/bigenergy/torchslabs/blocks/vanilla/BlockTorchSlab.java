package com.github.bigenergy.torchslabs.blocks.vanilla;

//import com.endlesnights.naturalslabsmod.blocks.FenceSlabBlock;

import com.github.bigenergy.torchslabs.SupportUtil;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class BlockTorchSlab extends TorchBlock
{
	// HANGING == false: torch sits on top of a BOTTOM slab (pokes down into it).
	// HANGING == true:  torch hangs from the underside of a slab and points down.
	//   LOWERED == false: hangs under a TOP slab    (slab underside at mid-block above -> torch sits high)
	//   LOWERED == true:  hangs under a BOTTOM slab  (slab underside at the block boundary -> torch sits half a block lower)
	public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
	public static final BooleanProperty LOWERED = BooleanProperty.create("lowered");

	protected static final VoxelShape SLAB_SHAPE = Block.box(6.0D, -8.0D, 6.0D, 10.0D, 8.0D, 10.0D);
	protected static final VoxelShape CEILING_SHAPE = Block.box(6.0D, 8.0D, 6.0D, 10.0D, 24.0D, 10.0D);
	protected static final VoxelShape CEILING_SHAPE_LOW = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);

	final ParticleOptions FLAMEPART;
	final Character TYPE;

	public BlockTorchSlab(Block.Properties properties, ParticleOptions particleType, Character type)
	{
		super(properties, particleType);
		this.TYPE = type;
		this.FLAMEPART = particleType;
		this.registerDefaultState(this.stateDefinition.any().setValue(HANGING, false).setValue(LOWERED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context)
	{
		if(state.getValue(HANGING))
			return state.getValue(LOWERED) ? CEILING_SHAPE_LOW : CEILING_SHAPE;
		return SLAB_SHAPE;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor world, BlockPos currentPos, BlockPos facingPos)
	{
		if(state.getValue(HANGING))
			return facing == Direction.UP && !canSurvive(state, world, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, world, currentPos, facingPos);
		else
			return facing == Direction.DOWN && !canSurvive(state, world, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos)
	{
		if(state.getValue(HANGING))
			return state.getValue(LOWERED)
					? SupportUtil.isBottomSupport(world.getBlockState(pos.above()))
					: SupportUtil.isTopSupport(world.getBlockState(pos.above()));
		else
			return SupportUtil.isBottomSupport(world.getBlockState(pos.below()))
					//|| (ModList.get().isLoaded("naturalslabsmod") && world.getBlockState(pos.relative(Direction.DOWN)).getBlock() instanceof FenceSlabBlock)
					;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(HANGING, LOWERED);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level world, BlockPos pos, RandomSource rand)
	{
		double x = pos.getX() + 0.5D;
		double z = pos.getZ() + 0.5D;
		// Flame sits at the lit tip. Floor torch: low. Hanging under a TOP slab: high. Hanging under a BOTTOM slab: half a block lower.
		double y;
		if(stateIn.getValue(HANGING))
			y = stateIn.getValue(LOWERED) ? pos.getY() + 0.3D : pos.getY() + 0.8D;
		else
			y = pos.getY() + 0.2D;

		world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
		world.addParticle(FLAMEPART, x, y, z, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter  world, BlockPos pos, Player player)
	{
		return new ItemStack(TYPE == 't' ? Items.TORCH : Items.AIR);
	}
}
