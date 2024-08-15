// ChildAccountAdapter.java
package com.mseei.myhealthcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ChildAccountViewHolder> {

    private List<ChildAccount> childAccountList;

    public GalleryAdapter(List<ChildAccount> childAccountList) {
        this.childAccountList = childAccountList;
    }

    @NonNull
    @Override
    public ChildAccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.child_account_item, parent, false);
        return new ChildAccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildAccountViewHolder holder, int position) {
        ChildAccount childAccount = childAccountList.get(position);
        String fullName = childAccount.getFirstName() + " " + childAccount.getFirstLastName() + " " + childAccount.getSecondLastName();
        holder.nameTextView.setText(fullName);
        holder.statusTextView.setText(childAccount.isStatus() ? "Activo" : "Inactivo");
        holder.realTimeMonitoringTextView.setText(childAccount.isRealTimeMonitoring() ? "Activo" : "Inactivo");
    }

    @Override
    public int getItemCount() {
        return childAccountList.size();
    }

    public static class ChildAccountViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView statusTextView;
        TextView realTimeMonitoringTextView;

        public ChildAccountViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tv_name);
            statusTextView = itemView.findViewById(R.id.tv_status);
            realTimeMonitoringTextView = itemView.findViewById(R.id.tv_real_time_monitoring);
        }
    }
}
