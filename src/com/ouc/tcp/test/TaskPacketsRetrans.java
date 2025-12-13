package com.ouc.tcp.test;

import java.util.List;
import java.util.TimerTask;

import com.ouc.tcp.client.Client;
import com.ouc.tcp.message.TCP_PACKET;

public class TaskPacketsRetrans extends TimerTask{
	private Client senderClient;
	private List<TCP_PACKET> unAckedPackets;
	
	public TaskPacketsRetrans(Client client, List<TCP_PACKET> packets4Retrans) {
		super();
		senderClient=client;
		unAckedPackets=packets4Retrans;	
	}
	
	@Override
	public void run() {
		
		for (TCP_PACKET pkt : unAckedPackets) {
			System.out.println("Retransmit: " + pkt.getTcpH().getTh_seq());
			senderClient.send(pkt);
		}
	}	
}
