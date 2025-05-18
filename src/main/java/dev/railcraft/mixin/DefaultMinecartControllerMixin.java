package dev.railcraft.mixin;

import dev.railcraft.block.custom.LubricatedRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import net.minecraft.entity.vehicle.MinecartController;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultMinecartController.class)
public abstract class DefaultMinecartControllerMixin extends MinecartController {

    protected DefaultMinecartControllerMixin(AbstractMinecartEntity minecart) {
        super(minecart);
    }

    @Unique
    private static final double LUBRICATED_SPEED_MULTIPLIER = 1.5;


    @Unique
    private boolean isOnLubricatedRail() {
        double x = this.minecart.getX();
        double y = this.minecart.getY();
        double z = this.minecart.getZ();

        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y);
        int k = MathHelper.floor(z);

        if (this.minecart.getWorld().getBlockState(new BlockPos(i, j - 1, k)).isIn(BlockTags.RAILS)) {
            --j;
        }

        BlockPos railPos = new BlockPos(i, j, k);
        BlockState blockState = this.minecart.getWorld().getBlockState(railPos);

        return blockState.isIn(BlockTags.RAILS) && blockState.getBlock() instanceof LubricatedRailBlock;
    }

    @Inject(
            method = "getSpeedRetention",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getSpeedRetention(CallbackInfoReturnable<Double> cir) {
        if (isOnLubricatedRail()) {
            // Equal to the speed retention of a rail with passenger (the close to 1, the longer the minecart stays in motion)
            cir.setReturnValue(0.997);
        }
    }

    @Inject(
            method = "getMaxSpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getMaxSpeed(ServerWorld world, CallbackInfoReturnable<Double> cir) {
        if (isOnLubricatedRail()) {
            // Increase max speed by 50% on lubricated rails (larger the number, faster the minecart goes)
            double baseSpeed = cir.getReturnValue();
            cir.setReturnValue(baseSpeed * LUBRICATED_SPEED_MULTIPLIER);
        }
    }

    // not sure if this is needed, but it seems to limit the speed of the minecart
    @Inject(
            method = "limitSpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void limitSpeed(Vec3d velocity, CallbackInfoReturnable<Vec3d> cir) {
        if (isOnLubricatedRail()) {
            Vec3d currentLimit = cir.getReturnValue();

            double currentMaxSpeed = Math.max(Math.abs(currentLimit.x), Math.abs(currentLimit.z));

            double lubricatedMaxSpeed = currentMaxSpeed * LUBRICATED_SPEED_MULTIPLIER;

            cir.setReturnValue(new Vec3d(
                    MathHelper.clamp(velocity.x, -lubricatedMaxSpeed, lubricatedMaxSpeed),
                    velocity.y,
                    MathHelper.clamp(velocity.z, -lubricatedMaxSpeed, lubricatedMaxSpeed)
            ));
        }
    }
}