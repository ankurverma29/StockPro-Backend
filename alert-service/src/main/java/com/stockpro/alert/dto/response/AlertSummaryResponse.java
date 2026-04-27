package com.stockpro.alert.dto.response;

import com.stockpro.alert.enums.AlertSeverity;
import com.stockpro.alert.enums.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Aggregated alert summary by type and severity.")
public class AlertSummaryResponse {

    @Schema(example = "LOW_STOCK")
    private AlertType type;

    @Schema(example = "WARNING")
    private AlertSeverity severity;

    @Schema(example = "10")
    private Long totalCount;

    @Schema(example = "3")
    private Long unreadCount;

    @Schema(example = "2026-04-25T09:30:00")
    private LocalDateTime latestAlertAt;
}
