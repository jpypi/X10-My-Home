package com.perplexingmind.x10;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class DeviceConnectDialog extends DialogFragment {
	private CharSequence[] items;
	private int title_resource;
	Method mcallback_method;
	Object mcallback_object;
	

	public DeviceConnectDialog(CharSequence[] items,int title_resource, Object callback_object, Method callback_method) {
		this.items=items;
		this.title_resource=title_resource;
		mcallback_method=callback_method;
		mcallback_object=callback_object;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		builder.setTitle(this.title_resource);
		OnClickListener listener=new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doCallback(which);
			}
		};
		builder.setItems(items, listener);
		
		return builder.create();
	}
	
	public void doCallback(int which){
		try {
			mcallback_method.invoke(mcallback_object, items[which]);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//((MainActivity) getActivity()).connectToDevice(items[which]);
	}
}
