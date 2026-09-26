package org.example.bankb.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankb.dto.TransferRequest;
import org.example.bankb.dto.TransferResponse;
import org.example.bankb.service.BankBTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank2")
@RequiredArgsConstructor
@Tag(name = "Bank B transfers", description = "Transfers processed by the issuing bank for cards beginning with 222222")
public class BankBController {

    private final BankBTransferService transferService;

    @PostMapping
    @Operation(summary = "Process a Bank B transfer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
            @ApiResponse(responseCode = "404", description = "Source or target account not found")
    })
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }
}
