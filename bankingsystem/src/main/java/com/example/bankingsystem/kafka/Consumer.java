package com.example.bankingsystem.kafka;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

public class Consumer {
	@KafkaListener(topics = "logs", groupId = "transaction")
	public void listenTransfer(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(new File("logs.txt"), true));
			writer.write(message + "\n");
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
