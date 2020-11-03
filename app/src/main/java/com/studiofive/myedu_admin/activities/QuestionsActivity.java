package com.studiofive.myedu_admin.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;

import com.studiofive.myedu_admin.Classes.Question;
import com.studiofive.myedu_admin.R;
import com.studiofive.myedu_admin.adapters.QuestionsAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuestionsActivity extends AppCompatActivity {
    @BindView(R.id.questions_toolbar)
    Toolbar toolbar;
    @BindView(R.id.questions_recyclerview)
    RecyclerView questionsRecyclerview;
    @BindView(R.id.addQuestionButton)
    Button questionButton;

    private List<Question> questionsList;
    private QuestionsAdapter questionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        questionsRecyclerview.setLayoutManager(layoutManager);
        
        loadQuestions();
    }

    private void loadQuestions() {
        questionsList.clear();
        questionsList.add(new Question("1", "Who I'm I (1)?", "Samuel Wahome", "B", "C", "D",1));
        questionsList.add(new Question("2", "Who I'm I (2)?", "Samuel Wahome", "B", "C", "D",1));
        questionsList.add(new Question("3", "Who I'm I (3)?", "Samuel Wahome", "B", "C", "D",1));

        questionsAdapter = new QuestionsAdapter(questionsList);
        questionsRecyclerview.setAdapter(questionsAdapter);
    }
}