package com.alonie.xaero_worldgen.bridge.core;


import com.alonie.xaero_worldgen.bridge.core.*;
import com.alonie.xaero_worldgen.bridge.state.*;
import com.alonie.xaero_worldgen.bridge.policy.*;
import com.alonie.xaero_worldgen.bridge.audit.*;
import com.alonie.xaero_worldgen.bridge.snapshot.*;
import com.alonie.xaero_worldgen.bridge.integration.voxy.*;
import com.alonie.xaero_worldgen.bridge.integration.xaero.*;
import com.alonie.xaero_worldgen.bridge.migration.*;
import me.cortex.voxy.commonImpl.WorldIdentifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.dimension.DimensionType;

import java.nio.file.Path;

public final class BridgePaths {
    private BridgePaths() {
    }

    public static Path getSaveRoot(ServerLevel world) {
        return world.getServer().getWorldPath(LevelResource.ROOT);
    }

    public static String getWorldHash(ServerLevel world) {
        return WorldIdentifier.of(world).getWorldId();
    }

    public static String getDimensionToken(ServerLevel world) {
        return sanitizeDimensionToken(world.dimension().toString());
    }

    public static String sanitizeDimensionToken(String rawDimensionKey) {
        return rawDimensionKey
            .replace("ResourceKey[", "")
            .replace("]", "")
            .replace("/", "_")
            .replace(":", "_")
            .trim();
    }

    public static Path getBridgeRoot(ServerLevel world) {
        return getSaveRoot(world).resolve("voxy").resolve("bridge");
    }

    public static Path getBridgeStubRegionDirectory(ServerLevel world) {
        return getBridgeRoot(world)
            .resolve("stub_regions")
            .resolve(getWorldHash(world))
            .resolve(getDimensionToken(world));
    }

    public static Path getBridgeStubRegionFile(ServerLevel world, int regionX, int regionZ) {
        return getBridgeStubRegionDirectory(world).resolve("r." + regionX + "." + regionZ + ".mca");
    }

    public static Path getQuarantineDirectory(ServerLevel world) {
        return getBridgeRoot(world)
            .resolve("quarantine")
            .resolve(getWorldHash(world))
            .resolve(getDimensionToken(world));
    }

    public static Path getQuarantineManifestFile(ServerLevel world) {
        return getQuarantineDirectory(world).resolve("manifest.log");
    }

    public static Path getDirtyFile(ServerLevel world) {
        return getBridgeRoot(world)
            .resolve("dirty")
            .resolve(getWorldHash(world))
            .resolve(getDimensionToken(world) + ".txt");
    }

    public static Path getKnownRegionsFile(ServerLevel world) {
        return getBridgeRoot(world)
            .resolve("known")
            .resolve(getWorldHash(world))
            .resolve(getDimensionToken(world) + ".txt");
    }

    public static Path getVoxyGenIndexFile(ServerLevel world) {
        return getSaveRoot(world).resolve("voxy_gen_" + getDimensionToken(world) + ".bin");
    }

    public static Path getRegionDirectory(ServerLevel world) {
        return DimensionType.getStorageFolder(world.dimension(), getSaveRoot(world)).resolve("region");
    }

    public static Path getRegionFile(ServerLevel world, int regionX, int regionZ) {
        return getRegionDirectory(world).resolve("r." + regionX + "." + regionZ + ".mca");
    }

    public static String getRuntimeCacheKey(ServerLevel world) {
        return getSaveRoot(world) + "|" + getWorldHash(world) + "|" + getDimensionToken(world);
    }
}


