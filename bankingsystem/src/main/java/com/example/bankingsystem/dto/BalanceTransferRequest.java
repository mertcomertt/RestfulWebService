package com.example.bankingsystem.dto;

import lombok.Data;

@Data
public class BalanceTransferRequest {

	private long transferredAccountNumber;
	private double amount;
}
