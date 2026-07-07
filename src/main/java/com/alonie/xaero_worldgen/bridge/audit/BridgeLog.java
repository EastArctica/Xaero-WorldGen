package com.alonie.xaero_worldgen.bridge.audit;

import org.slf4j.Logger;

public final class BridgeLog {
    private BridgeLog() {
    }

    public static void info(Logger logger, String template, Object... args) {
        if (BridgeAuditConfig.current().loggingEnabled()) {
            logger.info(template, args);
        }
    }

    public static void warn(Logger logger, String template, Object... args) {
        if (BridgeAuditConfig.current().loggingEnabled()) {
            logger.warn(template, args);
        }
    }
}