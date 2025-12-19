package com.ouc.tcp.test;

import java.util.TimerTask;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

public class TaskPacketsRetrans extends TimerTask{
	private Client senderClient;
	//private List<TCP_PACKET> unAckedPackets;
	private TCP_PACKET packet4Retrans;
	private TCP_Sender sender;
	
	public TaskPacketsRetrans(Client client, TCP_PACKET packet4Retrans, TCP_Sender sender) {
		super();
		senderClient=client;
		this.packet4Retrans=packet4Retrans;	
		this.sender = sender;
	}
	
	@Override
	public void run() {
		senderClient.send(packet4Retrans);		
		sender.ssthresh = Math.max((int)sender.windowSize / 2, 2);
		sender.windowSize = 1.0;
		System.out.println("Tahoe Event: Timeout. Resetting cwnd = "+(int)sender.windowSize);
		System.out.println("Tahoe Event: Multiplicative Decrease. Resetting ssthresh = "+sender.ssthresh);
//		for (TCP_PACKET pkt : unAckedPackets) {
//			System.out.println("Retransmit: " + pkt.getTcpH().getTh_seq());
//			senderClient.send(pkt);
//		}
	}	
}
