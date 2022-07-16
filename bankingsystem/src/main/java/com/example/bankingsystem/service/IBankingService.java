package com.example.bankingsystem.service;

import java.io.IOException;
import org.springframework.http.ResponseEntity;

import com.example.bankingsystem.dto.AccountBalanceRequest;
import com.example.bankingsystem.dto.BalanceTransferRequest;
import com.example.bankingsystem.model.Account;

public interface IBankingService {

	public ResponseEntity<?> createAccount(Account account);

	public ResponseEntity<?> getAccountByNumber(long accountNumber);

	public ResponseEntity<?> deposit(long accountNumber, AccountBalanceRequest request);

	public ResponseEntity<?> transfer(long accountNumber, BalanceTransferRequest request);

	public ResponseEntity<?> accountLog(long accountNumber) throws IOException;

	public double exchange(double amount, String toType, String baseType);

}
