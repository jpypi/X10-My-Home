package com.perplexingmind.x10;

import java.io.IOException;
import java.util.UUID;

import com.perplexingmind.x10.R;



import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.EditText;

public class ConnectThread extends Thread {

	private final BluetoothSocket mmSocket;
	private final BluetoothDevice mmDevice;
	private MainActivity parent;
	
	public ConnectThread(BluetoothDevice device, MainActivity parent_activity){
		parent=parent_activity;
		BluetoothSocket tmp=null;
		mmDevice=device;
		
		try{
			UUID uuid=UUID.fromString("d1890667-00a8-419f-ac00-2c8ea10b7081");
			tmp=device.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e){}
		mmSocket=tmp;
	}
	
	public void run(){
		//Cancel discovery 'cause it will slow down the connection
		parent.mBluetoothAdapter.cancelDiscovery();
		
		try{
			mmSocket.connect();
		} catch (IOException connectException){
			//Unable to connect; close the socket and get out
			try{
				mmSocket.close();
			} catch (IOException closeException){ }
			return;
		}
		
		EditText message_box=(EditText)parent.findViewById(R.id.message_box);
		
		ConnectedSocketThread cst=new ConnectedSocketThread(mmSocket);
		cst.write(message_box.getText().toString().getBytes());
		cst.cancel();
	}
	
	public void cancel(){
		try{
			mmSocket.close();
		} catch (IOException e) { }
	}
}
