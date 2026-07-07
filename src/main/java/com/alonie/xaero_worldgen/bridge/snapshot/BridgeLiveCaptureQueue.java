package com.alonie.xaero_worldgen.bridge.snapshot;


import com.alonie.xaero_worldgen.bridge.core.*;
import com.alonie.xaero_worldgen.bridge.state.*;
import com.alonie.xaero_worldgen.bridge.policy.*;
import com.alonie.xaero_worldgen.bridge.audit.*;
import com.alonie.xaero_worldgen.bridge.snapshot.*;
import com.alonie.xaero_worldgen.bridge.integration.voxy.*;
import com.alonie.xaero_worldgen.bridge.integration.xaero.*;
import com.alonie.xaero_worldgen.bridge.migration.*;
import com.alonie.xaero_worldgen.VwgXwmBridgeClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public final class BridgeLiveCaptureQueue {
    private static final ConcurrentHashMap<String, WorldQueue> QUEUES = new ConcurrentHashMap<>();

    private BridgeLiveCaptureQueue() {
    }

    public static void requestCapture(Level world, int chunkX, int chunkZ) {
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }

        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        if (!BridgeSourcePolicy.allowsBridgeDataPipeline(serverWorld, regionX, regionZ)) {
            return;
        }

        worldQueue(serverWorld).offer(chunkX, chunkZ);
    }

    public static void tickServer(MinecraftServer server) {
        long serverTick = server.getTickCount();
        for (ServerLevel world : server.getAllLevels()) {
            WorldQueue queue = QUEUES.get(BridgePaths.getRuntimeCacheKey(world));
            if (queue == null) {
                continue;
            }
            processWorld(world, queue, serverTick);
        }
    }

    public static void clearRuntimeState() {
        QUEUES.clear();
    }

    private static void processWorld(ServerLevel world, WorldQueue queue, long serverTick) {
        int processed = 0;
        long startedAt = System.nanoTime();
        while (processed < BridgePerfBudget.LIVE_CAPTURE_CHUNKS_PER_TICK
            && (System.nanoTime() - startedAt) < BridgePerfBudget.LIVE_CAPTURE_TIME_BUDGET_NANOS) {
            long packedChunk = queue.poll();
            if (packedChunk == Long.MIN_VALUE) {
                break;
            }

            int chunkX = (int) (packedChunk >> 32);
            int chunkZ = (int) packedChunk;
            int regionX = chunkX >> 5;
            int regionZ = chunkZ >> 5;
            if (!BridgeSourcePolicy.allowsBridgeDataPipeline(world, regionX, regionZ)) {
                continue;
            }
            BridgeChunkSnapshotStore.captureLiveChunk(world, chunkX, chunkZ);
            processed++;
        }

        if (serverTick % BridgePerfBudget.PERF_LOG_INTERVAL_TICKS == 0L) {
            int pending = queue.pendingSize();
            if (processed > 0 || pending > 0) {
                long elapsedNanos = System.nanoTime() - startedAt;
                int activeLeases = BridgeLoadLeaseTracker.countActiveNoPurge(BridgePaths.getRuntimeCacheKey(world));
                BridgeLog.info(
                    VwgXwmBridgeClient.LOGGER,
                    "[VWG->XWM Bridge][Perf] live_capture dim={} processed={} pending={} elapsedMs={} leaseActive={}",
                    world.dimension().identifier(),
                    processed,
                    pending,
                    String.format(java.util.Locale.ROOT, "%.3f", elapsedNanos / 1_000_000.0D),
                    activeLeases
                );
            }
        }
    }

    private static WorldQueue worldQueue(ServerLevel world) {
        return QUEUES.computeIfAbsent(BridgePaths.getRuntimeCacheKey(world), ignored -> new WorldQueue());
    }

    private static final class WorldQueue {
        private final ArrayDeque<Long> queue = new ArrayDeque<>();
        private final HashSet<Long> enqueued = new HashSet<>();

        private synchronized void offer(int chunkX, int chunkZ) {
            long packedChunk = packChunk(chunkX, chunkZ);
            if (enqueued.add(packedChunk)) {
                queue.addLast(packedChunk);
            }
        }

        private synchronized long poll() {
            Long packedChunk = queue.pollFirst();
            if (packedChunk == null) {
                return Long.MIN_VALUE;
            }
            enqueued.remove(packedChunk);
            return packedChunk;
        }

        private synchronized int pendingSize() {
            return queue.size();
        }
    }

    private static long packChunk(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }
}


