package com.studiofive.myedu_admin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.studiofive.myedu_admin.Classes.Question;
import com.studiofive.myedu_admin.R;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

import static com.studiofive.myedu_admin.activities.CategoryActivity.categoryList;
import static com.studiofive.myedu_admin.activities.CategoryActivity.selected_category_index;
import static com.studiofive.myedu_admin.activities.QuestionsActivity.questionsList;
import static com.studiofive.myedu_admin.activities.SetsActivity.selected_set_index;
import static com.studiofive.myedu_admin.activities.SetsActivity.setsIDs;

public class QuestionDetailsActivity extends AppCompatActivity {
    @BindView(R.id.question)
    EditText question;
    @BindView(R.id.optionA)
    EditText optionA;
    @BindView(R.id.optionB)
    EditText optionB;
    @BindView(R.id.optionC)
    EditText optionC;
    @BindView(R.id.optionD)
    EditText optionD;
    @BindView(R.id.addQuestionButton1)
    Button questionButton;
    @BindView(R.id.answer)
    EditText answer;
    @BindView(R.id.details_toolbar)
    Toolbar toolbar;

    private String qStr, aStr, bStr, cStr, dStr, ansStr;
    private Dialog loadingDialog;
    private FirebaseFirestore mFirestore;
    private String action;
    private int qID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_details);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Question " + String.valueOf(questionsList.size() + 1));

        loadingDialog = new Dialog(QuestionDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mFirestore = FirebaseFirestore.getInstance();

        action = getIntent().getStringExtra("ACTION");

        if(action.compareTo("EDIT") == 0)
        {
            qID = getIntent().getIntExtra("Q_ID",0);
            loadData(qID);
            getSupportActionBar().setTitle("Question " + String.valueOf(qID + 1));
            questionButton.setText("UPDATE");
        }
        else
        {
            getSupportActionBar().setTitle("Question " + String.valueOf(questionsList.size() + 1));
            questionButton.setText("ADD");
        }

        questionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qStr = question.getText().toString();
                aStr = optionA.getText().toString();
                bStr = optionB.getText().toString();
                cStr = optionC.getText().toString();
                dStr = optionD.getText().toString();
                ansStr = answer.getText().toString();

                if (qStr.isEmpty()){
                    question.setError("Enter Question");
                    return;
                }

                if (aStr.isEmpty()){
                    optionA.setError("Enter Option A");
                    return;
                }

                if (bStr.isEmpty()){
                    optionB.setError("Enter Option B");
                    return;
                }

                if (cStr.isEmpty()){
                    optionC.setError("Enter Option C");
                    return;
                }

                if (dStr.isEmpty()){
                    optionD.setError("Enter Option D");
                    return;
                }

                if (ansStr.isEmpty()){
                    answer.setError("Enter Answer");
                    return;
                }

                if(action.compareTo("EDIT") == 0)
                {
                    editQuestion();
                }else {
                    addNewQuestion();
                }
            }
        });
    }

    private void addNewQuestion() {
        loadingDialog.show();

        Map<String,Object> questionData = new ArrayMap<>();

        questionData.put("Question",qStr);
        questionData.put("A",aStr);
        questionData.put("B",bStr);
        questionData.put("C",cStr);
        questionData.put("D",dStr);
        questionData.put("Answer",ansStr);


        final String doc_id = mFirestore.collection("PreQuiz").document(categoryList.get(selected_category_index).getId())
                .collection(setsIDs.get(selected_set_index)).document().getId();

        mFirestore.collection("PreQuiz").document(categoryList.get(selected_category_index).getId())
                .collection(setsIDs.get(selected_set_index)).document(doc_id)
                .set(questionData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Map<String,Object> questionDoc = new ArrayMap<>();
                        questionDoc.put("Question" + String.valueOf(questionsList.size() + 1) + "_ID", doc_id);
                        questionDoc.put("Count",String.valueOf(questionsList.size() + 1));

                        mFirestore.collection("PreQuiz").document(categoryList.get(selected_category_index).getId())
                                .collection(setsIDs.get(selected_set_index)).document("Questions_List")
                                .update(questionDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toasty.success(QuestionDetailsActivity.this, " Question Added Successfully", Toast.LENGTH_SHORT, true).show();

                                        questionsList.add(new Question(
                                                doc_id,
                                                qStr,
                                                aStr,
                                                bStr,
                                                cStr,
                                                dStr,
                                                Integer.valueOf(ansStr)
                                        ));

                                        loadingDialog.dismiss();
                                        QuestionDetailsActivity.this.finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingDialog.dismiss();
                                        Toasty.error(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT, true).show();
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toasty.error(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT, true).show();
                    }
                });

    }

    private void loadData(int id) {
        question.setText(questionsList.get(id).getQuestion());
        optionA.setText(questionsList.get(id).getOptionA());
        optionB.setText(questionsList.get(id).getOptionB());
        optionC.setText(questionsList.get(id).getOptionC());
        optionD.setText(questionsList.get(id).getOptionD());
        answer.setText(String.valueOf(questionsList.get(id).getCorrectAns()));
    }

    private void editQuestion() {
        loadingDialog.show();

        Map<String,Object> quesData = new ArrayMap<>();
        quesData.put("Question", qStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("Answer",ansStr);


        mFirestore.collection("PreQuiz").document(categoryList.get(selected_category_index).getId())
                .collection(setsIDs.get(selected_set_index)).document(questionsList.get(qID).getQuestionID())
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toasty.success(QuestionDetailsActivity.this,"Question updated successfully",Toast.LENGTH_SHORT, true).show();

                        questionsList.get(qID).setQuestion(qStr);
                        questionsList.get(qID).setOptionA(aStr);
                        questionsList.get(qID).setOptionB(bStr);
                        questionsList.get(qID).setOptionC(cStr);
                        questionsList.get(qID).setOptionD(dStr);
                        questionsList.get(qID).setCorrectAns(Integer.valueOf(ansStr));

                        loadingDialog.dismiss();
                        QuestionDetailsActivity.this.finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toasty.error(QuestionDetailsActivity.this,e.getMessage(),Toast.LENGTH_SHORT, true).show();
                    }
                });

    }
}