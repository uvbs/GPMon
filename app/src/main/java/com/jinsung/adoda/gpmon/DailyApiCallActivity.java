package com.jinsung.adoda.gpmon;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jinsung.adoda.gpmon.data.DailyApiCalls;
import com.jinsung.adoda.gpmon.data.DataContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import cz.msebera.android.httpclient.Header;

public class DailyApiCallActivity extends FragmentActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_viewpager);

        getActionBar().setTitle(
                DataContainer.getInstance().getSelectedMachine().getName()
        );

        // ViewPager의 Fragment 전환 애니메이션 설정.
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());

    }

    @Override
    protected void onResume() {

        super.onResume();


    }

    @Override
    protected void onStart() {
        super.onStart();

        // Slow Queries 구현은 여기서 한다.
        // TODO Slow queries 구현을 하고 싶다면, 손은 꽤 많이 가겠지만,
        //   이 코드를 참고해서 API Calls를 보여줄지, Slow queries를 보여줄지에 따라 해당하는 데이터를 긁어와주면 된다.
        //   어느 데이터를 보여줄지에 대한 메뉴 선택은 컨텍스트 메뉴로 하면 편할 듯 하다.
        DataContainer.State curState = DataContainer.getInstance().getCurrentState();
        if (DataContainer.State.API_CALLS == curState) {
            // request total api list
            DataContainer.getInstance().requestAllApis(DailyApiCallActivity.this, new GetAllApisResponse());
        }
        else if (DataContainer.State.SLOW_QUERIES == curState) {
            // TODO
        }
    }

    // 일별 API Calls 데이터 차트를 그리는 역할은 여기서 한다.
    public class DailyApiCallsPageFragment extends Fragment implements OnChartValueSelectedListener, View.OnClickListener {

        protected TextView mDateTextView;
        protected HorizontalBarChart mChart;

        protected String mDate;
        protected String mSelectedApiName;

        DailyApiCallsPageFragment(String date) {
            super();
            mDate = date;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            ViewGroup rootView = (ViewGroup)inflater.inflate(
                R.layout.activity_daily_api_call, container, false
            );

            createChart(rootView);

            return rootView;
        }

        private void createChart (ViewGroup container) {
            mChart = (HorizontalBarChart) container.findViewById(R.id.chart1);
            mChart.setOnChartValueSelectedListener(this);
            mChart.setOnClickListener(this);

            mChart.setDrawGridBackground(false);
            mChart.setDrawBarShadow(false);
            mChart.setDrawValueAboveBar(true);
            mChart.setDescription("");

            // if more than 60 entries are displayed in the chart, no values will be drawn
            mChart.setMaxVisibleValueCount(50);
            // scaling can now only be done on x- and y-axis separately
            mChart.setPinchZoom(false);
            mChart.setScaleEnabled(false);

            setChartData(container);

            XAxis xl = mChart.getXAxis();
            xl.setPosition(XAxisPosition.BOTTOM);
            xl.setTypeface(Typeface.DEFAULT);
            xl.setDrawAxisLine(true);
            xl.setDrawGridLines(true);
            xl.setGridLineWidth(0.3f);

            YAxis yl = mChart.getAxisLeft();
            yl.setEnabled(false);

            YAxis yr = mChart.getAxisRight();
            yr.setTypeface(Typeface.DEFAULT);
            yr.setDrawAxisLine(true);
            yr.setDrawGridLines(true);
            yr.setLabelCount(3, false);

            mChart.animateY(2500);


            Legend l = mChart.getLegend();
            l.setPosition(LegendPosition.BELOW_CHART_LEFT);
            l.setFormSize(8f);
            l.setXEntrySpace(4f);
        }

        private void setChartData(ViewGroup viewGroup) {
            //String key = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            mDateTextView = (TextView) viewGroup.findViewById(R.id.date);
            mDateTextView.setText(mDate);

            ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
            ArrayList<String> xVals = new ArrayList<String>();

            DailyApiCalls temp = DataContainer.getInstance().getDailyApiCalls(mDate);
            ArrayList<String> allApis = DataContainer.getInstance().GetAllApis();
            for (int i = 0; i < allApis.size(); i++) {
                String apiName = allApis.get(i);
                xVals.add(apiName);
                yVals.add(new BarEntry(temp.getTotalCount(apiName), i));
            }

            BarDataSet set1 = new BarDataSet(yVals, "Total Api Request Count");

            ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(xVals, dataSets);
            data.setValueTextSize(10f);
            data.setValueTypeface(Typeface.DEFAULT);

            mChart.setData(data);
        }

        @Override
        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
            if (e == null)
                return;

            RectF bounds = mChart.getBarBounds((BarEntry) e);
            PointF position = mChart.getPosition(e, mChart.getData().getDataSetByIndex(dataSetIndex)
                    .getAxisDependency());

            Log.i("bounds", bounds.toString());
            Log.i("position", position.toString());

            BarEntry barEntry = (BarEntry)e;
            mSelectedApiName = mChart.getXValue(barEntry.getXIndex());
        }

        @Override
        public void onNothingSelected() { }

        @Override
        public void onClick(View v) {
            // onValueSelected는 민감도가 너무 높아서 onClick에서 액티비티 전환 처리를 한다.
            if (mChart.equals(v)) {

                if (null != mSelectedApiName) {
                    DataContainer.getInstance().setSelectedDate(mDateTextView.getText().toString());
                    Intent intent = new Intent(getApplicationContext(), HourlyApiCallsActivity.class);
                    intent.putExtra("data", DataContainer.getInstance().getApiCalls());
                    intent.putExtra("apiName", mSelectedApiName);

                    startActivity(intent);
                }
            }
        }
    }

    // 일별 Fragment를 생성해준다.
    // 현재는 데이터가 별로 없어서 데이터의 수만큼만 count로 잡지만,
    // 무한히 스크롤이 가능하게 하고 싶다면
    //   getCount에서 Integer.MAX_VALUE를 리턴하고 getItem에서 적절한 처리를 해주면 된다.
    //   참고 : http://arabiannight.tistory.com/entry/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9CAndorid-Viewpager-%EC%82%AC%EC%9A%A9-%ED%95%98%EA%B8%B0
    //   참고 : http://www.androidpub.com/2452586
    private class ApiCallsPagerAdapter extends FragmentStatePagerAdapter {

        public ApiCallsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Set<String> keySet = DataContainer.getInstance().getApiCalls().keySet();
            ArrayList<String> dates = new ArrayList<String>(keySet);
            Collections.sort(dates);

            return new DailyApiCallsPageFragment(dates.get(position));
        }

        @Override
        public int getCount() {
            return DataContainer.getInstance().getApiCalls().size();
        }
    }

    // 전체 api 목록을 얻은 이후의 UI 처리는 여기서 한다.
    public class GetAllApisResponse implements DataContainer.IResponseInterface {

        ProgressDialog dialog;

        @Override
        public void onStart() {
            dialog = new ProgressDialog(DailyApiCallActivity.this);
            dialog.setMessage(getString(R.string.dlgtext_waiting));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        public void onFailure(int stateCode, Header[] header, byte[] body, Throwable error) {
//            String errMsg = "State Code :" + stateCode + "\n";
//            errMsg += "Error Message :" + error.getMessage();
//            Toast.makeText(DailyApiCallActivity.this, errMsg, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), NetworkUnavailableActivity.class);
            intent.putExtra("context", "allapis");
            intent.putExtra("errorCode", stateCode);
            intent.putExtra("description", error.getMessage());
            startActivity(intent);
        }

        @Override
        public void onSuccess(int stateCode, Header[] header, byte[] body) {
            DataContainer.getInstance().requestApiCalls(
                    DailyApiCallActivity.this,
                    new GetApiCallsResponse()
            );
        }

        @Override
        public void onFinish() {
            dialog.dismiss();
            dialog = null;
        }
    }

    // api calls 통계 데이터 획득 이후의 UI 처리는 여기서 한다.
    public class GetApiCallsResponse implements  DataContainer.IResponseInterface {

        ProgressDialog dialog;

        @Override
        public void onStart() {
            dialog = new ProgressDialog(DailyApiCallActivity.this);
            dialog.setMessage(getString(R.string.dlgtext_waiting));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        public void onFailure(int stateCode, Header[] header, byte[] body, Throwable error) {
//            String errMsg = "State Code :" + stateCode + "\n";
//            errMsg += "Error Message :" + error.getMessage();
//            Toast.makeText(DailyApiCallActivity.this, errMsg, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), NetworkUnavailableActivity.class);
            intent.putExtra("context", "apicalls");
            intent.putExtra("errorCode", stateCode);
            intent.putExtra("description", error.getMessage());
            startActivity(intent);
        }

        @Override
        public void onSuccess(int stateCode, Header[] header, byte[] body) {
            mViewPager.setAdapter(new ApiCallsPagerAdapter(getSupportFragmentManager()));

            Set<String> keySet = DataContainer.getInstance().getApiCalls().keySet();
            ArrayList<String> dates = new ArrayList<String>(keySet);
            Collections.sort(dates);

            String selectedDate = DataContainer.getInstance().getSelectedDate();

            mViewPager.setCurrentItem(dates.indexOf(selectedDate));
        }

        @Override
        public void onFinish() {
            dialog.dismiss();
            dialog = null;
        }
    }

    // 화면 전환 효과를 위한 클래스.
    // 복 to the 붙 from http://developer.android.com/intl/ko/training/animation/screen-slide.html
    public class ZoomOutPageTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }

}
