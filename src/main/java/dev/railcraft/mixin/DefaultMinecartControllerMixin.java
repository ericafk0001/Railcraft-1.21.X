package dev.railcraft.mixin;

import dev.railcraft.block.custom.IceRailBlock;
import dev.railcraft.block.custom.LubricatedRailBlock;
import dev.railcraft.block.custom.PoweredIceRailBlock;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.enums.RailShape;
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
    @Unique
    private static final double ICE_SPEED_MULTIPLIER = 2.7;
    @Unique
    private static final double POWERED_ICE_RAIL_MULTIPLIER = 0.2;
    @Unique
    private static final double POWERED_ICE_RAIL_ON_MULTIPLIER = 10;
    @Unique
    private static final double GENTLE_PUSH = 0.02;
    @Unique
    private static final double MOVEMENT_THRESHOLD = 0.01;


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
            method = "moveOnRail",
            at = @At("TAIL")
    )
    private void applyPoweredIceRailLogic(ServerWorld world, CallbackInfo ci) {
        if (!isOnPoweredIceRail()) {
            return;
        }

        BlockPos blockPos = this.minecart.getRailOrMinecartPos();
        BlockState blockState = this.getWorld().getBlockState(blockPos);

        // Handle powered ice rail behavior
        if (isPoweredIceRailPowered()) {
            applyPoweredIceRailAcceleration(blockPos, blockState);
        } else {
            applyPoweredIceRailDeceleration();
        }
    }

    @Unique
    private void applyPoweredIceRailAcceleration(BlockPos railPos, BlockState railState) {
        Vec3d currentVelocity = this.getVelocity();
        double currentHorizontalSpeed = currentVelocity.horizontalLength();

        if (currentHorizontalSpeed > MOVEMENT_THRESHOLD) {
            // Add acceleration in direction of movement
            this.setVelocity(currentVelocity.add(
                    currentVelocity.x / currentHorizontalSpeed * POWERED_ICE_RAIL_ON_MULTIPLIER,
                    0.0,
                    currentVelocity.z / currentHorizontalSpeed * POWERED_ICE_RAIL_ON_MULTIPLIER
            ));
        } else {
            // Small push if nearly stopped
            double newXVelocity = currentVelocity.x;
            double newZVelocity = currentVelocity.z;
            RailShape railShape = railState.get(((AbstractRailBlock)railState.getBlock()).getShapeProperty());

            if (railShape == RailShape.EAST_WEST) {
                if (this.minecart.willHitBlockAt(railPos.west())) {
                    newXVelocity = GENTLE_PUSH;
                } else if (this.minecart.willHitBlockAt(railPos.east())) {
                    newXVelocity = -GENTLE_PUSH;
                }
            } else if (railShape == RailShape.NORTH_SOUTH) {
                if (this.minecart.willHitBlockAt(railPos.north())) {
                    newZVelocity = GENTLE_PUSH;
                } else if (this.minecart.willHitBlockAt(railPos.south())) {
                    newZVelocity = -GENTLE_PUSH;
                }
            }

            this.setVelocity(newXVelocity, currentVelocity.y, newZVelocity);
        }
    }

    @Unique
    private void applyPoweredIceRailDeceleration() {
        double speed = this.getVelocity().horizontalLength();
        if (speed < 0.03) {
            // Stop completely if moving very slowly
            this.setVelocity(Vec3d.ZERO);
        } else {
            // Reduce speed by half
            this.setVelocity(this.getVelocity().multiply(0.5, 0.0, 0.5));
        }
    }

}

