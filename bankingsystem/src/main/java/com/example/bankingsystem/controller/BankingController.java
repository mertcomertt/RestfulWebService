package com.example.bankingsystem.controller;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankingsystem.dto.AccountBalanceRequest;
import com.example.bankingsystem.dto.BalanceTransferRequest;
import com.example.bankingsystem.model.Account;
import com.example.bankingsystem.service.IBankingService;

@RestController
public class BankingController {
	
	@Autowired
	private IBankingService bankService;
	
	// Web Service 1 -> Creating Account with random account number with 10 digits
	@PostMapping(path="/accounts")
	public ResponseEntity<?> createAccount(@RequestBody Account request){
		return this.bankService.createAccount(request);
	}
	
	// Web Service 2 -> Reaching the details of given account number
	@GetMapping(path="/accounts/{accountNumber}")
	public ResponseEntity<?> getAccountByNumber(@PathVariable long accountNumber) throws IOException{
		return this.bankService.getAccountByNumber(accountNumber);
	}
	
	// Web Service 3 -> Deposit to the given account number
	@PatchMapping(path="/accounts/{accountNumber}")
	public ResponseEntity<?> deposit(@PathVariable long accountNumber,@RequestBody AccountBalanceRequest request){
		return this.bankService.deposit(accountNumber,request);
	}
	
	// Web Service 4 -> Transferring money from one account number to another
	@PostMapping(path="/accounts/{accountNumber}")
	public ResponseEntity<?> transfer(@PathVariable long accountNumber,@RequestBody BalanceTransferRequest request){
		return this.bankService.transfer(accountNumber,request);
	}
	
	// Web Service 5 -> Logging transactions for specific account number
	@CrossOrigin(origins={"http://localhost"})
	@GetMapping(path="/accounts/{accountNumber}/logs")
	public ResponseEntity<?> accountLog(@PathVariable long accountNumber) throws IOException{
		return this.bankService.accountLog(accountNumber);
	}
	
}