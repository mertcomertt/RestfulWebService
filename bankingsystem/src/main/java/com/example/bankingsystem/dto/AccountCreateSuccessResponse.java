package com.example.bankingsystem.dto;

import lombok.Data;

@Data
public class AccountCreateSuccessResponse {
	private String message;
	private long accountNumber;
}
