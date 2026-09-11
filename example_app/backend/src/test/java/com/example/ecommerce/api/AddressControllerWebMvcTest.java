package com.example.ecommerce.api;

import com.example.ecommerce.auth.AuthClient;
import com.example.ecommerce.auth.AuthPrincipal;
import com.example.ecommerce.domain.SavedAddress;
import com.example.ecommerce.service.AddressService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressController.class)
@Tag("api")
class AddressControllerWebMvcTest {

    private static final AuthPrincipal PRINCIPAL = new AuthPrincipal("u-1", "user@example.com");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddressService addressService;

    @MockBean
    private AuthClient authClient;

    @Test
    void createAddressShouldReturnCreatedPayload() throws Exception {
        SavedAddress created = new SavedAddress(
                "addr-1",
                "Jane Doe",
                "+1-555-100-100",
                "Main street 10",
                "Austin",
                "73301",
                true
        );
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        when(addressService.create(
                eq(PRINCIPAL.userId()),
                eq("Jane Doe"),
                eq("+1-555-100-100"),
                eq("Main street 10"),
                eq("Austin"),
                eq("73301"),
                eq(true)
        )).thenReturn(created);

        mockMvc.perform(post("/api/v1/addresses")
                        .header("Authorization", "Bearer test")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName": "Jane Doe",
                                  "phoneNumber": "+1-555-100-100",
                                  "addressLine": "Main street 10",
                                  "city": "Austin",
                                  "postalCode": "73301",
                                  "isDefault": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("addr-1"))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.city").value("Austin"))
                .andExpect(jsonPath("$.isDefault").value(true));

        verify(addressService).create(
                PRINCIPAL.userId(),
                "Jane Doe",
                "+1-555-100-100",
                "Main street 10",
                "Austin",
                "73301",
                true
        );
    }

    @Test
    void listAddressesShouldReturnPayload() throws Exception {
        SavedAddress address = new SavedAddress(
                "addr-1",
                "Jane Doe",
                "+1-555-100-100",
                "Main street 10",
                "Austin",
                "73301",
                true
        );
        when(authClient.verify(anyString())).thenReturn(PRINCIPAL);
        when(addressService.list(PRINCIPAL.userId())).thenReturn(List.of(address));

        mockMvc.perform(get("/api/v1/addresses")
                        .header("Authorization", "Bearer test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("addr-1"))
                .andExpect(jsonPath("$[0].fullName").value("Jane Doe"))
                .andExpect(jsonPath("$[0].city").value("Austin"))
                .andExpect(jsonPath("$[0].isDefault").value(true));

        verify(addressService).list(PRINCIPAL.userId());
    }
}
