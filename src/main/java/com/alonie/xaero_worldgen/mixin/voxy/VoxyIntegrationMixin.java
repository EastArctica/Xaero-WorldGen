package com.alonie.xaero_worldgen.mixin.voxy;

import com.alonie.xaero_worldgen.bridge.snapshot.BridgeLiveCaptureQueue;
import com.alonie.xaero_worldgen.bridge.integration.voxy.VoxyChunkReadinessTracker;
import com.alonie.xaero_worldgen.bridge.integration.voxy.VoxyDirtyRegionMarker;
import com.ethan.voxyworldgenv2.integration.VoxyIntegration;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VoxyIntegration.class)
public abstract class VoxyIntegrationMixin {
    @Inject(method = "ingestChunk", at = @At("TAIL"))
    private static void vwgxwm$markIngestedChunk(LevelChunk chunk, CallbackInfo ci) {
        VoxyChunkReadinessTracker.recordFullChunkIngest(chunk.getLevel(), chunk.getPos().x(), chunk.getPos().z());
        if (VoxyDirtyRegionMarker.markChunkDirty(chunk.getLevel(), chunk.getPos().x(), chunk.getPos().z())) {
            BridgeLiveCaptureQueue.requestCapture(chunk.getLevel(), chunk.getPos().x(), chunk.getPos().z());
        }
    }

    @Inject(
        method = "rawIngest(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/chunk/DataLayer;)V",
        at = @At("TAIL")
    )
    private static void vwgxwm$markRawChunk(LevelChunk chunk, DataLayer blockLight, CallbackInfo ci) {
        VoxyChunkReadinessTracker.recordFullChunkIngest(chunk.getLevel(), chunk.getPos().x(), chunk.getPos().z());
        if (VoxyDirtyRegionMarker.markChunkDirty(chunk.getLevel(), chunk.getPos().x(), chunk.getPos().z())) {
            BridgeLiveCaptureQueue.requestCapture(chunk.getLevel(), chunk.getPos().x(), chunk.getPos().z());
        }
    }

    @Inject(
        method = "rawIngest(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/chunk/LevelChunkSection;IIILnet/minecraft/world/level/chunk/DataLayer;Lnet/minecraft/world/level/chunk/DataLayer;)V",
        at = @At("TAIL")
    )
    private static void vwgxwm$markRawSection(
            Level world,
            LevelChunkSection section,
            int chunkX,
            int sectionY,
            int chunkZ,
            DataLayer blockLight,
            DataLayer skyLight,
            CallbackInfo ci
    ) {
        if (VoxyChunkReadinessTracker.recordSectionIngest(world, chunkX, sectionY, chunkZ)) {
            if (VoxyDirtyRegionMarker.markChunkDirty(world, chunkX, chunkZ)) {
                BridgeLiveCaptureQueue.requestCapture(world, chunkX, chunkZ);
            }
        }
    }

    @Inject(
        method = "rawIngest(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/chunk/LevelChunkSection;IIILnet/minecraft/world/level/chunk/DataLayer;)V",
        at = @At("TAIL")
    )
    private static void vwgxwm$markRawSectionSingleLight(
            Level world,
            LevelChunkSection section,
            int chunkX,
            int sectionY,
            int chunkZ,
            DataLayer blockLight,
            CallbackInfo ci
    ) {
        if (VoxyChunkReadinessTracker.recordSectionIngest(world, chunkX, sectionY, chunkZ)) {
            if (VoxyDirtyRegionMarker.markChunkDirty(world, chunkX, chunkZ)) {
                BridgeLiveCaptureQueue.requestCapture(world, chunkX, chunkZ);
            }
        }
    }
}

