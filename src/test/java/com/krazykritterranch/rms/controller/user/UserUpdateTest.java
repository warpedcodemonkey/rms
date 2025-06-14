package com.krazykritterranch.rms.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krazykritterranch.rms.controller.user.dto.UserUpdateDTO;
import com.krazykritterranch.rms.model.user.Customer;
import com.krazykritterranch.rms.service.user.UserService;
import com.krazykritterranch.rms.service.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserUpdateTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TenantContext tenantContext;

    @Autowired
    private ObjectMapper objectMapper;

    private Customer testCustomer;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer("testuser", "test@example.com", "password", "John", "Doe");
        testCustomer.setId(1L);
        testCustomer.setCustomerNumber("CUST001");
        testCustomer.setEmergencyContact("Jane Doe");
        testCustomer.setEmergencyPhone("555-0123");

        updateDTO = new UserUpdateDTO();
        updateDTO.setFirstName("John Updated");
        updateDTO.setLastName("Doe Updated");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setPhoneNumber("555-9999");
        updateDTO.setEmergencyContact("Jane Updated");
        updateDTO.setEmergencyPhone("555-4567");
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void testUpdateUser_Success() throws Exception {
        // Mock the service calls
        when(userService.findById(1L)).thenReturn(testCustomer);
        when(userService.updateUserWithDto(eq(1L), any(UserUpdateDTO.class))).thenReturn(testCustomer);
        when(tenantContext.isAccountUser()).thenReturn(true);
        when(tenantContext.getCurrentAccountId()).thenReturn(1L);
        when(tenantContext.getCurrentUserId()).thenReturn(1L);

        // Set up the test customer's account
        testCustomer.setPrimaryAccount(createMockAccount(1L));

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @WithMockUser(username = "otheruser", roles = {"CUSTOMER"})
    void testUpdateUser_Forbidden_DifferentAccount() throws Exception {
        // Mock scenario where user tries to update user from different account
        when(userService.findById(1L)).thenReturn(testCustomer);
        when(tenantContext.isAccountUser()).thenReturn(true);
        when(tenantContext.getCurrentAccountId()).thenReturn(2L); // Different account
        when(tenantContext.getCurrentUserId()).thenReturn(2L);

        // Set up the test customer's account
        testCustomer.setPrimaryAccount(createMockAccount(1L));

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateUser_Admin_Success() throws Exception {
        // Mock admin updating any user
        when(userService.findById(1L)).thenReturn(testCustomer);
        when(userService.updateUserWithDto(eq(1L), any(UserUpdateDTO.class))).thenReturn(testCustomer);
        when(tenantContext.isAdmin()).thenReturn(true);

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void testUpdateUser_ConflictEmail() throws Exception {
        when(userService.findById(1L)).thenReturn(testCustomer);
        when(userService.existsByEmail("updated@example.com")).thenReturn(true);
        when(tenantContext.isAccountUser()).thenReturn(true);
        when(tenantContext.getCurrentUserId()).thenReturn(1L);

        // Current user email is different
        testCustomer.setEmail("current@example.com");

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void testUpdateUser_ConflictUsername() throws Exception {
        updateDTO.setUsername("existinguser");

        when(userService.findById(1L)).thenReturn(testCustomer);
        when(userService.existsByUsername("existinguser")).thenReturn(true);
        when(tenantContext.isAccountUser()).thenReturn(true);
        when(tenantContext.getCurrentUserId()).thenReturn(1L);

        // Current username is different
        testCustomer.setUsername("currentuser");

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    void testUpdateUser_NotFound() throws Exception {
        when(userService.findById(999L)).thenReturn(null);

        mockMvc.perform(put("/api/user/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"CUSTOMER"})
    void testUpdateUser_ValidationError() throws Exception {
        // Create invalid DTO
        UserUpdateDTO invalidDTO = new UserUpdateDTO();
        invalidDTO.setEmail("invalid-email"); // Invalid email format
        invalidDTO.setNewPassword("123"); // Too short password

        when(userService.findById(1L)).thenReturn(testCustomer);
        when(tenantContext.isAccountUser()).thenReturn(true);
        when(tenantContext.getCurrentUserId()).thenReturn(1L);

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // Helper method to create mock account
    private com.krazykritterranch.rms.model.common.Account createMockAccount(Long accountId) {
        com.krazykritterranch.rms.model.common.Account account =
                new com.krazykritterranch.rms.model.common.Account();
        account.setId(accountId);
        account.setFarmName("Test Farm");
        return account;
    }
}