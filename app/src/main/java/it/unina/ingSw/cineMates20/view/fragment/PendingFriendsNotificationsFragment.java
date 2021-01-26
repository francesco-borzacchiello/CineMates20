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
import it.unina.ingSw.cineMates20.view.activity.NotificationsActivity;

public class PendingFriendsNotificationsFragment extends Fragment {

    private RecyclerView friendsNotificationsRecyclerView;
    private TextView noNotificationsAvailable;

    public PendingFriendsNotificationsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pending_friend_requests, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        if(!isAdded() || getActivity() == null) return;

        friendsNotificationsRecyclerView = view.findViewById(R.id.friendsNotificationsRecyclerView);
        noNotificationsAvailable = view.findViewById(R.id.noFriendsNotificationsAvailable);

        if(getActivity() instanceof NotificationsActivity) {
            NotificationsActivity notificationsActivity = (NotificationsActivity) getActivity();
            notificationsActivity.initializeNotificationsFriendRequestsAdapter(this);
        }
    }

    public void setFriendsNotificationsRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        if(!isAdded() || getActivity() == null) return;

        friendsNotificationsRecyclerView.setAdapter(adapter);

        friendsNotificationsRecyclerView.setItemViewCacheSize(30);

        friendsNotificationsRecyclerView.setLayoutManager(new LinearLayoutManager
                (getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
    }

    public void showEmptyNotificationsPage() {
        noNotificationsAvailable.setVisibility(View.VISIBLE);
    }
}