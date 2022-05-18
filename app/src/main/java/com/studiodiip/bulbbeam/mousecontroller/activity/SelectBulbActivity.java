package com.studiodiip.bulbbeam.mousecontroller.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.studiodiip.bulbbeam.mousecontroller.R;
import com.studiodiip.bulbbeam.mousecontroller.mouseSocketSender;
import com.studiodiip.bulbbeam.mousecontroller.objects.BeamBulb;
import com.studiodiip.bulbbeam.mousecontroller.util.BulbDiscoverer;

import java.util.ArrayList;
import java.util.Iterator;

public class SelectBulbActivity extends Activity {
    ListView lv;

    public void onCreate(Bundle selectBulb) {
        super.onCreate(selectBulb);
        setContentView(R.layout.activity_select_bulb);
        Log.d("SELECT BULB", "OnCreate");
        this.lv = (ListView) findViewById(R.id.listView);
        final String accountName = getIntent().getStringExtra("accountName");
        final String userName = getIntent().getStringExtra("userName");
        final String localMacAddress = getIntent().getStringExtra("macAddress");
        ArrayList<String> ips = new ArrayList<>();
        Iterator<BeamBulb> it = BulbDiscoverer.beamBulbs.iterator();
        while (it.hasNext()) {
            ips.add(it.next().ip);
        }

        this.lv.setAdapter((ListAdapter) new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ips));
        this.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.activity.SelectBulbActivity.AnonymousClass1 */

            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
                view.setSelected(true);
                String selectedItem = BulbDiscoverer.beamBulbs.get(position).ip;
                Log.d("LISTVIEW", selectedItem);
                MainActivity.SERVER_IP = selectedItem;
                MainActivity.mss = new mouseSocketSender(selectedItem, MainActivity.SERVERPORT, accountName, userName, localMacAddress);
                SelectBulbActivity.this.startActivity(new Intent(SelectBulbActivity.this, TestActivity.class));
                SelectBulbActivity.this.finish();
            }
        });
    }
}
