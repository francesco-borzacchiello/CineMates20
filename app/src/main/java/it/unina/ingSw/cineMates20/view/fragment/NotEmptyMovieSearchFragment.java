package it.unina.ingSw.cineMates20.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;

public class NotEmptyMovieSearchFragment extends Fragment {

    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter = null;

    public NotEmptyMovieSearchFragment() {}

    public NotEmptyMovieSearchFragment(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        this.adapter = adapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_not_empty, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(adapter != null && isAdded() && getActivity() != null) {
            RecyclerView moviesRecyclerView = view.findViewById(R.id.moviesRecyclerView);
            moviesRecyclerView.setAdapter(adapter);

            moviesRecyclerView.setHasFixedSize(true);
            moviesRecyclerView.setItemViewCacheSize(30);

            moviesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }
}