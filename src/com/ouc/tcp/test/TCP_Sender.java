/***************************2.1: ACK/NACK
**************************** Feng Hong; 2015-12-09*/

package com.ouc.tcp.test;

import java.util.ArrayList;
import java.util.List;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_HEADER;
import com.ouc.tcp.message.TCP_PACKET;
import com.ouc.tcp.message.TCP_SEGMENT;

public class TCP_Sender extends TCP_Sender_ADT {

	private TCP_PACKET tcpPack; // TCP packet to be sent

	// GBN needed
	private UDT_Timer udt_timer;
	private int windowSize = 4;
	private List<TCP_PACKET> unAckedPackets = new ArrayList<TCP_PACKET>();

	/* Constructor */
	public TCP_Sender() {
		super(); // Call superclass constructor
		super.initTCP_Sender(this); // Initialize TCP sender
	}

	@Override
	// Reliable sending (called by application layer): encapsulate application data,
	// generate TCP packet; needs modification
	public void rdt_send(int dataIndex, int[] appData) {

		// wait for spare window
		while (unAckedPackets.size() >= windowSize);

		try {
			TCP_HEADER newTcpH = tcpH.clone();
			newTcpH.setTh_seq(dataIndex * appData.length + 1);

			TCP_SEGMENT newTcpS = tcpS.clone();
			newTcpS.setData(appData);
			tcpPack = new TCP_PACKET(newTcpH, newTcpS, destinAddr);

			newTcpH.setTh_sum(CheckSum.computeChkSum(tcpPack));
			tcpPack.setTcpH(newTcpH);

			unAckedPackets.add(tcpPack);
			udt_send(tcpPack);

			// Sender has one timer for the oldest unackedpacket
			if(udt_timer==null) {
				udt_timer = new UDT_Timer();
				udt_timer.schedule(new UDT_RetransTask(client, tcpPack), 3000, 3000);
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	@Override
	// Unreliable sending: send packaged TCP packet through unreliable transmission
	// channel; only need to modify error flag
	public void udt_send(TCP_PACKET stcpPack) {
		// Set error control flag
		tcpH.setTh_eflag((byte) 7);
		// System.out.println("to send: "+stcpPack.getTcpH().getTh_seq());
		// Send packet
		client.send(stcpPack);
	}

	@Override
	// Loop check ackQueue
	// Loop check confirmation number queue for newly received ACK
	public void waitACK() {

	}

	@Override
	public void recv(TCP_PACKET recvPack) {

		// rdt_rcv(rcvpkt) && notcorrupt(rcvpkt)
		if (CheckSum.computeChkSum(recvPack) == recvPack.getTcpH().getTh_sum()) {

			int ack = recvPack.getTcpH().getTh_ack();

			System.out.println("Receive ACK Number： " + ack);

			boolean isAckNew = false;

			while ((!unAckedPackets.isEmpty()) && unAckedPackets.get(0).getTcpH().getTh_seq() <= ack) {
				unAckedPackets.remove(0);
				isAckNew = true;
			}

			if (isAckNew) {
				udt_timer.cancel();
				if (!unAckedPackets.isEmpty()) {
					udt_timer = new UDT_Timer();
					udt_timer.schedule(new TaskPacketsRetrans(client, unAckedPackets), 3000, 3000);
				} else {
					udt_timer = null;
				}
			}
		}
	}
}