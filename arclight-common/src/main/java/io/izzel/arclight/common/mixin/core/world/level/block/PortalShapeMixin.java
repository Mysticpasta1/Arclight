package io.izzel.arclight.common.mixin.core.world.level.block;

import io.izzel.arclight.common.bridge.core.block.PortalInfoBridge;
import io.izzel.arclight.common.bridge.core.block.PortalSizeBridge;
import io.izzel.arclight.common.bridge.core.world.IWorldBridge;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.extensions.IForgeBlockState;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.block.CraftBlockState;
import org.bukkit.craftbukkit.v.block.CraftBlockStates;
import org.bukkit.craftbukkit.v.event.CraftPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(PortalShape.class)
public abstract class PortalShapeMixin implements PortalSizeBridge {

    // @formatter:off
    @Final @Shadow private static BlockBehaviour.StatePredicate FRAME = IForgeBlockState::isPortalFrame;
    @Shadow @Final private LevelAccessor level;
    @Shadow public abstract void shadow$createPortalBlocks();
    @Shadow @Final private Direction.Axis axis;
    @Shadow @Nullable private BlockPos bottomLeft;
    @Shadow private int height;
    @Shadow @Final private Direction rightDir;
    @Shadow @Final private int width;
    @Shadow public static PortalInfo createPortalInfo(ServerLevel p_259301_, BlockUtil.FoundRectangle p_259931_, Direction.Axis p_259901_, Vec3 p_259630_, Entity p_259166_, Vec3 p_260043_, float p_259853_, float p_259667_) { return null; }
    // @formatter:on

    List<BlockState> blocks = new ArrayList<>();

    @Inject(method = "getDistanceUntilEdgeAboveFrame", at = @At(value = "HEAD"), cancellable = true)
    private void arclight$captureBlock(BlockPos p_77736_, Direction p_77737_, CallbackInfoReturnable<Integer> cir) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for(int i = 0; i <= 21; ++i) {
            blockpos$mutableblockpos.set(p_77736_).move(p_77737_, i);
            net.minecraft.world.level.block.state.BlockState blockstate = this.level.getBlockState(blockpos$mutableblockpos);
            if (!arclight$isEmpty(blockstate)) {
                if (FRAME.test(blockstate, this.level, blockpos$mutableblockpos)) {
                    blocks.add(CraftBlock.at(this.level, p_77736_).getState());
                    cir.setReturnValue(i);
                }
                break;
            }

            net.minecraft.world.level.block.state.BlockState blockstate1 = this.level.getBlockState(blockpos$mutableblockpos.move(Direction.DOWN));
            if (!FRAME.test(blockstate1, this.level, blockpos$mutableblockpos)) {
                break;
            }
        }

        cir.setReturnValue(0);
    }

    @Unique
    private static boolean arclight$isEmpty(net.minecraft.world.level.block.state.BlockState p_77718_) {
        return p_77718_.isAir() || p_77718_.is(BlockTags.FIRE) || p_77718_.is(Blocks.NETHER_PORTAL);
    }

    @Inject(method = "createPortalBlocks", cancellable = true, at = @At("HEAD"))
    private void arclight$buildPortal(CallbackInfo ci) {
        World world = ((WorldBridge) ((IWorldBridge) this.level).bridge$getMinecraftWorld()).bridge$getWorld();
        net.minecraft.world.level.block.state.BlockState blockState = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
        BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1)).forEach(pos -> {
            CraftBlockState state = CraftBlockStates.getBlockState(((IWorldBridge) this.level).bridge$getMinecraftWorld(), pos, 18);
            state.setData(blockState);
            this.blocks.add(state);
        });
        PortalCreateEvent event = new PortalCreateEvent(this.blocks, world, null, PortalCreateEvent.CreateReason.FIRE);
        Bukkit.getPluginManager().callEvent(event);
        arclight$ret = !event.isCancelled();
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    private transient boolean arclight$ret;

    public boolean createPortalBlocks() {
        this.shadow$createPortalBlocks();
        return arclight$ret;
    }

    @Override
    public boolean bridge$createPortal() {
        return createPortalBlocks();
    }

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "createPortalInfo", at = @At(value = "NEW", target = "net/minecraft/world/level/portal/PortalInfo"))
    private static PortalInfo arclight$setPortalInfo(Vec3 pos, Vec3 motion, float rotationYaw, float rotationPitch, ServerLevel world) {
        PortalInfo portalInfo = new PortalInfo(pos, motion, rotationYaw, rotationPitch);
        ((PortalInfoBridge) portalInfo).bridge$setWorld(world);
        ((PortalInfoBridge) portalInfo).bridge$setPortalEventInfo(ArclightCaptures.getCraftPortalEvent());
        return portalInfo;
    }

    private static PortalInfo createPortalInfo(ServerLevel world, BlockUtil.FoundRectangle result, Direction.Axis axis, Vec3 offsetVector, Entity entity, Vec3 motion, float rotationYaw, float rotationPitch, CraftPortalEvent event) {
        ArclightCaptures.captureCraftPortalEvent(event);
        return createPortalInfo(world, result, axis, offsetVector, entity, motion, rotationYaw, rotationPitch);
    }
}
