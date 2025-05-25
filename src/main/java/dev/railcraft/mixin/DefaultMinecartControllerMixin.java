package dev.railcraft.mixin;

import dev.railcraft.block.custom.IceRailBlock;
import dev.railcraft.block.custom.LubricatedRailBlock;
import dev.railcraft.block.custom.PoweredIceRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultMinecartController.class)
public abstract class DefaultMinecartControllerMixin extends MinecartController {

    protected DefaultMinecartControllerMixin(AbstractMinecartEntity minecart) {
        super(minecart);
    }

    @Unique
    private static final double LUBRICATED_SPEED_MULTIPLIER = 1.45;
    private static final double ICE_SPEED_MULTIPLIER = 2.7;
    private static final double POWERED_ICE_RAIL_MULTIPLIER = 0.2;
    private static final double POWERED_ICE_RAIL_ON_MULTIPLIER = 10;


    @Unique
    private BlockState getRail() {
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
        return (this.minecart.getWorld().getBlockState(railPos));
    }

    @Unique
    private <T> boolean isOnRailOfType(Class<T> railClass) {
        BlockState blockState = getRail();
        return blockState.isIn(BlockTags.RAILS) && railClass.isInstance(blockState.getBlock());
    }

    @Unique
    private boolean isRailPowered() {
        BlockState blockState = getRail();
        return blockState.isIn(BlockTags.RAILS) &&
                blockState.getBlock() instanceof PoweredRailBlock &&
                blockState.get(PoweredRailBlock.POWERED);
    }

    @Unique
    private boolean isOnLubricatedRail() {
        return isOnRailOfType(LubricatedRailBlock.class);
    }

    @Unique
    private boolean isOnIceRail() {
        return isOnRailOfType(IceRailBlock.class);
    }

    @Unique
    private boolean isOnPoweredIceRail() {
        return isOnRailOfType(PoweredIceRailBlock.class);
    }

    @Unique
    private boolean isPoweredIceRailPowered() {
        return isOnRailOfType(PoweredIceRailBlock.class) && isRailPowered();
    }

    @Inject(
            method = "getSpeedRetention",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getSpeedRetention(CallbackInfoReturnable<Double> cir) {
        if (isOnLubricatedRail()) {
            // Equal to the speed retention of a rail with passenger (the close to 1, the longer the minecart stays in motion)
            cir.setReturnValue(0.9971);
        } else if (isOnIceRail()) {
            cir.setReturnValue(0.998);
        }
        else if (isPoweredIceRailPowered()) {
            cir.setReturnValue(0.9999);
        }
        else if (isOnPoweredIceRail()) {
            cir.setReturnValue(0.91);
        }
    }

    @Inject(
            method = "getMaxSpeed",
            at = @At("RETURN"),
            cancellable = true
    )
    private void getMaxSpeed(ServerWorld world, CallbackInfoReturnable<Double> cir) {
        if (isOnLubricatedRail()) {
            // Increase max speed by set % on lubricated rails (larger the number, faster the minecart goes)
            double baseSpeed = cir.getReturnValue();
            cir.setReturnValue(baseSpeed * LUBRICATED_SPEED_MULTIPLIER);
        } else if (isOnIceRail()) {
            double baseSpeed = cir.getReturnValue();
            cir.setReturnValue(baseSpeed * ICE_SPEED_MULTIPLIER);
        }
        else if (isPoweredIceRailPowered()) {
            double baseSpeed = cir.getReturnValue();
            cir.setReturnValue(baseSpeed * POWERED_ICE_RAIL_ON_MULTIPLIER);
        }
        else if (isOnPoweredIceRail()) {
            double baseSpeed = cir.getReturnValue();
            cir.setReturnValue(baseSpeed * POWERED_ICE_RAIL_MULTIPLIER);
        }
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void onTick(CallbackInfo ci) {
        if (isOnIceRail() || isPoweredIceRailPowered()) {
            Vec3d velocity = this.minecart.getVelocity();

            // Find the direction of motion on XZ plane
            double motionMagnitude = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            if (motionMagnitude > 0.01) {
                double boostStrength = isPoweredIceRailPowered() ? 0.12 : 0.04;
                double boostX = (velocity.x / motionMagnitude) * boostStrength;
                double boostZ = (velocity.z / motionMagnitude) * boostStrength;

                // Apply boost
                this.minecart.setVelocity(
                        velocity.x + boostX,
                        velocity.y,
                        velocity.z + boostZ
                );
            }
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
        } else if (isOnIceRail()) {
            Vec3d currentLimit = cir.getReturnValue();

            double currentMaxSpeed = Math.max(Math.abs(currentLimit.x), Math.abs(currentLimit.z));

            double iceMaxSpeed = currentMaxSpeed * ICE_SPEED_MULTIPLIER;

            cir.setReturnValue(new Vec3d(
                    MathHelper.clamp(velocity.x, -iceMaxSpeed, iceMaxSpeed),
                    velocity.y,
                    MathHelper.clamp(velocity.z, -iceMaxSpeed, iceMaxSpeed)
            ));

        } else if (isPoweredIceRailPowered()) {
            Vec3d currentLimit = cir.getReturnValue();

            double currentMaxSpeed = Math.max(Math.abs(currentLimit.x), Math.abs(currentLimit.z));

            double poweredIceRailOnMaxSpeed = currentMaxSpeed * POWERED_ICE_RAIL_ON_MULTIPLIER;

            cir.setReturnValue(new Vec3d(
                    MathHelper.clamp(velocity.x, -poweredIceRailOnMaxSpeed, poweredIceRailOnMaxSpeed),
                    velocity.y,
                    MathHelper.clamp(velocity.z, -poweredIceRailOnMaxSpeed, poweredIceRailOnMaxSpeed)
            ));
        }
        else if (isOnPoweredIceRail()) {
            Vec3d currentLimit = cir.getReturnValue();

            double currentMaxSpeed = Math.max(Math.abs(currentLimit.x), Math.abs(currentLimit.z));

            double poweredIceRailMaxSpeed = currentMaxSpeed * POWERED_ICE_RAIL_MULTIPLIER;

            cir.setReturnValue(new Vec3d(
                    MathHelper.clamp(velocity.x, -poweredIceRailMaxSpeed, poweredIceRailMaxSpeed),
                    velocity.y,
                    MathHelper.clamp(velocity.z, -poweredIceRailMaxSpeed, poweredIceRailMaxSpeed)
            ));
    }


}}

