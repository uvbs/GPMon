package com.jinsung.adoda.gpmon.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import com.jinsung.adoda.gpmon.MainActivity;
import com.jinsung.adoda.gpmon.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by JinSung on 2015-12-27.
 */
public class DataContainer implements Serializable {

    private static DataContainer sDataContainer = new DataContainer();

    // context data
    private Machine mSelectedMachine = null;

    // all data
    private ArrayList<Machine> mMachines = new ArrayList<Machine>();

    // web client
    private transient AsyncHttpClient mClient = new AsyncHttpClient();


    public static DataContainer getInstance() {
        return sDataContainer;
    }

    public boolean requestMachines(
        Context ctx,
        IResponseInterface responseInterface
    ) {
        RequestHandle handle = mClient.get(
            ctx.getString(R.string.url_machines),
            new GetMachinesResponse(
                ctx, responseInterface
            )
        );
        if (null == handle)
            return false;

        return true;
    }

    public ArrayList<Machine> getMachines() {
        return mMachines;
    }

    public boolean setSelectedMachine(Machine machine) {
        if (-1 == mMachines.indexOf(machine))
            return false;

        mSelectedMachine = machine;
        return true;
    }

    public Machine getSelectedMachine() {
        return mSelectedMachine;
    }

    public interface IResponseInterface {
        public void onStart();

        public void onFailure(int stateCode, Header[] header, byte[] body, Throwable error);

        public void onSuccess(int stateCode, Header[] header, byte[] body);

        public void onFinish();
    }

    public class GetMachinesResponse extends AsyncHttpResponseHandler {

        private Context mCtx;
        private IResponseInterface mRespIFace;



        private GetMachinesResponse(Context ctx, IResponseInterface respIFace) {
            mCtx = ctx;
            mRespIFace = respIFace;
        }

        @Override
        public void onStart() {
            if (null != mRespIFace)
                mRespIFace.onStart();
        }

        @Override
        public void onFailure(int stateCode, Header[] header, byte[] body, Throwable error) {
            if (null != mRespIFace)
                mRespIFace.onFailure(stateCode, header, body, error);
        }

        @Override
        public void onSuccess(int stateCode, Header[] header, byte[] body) {
            mMachines.clear();
            try {
                //통신 결과를 문자열로 변환한다.
                String response = new String(body, "UTF-8");

                //문자열을 JSONArray로 변환한다.
                JSONArray jsonArray = new JSONArray(response);

                for (int i = 0; i < jsonArray.length(); i++) {
                    mMachines.add(new Machine(jsonArray.getString(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (null != mRespIFace)
                mRespIFace.onSuccess(stateCode, header, body);
        }

        // 성공, 실패 여부에 상관 없이 통신이 종료되면 실행.
        @Override
        public void onFinish() {
            if (null != mRespIFace)
                mRespIFace.onFinish();
        }
    }
}
