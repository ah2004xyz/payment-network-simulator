package org.example.usecase;

import lombok.RequiredArgsConstructor;
import org.example.core.collection.RoutingRequest;
import org.example.core.collection.RoutingResponse;
import org.example.core.model.Bank1Model;
import org.example.core.model.Bank2Model;
import org.example.core.model.BankModel;
import org.example.core.repository.RoutingRequestRepository;
import org.example.core.repository.RoutingResponseRepository;
import org.example.exception.BankNotSupportedException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class BankRoutingUseCase implements UseCase<BankRoutingUseCaseRequest, BankRoutingUseCaseResponse> {

    private final RestTemplate restTemplate;
    private final RoutingRequestRepository requestRepository;
    private final RoutingResponseRepository responseRepository;

    @Value("${bank.a.base-url:http://localhost:8080/bank}")
    private String bankABaseUrl;

    @Value("${bank.b.base-url:http://localhost:8081/bank}")
    private String bankBBaseUrl;


    @Override
    public BankRoutingUseCaseResponse execute(BankRoutingUseCaseRequest request) {
        System.out.println("first service");
        requestRepository.save(requestDTOToCollection(request));
        String sourceFirstSixCardNumber = request.getSourceCardNumber().substring(0, 6);

        Map<String, String> banks = Map.of(
                "111111", bankABaseUrl + "/bank1",
                "222222", bankBBaseUrl + "/bank2"
        );

        String api = banks.getOrDefault(sourceFirstSixCardNumber, null);
        if (api == null) throw new BankNotSupportedException();

        BankModel model = switch (sourceFirstSixCardNumber) {
            case "111111" -> new Bank1Model()
                    .setAmount(request.getAmount())
                    .setSourceCardNumber(request.getSourceCardNumber())
                    .setTargetAccountNumber(request.getTargetAccountNumber());
            case "222222" -> new Bank2Model()
                    .setAmount(request.getAmount())
                    .setSourceCardNumber(request.getSourceCardNumber())
                    .setTargetAccountNumber(request.getTargetAccountNumber());
            default -> throw new BankNotSupportedException();
        };
        BankRoutingUseCaseResponse response = restTemplate.postForObject(api, model, BankRoutingUseCaseResponse.class);
        responseRepository.save(responseDTOToCollection(response));
        System.out.println("end service");
        return response;
    }

    private RoutingRequest requestDTOToCollection(BankRoutingUseCaseRequest request){
        return new RoutingRequest()
                .setAmount(request.getAmount())
                .setSourceCardNumber(request.getSourceCardNumber())
                .setTargetAccountNumber(request.getTargetAccountNumber());
    }

    private RoutingResponse responseDTOToCollection(BankRoutingUseCaseResponse response){
        return new RoutingResponse()
                .setAmount(response.getAmount())
                .setStatus(response.getStatus())
                .setTransactionDate(response.getTransactionDate());
    }

    //need a repo to save requests into db
    //get request (that inputs are validated in PSP)
    //find that specified bank for each card based on first 6 number of card number
    //send request to bank using restTemplate

}
