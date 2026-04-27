package com.stockpro.supplier.service;

import com.stockpro.supplier.dto.SupplierRequest;
import com.stockpro.supplier.dto.SupplierResponse;
import java.util.List;

public interface SupplierService {

    SupplierResponse createSupplier(SupplierRequest request);

    SupplierResponse getById(Long supplierId);

    List<SupplierResponse> getAllSuppliers();

    List<SupplierResponse> searchSuppliers(String name);

    SupplierResponse updateSupplier(Long supplierId, SupplierRequest request);

    SupplierResponse deactivateSupplier(Long supplierId);

    List<SupplierResponse> getByCity(String city);

    List<SupplierResponse> getByCountry(String country);

    SupplierResponse updateRating(Long supplierId, Double newRating);
}
