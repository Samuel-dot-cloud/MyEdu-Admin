package com.studiofive.myedu_admin.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.studiofive.myedu_admin.Classes.Category;
import com.studiofive.myedu_admin.R;
import com.studiofive.myedu_admin.adapters.CategoryAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
    private EditText categoryNameEdit, categoryDetailEdit;
    private ImageView categoryImage;
    private int REQUEST_CODE = 5;
    private Uri imageUri;

    public static List<Category> categoryList = new ArrayList<>();
    public static int selected_category_index = 0;

    private FirebaseFirestore mFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

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
        categoryDetailEdit = addCategoryDialog.findViewById(R.id.categoryDescEditText);
        categoryImage = addCategoryDialog.findViewById(R.id.imageView);

        mFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("Category Images");

        clickEvents();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        categoryRecyclerview.setLayoutManager(linearLayoutManager);


        loadData();

    }

    private void clickEvents() {
        categoryButton.setOnClickListener(v -> {
            categoryNameEdit.getText().clear();
            categoryDetailEdit.getText().clear();
            addCategoryDialog.show();
        });

        dialogButton.setOnClickListener(v -> {
            if (categoryNameEdit.getText().toString().isEmpty()) {
                categoryNameEdit.setError("Enter Category Name!");
                return;
            } else if (categoryDetailEdit.getText().toString().isEmpty()){
                categoryDetailEdit.setError("Enter Category Details!");
                return;
            }

            addNewCategory(categoryNameEdit.getText().toString(), categoryDetailEdit.getText().toString());
        });

        categoryImage.setOnClickListener(v -> {
            choosePicture();
        });
    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void loadData() {

        loadingDialog.show();
        categoryList.clear();
        mFirestore.collection("PreQuiz").document("Categories")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    long count = (long) documentSnapshot.get("Count");

                    for (int i = 1; i < count + 1; i++) {
                        String categoryName = documentSnapshot.getString("Cat" + i + "_Name");
                        String categoryID = documentSnapshot.getString("Cat" + i + "_ID");
                        String categoryDescription = documentSnapshot.getString("Cat" + i + "_Description");
                        String categoryImage = documentSnapshot.getString("Cat" + i + "_Image");
                        categoryList.add(new Category(categoryID, categoryName, categoryDescription, categoryImage, "0", "1"));

                    }
                    adapter = new CategoryAdapter(categoryList);
                    categoryRecyclerview.setAdapter(adapter);

                } else {
                    Toasty.error(CategoryActivity.this, "Something went wrong loading categories!!!", Toast.LENGTH_SHORT, true).show();
                    finish();
                }

            } else {
                Toasty.error(CategoryActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT, true).show();
            }

            loadingDialog.dismiss();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            categoryImage.setImageURI(imageUri);
        }
    }

    private void uploadPicture(String documentID) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Image...");
        progressDialog.show();


        StorageReference filePath = storageReference.child(documentID + "." + getMimeType(getApplicationContext(), imageUri));
        filePath.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful()) ;
                    Uri downloadUri = uriTask.getResult();

                    final String downloadPic_url = String.valueOf(downloadUri);

                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("Cat" + (categoryList.size()) + "_Image", downloadPic_url);

                    mFirestore.collection("PreQuiz").document("Categories")
                            .update(hashMap)
                            .addOnSuccessListener(aVoid -> {
                                progressDialog.dismiss();
                                Snackbar.make(findViewById(android.R.id.content), "Image uploaded", Snackbar.LENGTH_LONG).show();
                            }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
                    });


                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Snackbar.make(findViewById(android.R.id.content), e.getMessage(), Snackbar.LENGTH_LONG).show();
        })
                .addOnProgressListener(snapshot -> {
                    double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressDialog.setMessage("Percentage: " + (int) progressPercent + "%");
                });


    }


    private void addNewCategory(String title, String description) {
        addCategoryDialog.dismiss();
        loadingDialog.show();

        Map<String, Object> categoryData = new ArrayMap<>();
        categoryData.put("Name", title);
        categoryData.put("Sets", 0);
        categoryData.put("Counter", "1");

        final String documentID = mFirestore.collection("PreQuiz").document().getId();
        mFirestore.collection("PreQuiz").document(documentID)
                .set(categoryData)
                .addOnSuccessListener(aVoid -> {

                    String savedID = documentID;
                    uploadPicture(savedID);

                    Map<String, Object> categoryDoc = new ArrayMap<>();
                    categoryDoc.put("Cat" + (categoryList.size() + 1) + "_Name", title);
                    categoryDoc.put("Cat" + (categoryList.size() + 1) + "_ID", documentID);
                    categoryDoc.put("Cat" + (categoryList.size() + 1) + "_Description", description);
                    categoryDoc.put("Count", categoryList.size() + 1);



                    mFirestore.collection("PreQuiz").document("Categories")
                            .update(categoryDoc)
                            .addOnSuccessListener(aVoid1 -> {
                                Toasty.success(CategoryActivity.this, "Category added successfully", Toast.LENGTH_SHORT, true).show();

                                categoryList.add(new Category(documentID, title, description, "","0", "1"));

                                adapter.notifyItemInserted(categoryList.size());

                                loadingDialog.dismiss();
                            }).addOnFailureListener(e -> {
                        loadingDialog.dismiss();
                        Toasty.error(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT, true).show();
                    });

                }).addOnFailureListener(e -> {
            loadingDialog.dismiss();
            Toasty.error(CategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT, true).show();
        });
    }

    private static String getMimeType(Context context, Uri uri) {
        String extension;
        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        return extension;
    }
}