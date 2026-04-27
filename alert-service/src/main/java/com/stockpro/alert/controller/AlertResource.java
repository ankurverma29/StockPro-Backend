package com.stockpro.alert.controller;

import com.stockpro.alert.dto.request.AlertSearchRequest;
import com.stockpro.alert.dto.response.AlertActionResponse;
import com.stockpro.alert.dto.response.AlertResponse;
import com.stockpro.alert.dto.response.AlertSummaryResponse;
import com.stockpro.alert.dto.response.ApiResponse;
import com.stockpro.alert.dto.response.UnreadCountResponse;
import com.stockpro.alert.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/alerts")
@Tag(name = "Alert Management", description = "Dashboard alerts, system notifications, and acknowledgment APIs.")
@SecurityRequirement(name = "bearerAuth")
public class AlertResource {

    private static final String SEARCH_ALERT_EXAMPLE = """
            {
              "type": "LOW_STOCK",
              "severity": "CRITICAL",
              "isRead": false,
              "isAcknowledged": false,
              "startDate": "2024-01-01",
              "endDate": "2024-03-31",
              "page": 0,
              "size": 20,
              "sortBy": "createdAt",
              "sortDir": "desc"
            }
            """;

    private final AlertService alertService;

    public AlertResource(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get alert by id")
    public ResponseEntity<ApiResponse<AlertResponse>> getById(@PathVariable Long id) {
        AlertResponse response = alertService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Alert retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get all alerts with pagination")
    public ResponseEntity<ApiResponse<Page<AlertResponse>>> getAllAlerts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Page<AlertResponse> response = alertService.getAllAlerts(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "Alerts retrieved successfully"));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(
            summary = "Search alerts with filters",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "SearchAlerts", value = SEARCH_ALERT_EXAMPLE))))
    public ResponseEntity<ApiResponse<Page<AlertResponse>>> searchAlerts(@Valid @RequestBody AlertSearchRequest request) {
        Page<AlertResponse> response = alertService.searchAlerts(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Alert search results retrieved successfully"));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Mark alert as read")
    public ResponseEntity<ApiResponse<AlertResponse>> markAsRead(@PathVariable Long id) {
        AlertResponse response = alertService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Alert marked as read"));
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Mark all unread alerts as read")
    public ResponseEntity<ApiResponse<AlertActionResponse>> markAllAsRead() {
        AlertActionResponse response = alertService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success(response, "All alerts marked as read"));
    }

    @PutMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Acknowledge alert with optional notes")
    public ResponseEntity<ApiResponse<AlertResponse>> acknowledge(@PathVariable Long id, @RequestParam(required = false) String notes) {
        AlertResponse response = alertService.acknowledge(id, notes);
        return ResponseEntity.ok(ApiResponse.success(response, "Alert acknowledged successfully"));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get count of unread alerts")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        UnreadCountResponse response = alertService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(response, "Unread count retrieved successfully"));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get alert summary by type and severity")
    public ResponseEntity<ApiResponse<List<AlertSummaryResponse>>> getAlertSummary() {
        List<AlertSummaryResponse> response = alertService.getAlertSummary();
        return ResponseEntity.ok(ApiResponse.success(response, "Alert summary retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete alert (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteAlert(@PathVariable Long id) {
        alertService.deleteAlert(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Alert deleted successfully"));
    }
}
