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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.studiofive.myedu_admin.R;
import com.studiofive.myedu_admin.activities.SetsActivity;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

import static com.studiofive.myedu_admin.activities.CategoryActivity.categoryList;
import static com.studiofive.myedu_admin.activities.CategoryActivity.selected_category_index;

public class SetsAdapter extends RecyclerView.Adapter<SetsAdapter.ViewHolder> {
    private List<String> setsIDs;

    public SetsAdapter(List<String> setsIDs) {
        this.setsIDs = setsIDs;
    }

    @NonNull
    @Override
    public SetsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetsAdapter.ViewHolder holder, int position) {
        String setID = setsIDs.get(position);
        holder.setData(position, setID, this);

    }

    @Override
    public int getItemCount() {
        return setsIDs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.categoryName)
        TextView setName;
        @BindView(R.id.categoryDelete)
        ImageView setDelete;

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

        public void setData(int position, String setID, final SetsAdapter setsAdapter) {
            setName.setText("Set " + String.valueOf(position + 1));

            setDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Set")
                            .setMessage("Do you want to delete this set?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteSet(position, setID, itemView.getContext(), setsAdapter);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    dialog.getButton(dialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.GREEN);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 50, 0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);
                }

            });
        }

        private void deleteSet(int position, final String setID, final Context context, final SetsAdapter setsAdapter) {
            loadingDialog.show();

            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            mFirestore.collection("PreQuiz").document(categoryList.get(selected_category_index).getId())
                    .collection(setID).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            WriteBatch batch = mFirestore.batch();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                batch.delete(doc.getReference());
                            }

                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Map<String, Object> categoryDoc = new ArrayMap<>();
                                    int index = 1;
                                    for (int i = 0; i < setsIDs.size(); i++) {
                                        if (i != position) {
                                            categoryDoc.put("Set" + String.valueOf(index) + "_ID", setsIDs.get(i));
                                            index++;
                                        }
                                    }

                                    categoryDoc.put("Sets", index - 1);

                                    mFirestore.collection("PreQuiz").document(categoryList.get(selected_category_index).getId())
                                            .update(categoryDoc)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toasty.success(context, "Set deleted Successfully", Toast.LENGTH_SHORT, true).show();

                                                    SetsActivity.setsIDs.remove(position);

                                                    categoryList.get(selected_category_index).setNo0fSets(String.valueOf(SetsActivity.setsIDs.size()));

                                                    setsAdapter.notifyDataSetChanged();

                                                    loadingDialog.dismiss();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    loadingDialog.dismiss();
                                                    Toasty.error(context, e.getMessage(), Toast.LENGTH_SHORT, true).show();

                                                }
                                            });
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            loadingDialog.dismiss();
                                            Toasty.error(context, e.getMessage(), Toast.LENGTH_SHORT, true).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingDialog.dismiss();
                            Toasty.error(context, e.getMessage(), Toast.LENGTH_SHORT, true).show();
                        }
                    });
        }


    }
}

