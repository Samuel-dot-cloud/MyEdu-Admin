package com.studiofive.myedu_admin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.studiofive.myedu_admin.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        holder.setData(position);

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setData(int position) {
            setName.setText("Set " + String.valueOf(position + 1));
        }
    }
}
