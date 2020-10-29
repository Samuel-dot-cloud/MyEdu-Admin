package com.studiofive.myedu_admin;

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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.studiofive.myedu_admin.Classes.Category;
import com.studiofive.myedu_admin.adapters.CategoryAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private Button dialogButton;
    private EditText categoryNameEdit;

    public static List<Category> categoryList = new ArrayList<>();
    private FirebaseFirestore mFirestore;
    private Dialog loadingDialog, addCategoryDialog;
    private CategoryAdapter adapter;

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

        addCategoryDialog = new Dialog(CategoryActivity.this);
        addCategoryDialog.setContentView(R.layout.add_category_dialog);
        addCategoryDialog.setCancelable(true);
        addCategoryDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogButton = addCategoryDialog.findViewById(R.id.addCategoryButtonDialog);
        categoryNameEdit = addCategoryDialog.findViewById(R.id.categoryNameEditText);

        mFirestore = FirebaseFirestore.getInstance();

        clickEvents();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        categoryRecyclerview.setLayoutManager(linearLayoutManager);


        loadData();

    }

    private void clickEvents() {
        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryNameEdit.getText().clear();
                addCategoryDialog.show();
            }
        });

        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoryNameEdit.getText().toString().isEmpty()){
                    categoryNameEdit.setError("Enter Category Name!");
                    return;
                }

                addNewCategory(categoryNameEdit.getText().toString());
            }
        });
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
                            String categoryName = documentSnapshot.getString("Cat" + String.valueOf(i) + "_Name");
                            String categoryID = documentSnapshot.getString("Cat" + String.valueOf(i) + "_ID");
                            categoryList.add(new Category(categoryID, categoryName, "0"));

                        }
                         adapter = new CategoryAdapter(categoryList);
                        categoryRecyclerview.setAdapter(adapter);

                    }else {
                        Toasty.error(CategoryActivity.this, "Something went wrong loading categories!!!", Toast.LENGTH_SHORT, true).show();
                        finish();
                    }

                }else {
                    Toasty.error(CategoryActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT, true).show();
                }

                loadingDialog.dismiss();
            }
        });
    }

    private void addNewCategory(String title) {
        addCategoryDialog.dismiss();
        loadingDialog.show();

        Map<String, Object> categoryData = new ArrayMap<>();
        categoryData.put("Name", title);
        categoryData.put("Sets", 0);

        String documentID = mFirestore.collection("PreQuiz").document().getId();
        mFirestore.collection("PreQuiz").document(documentID)
                .set(categoryData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> categoryDoc = new ArrayMap<>();
                        categoryDoc.put("Cat" + String.valueOf(categoryList.size() + 1) + "_Name", title);
                        categoryDoc.put("Cat" + String.valueOf(categoryList.size() + 1) + "_ID", documentID);
                        categoryDoc.put("Count", categoryList.size() + 1);

                        mFirestore.collection("PreQuiz").document("Categories")
                                .update(categoryDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toasty.success(CategoryActivity.this, "Category added successfully", Toast.LENGTH_SHORT, true).show();

                                        categoryList.add(new Category(documentID, title, "0"));

                                        adapter.notifyItemInserted(categoryList.size());

                                        loadingDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingDialog.dismiss();
                                Toasty.error(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT, true).show();
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialog.dismiss();
                Toasty.error(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT, true).show();
            }
        });
    }
}