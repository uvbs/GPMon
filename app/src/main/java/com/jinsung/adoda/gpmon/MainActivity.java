package com.jinsung.adoda.gpmon;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    final String URL = "http://gpmon-adodajs.rhcloud.com/v1/machines.php";
    private AsyncHttpClient mClient;
    private GetTargetHostsResponse mResponse;

    private ListView mListView;
    private MachinesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.activity_mylist_listview);

        mClient = new AsyncHttpClient();
        mResponse = new GetTargetHostsResponse();

        mAdapter = new MachinesAdapter(this, R.layout.list_row, new ArrayList<Machine>());
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.clear();
        mClient.get(URL, mResponse);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Machine machine = mAdapter.getItem(arg2);
        Toast.makeText(this, machine.getName(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), DailyApiCallActivity.class);
        intent.putExtra("targetMachine", machine);

        startActivity(intent);
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

    public class GetTargetHostsResponse extends AsyncHttpResponseHandler {

        ProgressDialog dialog;

        @Override
        public void onStart() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("잠시만 기다려주세요...");
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
            try {
                //통신 결과를 문자열로 변환한다.
                String response = new String(body, "UTF-8");

                //문자열을 JSONArray로 변환한다.
                JSONArray jsonArray = new JSONArray(response);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    mAdapter.add(new Machine(obj.getString("machine_name")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 성공, 실패 여부에 상관 없이 통신이 종료되면 실행.
        @Override
        public void onFinish() {
            dialog.dismiss();
            dialog = null;
        }
    }
}
