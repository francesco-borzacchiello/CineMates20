package it.unina.ingSw.cineMates20.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;

public class NotEmptyFriendsSearchFragment extends Fragment {

    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter = null;

    public NotEmptyFriendsSearchFragment() {}

    public NotEmptyFriendsSearchFragment(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_not_empty_friends_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(adapter != null && isAdded() && getActivity() != null) {
            RecyclerView friendsSearchRecyclerView = view.findViewById(R.id.friendsSearchRecyclerView);
            friendsSearchRecyclerView.setAdapter(adapter);

            friendsSearchRecyclerView.setHasFixedSize(true);
            friendsSearchRecyclerView.setItemViewCacheSize(30);

            friendsSearchRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        }
    }
}