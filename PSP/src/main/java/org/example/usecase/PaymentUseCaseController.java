package org.example.usecase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "payment")
@RequiredArgsConstructor
public class PaymentUseCaseController {

    private final PaymentUseCase paymentUseCase;

    @PostMapping(value = "purchase")
    public PaymentUseCaseResponse processPaymentFlow(@Valid @RequestBody PaymentUseCaseRequest request){
        return paymentUseCase.execute(request);
    }

}
