package com.fooddelivery.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.dto.request.*;
import com.fooddelivery.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    private String customerToken;
    private String ownerToken;
    private Long restaurantId;
    private Long menuItemId;
    private Long cityId;

    @BeforeEach
    void setUp() throws Exception {
        customerToken = registerAndGetToken("customer@order.test", "9800000001", UserRole.CUSTOMER);
        ownerToken = registerAndGetToken("owner@order.test", "9800000002", UserRole.RESTAURANT_OWNER);
        String adminToken = registerAndGetToken("admin@order.test", "9800000003", UserRole.ADMIN);

        // Create city
        CreateCityRequest cityReq = new CreateCityRequest();
        cityReq.setName("TestCity-Order");
        cityReq.setState("TestState");
        MvcResult cityResult = mockMvc.perform(post("/api/cities")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(cityReq)))
                .andExpect(status().isCreated()).andReturn();
        cityId = extractId(cityResult, "$.data.id");

        // Get owner user id
        Long ownerUserId = extractId(registerResult("owner@order.test", "9800000002", UserRole.RESTAURANT_OWNER), null);

        // Create restaurant
        CreateRestaurantRequest restReq = new CreateRestaurantRequest();
        restReq.setName("Integration Test Restaurant");
        restReq.setAddress("123 Test Avenue");
        restReq.setPhone("9800000009");
        restReq.setCityId(cityId);
        MvcResult restResult = mockMvc.perform(post("/api/restaurants")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(restReq)))
                .andExpect(status().isCreated()).andReturn();
        restaurantId = extractId(restResult, "$.data.id");

        // Open restaurant
        mockMvc.perform(patch("/api/restaurants/" + restaurantId + "/open")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        // Add menu item
        MenuItemRequest menuReq = new MenuItemRequest();
        menuReq.setName("Masala Dosa");
        menuReq.setPrice(new BigDecimal("120.00"));
        menuReq.setStockQuantity(50);
        menuReq.setAvailable(true);
        MvcResult menuResult = mockMvc.perform(post("/api/restaurants/" + restaurantId + "/menu")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(menuReq)))
                .andExpect(status().isCreated()).andReturn();
        menuItemId = extractId(menuResult, "$.data.id");
    }

    @Test
    void fullOrderLifecycle_placed_to_delivered() throws Exception {
        // Customer places order
        PlaceOrderRequest orderReq = new PlaceOrderRequest();
        orderReq.setRestaurantId(restaurantId);
        orderReq.setDeliveryAddress("456 Customer Lane");
        orderReq.setPaymentMethod("UPI");
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setMenuItemId(menuItemId);
        itemReq.setQuantity(2);
        orderReq.setItems(List.of(itemReq));

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(orderReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PLACED"))
                .andExpect(jsonPath("$.data.totalAmount").value(240.0))
                .andExpect(jsonPath("$.data.payment.status").value("COMPLETED"))
                .andReturn();

        Long orderId = extractId(orderResult, "$.data.id");

        // Restaurant accepts order
        UpdateOrderStatusRequest acceptReq = new UpdateOrderStatusRequest();
        acceptReq.setStatus("ACCEPTED");
        mockMvc.perform(patch("/api/orders/" + orderId + "/restaurant-status")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(acceptReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));

        // Restaurant starts preparing
        UpdateOrderStatusRequest prepReq = new UpdateOrderStatusRequest();
        prepReq.setStatus("PREPARING");
        mockMvc.perform(patch("/api/orders/" + orderId + "/restaurant-status")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(prepReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PREPARING"));

        // Restaurant marks ready
        UpdateOrderStatusRequest readyReq = new UpdateOrderStatusRequest();
        readyReq.setStatus("READY_FOR_PICKUP");
        mockMvc.perform(patch("/api/orders/" + orderId + "/restaurant-status")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(readyReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY_FOR_PICKUP"));
    }

    @Test
    void placeOrder_closedRestaurant_returns400() throws Exception {
        // Close restaurant first
        mockMvc.perform(patch("/api/restaurants/" + restaurantId + "/close")
                .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        PlaceOrderRequest orderReq = new PlaceOrderRequest();
        orderReq.setRestaurantId(restaurantId);
        orderReq.setDeliveryAddress("456 Customer Lane");
        orderReq.setPaymentMethod("UPI");
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setMenuItemId(menuItemId);
        itemReq.setQuantity(1);
        orderReq.setItems(List.of(itemReq));

        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(orderReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void customerCancelsOrder_stockRestored() throws Exception {
        PlaceOrderRequest orderReq = new PlaceOrderRequest();
        orderReq.setRestaurantId(restaurantId);
        orderReq.setDeliveryAddress("456 Customer Lane");
        orderReq.setPaymentMethod("CARD");
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setMenuItemId(menuItemId);
        itemReq.setQuantity(3);
        orderReq.setItems(List.of(itemReq));

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(orderReq)))
                .andExpect(status().isCreated()).andReturn();

        Long orderId = extractId(orderResult, "$.data.id");

        mockMvc.perform(patch("/api/orders/" + orderId + "/cancel")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    void unauthorizedAccess_toOrderPlacement_returns403() throws Exception {
        PlaceOrderRequest orderReq = new PlaceOrderRequest();
        orderReq.setRestaurantId(restaurantId);
        orderReq.setDeliveryAddress("456 Lane");
        orderReq.setPaymentMethod("CARD");
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setMenuItemId(menuItemId);
        itemReq.setQuantity(1);
        orderReq.setItems(List.of(itemReq));

        // Restaurant owner cannot place orders
        mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(orderReq)))
                .andExpect(status().isForbidden());
    }

    // --- Helpers ---

    private String registerAndGetToken(String email, String phone, UserRole role) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail(email);
        req.setPhone(phone);
        req.setPassword("password123");
        req.setRole(role);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andReturn();

        if (result.getResponse().getStatus() == 409) {
            // Already exists - login instead
            LoginRequest loginReq = new LoginRequest();
            loginReq.setEmail(email);
            loginReq.setPassword("password123");
            result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(loginReq)))
                    .andReturn();
        }

        String body = result.getResponse().getContentAsString();
        return mapper.readTree(body).path("data").path("token").asText();
    }

    private MvcResult registerResult(String email, String phone, UserRole role) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail(email);
        req.setPhone(phone);
        req.setPassword("password123");
        req.setRole(role);
        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andReturn();
    }

    private Long extractId(MvcResult result, String jsonPath) throws Exception {
        String body = result.getResponse().getContentAsString();
        var root = mapper.readTree(body);
        if (jsonPath == null) {
            return root.path("data").path("userId").asLong();
        }
        String[] parts = jsonPath.replace("$.", "").split("\\.");
        var node = root;
        for (String part : parts) {
            node = node.path(part);
        }
        return node.asLong();
    }
}
