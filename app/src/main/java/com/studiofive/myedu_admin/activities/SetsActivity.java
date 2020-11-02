package com.studiofive.myedu_admin.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.studiofive.myedu_admin.R;
import com.studiofive.myedu_admin.adapters.SetsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

import static com.studiofive.myedu_admin.activities.CategoryActivity.categoryList;
import static com.studiofive.myedu_admin.activities.CategoryActivity.selected_category_index;

public class SetsActivity extends AppCompatActivity {
    @BindView(R.id.setsToolbar)
    Toolbar toolbar;
    @BindView(R.id.setsRecyclerView)
    RecyclerView setRecyclerview;
    @BindView(R.id.addSetButton)
    Button setButton;

    public static List<String> setsIDs = new ArrayList<>();
    private SetsAdapter setsAdapter;
    private FirebaseFirestore mFireStore;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sets");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadingDialog = new Dialog(SetsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewSet();
            }
        });

        mFireStore = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setRecyclerview.setLayoutManager(layoutManager);
        
        loadSets();
    }

    private void loadSets() {
        setsIDs.clear();
        loadingDialog.show();
        mFireStore.collection("PreQuiz").document(categoryList.get(selected_category_index).getId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                long noOfSets = (long) documentSnapshot.get("Sets");
                for (int i = 0; i < noOfSets; i++){
                    setsIDs.add(documentSnapshot.getString("Set" + String.valueOf(i) + "_ID"));
                }

                categoryList.get(selected_category_index).setSetCounter(documentSnapshot.getString("Counter"));
                categoryList.get(selected_category_index).setNo0fSets(String.valueOf(noOfSets));

                setsAdapter = new SetsAdapter(setsIDs);
                setRecyclerview.setAdapter(setsAdapter);

                loadingDialog.dismiss();

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toasty.error(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT, true).show();
                    }
                });

    }

    private void addNewSet() {
        loadingDialog.show();
        String current_category_id = categoryList.get(selected_category_index).getId();
        String current_counter = categoryList.get(selected_category_index).getSetCounter();
        Map<String, Object> questionData = new ArrayMap<>();
        questionData.put("Count", "0");

        mFireStore.collection("PreQuiz").document(current_category_id)
                .collection(current_counter).document("Questions_List")
                .set(questionData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> categoryDoc = new ArrayMap<>();
                        categoryDoc.put("Counter", String.valueOf(Integer.valueOf(current_counter) + 1));
                        categoryDoc.put("Set" + String.valueOf(setsIDs.size() + 1) + "_ID", current_counter);
                        categoryDoc.put("Sets", setsIDs.size() + 1);

                        mFireStore.collection("PreQuiz").document(current_category_id)
                                .update(categoryDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toasty.success(SetsActivity.this, "Set added successfully", Toast.LENGTH_SHORT, true).show();
                                        setsIDs.add(current_counter);
                                        categoryList.get(selected_category_index).setNo0fSets(String.valueOf(setsIDs.size()));
                                        categoryList.get(selected_category_index).setSetCounter(String.valueOf(Integer.valueOf(current_counter) + 1));

                                        setsAdapter.notifyItemInserted(setsIDs.size());
                                        loadingDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadingDialog.dismiss();
                                        Toasty.error(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT, true).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingDialog.dismiss();
                        Toasty.error(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT, true).show();
                    }
                });

    }
}