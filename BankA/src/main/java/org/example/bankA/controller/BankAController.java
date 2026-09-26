package org.example.bankA.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankA.dtos.TransferRequestDTO;
import org.example.bankA.dtos.TransferResponseDTO;
import org.example.bankA.model.service.BankATransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank1")
@RequiredArgsConstructor
@Tag(name = "Bank A transfers", description = "Transfers processed by the issuing bank for cards beginning with 111111")
public class BankAController {

    private final BankATransferService transferService;

    @PostMapping
    @Operation(summary = "Process a Bank A transfer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
            @ApiResponse(responseCode = "404", description = "Source or target account not found")
    })
    public TransferResponseDTO transfer(@Valid @RequestBody TransferRequestDTO request){
        return transferService.transfer(request);
    }
}
