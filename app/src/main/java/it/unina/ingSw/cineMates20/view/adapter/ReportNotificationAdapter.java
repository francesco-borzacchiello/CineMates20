package it.unina.ingSw.cineMates20.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.List;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.NotificationController;

public class ReportNotificationAdapter extends Adapter<ViewHolder> {

    private final Context context;
    private final List<String> reportsSubjects,
                               reportsOutcomes;
    private final List<Runnable> deleteNotificationEvents;

    public ReportNotificationAdapter(Context context, List<String> reportsSubjects,
                                     List<String> reportsOutcomes, List<Runnable> deleteNotificationEvents) {
        this.context = context;
        this.reportsSubjects = reportsSubjects;
        this.reportsOutcomes = reportsOutcomes;
        this.deleteNotificationEvents = deleteNotificationEvents;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.report_notification_container, parent, false);
        return new ReportNotificationAdapter.ReportNotificationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(holder.getClass() != ReportNotificationAdapter.ReportNotificationHolder.class)
            throw new IllegalArgumentException("Holder non valido!");

        ReportNotificationAdapter.ReportNotificationHolder notificationHolder = (ReportNotificationAdapter.ReportNotificationHolder)holder;
        notificationHolder.subjectTextView.setText(reportsSubjects.get(position));
        notificationHolder.outcomeTextView.setText(reportsOutcomes.get(position));

        notificationHolder.deleteNotificationButton.setOnClickListener(addListenerToDeleteNotification(position));
    }

    @NonNull
    private OnClickListener addListenerToDeleteNotification(int position) {
        return v -> {
            try{
                Runnable r = deleteNotificationEvents.get(position);
                if(NotificationController.getNotificationControllerInstance().isInternetAvailable())
                    deleteItem(position);
                new Thread(r).start();
            } catch(NullPointerException ignore) {}
        };
    }

    private void deleteItem(int position) {
        reportsSubjects.remove(position);
        reportsOutcomes.remove(position);
        deleteNotificationEvents.remove(position);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, reportsSubjects.size());

        if(reportsSubjects.size() == 0)
            NotificationController.getNotificationControllerInstance().showEmptyReportsNotificationPage(true);
    }

    @Override
    public long getItemId(int position) {
        return deleteNotificationEvents.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return reportsSubjects.size();
    }

    private static class ReportNotificationHolder extends ViewHolder{

        TextView subjectTextView,
                outcomeTextView;
        ImageView deleteNotificationButton;

        private ReportNotificationHolder(@NonNull View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subjectNotificationReport);
            outcomeTextView = itemView.findViewById(R.id.outcomeNotificationReport);
            deleteNotificationButton = itemView.findViewById(R.id.deleteReportNotificationButton);
        }
    }
}
