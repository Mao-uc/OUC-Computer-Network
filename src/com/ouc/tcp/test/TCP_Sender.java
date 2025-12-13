/***************************2.1: ACK/NACK
**************************** Feng Hong; 2015-12-09*/

package com.ouc.tcp.test;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_PACKET;

public class TCP_Sender extends TCP_Sender_ADT {

	private TCP_PACKET tcpPack; // TCP packet to be sent
	private volatile int flag = 0;
	UDT_Timer udt_timer;
	
	/* Constructor */
	public TCP_Sender() {
		super(); // Call superclass constructor
		super.initTCP_Sender(this); // Initialize TCP sender
	}

	@Override
	// Reliable sending (called by application layer): encapsulate application data,
	// generate TCP packet; needs modification
	public void rdt_send(int dataIndex, int[] appData) {

		// Generate TCP packet (set sequence number and data field/checksum), pay
		// attention to packing order
		tcpH.setTh_seq(dataIndex * appData.length + 1);// Set packet sequence number as byte stream number:
		tcpS.setData(appData);
		tcpPack = new TCP_PACKET(tcpH, tcpS, destinAddr);

		tcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
		tcpPack.setTcpH(tcpH);

		// Send TCP packet
		udt_send(tcpPack);
		udt_timer = new UDT_Timer();
		udt_timer.schedule(new UDT_RetransTask(client, tcpPack), 3000, 3000);
		
		
		flag = 0;

		waitACK();
	}

	@Override
	// Unreliable sending: send packaged TCP packet through unreliable transmission
	// channel; only need to modify error flag
	public void udt_send(TCP_PACKET stcpPack) {
		// Set error control flag
		tcpH.setTh_eflag((byte) 4);
		// System.out.println("to send: "+stcpPack.getTcpH().getTh_seq());
		// Send packet
		client.send(stcpPack);
	}

	@Override
	// Loop check ackQueue
	// Loop check confirmation number queue for newly received ACK
	public void waitACK() {
		while (flag == 0) {
			if (!ackQueue.isEmpty()) {
				int currentAck = ackQueue.poll();
				if (currentAck == tcpPack.getTcpH().getTh_seq()) {
					System.out.println("Clear: " + tcpPack.getTcpH().getTh_seq());
					flag = 1;
					udt_timer.cancel();
				} else {
					System.out.println("Retransmit: " + tcpPack.getTcpH().getTh_seq());
					udt_send(tcpPack);
					flag = 0;
				}
			}
		}
	}

	@Override
	// Receive ACK packet: check checksum, insert confirmation number into ack
	// queue; NACK confirmation number is -1; no modification needed
	public void recv(TCP_PACKET recvPack) {
		
		if(CheckSum.computeChkSum(recvPack)==recvPack.getTcpH().getTh_sum()) {
			System.out.println("Receive ACK Number： " + recvPack.getTcpH().getTh_ack());			
			ackQueue.add(recvPack.getTcpH().getTh_ack());
			System.out.println();			
		}else {
			System.out.println("Receive error ACK");
			ackQueue.add(-1);
			System.out.println();
		}
	}
}