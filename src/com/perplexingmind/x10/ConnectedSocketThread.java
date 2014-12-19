package com.perplexingmind.x10;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;

public class ConnectedSocketThread extends Thread {
	private final BluetoothSocket bt_socket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	
	public ConnectedSocketThread(BluetoothSocket socket){
		bt_socket=socket;
		InputStream tmp_in=null;
		OutputStream tmp_out=null;
		
		try{
			tmp_in=socket.getInputStream();
			tmp_out=socket.getOutputStream();
		} catch (IOException e){}
		
		mmInStream=tmp_in;
		mmOutStream=tmp_out;
	}
	
	public void run(){
		byte[] buffer=new byte[1024]; //buffer store for the stream
		int bytes;
		
		while (true){
			try{
				bytes=mmInStream.read(buffer);
			} catch (IOException e){
				break;
			}
		}
	}
	
	public void write(byte[] bytes){
		try{
			mmOutStream.write(bytes);
		} catch (IOException e) {}
	}
	
	public void cancel(){
		try{
			bt_socket.close();
		} catch (IOException e){}
	}
}
