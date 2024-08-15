package com.mseei.myhealthcare;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// ChildAccountAdapter.java

// ChildAccountAdapter.java

public class ChildAccountAdapter extends RecyclerView.Adapter<ChildAccountAdapter.ViewHolder> {

    private Context context;
    private List<ChildAccount> childAccountList;

    public ChildAccountAdapter(Context context, List<ChildAccount> childAccountList) {
        this.context = context;
        this.childAccountList = childAccountList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.child_account_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChildAccount childAccount = childAccountList.get(position);

        holder.tvName.setText(childAccount.getFirstName() + " " + childAccount.getFirstLastName());
        holder.tvStatus.setText(childAccount.isStatus() ? "Active" : "Inactive");
        holder.tvRealTimeMonitoring.setText(childAccount.isRealTimeMonitoring() ? "Enabled" : "Disabled");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AssociatedChildAccountInformation.class);
            intent.putExtra("childAccountID", childAccount.getChildAccountID());
            intent.putExtra("loginEmail", childAccount.getLoginEmail());
            intent.putExtra("firstName", childAccount.getFirstName());
            intent.putExtra("firstLastName", childAccount.getFirstLastName());
            intent.putExtra("secondLastName", childAccount.getSecondLastName());
            intent.putExtra("status", childAccount.isStatus());
            intent.putExtra("blocked", childAccount.isBlocked());
            intent.putExtra("failedLoginAttempts", childAccount.getFailedLoginAttempts());
            intent.putExtra("createdOn", childAccount.getCreatedOn());
            intent.putExtra("realTimeMonitoring", childAccount.isRealTimeMonitoring());
            intent.putExtra("perimeter", childAccount.getPerimeter());
            intent.putExtra("pendingLocationConfig", childAccount.isPendingLocationConfig());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return childAccountList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvRealTimeMonitoring;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvRealTimeMonitoring = itemView.findViewById(R.id.tv_real_time_monitoring);
        }
    }
}


