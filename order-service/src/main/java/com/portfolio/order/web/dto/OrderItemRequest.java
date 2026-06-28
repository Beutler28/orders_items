package com.portfolio.order.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderItemRequest(

        @NotBlank(message = "productId é obrigatório")
        String productId,

        @Min(value = 1, message = "quantity deve ser no mínimo 1")
        int quantity,

        @NotNull(message = "unitPrice é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "unitPrice deve ser positivo")
        BigDecimal unitPrice
) {}
