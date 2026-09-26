package org.example.usecase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.example.config.Configuration;

@Component
@RequiredArgsConstructor
public class BankRoutingUseCaseController {

    private final BankRoutingUseCase bankRoutingUseCase;

    @RabbitListener(queues = Configuration.QUEUE)
    public BankRoutingUseCaseResponse routingProcess(@Valid @RequestBody BankRoutingUseCaseRequest request){
        return bankRoutingUseCase.execute(request);
    }

}
