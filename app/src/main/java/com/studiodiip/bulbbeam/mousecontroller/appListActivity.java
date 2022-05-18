package com.studiodiip.bulbbeam.mousecontroller;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.studiodiip.bulbbeam.mousecontroller.activity.MainActivity;

public class appListActivity extends Activity {
    ListView lv;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_app_list);
        this.lv = (ListView) findViewById(R.id.listView2);
        this.lv.setChoiceMode(1);
        if (mouseSocketSender.receivedAppList.length() == 0) {
            finish();
        }
        this.lv.setAdapter((ListAdapter) new ArrayAdapter(this, android.R.layout.simple_list_item_1, mouseSocketSender.receivedAppList.split(":")));
        this.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.appListActivity.AnonymousClass1 */

            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long arg3) {
                view.setSelected(true);
                MainActivity.mss.sendSocket("a", String.valueOf(position));
                appListActivity.this.finish();
            }
        });
        ((Button) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            /* class com.studiodiip.bulbbeam.mousecontroller.appListActivity.AnonymousClass2 */

            public void onClick(View view) {
                appListActivity.this.finish();
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_list, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
