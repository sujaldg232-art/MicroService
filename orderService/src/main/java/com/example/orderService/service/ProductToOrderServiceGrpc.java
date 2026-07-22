package com.example.orderService.service;

import com.example.orderService.dtos.OrderLineDtos.OrderLineRequestDto;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.grpc.OrderlineValidationDto;
import org.example.grpc.OrderLineValidationOutPut;
import org.example.grpc.OrderLineValidationServiceGrpc;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductToOrderServiceGrpc {

    @GrpcClient("productService")
    private OrderLineValidationServiceGrpc.OrderLineValidationServiceBlockingStub validationStub;

    public List<OrderLineValidationOutPut> validateOrderlines(List<OrderLineRequestDto> orderLineRequestDtoList) {
        org.example.grpc.BatchOrderLineValidationRequest request = org.example.grpc.BatchOrderLineValidationRequest.newBuilder()
                .addAllRequests(orderLineRequestDtoList.stream()
                        .map(dto -> OrderlineValidationDto.newBuilder()
                                .setProductID(String.valueOf(dto.productID()))
                                .setQuantity(dto.quantity())
                                .build())
                        .toList())
                .build();

        for (int i = 0; i < 3; i++) {
            try {
                return validationStub.withDeadlineAfter(1000, TimeUnit.MILLISECONDS)
                        .validateMultipleOrderLines(request)
                        .getResponsesList();
            } catch (io.grpc.StatusRuntimeException e) {
                if (i == 2) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Batch validation failed after 3 attempts",
                            e
                    );
                }
            }
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected batch validation failure"
        );
    }

    public OrderLineValidationOutPut validateOrderLine(OrderLineRequestDto orderLineRequestDto) {
        OrderlineValidationDto orderlineValidationDto = OrderlineValidationDto.newBuilder()
                .setProductID(String.valueOf(orderLineRequestDto.productID()))
                .setQuantity(orderLineRequestDto.quantity())
                .build();

        for (int i = 0; i < 3; i++) {
            try {
                return validationStub.withDeadlineAfter(100, TimeUnit.MILLISECONDS)
                        .validateOrderLine(orderlineValidationDto);
            } catch (io.grpc.StatusRuntimeException e) {
                if (i == 2) {
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Product validation failed after 3 attempts",
                            e
                    );
                }
            }
        }
        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected validation failure"
        );
    }
}