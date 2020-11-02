
package com.example.myapplication.ui.notifications;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.myapplication.GlobalData;
import com.example.myapplication.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.example.myapplication.R.layout.fragment_notifications;

public class NotificationsFragment extends Fragment implements
        OnChartValueSelectedListener {

    private PieChart chart;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View root = inflater.inflate(fragment_notifications, container, false);


        chart_init(root);
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                response();
            }
        }.start();
        return root;
    }

    private void chart_init(View root) {
        chart = root.findViewById(R.id.chart2);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setCenterText(generateCenterSpannableText());
        chart.setExtraOffsets(20.f, 0.f, 20.f, 0.f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(true);
        chart.setRotationAngle(0);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        // add a selection listener
        chart.setOnChartValueSelectedListener(this);

        chart.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);


        //更改格式
        boolean toSet = !chart.isDrawRoundedSlicesEnabled() || !chart.isDrawHoleEnabled();
        chart.setDrawRoundedSlices(toSet);
        if (toSet && !chart.isDrawHoleEnabled()) {
            chart.setDrawHoleEnabled(true);
        }
        if (toSet && chart.isDrawSlicesUnderHoleEnabled()) {
            chart.setDrawSlicesUnderHole(false);
        }
        //使用实际值
        chart.setUsePercentValues(!chart.isUsePercentValuesEnabled());
        chart.invalidate();

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onStart() {

        super.onStart();
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                response();
            }
        }.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void response() {
        try {
            //发送
            String host = "192.168.1.14";
            int port = 9999;
            final Socket socket = new Socket(host, port);
            final OutputStream outputStream = socket.getOutputStream();


            GlobalData globalData = new GlobalData();
            String phoneNumber = globalData.getPhoneNumber();
            try {

                //头文件处理

                //电话唯一识别码
                StringBuilder number_string = new StringBuilder(phoneNumber);
                while (number_string.length() < 15) {
                    number_string.append(" ");
                }
                byte[] number_string_bytes = number_string.toString().getBytes();
                outputStream.write(number_string_bytes);
                outputStream.flush();

                //flag传递
                String flag_str = "1";
                byte[] flag_string_bytes = flag_str.getBytes();
                outputStream.write(flag_string_bytes);
                outputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

            //response
            InputStream result = null;
            try {

                result = socket.getInputStream();
                byte[] temp = new byte[1024];
                int temp_size = result.read(temp);
                String response = utfToString(temp, temp_size);
                response.trim();
                setData(response);
                result.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String utfToString(byte[] data, int size) {
        return new String(data, 0, size, StandardCharsets.UTF_8);

    }


    private void setData(String result) {
        String[] strs = result.split(",");
        String lab = "left,center,right,alert";
        String[] labs = lab.split(",");
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(Integer.parseInt(strs[3]), labs[3]));

        PieDataSet dataSet = new PieDataSet(entries, "Election Results");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.2f);
        dataSet.setValueLinePart2Length(0.4f);

        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);
        chart.setData(data);
        chart.highlightValues(null);
        chart.invalidate();
    }

    private SpannableString generateCenterSpannableText() {

        SpannableString s = new SpannableString("EyeTrack\ndeveloped by charlie Wang");
        s.setSpan(new RelativeSizeSpan(3.5f), 0, 8, 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), 8, s.length() - 13, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), 8, s.length() - 13, 0);
        s.setSpan(new RelativeSizeSpan(.65f), 8, s.length() - 13, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 13, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 13, s.length(), 0);
        return s;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", xIndex: " + e.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }

}
