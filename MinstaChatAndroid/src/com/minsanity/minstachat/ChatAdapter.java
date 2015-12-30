package com.minsanity.minstachat;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter{
	private Context context;
    private List<Message> messagesItems;

    public ChatAdapter(Context context, List<Message> messageItems){
    	this.context = context;
    	this.messagesItems = messageItems;
    }

    @Override
    public int getCount() {
        return messagesItems.size();
    }
 
    @Override
    public Object getItem(int position) {
        return messagesItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, ViewGroup parent) {
 
        /**
         * The following list not implemented reusable list items as list items
         * are showing incorrect data Add the solution if you have one
         * */
 
        Message m = messagesItems.get(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        
        if(m.isOwner()) {
        	if(m.isWhisper()){
            	view = mInflater.inflate(R.layout.list_item_whisper_right, null);
        	}else{
        		view = mInflater.inflate(R.layout.list_item_message_right, null);        		
        	}
        }else{
        	if(m.isWhisper()){
        		view = mInflater.inflate(R.layout.list_item_whisper_left, null);
        	}else if(m.isServer()){
        		view = mInflater.inflate(R.layout.list_item_server_left, null);
        	}else{
        		view = mInflater.inflate(R.layout.list_item_message_left, null);        		
        	}
        }
 
        TextView lblFrom = (TextView) view.findViewById(R.id.lblMsgFrom);
        TextView txtMsg = (TextView) view.findViewById(R.id.txtMsg);
 
        txtMsg.setText(m.getMessage());
        lblFrom.setText(m.getFromName());
 
        return view;
    }


    
}
