package com.mseei.myhealthcare;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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
        // Crear el diseño del elemento de la lista
        View view = LayoutInflater.from(context).inflate(R.layout.child_account_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener el objeto ChildAccount en la posición actual
        ChildAccount childAccount = childAccountList.get(position);

        // Usar recursos de cadena para establecer el texto de los TextViews
        holder.tvName.setText(context.getString(R.string.full_name, childAccount.getFirstName(), childAccount.getFirstLastName()));
        holder.tvStatus.setText(childAccount.isStatus()
                ? context.getString(R.string.status_active)
                : context.getString(R.string.status_inactive));
        holder.tvRealTimeMonitoring.setText(childAccount.isRealTimeMonitoring()
                ? context.getString(R.string.real_time_monitoring_enabled)
                : context.getString(R.string.real_time_monitoring_disabled));

        // Manejar el clic en el elemento del RecyclerView
        holder.itemView.setOnClickListener(v -> {
            Log.d("ChildAccountAdapter", context.getString(R.string.log_item_clicked, childAccount.getFirstName()));

            // Crear el Intent para abrir la pantalla de detalles
            Intent intent = new Intent(context, AssociatedChildAccountInformation.class);

            // Pasar los datos necesarios a la nueva actividad
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

            // Iniciar la actividad
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // Devolver el número total de elementos en la lista
        return childAccountList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvRealTimeMonitoring;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Inicializar los TextViews
            tvName = itemView.findViewById(R.id.tv_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvRealTimeMonitoring = itemView.findViewById(R.id.tv_real_time_monitoring);
        }
    }
}
