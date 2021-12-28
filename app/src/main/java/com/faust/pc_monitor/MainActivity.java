package com.faust.pc_monitor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.eazegraph.lib.charts.ValueLineChart;
import org.eazegraph.lib.models.ValueLinePoint;
import org.eazegraph.lib.models.ValueLineSeries;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {


    OkHttpClient http_client = new OkHttpClient();
    CircularProgressBar CPUProgressBar;
    CircularProgressBar RAMProgressBar;
    TextView CPU_text;
    TextView RAM_text;

    ValueLineChart CPU_graph;
    ValueLineSeries series = new ValueLineSeries();
    List<ValueLinePoint> CPU_graph_points = new ArrayList<ValueLinePoint>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View decorView = this.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        CPUProgressBar = findViewById(R.id.CPUProgressBar);
        CPUProgressBar.setProgress(0.f);
        CPUProgressBar.setProgressMax(100f);
        CPUProgressBar.setBackgroundProgressBarColor(Color.GRAY);
        CPUProgressBar.setProgressBarWidth(7f);
        CPUProgressBar.setBackgroundProgressBarWidth(3f);

        RAMProgressBar = findViewById(R.id.RAMProgressBar);
        RAMProgressBar.setProgress(0.f);
        RAMProgressBar.setProgressMax(100f);
        RAMProgressBar.setBackgroundProgressBarColor(Color.GRAY);
        RAMProgressBar.setProgressBarWidth(7f);
        RAMProgressBar.setBackgroundProgressBarWidth(3f);

        CPU_text = findViewById(R.id.CPU_text);
        RAM_text = findViewById(R.id.RAM_text);

        CPU_graph = (ValueLineChart) findViewById(R.id.CPU_graph);

        CPU_graph.setUseDynamicScaling(false);
        CPU_graph.setY(100.0f);
        series.setColor(0xFF56B7F1);


        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(500);
                        do_GET("http://192.168.0.103:8080");
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }



    void do_GET(String url) throws IOException{
        Request request = new Request.Builder()
                .url(url)
                .build();

        http_client.newCall(request)
                .enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, IOException e) {
                        // Error

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String res = response.body().string();
                        try {
                            JSONObject json_answer = new JSONObject(res);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        CPUProgressBar.setProgressWithAnimation((float)json_answer.getDouble("CPU"), (long)500);
                                        RAMProgressBar.setProgressWithAnimation((float)json_answer.getDouble("RAM"), (long)500);
                                        CPU_text.setText("CPU: " + json_answer.getDouble("CPU") + "%");
                                        RAM_text.setText("RAM: " + json_answer.getDouble("RAM") + "%");

                                        while (CPU_graph_points.size() > 100)
                                        {
                                            CPU_graph_points.remove(0);
                                        }

                                        CPU_graph_points.add(new ValueLinePoint("", (float)json_answer.getDouble("CPU")));

                                        series.setSeries(CPU_graph_points);

                                        CPU_graph.clearChart();
                                        CPU_graph.addSeries(series);

                                        //CPU_graph.startAnimation();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.i("TAG", res);
                    }
                });
    }
}