package com.stockpro.product.controller;

import com.stockpro.product.dto.request.CreateProductRequest;
import com.stockpro.product.dto.request.ProductSearchRequest;
import com.stockpro.product.dto.request.UpdateProductRequest;
import com.stockpro.product.dto.response.ApiResponse;
import com.stockpro.product.dto.response.LowStockProductResponse;
import com.stockpro.product.dto.response.ProductResponse;
import com.stockpro.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1/products")
@Tag(name = "Product Management", description = "Product catalogue CRUD, search, barcode lookup and low-stock metadata APIs.")
@SecurityRequirement(name = "bearerAuth")
public class ProductResource {

    private static final String CREATE_PRODUCT_EXAMPLE = """
            {
              "sku": "SKU-1001",
              "name": "Industrial Drill",
              "description": "Heavy-duty industrial drill for warehouse maintenance.",
              "category": "Tools",
              "brand": "Bosch",
              "unitOfMeasure": "Piece",
              "costPrice": 1499.5000,
              "sellingPrice": 1999.9000,
              "reorderLevel": 10.0000,
              "maxStockLevel": 100.0000,
              "leadTimeDays": 5,
              "imageUrl": "https://cdn.stockpro.com/products/drill.png",
              "barcode": "8901234567890"
            }
            """;

    private static final String UPDATE_PRODUCT_EXAMPLE = """
            {
              "name": "Industrial Drill Pro",
              "description": "Updated drill model with extended duty cycle.",
              "category": "Tools",
              "brand": "Bosch",
              "unitOfMeasure": "Piece",
              "costPrice": 1550.0000,
              "sellingPrice": 2099.9900,
              "reorderLevel": 12.0000,
              "maxStockLevel": 120.0000,
              "leadTimeDays": 7,
              "imageUrl": "https://cdn.stockpro.com/products/drill-pro.png",
              "barcode": "8901234567891",
              "isActive": true
            }
            """;

    private static final String SEARCH_PRODUCT_EXAMPLE = """
            {
              "name": "drill",
              "category": "Tools",
              "brand": "Bosch",
              "sku": "SKU-1001",
              "barcode": "8901234567890",
              "isActive": true,
              "page": 0,
              "size": 20,
              "sortBy": "name",
              "sortDir": "asc"
            }
            """;

    private final ProductService productService;

    public ProductResource(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(
            summary = "Create product",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "CreateProduct", value = CREATE_PRODUCT_EXAMPLE))))
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get product by id")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        ProductResponse response = productService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Product retrieved successfully"));
    }

    @GetMapping("/sku/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get product by SKU")
    public ResponseEntity<ApiResponse<ProductResponse>> getBySku(@PathVariable String sku) {
        ProductResponse response = productService.getBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(response, "Product retrieved successfully by SKU"));
    }

    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get products by category")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByCategory(@PathVariable String category) {
        List<ProductResponse> response = productService.getByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(response, "Products retrieved successfully for category: " + category));
    }

    @GetMapping("/brand/{brand}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get products by brand")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getByBrand(@PathVariable String brand) {
        List<ProductResponse> response = productService.getByBrand(brand);
        return ResponseEntity.ok(ApiResponse.success(response, "Products retrieved successfully for brand: " + brand));
    }

    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get product by barcode")
    public ResponseEntity<ApiResponse<ProductResponse>> getByBarcode(@PathVariable String barcode) {
        ProductResponse response = productService.getByBarcode(barcode);
        return ResponseEntity.ok(ApiResponse.success(response, "Product retrieved successfully by barcode"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) int size,
            @RequestParam(defaultValue = "productId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        Page<ProductResponse> response = productService.getAllProducts(page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response, "Products retrieved successfully"));
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(
            summary = "Search products with filters",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "SearchProducts", value = SEARCH_PRODUCT_EXAMPLE))))
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(@Valid @RequestBody ProductSearchRequest request) {
        Page<ProductResponse> response = productService.searchProducts(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Product search results retrieved successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(
            summary = "Update product",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(examples = @ExampleObject(name = "UpdateProduct", value = UPDATE_PRODUCT_EXAMPLE))))
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Product updated successfully"));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Deactivate product")
    public ResponseEntity<ApiResponse<ProductResponse>> deactivateProduct(@PathVariable Long id) {
        ProductResponse response = productService.deactivateProduct(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Product deactivated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "Safely delete product by performing a soft delete")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'INVENTORY_MANAGER', 'WAREHOUSE_STAFF', 'PURCHASE_OFFICER')")
    @Operation(summary = "Get low-stock product metadata using warehouse quantities and product reorder thresholds")
    public ResponseEntity<ApiResponse<List<LowStockProductResponse>>> getLowStockProducts() {
        List<LowStockProductResponse> response = productService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success(response, "Low-stock products metadata retrieved successfully"));
    }
}
