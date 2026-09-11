package com.example.ecommerce.api;

import com.example.ecommerce.api.model.AddressResponse;
import com.example.ecommerce.api.model.CreateAddressRequest;
import com.example.ecommerce.auth.AuthPrincipal;
import com.example.ecommerce.auth.AuthRequestAttributes;
import com.example.ecommerce.domain.SavedAddress;
import com.example.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public List<AddressResponse> listAddresses(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal
    ) {
        return addressService.list(principal.userId()).stream()
                .map(AddressController::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse createAddress(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        boolean isDefault = request.isDefault() != null && request.isDefault();
        SavedAddress created = addressService.create(
                principal.userId(),
                request.fullName(),
                request.phoneNumber(),
                request.addressLine(),
                request.city(),
                request.postalCode(),
                isDefault
        );
        return toResponse(created);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(
            @RequestAttribute(AuthRequestAttributes.PRINCIPAL) AuthPrincipal principal,
            @PathVariable String addressId
    ) {
        addressService.delete(principal.userId(), addressId);
    }

    private static AddressResponse toResponse(SavedAddress address) {
        return new AddressResponse(
                address.id(),
                address.fullName(),
                address.phoneNumber(),
                address.addressLine(),
                address.city(),
                address.postalCode(),
                address.isDefault()
        );
    }
}
