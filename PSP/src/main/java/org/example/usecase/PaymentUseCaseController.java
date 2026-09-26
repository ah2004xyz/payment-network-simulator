package org.example.usecase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "payment")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Public payment entry point for merchants")
public class PaymentUseCaseController {

    private final PaymentUseCase paymentUseCase;

    @PostMapping(value = "purchase")
    @Operation(summary = "Submit a card purchase")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment completed"),
            @ApiResponse(responseCode = "400", description = "Invalid payment request"),
            @ApiResponse(responseCode = "404", description = "Merchant or issuing bank not found"),
            @ApiResponse(responseCode = "500", description = "Infrastructure or downstream service failure")
    })
    public PaymentUseCaseResponse processPaymentFlow(@Valid @RequestBody PaymentUseCaseRequest request){
        return paymentUseCase.execute(request);
    }

}
