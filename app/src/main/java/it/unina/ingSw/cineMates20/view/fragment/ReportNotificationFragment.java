package it.unina.ingSw.cineMates20.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.view.activity.NotificationActivity;

public class ReportNotificationFragment extends Fragment {

    private RecyclerView reportNotificationsRecyclerView;
    private TextView noNotificationsAvailable;

    public ReportNotificationFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_notification, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        if(!isAdded() || getActivity() == null) return;

        reportNotificationsRecyclerView = view.findViewById(R.id.reportNotificationsRecyclerView);
        noNotificationsAvailable = view.findViewById(R.id.noReportNotificationsAvailable);

        if(getActivity() instanceof NotificationActivity) {
            NotificationActivity notificationActivity = (NotificationActivity) getActivity();
            notificationActivity.initializeReportNotificationAdapter(this);
        }
    }

    public void setReportNotificationRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        if(!isAdded() || getActivity() == null) return;

        getActivity().runOnUiThread(()-> {
            reportNotificationsRecyclerView.setAdapter(adapter);

            reportNotificationsRecyclerView.setItemViewCacheSize(30);

            reportNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager
                    (getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        });
    }

    public void showEmptyNotificationsPage(boolean show) {
        if(show && getActivity() != null)
            getActivity().runOnUiThread(()-> noNotificationsAvailable.setVisibility(View.VISIBLE));
        else if(getActivity() != null)
            getActivity().runOnUiThread(()-> noNotificationsAvailable.setVisibility(View.GONE));
    }
}