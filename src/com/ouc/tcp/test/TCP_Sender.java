/***************************2.1: ACK/NACK
**************************** Feng Hong; 2015-12-09*/

package com.ouc.tcp.test;

//import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import com.ouc.tcp.client.TCP_Sender_ADT;
import com.ouc.tcp.client.UDT_RetransTask;
import com.ouc.tcp.client.UDT_Timer;
import com.ouc.tcp.message.TCP_HEADER;
import com.ouc.tcp.message.TCP_PACKET;
import com.ouc.tcp.message.TCP_SEGMENT;

public class TCP_Sender extends TCP_Sender_ADT {

	private TCP_PACKET tcpPack; // TCP packet to be sent

	// TCP needed
	private UDT_Timer udt_timer;
	private int windowSize = 4;
	private ConcurrentSkipListMap<Integer, TCP_PACKET> unAckedPackets = new ConcurrentSkipListMap<Integer, TCP_PACKET>();
	//private ConcurrentSkipListMap<Integer, UDT_Timer> timers = new ConcurrentSkipListMap<Integer, UDT_Timer>();
	
	/* Constructor */
	public TCP_Sender() {
		super(); // Call superclass constructor
		super.initTCP_Sender(this); // Initialize TCP sender
	}

	@Override
	// Reliable sending (called by application layer): encapsulate application data,
	// generate TCP packet
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

			unAckedPackets.put(newTcpH.getTh_seq(),tcpPack);
			udt_send(tcpPack);

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

			while((!unAckedPackets.isEmpty()) && (unAckedPackets.firstKey() <= ack)) {
				unAckedPackets.remove(unAckedPackets.firstKey());
				isAckNew = true;
			}

			if (isAckNew) {
				udt_timer.cancel();
				if (!unAckedPackets.isEmpty()) {
					TCP_PACKET basePacket = unAckedPackets.firstEntry().getValue();
					udt_timer = new UDT_Timer();
					udt_timer.schedule(new UDT_RetransTask(client, basePacket), 3000, 3000);
				} else {
					udt_timer = null;
				}
			}
		}
	}
}