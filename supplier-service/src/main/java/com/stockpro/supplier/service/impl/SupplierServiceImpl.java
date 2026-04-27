package com.stockpro.supplier.service.impl;

import com.stockpro.supplier.dto.SupplierRequest;
import com.stockpro.supplier.dto.SupplierResponse;
import com.stockpro.supplier.entity.Supplier;
import com.stockpro.supplier.exception.BadRequestException;
import com.stockpro.supplier.exception.ConflictException;
import com.stockpro.supplier.exception.ResourceNotFoundException;
import com.stockpro.supplier.mapper.SupplierMapper;
import com.stockpro.supplier.repository.SupplierRepository;
import com.stockpro.supplier.service.SupplierService;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    public SupplierServiceImpl(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Override
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (request == null) {
            throw new BadRequestException("Supplier payload is required.");
        }

        String normalizedTaxId = request.getTaxId().trim().toUpperCase(Locale.ROOT);
        supplierRepository.findByTaxIdIgnoreCase(normalizedTaxId)
                .ifPresent(existingSupplier -> {
                    throw new ConflictException("Supplier taxId already exists: " + normalizedTaxId);
                });

        Supplier supplier = supplierMapper.toEntity(request);
        supplier.setTaxId(normalizedTaxId);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getById(Long supplierId) {
        return supplierMapper.toResponse(getSupplierEntity(supplierId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .sorted(Comparator.comparing(Supplier::getName, String.CASE_INSENSITIVE_ORDER))
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> searchSuppliers(String name) {
        if (!StringUtils.hasText(name)) {
            return getAllSuppliers();
        }

        return supplierRepository.searchByName(name.trim()).stream()
                .sorted(Comparator.comparing(Supplier::getName, String.CASE_INSENSITIVE_ORDER))
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    public SupplierResponse updateSupplier(Long supplierId, SupplierRequest request) {
        Supplier existingSupplier = getSupplierEntity(supplierId);

        if (request == null) {
            throw new BadRequestException("Supplier payload is required.");
        }

        String normalizedTaxId = request.getTaxId().trim().toUpperCase(Locale.ROOT);
        supplierRepository.findByTaxIdIgnoreCase(normalizedTaxId)
                .filter(s -> !s.getSupplierId().equals(supplierId))
                .ifPresent(existingSupplierConflict -> {
                    throw new ConflictException("Supplier taxId already exists: " + normalizedTaxId);
                });

        supplierMapper.updateEntity(request, existingSupplier);
        existingSupplier.setTaxId(normalizedTaxId);
        return supplierMapper.toResponse(supplierRepository.save(existingSupplier));
    }

    @Override
    public SupplierResponse deactivateSupplier(Long supplierId) {
        Supplier supplier = getSupplierEntity(supplierId);
        supplier.setIsActive(Boolean.FALSE);
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getByCity(String city) {
        if (!StringUtils.hasText(city)) {
            throw new BadRequestException("city is required.");
        }

        return supplierRepository.findByCityIgnoreCase(city.trim()).stream()
                .sorted(Comparator.comparing(Supplier::getName, String.CASE_INSENSITIVE_ORDER))
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> getByCountry(String country) {
        if (!StringUtils.hasText(country)) {
            throw new BadRequestException("country is required.");
        }

        return supplierRepository.findByCountryIgnoreCase(country.trim()).stream()
                .sorted(Comparator.comparing(Supplier::getName, String.CASE_INSENSITIVE_ORDER))
                .map(supplierMapper::toResponse)
                .toList();
    }

    @Override
    public SupplierResponse updateRating(Long supplierId, Double newRating) {
        Supplier supplier = getSupplierEntity(supplierId);
        supplier.setRating(validateRating(newRating));
        return supplierMapper.toResponse(supplierRepository.save(supplier));
    }

    private Supplier getSupplierEntity(Long supplierId) {
        if (supplierId == null) {
            throw new BadRequestException("supplierId is required.");
        }

        return supplierRepository.findBySupplierId(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + supplierId));
    }

    private Double validateRating(Double rating) {
        if (rating == null || rating < 0.0 || rating > 5.0) {
            throw new BadRequestException("Supplier rating must be between 0.0 and 5.0.");
        }
        return rating;
    }
}
