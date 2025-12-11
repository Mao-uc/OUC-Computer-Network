package com.ouc.tcp.test;

import com.ouc.tcp.message.TCP_HEADER;
import com.ouc.tcp.message.TCP_PACKET;

public class CheckSum {
	
	/* Calculate the TCP packet checksum: only validate seq, ack, and sum in the TCP header, as well as the TCP data field */
	public static short computeChkSum(TCP_PACKET tcpPack) {
		long checkSum = 0;
		TCP_HEADER Htmp = tcpPack.getTcpH();
		int data[] = tcpPack.getTcpS().getData();
		
		int Seq=Htmp.getTh_seq();
		int Ack=Htmp.getTh_ack();
		
		checkSum = (Seq >> 16) + (Seq&0xffff)  + (Ack >> 16) + (Ack&0xffff);// + Htmp.getTh_sum();
		for(int i=0;i<data.length;i++) {
			checkSum+=((data[i]>>16)+ (data[i]&0xffff));
		}
		while((checkSum>>16) > 0) {
			checkSum = (checkSum & 0xffff) + (checkSum >> 16);
		}
		checkSum =~checkSum;
		
		return (short) checkSum;
	}
}