package com.example.ecommerce.service;

import com.example.ecommerce.domain.SavedAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
class AddressServiceTest {

    private static final String USER_ID = "u-1";

    private AddressService addressService;

    @BeforeEach
    void setUp() {
        addressService = new AddressService();
    }

    @Test
    void createShouldPersistAddressForUser() {
        SavedAddress created = addressService.create(
                USER_ID,
                "Jane Doe",
                "+1-555-100-100",
                "Main street 10",
                "Austin",
                "73301",
                true
        );

        assertThat(created.id()).isNotBlank();
        assertThat(created.fullName()).isEqualTo("Jane Doe");
        assertThat(created.city()).isEqualTo("Austin");
        assertThat(created.isDefault()).isTrue();
        assertThat(addressService.get(USER_ID, created.id())).isEqualTo(created);
    }

    @Test
    void listShouldReturnAddressesForUser() {
        SavedAddress first = addressService.create(
                USER_ID,
                "Jane Doe",
                "+1-555-100-100",
                "Main street 10",
                "Austin",
                "73301",
                true
        );
        SavedAddress second = addressService.create(
                USER_ID,
                "John Doe",
                "+1-555-200-200",
                "Second avenue 5",
                "Dallas",
                "75001",
                false
        );

        List<SavedAddress> listed = addressService.list(USER_ID);

        assertThat(listed).containsExactly(first, second);
        assertThat(addressService.list("other-user")).isEmpty();
    }
}
