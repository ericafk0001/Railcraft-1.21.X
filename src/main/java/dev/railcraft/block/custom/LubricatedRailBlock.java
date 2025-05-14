package dev.railcraft.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LubricatedRailBlock extends PoweredRailBlock {
    public LubricatedRailBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        super.onEntityCollision(state, world, pos, entity);
        if (entity instanceof AbstractMinecartEntity minecart) {
            Vec3d velocity = minecart.getVelocity();
            double speed = 1;
            minecart.setVelocity(velocity.multiply(speed));
        }
    }
}