package com.jinsung.adoda.gpmon;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.jinsung.adoda.gpmon.data.DataContainer;
import com.jinsung.adoda.gpmon.data.Machine;
import com.jinsung.adoda.gpmon.fortest.TestBase;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MachinesActivity extends TestBase implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private MachinesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machines);

        // 머신 리스트 뷰를 생성한다.
        mListView = (ListView) findViewById(R.id.listView);
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    protected void onStart() {
        super.onStart();

        // 액티비티로 들어올 때마다, 머신 목록을 다시 읽어온다.
        // 읽어왔을 때의 처리는 GetMachinesInterface 구현에서 한다.
        DataContainer.getInstance().requestMachines(
                MachinesActivity.this,
                new GetMachinesInterface()
        );
    }


    @Override
    public void onBackPressed() {

        String alertTitle = getResources().getString(R.string.app_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(alertTitle);
        builder.setMessage("정말 종료하시겠습니까?");
        builder.setNegativeButton("아니오", null);
        builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                moveTaskToBack(true);
                finish();
            }
        });

        builder.show();

    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Machine machine = mAdapter.getItem(arg2);

        // 그럴 일은 없어보이지만, 혹시라도 잘못된 머신 선택인지 체크한다.
        if (false == DataContainer.getInstance().setSelectedMachine(machine)) {
            Toast.makeText(this, getString(R.string.toast_invalid_selection), Toast.LENGTH_SHORT).show();
            return;
        }
        else
            Toast.makeText(this, machine.getName(), Toast.LENGTH_SHORT).show();

        // 머신 별 API Calls 화면으로 이동한다.
        Intent intent = new Intent(getApplicationContext(), DailyApiCallActivity.class);
        intent.putExtra("dataContainer", DataContainer.getInstance());
        intent.putExtra("targetMachine", DataContainer.getInstance().getSelectedMachine());

        startActivity(intent);
    }

    public class GetMachinesInterface implements DataContainer.IResponseInterface {

        ProgressDialog dialog = null;

        @Override
        public void onStart() {
            dialog = new ProgressDialog(MachinesActivity.this);
            dialog.setMessage(getString(R.string.dlgtext_waiting));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        public void onFailure(int stateCode, Header[] header, byte[] body, Throwable error) {
//            String errMsg = "State Code :" + stateCode + "\n";
//            errMsg += "Error Message :" + error.getMessage();
//            Toast.makeText(MachinesActivity.this, errMsg, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), NetworkUnavailableActivity.class);
            intent.putExtra("context", "machines");
            intent.putExtra("errorCode", stateCode);
            intent.putExtra("description", error.getMessage());
            startActivity(intent);
        }

        @Override
        public void onSuccess(int stateCode, Header[] header, byte[] body) {
            mAdapter = new MachinesAdapter(
                    MachinesActivity.this, R.layout.list_row,
                DataContainer.getInstance().getMachines()
            );
            mListView = (ListView) findViewById(R.id.listView);
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(MachinesActivity.this);
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
