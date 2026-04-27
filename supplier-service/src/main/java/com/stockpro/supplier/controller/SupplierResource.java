package com.stockpro.supplier.controller;

import com.stockpro.supplier.dto.ApiResponse;
import com.stockpro.supplier.dto.SupplierRatingRequest;
import com.stockpro.supplier.dto.SupplierRequest;
import com.stockpro.supplier.dto.SupplierResponse;
import com.stockpro.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/suppliers")
@Tag(name = "Supplier Management", description = "Supplier registry, geo-search, and vendor performance APIs.")
@SecurityRequirement(name = "bearerAuth")
public class SupplierResource {

    private final SupplierService supplierService;

    public SupplierResource(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_OFFICER')")
    @Operation(summary = "Create supplier")
    public ResponseEntity<ApiResponse<SupplierResponse>> createSupplier(@Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Supplier created successfully"));
    }

    @GetMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'PURCHASE_OFFICER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get supplier by id")
    public ResponseEntity<ApiResponse<SupplierResponse>> getById(@PathVariable Long supplierId) {
        SupplierResponse response = supplierService.getById(supplierId);
        return ResponseEntity.ok(ApiResponse.success(response, "Supplier retrieved successfully"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'PURCHASE_OFFICER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get all suppliers")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getAllSuppliers() {
        List<SupplierResponse> response = supplierService.getAllSuppliers();
        return ResponseEntity.ok(ApiResponse.success(response, "Suppliers retrieved successfully"));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'PURCHASE_OFFICER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Search suppliers by name")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> searchSuppliers(@RequestParam(required = false) String name) {
        List<SupplierResponse> response = supplierService.searchSuppliers(name);
        return ResponseEntity.ok(ApiResponse.success(response, "Suppliers search results retrieved successfully"));
    }

    @GetMapping("/city/{city}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'PURCHASE_OFFICER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get suppliers by city")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getByCity(@PathVariable String city) {
        List<SupplierResponse> response = supplierService.getByCity(city);
        return ResponseEntity.ok(ApiResponse.success(response, "Suppliers in " + city + " retrieved successfully"));
    }

    @GetMapping("/country/{country}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'MANAGER', 'PURCHASE_OFFICER', 'WAREHOUSE_STAFF')")
    @Operation(summary = "Get suppliers by country")
    public ResponseEntity<ApiResponse<List<SupplierResponse>>> getByCountry(@PathVariable String country) {
        List<SupplierResponse> response = supplierService.getByCountry(country);
        return ResponseEntity.ok(ApiResponse.success(response, "Suppliers in " + country + " retrieved successfully"));
    }

    @PutMapping("/{supplierId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_OFFICER')")
    @Operation(summary = "Update supplier profile")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateSupplier(@PathVariable Long supplierId,
            @Valid @RequestBody SupplierRequest request) {
        SupplierResponse response = supplierService.updateSupplier(supplierId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Supplier updated successfully"));
    }

    @PutMapping("/{supplierId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_OFFICER')")
    @Operation(summary = "Soft deactivate supplier")
    public ResponseEntity<ApiResponse<SupplierResponse>> deactivateSupplier(@PathVariable Long supplierId) {
        SupplierResponse response = supplierService.deactivateSupplier(supplierId);
        return ResponseEntity.ok(ApiResponse.success(response, "Supplier deactivated successfully"));
    }

    @PutMapping("/{supplierId}/rating")
    @PreAuthorize("hasAnyRole('ADMIN', 'PURCHASE_OFFICER')")
    @Operation(summary = "Update supplier rating")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateRating(@PathVariable Long supplierId,
            @Valid @RequestBody SupplierRatingRequest request) {
        SupplierResponse response = supplierService.updateRating(supplierId, request.getNewRating());
        return ResponseEntity.ok(ApiResponse.success(response, "Supplier rating updated successfully"));
    }

    @PutMapping("/internal/{supplierId}/rating")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Internal endpoint used by purchase-service to update supplier performance rating")
    public ResponseEntity<ApiResponse<SupplierResponse>> updateRatingInternal(@PathVariable Long supplierId,
            @Valid @RequestBody SupplierRatingRequest request) {
        SupplierResponse response = supplierService.updateRating(supplierId, request.getNewRating());
        return ResponseEntity.ok(ApiResponse.success(response, "Supplier rating updated successfully (internal)"));
    }
}
