package com.studiofive.myedu_admin.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.studiofive.myedu_admin.CategoryActivity;
import com.studiofive.myedu_admin.Classes.Category;
import com.studiofive.myedu_admin.R;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<Category> categoryList;


    public CategoryAdapter(List<Category> categoryList) {
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        String title = categoryList.get(position).getName();

        holder.setData(title, position, this);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.categoryName)
        TextView categoryName;
        @BindView(R.id.categoryDelete)
        ImageView categoryDelete;

        private Dialog loadingDialog;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        private void setData(String title, int position, CategoryAdapter adapter){
            categoryName.setText(title);

            categoryDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Category")
                            .setMessage("Do you want to delete this category?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteCategory(position, itemView.getContext(), adapter);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    dialog.getButton(dialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.GREEN);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0 ,50, 0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);
                }
            });
        }

        private void deleteCategory(final int id, Context mContext, CategoryAdapter adapter) {
            loadingDialog.show();

            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

            Map<String, Object> categoryDoc = new ArrayMap<>();
            int index = 1;
            for (int i = 0; i < categoryList.size(); i++){
                if (i != id){
                    categoryDoc.put("Cat" + String.valueOf(index) + "_ID", categoryList.get(i).getId());
                    categoryDoc.put("Cat" + String.valueOf(index) + "_Name", categoryList.get(i).getName());
                    index++;
                }
            }

            categoryDoc.put("Count", index - 1);

            mFirestore.collection("PreQuiz").document("Categories")
                    .set(categoryDoc)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toasty.success(mContext.getApplicationContext(), "Category deleted successfully", Toast.LENGTH_SHORT, true).show();
                            CategoryActivity.categoryList.remove(id);
                            adapter.notifyDataSetChanged();
                            loadingDialog.dismiss();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingDialog.dismiss();
                    Toasty.success(mContext.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT, true).show();
                }
            });
        }
    }
}
