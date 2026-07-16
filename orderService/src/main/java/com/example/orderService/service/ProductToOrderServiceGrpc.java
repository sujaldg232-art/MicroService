package com.example.orderService.service;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.grpc.ProductValidationDto;
import org.example.grpc.ProductValidationDtoResponse;
import org.example.grpc.productValidateDtoGrpc;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.TimeUnit;

@Service
public class ProductToOrderServiceGrpc {

    @GrpcClient("productService")
    private productValidateDtoGrpc.productValidateDtoBlockingStub userBlockingStub;

    public ProductValidationDtoResponse validateOrderLine(OrderLineRequestDto orderLineRequestDto) {
        ProductValidationDto productValidationDto = ProductValidationDto.newBuilder()
                .setProductID(String.valueOf(orderLineRequestDto.productID()))
                .setQuantity(orderLineRequestDto.quantity())
                .build();

        for (int i = 0; i < 3; i++) {
            try {
                return userBlockingStub.withDeadlineAfter(100, java.util.concurrent.TimeUnit.MILLISECONDS)
                        .validateProductValidation(productValidationDto);
            } catch (io.grpc.StatusRuntimeException e) {
                if (i == 2) {
                    throw new org.springframework.web.server.ResponseStatusException(
                            org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                            "Product validation failed after 3 attempts",
                            e
                    );
                }
            }
        }
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected validation failure"
        );
    }
}