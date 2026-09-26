package org.example.bankA.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bankA.dtos.TransferRequestDTO;
import org.example.bankA.dtos.TransferResponseDTO;
import org.example.bankA.model.service.AccountService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/bank1", "/bank2"})
@RequiredArgsConstructor
public class BankController {

    private final AccountService accountService;

    @PostMapping
    public TransferResponseDTO transfer(@Valid @RequestBody TransferRequestDTO request){
        return accountService.transfer(request);
    }
}
