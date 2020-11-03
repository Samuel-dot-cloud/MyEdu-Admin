package com.studiofive.myedu_admin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.studiofive.myedu_admin.Classes.Question;
import com.studiofive.myedu_admin.R;
import com.studiofive.myedu_admin.adapters.QuestionsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

import static com.studiofive.myedu_admin.activities.CategoryActivity.categoryList;
import static com.studiofive.myedu_admin.activities.CategoryActivity.selected_category_index;
import static com.studiofive.myedu_admin.activities.SetsActivity.selected_set_index;
import static com.studiofive.myedu_admin.activities.SetsActivity.setsIDs;

public class QuestionsActivity extends AppCompatActivity {
    @BindView(R.id.questions_toolbar)
    Toolbar toolbar;
    @BindView(R.id.questions_recyclerview)
    RecyclerView questionsRecyclerview;
    @BindView(R.id.addQuestionButton)
    Button questionButton;

    public static List<Question> questionsList = new ArrayList<>();
    private QuestionsAdapter questionsAdapter;
    private FirebaseFirestore mFirestore;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Questions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialog = new Dialog(QuestionsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        questionsRecyclerview.setLayoutManager(layoutManager);

        mFirestore = FirebaseFirestore.getInstance();

        loadQuestions();
    }

    private void loadQuestions() {
        questionsList.clear();
        loadingDialog.show();

        mFirestore.collection("PreQuiz").document(categoryList.get(selected_category_index).getId())
                .collection(setsIDs.get(selected_set_index)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            docList.put(doc.getId(), doc);
                        }

                        QueryDocumentSnapshot questionListDoc = docList.get("Questions_List");

                        String count = questionListDoc.getString("Count");

                        for (int i = 0; i < Integer.valueOf(count); i++) {
                            String questionID = questionListDoc.getString("Question" + String.valueOf(i + 1) + "_ID");

                            QueryDocumentSnapshot quesDoc = docList.get(questionID);

                            questionsList.add(new Question(
                                    questionID,
                                    quesDoc.getString("Question"),
                                    quesDoc.getString("A"),
                                    quesDoc.getString("B"),
                                    quesDoc.getString("C"),
                                    quesDoc.getString("D"),
                                    Integer.valueOf(quesDoc.getString("Answer"))
                            ));

                        }
                        questionsAdapter = new QuestionsAdapter(questionsList);
                        questionsRecyclerview.setAdapter(questionsAdapter);

                        loadingDialog.dismiss();

                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toasty.error(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT, true).show();
                    }
                });
    }
}