package com.studiofive.myedu_admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.studiofive.myedu_admin.Classes.CategoryAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class CategoryActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.categoryRecyclerView)
    RecyclerView categoryRecyclerview;
    @BindView(R.id.addCategoryButton)
    Button categoryButton;

    public static List<String> categoryList = new ArrayList<>();
    private FirebaseFirestore mFirestore;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");

        loadingDialog = new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mFirestore = FirebaseFirestore.getInstance();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        categoryRecyclerview.setLayoutManager(linearLayoutManager);

        loadData();
    }

    private void loadData() {

        loadingDialog.show();
        categoryList.clear();
        mFirestore.collection("PreQuiz").document("Categories")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        long count = (long) documentSnapshot.get("Count");

                        for (int i = 1; i< count; i++){
                            String categoryName = documentSnapshot.getString("Cat" + String.valueOf(i));
                            categoryList.add(categoryName);

                        }

                        CategoryAdapter adapter = new CategoryAdapter(categoryList);
                        categoryRecyclerview.setAdapter(adapter);

                    }else {
                        Toasty.error(CategoryActivity.this, "Something went wrong loading categories!!!", Toast.LENGTH_SHORT, true).show();
                    }
                    finish();
                }else {
                    Toasty.error(CategoryActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT, true).show();
                }

                loadingDialog.dismiss();
            }
        });
    }
}