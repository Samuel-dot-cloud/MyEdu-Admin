package com.studiofive.myedu_admin.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.studiofive.myedu_admin.R;
import com.studiofive.myedu_admin.adapters.SetsAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetsActivity extends AppCompatActivity {
    @BindView(R.id.setsToolbar)
    Toolbar toolbar;
    @BindView(R.id.setsRecyclerView)
    RecyclerView setRecyclerview;
    @BindView(R.id.addSetButton)
    Button setButton;

    public static List<String> setsIDs = new ArrayList<>();
    private SetsAdapter setsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sets");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setRecyclerview.setLayoutManager(layoutManager);
        
        loadSets();
    }

    private void loadSets() {
        setsIDs.clear();
        setsIDs.add("A");
        setsIDs.add("B");
        setsIDs.add("C");
        setsIDs.add("D");

        setsAdapter = new SetsAdapter(setsIDs);
        setRecyclerview.setAdapter(setsAdapter);

    }
}