package com.example.bankingsystem.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class Account implements Serializable {

	private static final long serialVersionUID = 1L;
	private long number;
	private String name;
	private String surname;
	private String email;
	private String tc;
	private String type;
	private double balance;
	private String lastUpdateDate;

	public String toFile() {
		return this.number + "," + this.name + "," + this.surname + "," + this.email + "," + this.tc + "," + this.type
				+ "," + this.balance + "," + this.lastUpdateDate;
	}

}
