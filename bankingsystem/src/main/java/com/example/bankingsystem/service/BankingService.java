package com.example.bankingsystem.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.bankingsystem.dto.AccountBalanceRequest;
import com.example.bankingsystem.dto.AccountCreateSuccessResponse;
import com.example.bankingsystem.dto.BalanceTransferRequest;
import com.example.bankingsystem.model.Account;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
public class BankingService implements IBankingService {

	@Autowired
	private KafkaTemplate<String, String> producer;

	@Autowired
	private RestTemplate client;

	@Override
	public ResponseEntity<?> createAccount(Account request) {
		if (request.getType().equals("USD") || request.getType().equals("TRY") || request.getType().equals("GAU")) {
			Account account = new Account();
			account.setNumber(new Random().nextLong(999999999L, 10000000000L));
			account.setName(request.getName());
			account.setSurname(request.getSurname());
			account.setEmail(request.getEmail());
			account.setTc(request.getTc());
			account.setType(request.getType());
			DateFormat structure = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date(System.currentTimeMillis());
			account.setLastUpdateDate(structure.format(date));
			account.setBalance(0);
			try {
				ObjectOutputStream outstream = new ObjectOutputStream(
						new FileOutputStream(new File("./" + account.getNumber())));
				outstream.writeObject(account);
				outstream.close();
				AccountCreateSuccessResponse successResponse = new AccountCreateSuccessResponse();
				successResponse.setMessage("Account Created");
				successResponse.setAccountNumber(account.getNumber());
				return ResponseEntity.ok().body(successResponse);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			String msg = "Invalid Account Type:" + request.getType();
			return ResponseEntity.badRequest().body(msg);
		}
		return null;
	}

	@Override
	public ResponseEntity<?> getAccountByNumber(long accountNumber) throws IOException {
		ObjectInputStream instream = new ObjectInputStream(new FileInputStream(new File("./" + accountNumber)));
		try {
			Account account = (Account) instream.readObject();
			return ResponseEntity.ok().body(account);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ResponseEntity<?> deposit(long accountNumber, AccountBalanceRequest request) {
		try {
			ObjectInputStream inputstream = new ObjectInputStream(new FileInputStream(new File("./" + accountNumber)));
			try {
				Account account = (Account) inputstream.readObject();
				account.setBalance(account.getBalance() + request.getBalance());
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date date = new Date(System.currentTimeMillis());
				account.setLastUpdateDate(dateFormat.format(date));
				ObjectOutputStream outstream = new ObjectOutputStream(
						new FileOutputStream(new File("./" + account.getNumber())));
				outstream.writeObject(account);
				outstream.close();
				String msg = accountNumber + " deposit amount: " + request.getBalance() + " " + account.getType();
				producer.send("logs", msg);
				return ResponseEntity.ok().body(account);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ResponseEntity<?> transfer(long accountNumber, BalanceTransferRequest request) {
		try {
			ObjectInputStream instream = new ObjectInputStream(new FileInputStream(new File("./" + accountNumber)));
			ObjectInputStream instream2 = new ObjectInputStream(
					new FileInputStream(new File("./" + request.getTransferredAccountNumber())));
			try {
				Account transferringAccount = (Account) instream.readObject();
				Account transferredAccount = (Account) instream2.readObject();
				if (0 <= transferringAccount.getBalance() - request.getAmount()) {
					if (transferringAccount.getType() != transferredAccount.getType()) {
						double result = this.exchange(request.getAmount(), transferredAccount.getType(),
								transferringAccount.getType());
						transferredAccount.setBalance(transferredAccount.getBalance() + result);
					} else {
						transferredAccount.setBalance(transferredAccount.getBalance() + request.getAmount());
					}
					transferringAccount.setBalance(transferringAccount.getBalance() - request.getAmount());
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date(System.currentTimeMillis());
					transferringAccount.setLastUpdateDate(dateFormat.format(date));
					transferredAccount.setLastUpdateDate(dateFormat.format(date));
					ObjectOutputStream outstream = new ObjectOutputStream(
							new FileOutputStream(new File("./" + accountNumber)));
					outstream.writeObject(transferringAccount);
					outstream.close();
					ObjectOutputStream outstream2 = new ObjectOutputStream(
							new FileOutputStream(new File("./" + request.getTransferredAccountNumber())));
					os2.writeObject(transferredAccount);
					os2.close();
					String logMessage = accountNumber + " transfer amount: " + request.getAmount() + " "
							+ transferringAccount.getType() + " ,transferred_account: "
							+ request.getTransferredAccountNumber();
					producer.send("logs", logMessage);
					String message = "Transferred Successfully";
					return ResponseEntity.ok().body(message);
				} else {
					String message = "Insufficient balance";
					return ResponseEntity.badRequest().body(message);
				}

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ResponseEntity<?> accountLog(long accountNumber) throws IOException {
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("logs.txt"));
			String row = reader.readLine();
			ArrayList<String> list = new ArrayList<String>();
			while (row != null) {
				String[] pieces = row.split(" ");
				if (pieces[0].equals(accountNumber + "")) {
					if (pieces[1].equals("deposit")) {
						list.add(accountNumber + " nolu hesaba " + pieces[3] + " " + pieces[4] + " yatırılmıştır.");
					} else {
						list.add(accountNumber + " hesaptan " + pieces[6] + " hesaba " + pieces[3] + " " + pieces[4]
								+ "  transfer edilmiştir.");
					}
					row = reader.readLine();
				} else {
					row = reader.readLine();
				}
			}

			reader.close();
			return ResponseEntity.ok().body(list);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public double exchange(double amount, String converted, String mainType) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("content-type", "application/json");
		headers.add("authorization", "yourkey");
		String url = "https://api.collectapi.com/economy/exchange?int=" + amount + "&to=" + converted + "&base="
				+ mainType;
		HttpEntity<?> requestEntity = new HttpEntity<>(headers);
		ResponseEntity<String> response = client.exchange(url, HttpMethod.GET, requestEntity, String.class);
		String outcome = response.getBody();
		ObjectMapper objectMapper = new ObjectMapper();
		Double result = 0.0;
		JsonNode node;
		try {
			node = objectMapper.readTree(outcome);
			JsonNode tmp = node.get("result");
			ArrayNode data = (ArrayNode) tmp.get("data");
			JsonNode info = data.get(0);
			result = info.get("calculated").asDouble();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
