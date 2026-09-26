package org.example.usecase;

import lombok.RequiredArgsConstructor;
import org.example.core.collections.Merchant;
import org.example.core.collections.PaymentRequest;
import org.example.core.collections.PaymentResponse;
import org.example.core.model.PaymentModel;
import org.example.core.repository.MerchantRepository;
import org.example.core.repository.PaymentRequestRepository;
import org.example.core.repository.PaymentResponseRepository;
import org.example.exception.MerchantNotSupportedException;
import org.example.configuration.RabbitMQConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentUseCase implements UseCase<PaymentUseCaseRequest, PaymentUseCaseResponse> {

    private final AmqpTemplate amqpTemplate;
    private final MerchantRepository merchantRepository;
    private final PaymentRequestRepository requestRepository;
    private final PaymentResponseRepository responseRepository;

    @Override
    public PaymentUseCaseResponse execute(PaymentUseCaseRequest request) {
        if (request.getTraceId() == null || request.getTraceId().isBlank()) {
            request.setTraceId(UUID.randomUUID().toString());
        }
        System.out.println("first service");
        requestRepository.save(requestDTOToCollection(request));

        Optional<Merchant> merchant = merchantRepository.findById(request.getMerchantNumber());
        if (merchant.isEmpty()) throw new MerchantNotSupportedException();
        PaymentModel model = new PaymentModel()
                .setTraceId(request.getTraceId())
                .setAmount(request.getAmount())
                .setTargetAccountNumber(merchant.get().getAccountNumber())
                .setSourceCardNumber(request.getSourceCardNumber());

        PaymentUseCaseResponse response = amqpTemplate.convertSendAndReceiveAsType(RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                model,
                ParameterizedTypeReference.forType(PaymentUseCaseResponse.class));

        if (response == null) {
            throw new IllegalStateException("No response received from Shaparak");
        }

        responseRepository.save(responseDTOToCollection(response, request.getTraceId()));
        System.out.println("end service");
        return response;
    }

    private PaymentRequest requestDTOToCollection(PaymentUseCaseRequest request){
        return new PaymentRequest()
                .setTraceId(request.getTraceId())
                .setAmount(request.getAmount())
                .setMerchantNumber(request.getMerchantNumber())
                .setSourceCardNumber(request.getSourceCardNumber());
    }

    private PaymentResponse responseDTOToCollection(PaymentUseCaseResponse response, String traceId){
        return new PaymentResponse()
                .setTraceId(traceId)
                .setAmount(response.getAmount())
                .setStatus(response.getStatus())
                .setTransactionDate(response.getTransactionDate());
    }

}
