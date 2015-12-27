package com.jinsung.adoda.gpmon;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jinsung.adoda.gpmon.data.DataContainer;
import com.jinsung.adoda.gpmon.data.Machine;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private MachinesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.activity_mylist_listview);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mAdapter)
            mAdapter.clear();
        DataContainer.getInstance().requestMachines(
            MainActivity.this,
            new GetMachinesInterface()
        );
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Machine machine = mAdapter.getItem(arg2);

        if (false == DataContainer.getInstance().setSelectedMachine(machine)) {
            Toast.makeText(this, getString(R.string.toast_invalid_selection), Toast.LENGTH_SHORT).show();
            return;
        }
        else
            Toast.makeText(this, machine.getName(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), DailyApiCallActivity.class);
        intent.putExtra("dataContainer", DataContainer.getInstance());
        intent.putExtra("targetMachine", DataContainer.getInstance().getSelectedMachine());

        startActivity(intent);
    }

    public class GetMachinesInterface implements DataContainer.IResponseInterface {

        ProgressDialog dialog = null;

        @Override
        public void onStart() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage(getString(R.string.dlgtext_waiting));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        public void onFailure(int stateCode, Header[] header, byte[] body, Throwable error) {
            String errMsg = "State Code :" + stateCode + "\n";
            errMsg += "Error Message :" + error.getMessage();
            Toast.makeText(MainActivity.this, errMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(int stateCode, Header[] header, byte[] body) {
            mAdapter = new MachinesAdapter(
                MainActivity.this, R.layout.list_row,
                DataContainer.getInstance().getMachines()
            );
            mListView = (ListView) findViewById(R.id.listView);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(MainActivity.this);
        }

        @Override
        public void onFinish() {
            dialog.dismiss();
            dialog = null;
        }
    }

    public class MachinesAdapter extends ArrayAdapter<Machine> {

        private final Context mContext;

        public MachinesAdapter(Context context, int resource, List<Machine> objects) {
            super(context, resource, objects);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) convertView;
            if (view == null) {
                view = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
            }

            view.setGravity(Gravity.CENTER);
            view.setTypeface(Typeface.DEFAULT);
            view.setTextSize(20);
            view.setText(getItem(position).getName());

            return view;
        }
    }


}
