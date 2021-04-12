package de.ellpeck.craftabledeeds.mixin;

import de.ellpeck.craftabledeeds.DeedStorage;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DispenserBlock.class)
public abstract class MixinDispenserBlock extends ContainerBlock {

    protected MixinDispenserBlock(Properties builder) {
        super(builder);
    }

    @Inject(at = @At("HEAD"), method = "dispense(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/util/math/BlockPos;)V", cancellable = true)
    protected void dispense(ServerWorld worldIn, BlockPos pos, CallbackInfo callback) {
        DeedStorage storage = DeedStorage.get(worldIn);
        DeedStorage.Claim claim = storage.getClaim(pos.getX(), pos.getY(), pos.getZ());
        if (claim != null && claim.isActive() && !claim.canDispensersPlace)
            callback.cancel();
    }
}
