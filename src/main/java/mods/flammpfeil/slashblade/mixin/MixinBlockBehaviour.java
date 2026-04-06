package mods.flammpfeil.slashblade.mixin;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockBehaviour.BlockStateBase.class, remap = false)
public abstract class MixinBlockBehaviour {

    @Unique
    private BlockState slashblade$self() {
        return (BlockState) (Object) this;
    }

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false  // ← 追加
    )
    private void slashblade$getCollisionShape(
            BlockGetter level,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        BlockState self = slashblade$self();

        if (!self.is(BlockTags.LEAVES)) {
            return;
        }
        if (context.isDescending()) {
            return;
        }
        if (!(context instanceof EntityCollisionContext entityContext)) {
            return;
        }
        if (!(entityContext.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack itemStack = player.getMainHandItem();
        if (!(itemStack.getItem() instanceof ItemSlashBlade)) {
            return;
        }

        cir.setReturnValue(Blocks.SCAFFOLDING.defaultBlockState().getCollisionShape(level, pos, context));
    }

    @Inject(
            method = "getVisualShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false  // ← 追加
    )
    private void slashblade$getVisualShape(
            BlockGetter level,
            BlockPos pos,
            CollisionContext context,
            CallbackInfoReturnable<VoxelShape> cir
    ) {
        BlockState self = slashblade$self();

        if (!self.is(BlockTags.LEAVES)) {
            return;
        }

        cir.setReturnValue(Blocks.SCAFFOLDING.defaultBlockState().getVisualShape(level, pos, context));
    }
}