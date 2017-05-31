package de.vodafone.innogarage.sfcdi_monitor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Handler handler = new Handler();
    ConnectionManager conMan = new ConnectionManager();
    List<Connection> cons = conMan.getConnections();
    Timer timer = new Timer();
    TimerTask timerTask;
    Context globalConext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        globalConext = this;

        final Button button = (Button) findViewById(R.id.bt_invite);
        button.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){

                conMan.sendInvitation();
            }
        });

        final Button button2 = (Button) findViewById(R.id.bt_Disconnect);
        button2.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){

                conMan.sendInvitation();
            }
        });

        // TODO: check if this works, if so remove it
        // does not work properly with the given software structure
        // new Inviter().start();

        timerTask = new TimerTask() {

            public void run() {
                new ScannerTask() {

                    protected void onPostExecute(List<String> result) {

                         if (!cons.isEmpty() &&!result.isEmpty()) {

                             ListView liste = (ListView) findViewById(R.id.lv_SFCDListe);
                             liste.setAdapter(new ListViewAdapter(globalConext, conMan));

                             TextView temp = (TextView) findViewById(R.id.sfcdname);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.temperature);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.currenttime);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.resetcounter);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.mode);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.systemmode);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.psstate);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.lteband);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.ltebw);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.imsregstate);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.lterxchan);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.ltetxchan);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.ltecastate);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.emmstate);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.rrcstate);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.rxmrssi);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.txpower);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.rxdrssi);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.tac);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.rsrq);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.sinr);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.cellid);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.rxmrsrp);
                             temp.setText(result.remove(0));
                             temp = (TextView) findViewById(R.id.rxdrsrp);
                             temp.setText(result.remove(0));
                        }
                    }
                }.execute(conMan);
            }
        };
        timer.schedule(timerTask, 0, 200);
    }

    public class ListViewAdapter extends BaseAdapter {

        ConnectionManager conMan;
        private Context context;

        public ListViewAdapter(Context c,ConnectionManager conMan) {
            this.conMan = conMan;
            context = c;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.explistheader, parent , false);

            Button button = (Button) row.findViewById(R.id.bt_details);
            TextView tv_online = (TextView) row.findViewById(R.id.side_online);
            TextView tv_hostname = (TextView) row.findViewById(R.id.side_name);

            final Connection con = conMan.getConnections().get(position);

            button.setOnClickListener(new View.OnClickListener(){

                public void onClick(View v){

                    for(Connection con : conMan.getConnections()){

                        con.setFocus(false);

                    }
                    con.setFocus(true);
                }
            });

            tv_hostname.setText(con.getName());

            if (con.isOnline()) {

                tv_online.setText("ONLINE");
                tv_online.setBackgroundColor(Color.GREEN);
            } else {

                tv_online.setText("OFFLINE");
                tv_online.setBackgroundColor(Color.RED);
            }

            if (con.isFocus()) {

                button.setBackgroundColor(Color.GREEN);
            } else {

                button.setBackgroundColor(Color.GRAY);
            }

            return row;
        }

        @Override
        public int getCount() {
            return cons.size();
        }

        @Override
        public Object getItem(int position) {
            return cons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

}
