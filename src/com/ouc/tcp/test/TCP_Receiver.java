/***************************2.1: ACK/NACK*****************/
/***** Feng Hong; 2015-12-09******************************/
package com.ouc.tcp.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.ouc.tcp.client.TCP_Receiver_ADT;
import com.ouc.tcp.message.TCP_PACKET;

public class TCP_Receiver extends TCP_Receiver_ADT {

	private TCP_PACKET ackPack; // ACK packet to be replied
	int sequence = 1;// Used to record the sequence number of the current packet to be received, note
						// that the packet sequence number is not completely
	int lastack = -1;
	
	/* Constructor */
	public TCP_Receiver() {
		super(); // Call superclass constructor
		super.initTCP_Receiver(this); // Initialize TCP receiver
	}

	@Override
	// Packet received: check checksum, set ACK packet to reply
	public void rdt_recv(TCP_PACKET recvPack) {
		// Check checksum, generate ACK
		if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {

			int sequence_cur = recvPack.getTcpH().getTh_seq();
			
			// Generate ACK packet (set acknowledgment number)
			tcpH.setTh_ack(sequence_cur);
			ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
			tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
			// Reply ACK packet
			reply(ackPack);
			
			if (sequence_cur == sequence) {
				// Insert the correctly received and ordered data into the data queue, ready for
				// delivery
				dataQueue.add(recvPack.getTcpS().getData());
				lastack = sequence;
				sequence += recvPack.getTcpS().getData().length;
			} else {
				System.out.println("Duplicate packet, Seq =" + sequence_cur);
			}
		} else {
			System.out.println("Recieve Computed: " + CheckSum.computeChkSum(recvPack));
			System.out.println("Recieved Packet" + recvPack.getTcpH().getTh_sum());
			System.out
					.println("Problem: Packet Number: " + recvPack.getTcpH().getTh_seq() + " + InnerSeq:  " + sequence);
			tcpH.setTh_ack(lastack);
			ackPack = new TCP_PACKET(tcpH, tcpS, recvPack.getSourceAddr());
			tcpH.setTh_sum(CheckSum.computeChkSum(ackPack));
			// Reply ACK packet
			reply(ackPack);
		}

		System.out.println();

		// Deliver data (deliver every 20 groups of data)
		if (dataQueue.size() == 20)
			deliver_data();
	}

	@Override
	// Deliver data (write data to file); no modification needed
	public void deliver_data() {
		// Check dataQueue, write data to file
		File fw = new File("recvData.txt");
		BufferedWriter writer;

		try {
			writer = new BufferedWriter(new FileWriter(fw, true));

			// Loop to check if there is new data to be delivered in the data queue
			while (!dataQueue.isEmpty()) {
				int[] data = dataQueue.poll();

				// Write data to file
				for (int i = 0; i < data.length; i++) {
					writer.write(data[i] + "\n");
				}

				writer.flush(); // Clear output buffer
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	// Reply ACK packet
	public void reply(TCP_PACKET replyPack) {
		// Set error control flag
		tcpH.setTh_eflag((byte) 1); // eFlag=0, channel has no error

		// Send packet
		client.send(replyPack);
	}

}