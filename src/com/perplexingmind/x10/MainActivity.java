package com.perplexingmind.x10;


import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private UUID uuid=UUID.fromString("de92f5ce-f218-4e39-a08b-72dd386ad41f");
	public static CharSequence[] house_codes={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P"};
	private int REQUEST_ENABLE_BT=1234;
	
	private boolean btEnabled=false;
	private BluetoothDevice connected_device;
	private BluetoothSocket device_socket;
	public BluetoothAdapter mBluetoothAdapter;
	
	private Menu action_bar_menu;
	
	public int x10_current_unit=1;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_2);
		try{
			connected_device=savedInstanceState.getParcelable("connected_device");
			TextView status_message=(TextView)findViewById(R.id.status);
			status_message.setText(getString(R.string.status_connected)+" "+connected_device.getName());
		}catch (NullPointerException e){}//Do nothing there was no connected device before
		try{
			x10_current_unit=savedInstanceState.getInt("x10_current_unit");
			if (x10_current_unit==0) {x10_current_unit=1;}
			if (x10_current_unit!=0){
				((TextView) findViewById(R.id.current_unit)).setText(getString(R.string.current_unit)+" "+String.valueOf(x10_current_unit));
			}
		}catch (NullPointerException e){}
		
		ListView unit_codes_lv=(ListView) findViewById(R.id.unit_codes);
		String[] unit_codes=new String[16];
		for(int i=1;i<17;i++){
			unit_codes[i-1]=String.valueOf(i);
		}
		ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,unit_codes);
		unit_codes_lv.setAdapter(adapter);
		unit_codes_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				String item=(String) parent.getItemAtPosition(position);
				x10_current_unit=Integer.parseInt(item);
				((TextView) findViewById(R.id.current_unit)).setText(getString(R.string.current_unit)+" "+item);
			}
		});
	}
	
	@Override
	protected void onStart(){
		super.onResume();
		mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter==null){
			//Device doesn't support bluetooth :/
		}
		
		if(!mBluetoothAdapter.isEnabled()){
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}else{
			btEnabled=true;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		action_bar_menu=menu;
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.action_settings:
			
			try {				
				Class params[]=new Class[1];
				params[0]=CharSequence.class;
				DeviceConnectDialog dev_conn;
				dev_conn = new DeviceConnectDialog(house_codes,R.string.house_code,this,this.getClass().getDeclaredMethod("setHouseCode",params));
				dev_conn.show(getFragmentManager(), "dev_conn_dlg");
			} catch (NoSuchMethodException e) {
				System.out.println("Arg hc");
				e.printStackTrace();
			}
			
			/*System.out.println("The settings button was clicked!");
			char x=(char)(((char)item.getTitleCondensed().toString().charAt(0))+1);
			item.setTitleCondensed(String.valueOf(x));*/
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putInt("x10_current_unit", x10_current_unit);
		outState.putParcelable("connected_device", connected_device);
	}

	
	public void setHouseCode(CharSequence code){
		action_bar_menu.findItem(R.id.action_settings).setTitleCondensed(code);
	}
	
	public void lightOn(View view){
		String hc=action_bar_menu.findItem(R.id.action_settings).getTitleCondensed().toString();
		String uc=String.valueOf(x10_current_unit);
		String command="on";
		sendData(hc+uc+" "+command);
	}
	
	public void lightOff(View view){
		String hc=action_bar_menu.findItem(R.id.action_settings).getTitleCondensed().toString();
		String uc=String.valueOf(x10_current_unit);
		String command="off";
		sendData(hc+uc+" "+command);
	}
	
	public void lightBright(View view){
		String hc=action_bar_menu.findItem(R.id.action_settings).getTitleCondensed().toString();
		String uc=String.valueOf(x10_current_unit);
		String command="bright";
		sendData(hc+uc+" "+command);
	}
	
	public void lightDim(View view){
		String hc=action_bar_menu.findItem(R.id.action_settings).getTitleCondensed().toString();
		String uc=String.valueOf(x10_current_unit);
		String command="dim";
		sendData(hc+uc+" "+command);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode==REQUEST_ENABLE_BT){
			if (resultCode==RESULT_OK){
				btEnabled=true;
			}else{
				//They hit cancel... :/
				makeToast(getString(R.string.app_name)+" will not work with bluetooth disabled.");
			}
		}
	}
	
	
	public void sendMessage(View veiw){ 
		EditText message_box=(EditText)findViewById(R.id.message_box);
		try{
			device_socket=connected_device.createRfcommSocketToServiceRecord(uuid);
			try{
				device_socket.connect();
				OutputStream out_stream=device_socket.getOutputStream();
				byte[] data=message_box.getText().toString().getBytes();
				byte control_flags=0; //Control flag only controls "stay alive" on server side right now. (a 1 is stay alive 0 is don't)
				if (message_box.getText().toString().equalsIgnoreCase("done")){
					control_flags=0;
				}
				byte[] header={(byte) (0xF0&data.length), (byte) (0x0F&data.length), control_flags};
				
				out_stream.write(header);
				out_stream.write(data);
				message_box.setText("");
				if (control_flags==0){
					device_socket.close();
				}
			} catch (IOException e) {}
			
		}catch (IOException e) {}
		catch (NullPointerException e){
			makeToast("No device selected for connection!");
		}
	}
	
	public void sendData(CharSequence data){
		try{
			device_socket=connected_device.createRfcommSocketToServiceRecord(uuid);
			try{
				device_socket.connect();
				OutputStream out_stream=device_socket.getOutputStream();
				byte[] send_data=data.toString().getBytes();
				byte control_flags=0; //Control flag only controls "stay alive" on server side right now. (a 1 is stay alive 0 is don't)
				byte[] header={(byte) (0xF0&send_data.length), (byte) (0x0F&send_data.length), control_flags};
				
				out_stream.write(header);
				out_stream.write(send_data);
				if (control_flags==0){
					device_socket.close();
				}
			} catch (IOException e) {}
			
		}catch (IOException e) {}
		catch (NullPointerException e){
			makeToast("No device selected for connection!");
		}
	}
	
	
	public void connectToDevice(CharSequence device_name){
		Set<BluetoothDevice> pairedDevices=mBluetoothAdapter.getBondedDevices();
		CharSequence[] devices=new CharSequence[pairedDevices.size()];
		
		if (pairedDevices.size()>0){
			int i=0;
			for (BluetoothDevice device:pairedDevices){
				devices[i]=device.getName();
				i++;
				if (device.getName().contentEquals(device_name)){
					connected_device=device;
					TextView status_message=(TextView)findViewById(R.id.status);
					status_message.setText(getString(R.string.status_connected)+" "+connected_device.getName());
					/*for (ParcelUuid uuid: connected_device.getUuids()){
						System.out.println(uuid);
					}
					System.out.println("-------------");*/
					
				}
			}
		}
	}
	
	public void connectClicked(View view){
		if (btEnabled){
			Set<BluetoothDevice> pairedDevices=mBluetoothAdapter.getBondedDevices();
			CharSequence[] devices=new CharSequence[pairedDevices.size()];
			
			if (pairedDevices.size()>0){
				int i=0;
				for (BluetoothDevice device:pairedDevices){
					devices[i]=device.getName();
					i++;
				}
				
				try {
					Class params[]=new Class[1];
					params[0]=CharSequence.class;
					DeviceConnectDialog dev_conn;
					dev_conn = new DeviceConnectDialog(devices,R.string.device_connect,this,this.getClass().getDeclaredMethod("connectToDevice",params));
					dev_conn.show(getFragmentManager(), "dev_conn_dlg");
				} catch (NoSuchMethodException e) {
					System.out.println("Device connect error.");
					e.printStackTrace();
				}
				
			}
		}		
	}
	
	public void makeToast(String message){
		Toast t=Toast.makeText(this, message, Toast.LENGTH_SHORT);
		t.setGravity(Gravity.LEFT|Gravity.BOTTOM, 0, 0);
		t.show();
	}
	
}
