package it.unina.ingSw.cineMates20.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.MoviesListsController;

public class HomeStyleMovieAdapter extends Adapter<ViewHolder> {
    private final Context context;
    private final List<String> titles, linkImage;
    private final List<Long> moviesIds;
    private final List<Runnable> movieCardListeners;
    private final SortedSet<Integer> deleteList;

    private int visibilityCheckBox = View.GONE;

    public HomeStyleMovieAdapter(Context context, List<String> titles,
                                 List<String> linkImage, List<Runnable> movieCardListeners, List<Long> moviesIds) {
        this.context = context;
        this.titles = titles;
        this.linkImage = linkImage;
        this.movieCardListeners = movieCardListeners;
        this.moviesIds = moviesIds;

        deleteList = new TreeSet<>();
    }

    public SortedSet<Long> getDeleteList() {
        SortedSet<Long> moviesId = new TreeSet<>();

        for(Integer position : deleteList)
            moviesId.add(moviesIds.get(position));

        return moviesId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.movie_container_home, parent, false);
        return new HomeStyleMovieAdapter.MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if(holder.getClass() != MovieHolder.class)
            throw new IllegalArgumentException("Holder non valido!");

        MovieHolder movieHolder = (MovieHolder)holder;

        movieHolder.checkBox.setVisibility(visibilityCheckBox);
        if(movieHolder.isInitialized) return;

        movieHolder.isInitialized = true;
        movieHolder.titleTextView.setText(titles.get(position));

        String firstPath = context.getResources().getString(R.string.first_path_image);
        if(linkImage.get(position) != null && !linkImage.get(position).equals("")) {
            Picasso.get().load(firstPath +
                linkImage.get(position)).resize(270, 360).noFade().into(movieHolder.movieImageView,
                new Callback() {
                    @Override
                    public void onSuccess() {
                        movieHolder.movieImageView.setAlpha(0f);
                        movieHolder.movieImageView.animate().setDuration(300).alpha(1f).start();
                    }

                    @Override
                    public void onError(Exception e) {}
                });
        }

        movieHolder.movieCardView.setOnClickListener(addListenerForMovieCard(movieHolder));

        if(moviesIds != null) { //Allora questo adapter è per MoviesListActivity
            movieHolder.movieCardView.setOnLongClickListener(view -> {

                if(isDeleteEnabled())  return true;

                visibilityCheckBox = View.VISIBLE;
                notifyDataSetChanged();
                movieHolder.checkBox.setChecked(true);
                return true;
            });
        }
    }

    public void updateVisibility(){
        if(visibilityCheckBox == View.VISIBLE)
            visibilityCheckBox = View.GONE;
        else
            visibilityCheckBox = View.VISIBLE;
    }

    @NotNull
    @Contract(pure = true)
    private OnClickListener addListenerForMovieCard(MovieHolder movieHolder) {
        return v -> {
            if (!isDeleteEnabled()) {
                try {
                    movieCardListeners.get(movieHolder.getLayoutPosition()).run();
                } catch (NullPointerException ignore) {}
            } else
                movieHolder.checkBox.setChecked(!movieHolder.checkBox.isChecked());
        };
    }

    @Override
    public long getItemId(int position) {
        return movieCardListeners.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return movieCardListeners.size();
    }

    public boolean isDeleteEnabled() {
        return visibilityCheckBox == View.VISIBLE;
    }

    public void resetAllMoviesCheckBoxes() {
        RecyclerView recyclerView = MoviesListsController.getMoviesListControllerInstance().
                getMoviesRecyclerView();

        for (int x = recyclerView.getChildCount(), i = 0; i < x; ++i) {
            MovieHolder holder = ((MovieHolder)recyclerView.getChildViewHolder(recyclerView.getChildAt(i)));
            holder.checkBox.setChecked(false);
        }
    }

    public void deleteSelectedItem() {
        if(deleteList.size() > 0) {
            int deletedElements = 0;
            for(int position : deleteList) {
                titles.remove(position - deletedElements);
                linkImage.remove(position - deletedElements);
                movieCardListeners.remove(position - deletedElements++);
            }

            deleteList.clear();
        }

        if(titles.size() == 0)
            MoviesListsController.getMoviesListControllerInstance().showEmptyMovieList();
    }

    private class MovieHolder extends ViewHolder {

        TextView titleTextView;
        ImageView movieImageView;
        CardView movieCardView;
        CheckBox checkBox;
        boolean isInitialized;

        private MovieHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.movie_container_checkbox);

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked)
                    deleteList.add(getLayoutPosition());
                else
                    deleteList.remove(getLayoutPosition());
            });

            titleTextView = itemView.findViewById(R.id.home_movie_title);
            movieImageView = itemView.findViewById(R.id.home_movie_cover);
            movieCardView = itemView.findViewById(R.id.homeMovieCardView);
        }
    }
}
