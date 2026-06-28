package com.dk.jpatesting.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to create a new order")
public class CreateOrderRequest {


    @Schema(description = "Unique order number", example = "ORD-2026-001")
    @NotBlank(message = "Order number is required")
    @Size(min = 1, max = 50, message = "Order number must not exceed 50 characters")
    private String orderNumber;

    @Schema(description = "Order description", example = "Electronics purchase")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Schema(description = "Total order amount", example = "299.99")
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total amount must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Total amount must have at most 8 integer digits and 2 decimal places")
    private BigDecimal totalAmount;

}
