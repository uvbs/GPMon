package com.jinsung.adoda.gpmon.data;

import android.content.Context;
import android.util.Log;

import com.jinsung.adoda.gpmon.R;
import com.jinsung.adoda.gpmon.utils.DateUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by JinSung on 2015-12-27.
 */
public class DataContainer implements Serializable {

    /***********************************************************************************************
     * enumerations
     **********************************************************************************************/
    public enum State {
        API_CALLS,
        SLOW_QUERIES,
    }

    /***********************************************************************************************
     * private member variables
     **********************************************************************************************/
    private static DataContainer sDataContainer = new DataContainer();

    /* context data */

    private Machine mSelectedMachine = null;
    private State mState = State.API_CALLS;
    private String mSelectedDate = null;

    /* all data */

    // machine
    private boolean            mIsLoadedMachines = false;
    private ArrayList<Machine> mMachines = new ArrayList<Machine>();

    // api calls
    private boolean                         mIsLoadedApiCalls = false;
    private ArrayList<String>               mAllApis = new ArrayList<String>();
    private ArrayList<String>               mApiCallsDates = new ArrayList<String>();
    private HashMap<String, DailyApiCalls>  mApiCalls = new HashMap<String, DailyApiCalls>();

    // slow queries
    // ...

    /* web client object */
    private transient AsyncHttpClient mClient = new AsyncHttpClient();

    /***********************************************************************************************
     * interface for web client response
     **********************************************************************************************/
    public interface IResponseInterface {
        void onStart();
        void onFailure(int stateCode, Header[] header, byte[] body, Throwable error);
        void onSuccess(int stateCode, Header[] header, byte[] body);
        void onFinish();
    }

    /***********************************************************************************************
     * public methods
     **********************************************************************************************/
    public static DataContainer getInstance() {
        return sDataContainer;
    }

    /* for machines */

    public boolean requestMachines(Context ctx, IResponseInterface responseInterface) {
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

    public boolean isLoadedMachines () { return mIsLoadedMachines; }

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

    /* for current view state */

    public void setState(State newState) {
        mState = newState;
    }

    public State getCurrentState() {
        return mState;
    }

    /* for apis */

    public boolean requestAllApis(Context ctx, IResponseInterface responseInterface) {
        RequestHandle handle = mClient.get(
            ctx.getString(R.string.url_apis),
            new GetAllApisResponse(
                ctx, responseInterface
            )
        );
        if (null == handle)
            return false;

        return true;
    }

    public boolean requestApiCalls(Context ctx, IResponseInterface responseInterface) {
        RequestHandle handle = mClient.get(
            String.format(ctx.getString(R.string.url_api_calls), mSelectedMachine.getName()),
            new GetApiCallsResponse(
                ctx, responseInterface
            )
        );

        if (null == handle)
            return false;

        return true;
    }

    public boolean ismIsLoadedApiCalls () { return mIsLoadedApiCalls; }

    public String getSelectedDate() {
        if (null == mSelectedDate)
            mSelectedDate = DateUtil.getToday();

        return mSelectedDate;
    }

    public void setSelectedDate(String newDate) {
        if (false == DateUtil.isValidDateStr(newDate))
            mSelectedDate = DateUtil.getToday();
        else
            mSelectedDate = newDate;
    }

    public ArrayList<String> GetAllApis() {
        return mAllApis;
    }

    public HashMap<String, DailyApiCalls> getApiCalls() {
        return mApiCalls;
    }

    public DailyApiCalls getDailyApiCalls() {
        return mApiCalls.get(getSelectedDate());
    }

    public DailyApiCalls getDailyApiCalls (String date) {
        return mApiCalls.get(date);
    }

    /***********************************************************************************************
     * private methods
     **********************************************************************************************/


    /***********************************************************************************************
     * private common response handler for "request*"
     **********************************************************************************************/
    public class CommonResponseHandler extends AsyncHttpResponseHandler {

        protected Context mCtx;
        protected IResponseInterface mIface;

        CommonResponseHandler (Context ctx, IResponseInterface iface) {
            mCtx = ctx;
            mIface = iface;
        }

        @Override
        public void onStart() {
            if (null != mIface)
                mIface.onStart();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            if (null != mIface)
                mIface.onSuccess(statusCode, headers, responseBody);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            if (null != mIface)
                mIface.onFailure(statusCode, headers, responseBody, error);
        }

        // 성공, 실패 여부에 상관 없이 통신이 종료되면 실행.
        @Override
        public void onFinish() {
            if (null != mIface)
                mIface.onFinish();
        }
    }

    /***********************************************************************************************
     * private response handler for "requestMachines"
     **********************************************************************************************/
    public class GetMachinesResponse extends CommonResponseHandler {

        GetMachinesResponse(Context ctx, IResponseInterface iface) {
            super(ctx, iface);
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

            mIsLoadedMachines = true;

            super.onSuccess(stateCode, header, body);
        }
    }

    /***********************************************************************************************
     * private response handler for "requestAllApis"
     **********************************************************************************************/
    public class GetAllApisResponse extends CommonResponseHandler {

        private GetAllApisResponse(Context ctx, IResponseInterface iface) {
            super(ctx, iface);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] body) {
            try {
                //통신 결과를 문자열로 변환한다.
                String response = new String(body, "UTF-8");

                //문자열을 JSONArray로 변환한다.
                JSONArray jsonArray = new JSONArray(response);

                for (int i = 0; i < jsonArray.length(); i++) {
                    String jsonData = (String)jsonArray.get(i);

                    if (!mAllApis.contains(jsonData))
                        mAllApis.add(jsonData);
                }
                Collections.sort(mAllApis);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            super.onSuccess(statusCode, headers, body);
        }
    }

    /***********************************************************************************************
     * private response handler for "requestApiCalls"
     **********************************************************************************************/
    public class GetApiCallsResponse extends CommonResponseHandler {

        GetApiCallsResponse(Context ctx, IResponseInterface iface) {
            super(ctx, iface);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] body) {
            try {
                //통신 결과를 문자열로 변환한다.
                String response = new String(body, "UTF-8");
                Log.d("ApiCalls response", response);
                //문자열을 JSONArray로 변환한다.
                JSONArray jsonArray = new JSONArray(response);

                DailyApiCalls dailyApiCalls;
                ArrayList apiInfolist;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    dailyApiCalls = new DailyApiCalls();

                    String date = obj.getString("date");
                    JSONArray results = obj.getJSONArray("results");
                    for (int j = 0; j < results.length(); j++) {
                        JSONObject result = results.getJSONObject(j);

                        int hour = result.getInt("time");
                        JSONArray apis = result.getJSONArray("apis");
                        apiInfolist = new ArrayList();

                        for (int k = 0; k < apis.length(); k++) {
                            JSONObject api = apis.getJSONObject(k);

                            if (api.isNull("name") || api.isNull("count"))
                                continue;
                            String apiName = api.getString("name");
                            int requestCount = api.getInt("count");

                            apiInfolist.add(new ApiInfo(apiName, requestCount));

                            if (!mAllApis.contains(apiName))
                                mAllApis.add(apiName);
                        }

                        dailyApiCalls.addItem(hour, apiInfolist);
                    }

                    if (results.length() > 0 && DateUtil.isValidDateStr(date))
                        mApiCallsDates.add(date);

                    mApiCalls.put(date, dailyApiCalls);
                }

                Collections.sort(mApiCallsDates);
                Collections.sort(mAllApis);

                if (null == mSelectedDate) {
                    if (mApiCallsDates.isEmpty())
                        mSelectedDate = DateUtil.getToday();
                    else
                        mSelectedDate = mApiCallsDates.get(mApiCallsDates.size()-1);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mIsLoadedApiCalls = true;

            super.onSuccess(statusCode, headers, body);
        }
    }
}
