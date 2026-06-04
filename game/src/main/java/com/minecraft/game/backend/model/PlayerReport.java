package com.minecraft.game.backend.model;

import com.minecraft.game.report.ReportReason;
import com.minecraft.game.report.ReportStatus;

import java.time.Instant;
import java.util.UUID;

public record PlayerReport(
        long id,
        UUID reporterId,
        String reporterName,
        UUID targetId,
        String targetName,
        ReportReason reason,
        String details,
        String serverId,
        ReportStatus status,
        UUID staffId,
        String staffNote,
        Instant createdAt,
        Instant updatedAt
) {}
