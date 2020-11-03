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
import com.studiofive.myedu_admin.Classes.Question;
import com.studiofive.myedu_admin.R;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

import static com.studiofive.myedu_admin.activities.CategoryActivity.categoryList;
import static com.studiofive.myedu_admin.activities.CategoryActivity.selected_category_index;
import static com.studiofive.myedu_admin.activities.SetsActivity.selected_set_index;
import static com.studiofive.myedu_admin.activities.SetsActivity.setsIDs;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.ViewHolder> {
    private List<Question> questionsList;

    public QuestionsAdapter(List<Question> questionsList) {
        this.questionsList = questionsList;
    }

    @NonNull
    @Override
    public QuestionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionsAdapter.ViewHolder holder, int position) {
        holder.setData(position, this);
    }

    @Override
    public int getItemCount() {
        return questionsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.categoryName)
        TextView questionName;
        @BindView(R.id.categoryDelete)
        ImageView questionDelete;

        private Dialog loadingDialog;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        private void setData(int position, final QuestionsAdapter questionsAdapter){
            questionName.setText("Question " + String.valueOf(position+1));

            questionDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Question")
                            .setMessage("Do you want to delete this Question ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    deleteQuestion(position, itemView.getContext(), questionsAdapter);
                                }
                            })
                            .setNegativeButton("Cancel",null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    dialog.getButton(dialog.BUTTON_POSITIVE).setBackgroundColor(Color.RED);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.GREEN);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,50,0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);
                }
            });
        }

        private void deleteQuestion(final int position, final Context context, final QuestionsAdapter questionsAdapter){
            loadingDialog.show();

            final FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

            mFirestore.collection("PreQuiz").document(categoryList.get(selected_category_index).getId())
                    .collection(setsIDs.get(selected_set_index)).document(questionsList.get(position).getQuestionID())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Map<String,Object> questionDoc = new ArrayMap<>();
                            int index=1;
                            for(int i=0; i< questionsList.size(); i++)
                            {
                                if(i != position)
                                {
                                    questionDoc.put("Question" + String.valueOf(index) + "_ID", questionsList.get(i).getQuestionID());
                                    index++;
                                }
                            }

                            questionDoc.put("Count", String.valueOf(index - 1));

                            mFirestore.collection("PreQuiz").document(categoryList.get(selected_category_index).getId())
                                    .collection(setsIDs.get(selected_set_index)).document("Questions_List")
                                    .set(questionDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toasty.success(context, "Question Deleted Successfully", Toast.LENGTH_SHORT, true).show();

                                            questionsList.remove(position);
                                            questionsAdapter.notifyDataSetChanged();

                                            loadingDialog.dismiss();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            loadingDialog.dismiss();
                                            Toasty.error(context, e.getMessage(),Toast.LENGTH_SHORT, true).show();
                                        }
                                    });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingDialog.dismiss();
                            Toasty.error(context, e.getMessage(),Toast.LENGTH_SHORT, true).show();
                        }
                    });

        }
    }
}
