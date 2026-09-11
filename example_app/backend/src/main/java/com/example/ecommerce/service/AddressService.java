package com.example.ecommerce.service;

import com.example.ecommerce.domain.SavedAddress;
import com.example.ecommerce.web.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AddressService {

    private final ConcurrentHashMap<String, Map<String, SavedAddress>> addressesByUserId = new ConcurrentHashMap<>();

    public SavedAddress create(
            String userId,
            String fullName,
            String phoneNumber,
            String addressLine,
            String city,
            String postalCode,
            boolean isDefault
    ) {
        Map<String, SavedAddress> userAddresses = getOrCreateUserAddresses(userId);
        boolean makeDefault = isDefault || userAddresses.isEmpty();
        if (makeDefault) {
            clearDefaultFlags(userAddresses);
        }
        String id = UUID.randomUUID().toString();
        SavedAddress address = new SavedAddress(
                id,
                fullName,
                phoneNumber,
                addressLine,
                city,
                postalCode,
                makeDefault
        );
        userAddresses.put(id, address);
        return address;
    }

    public List<SavedAddress> list(String userId) {
        return new ArrayList<>(getOrCreateUserAddresses(userId).values());
    }

    public SavedAddress get(String userId, String addressId) {
        SavedAddress address = getOrCreateUserAddresses(userId).get(addressId);
        if (address == null) {
            throw new NotFoundException("Address not found: " + addressId);
        }
        return address;
    }

    public void delete(String userId, String addressId) {
        Map<String, SavedAddress> userAddresses = getOrCreateUserAddresses(userId);
        SavedAddress removed = userAddresses.remove(addressId);
        if (removed == null) {
            throw new NotFoundException("Address not found: " + addressId);
        }
        if (removed.isDefault() && !userAddresses.isEmpty()) {
            String firstId = userAddresses.keySet().iterator().next();
            SavedAddress first = userAddresses.get(firstId);
            userAddresses.put(firstId, withDefault(first, true));
        }
    }

    private static void clearDefaultFlags(Map<String, SavedAddress> userAddresses) {
        for (Map.Entry<String, SavedAddress> entry : userAddresses.entrySet()) {
            SavedAddress current = entry.getValue();
            if (current.isDefault()) {
                entry.setValue(withDefault(current, false));
            }
        }
    }

    private static SavedAddress withDefault(SavedAddress address, boolean isDefault) {
        return new SavedAddress(
                address.id(),
                address.fullName(),
                address.phoneNumber(),
                address.addressLine(),
                address.city(),
                address.postalCode(),
                isDefault
        );
    }

    private Map<String, SavedAddress> getOrCreateUserAddresses(String userId) {
        return addressesByUserId.computeIfAbsent(userId, ignored -> new LinkedHashMap<>());
    }
}
