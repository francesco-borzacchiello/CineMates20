package it.unina.ingSw.cineMates20.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.SearchMovieController;

public class SearchMovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final Context context;
    private final List<String> title, description, linkImage;
    private final List<Runnable> threeDotsListeners, showDetailsMovieListeners;

    public SearchMovieAdapter(Context context, List<String> title, List<String> description,
                              List<String> linkImage, List<Runnable> threeDotsListeners,
                              List<Runnable> showDetailsMovieListeners) {
        this.context = context;
        this.title = title;
        this.description = description;
        this.linkImage = linkImage;
        this.threeDotsListeners = threeDotsListeners;
        this.showDetailsMovieListeners = showDetailsMovieListeners;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.movie_container_result_search, parent, false);
        return new MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MovieHolder movieHolder;
        if(holder.getClass() != MovieHolder.class)
            throw new IllegalArgumentException("Holder non valido!");

        movieHolder = (MovieHolder)holder;
        movieHolder.titleTextView.setText(title.get(position));
        movieHolder.descriptionTextView.setText(description.get(position));

        String firstPath = context.getResources().getString(R.string.first_path_poster_image);
        if(linkImage.get(position) != null)
            Picasso.get().load(firstPath +
                    linkImage.get(position)).resize(270, 360)
                    .noFade().into(movieHolder.coverImageView,
                    new Callback() {
                        @Override
                        public void onSuccess() {
                            movieHolder.coverImageView.setAlpha(0f);
                            movieHolder.coverImageView.animate().setDuration(300).alpha(1f).start();
                        }
                        @Override
                        public void onError(Exception e) {}
                    });

        initializeAllListener(movieHolder, position);
        SearchMovieController.getSearchMovieControllerInstance().hideSearchMovieProgressBar();
    }

    private void initializeAllListener(@NotNull MovieHolder movieHolder, int position) {
        movieHolder.threeDotsImageView.setOnClickListener(addListenerForThreeDots(position));
        movieHolder.movieCardView.setOnClickListener(addListenerForViewDetailsMovie(position));
    }

    @NotNull
    @Contract(pure = true)
    private View.OnClickListener addListenerForThreeDots(int position) {
        return listener -> {
            try{
                threeDotsListeners.get(position).run();
            } catch(NullPointerException ignore) {}
        };
    }

    @NotNull
    @Contract(pure = true)
    private View.OnClickListener addListenerForViewDetailsMovie(int position) {
        return listener -> {
            try{
                showDetailsMovieListeners.get(position).run();
            } catch(NullPointerException ignore) {}
        };
    }

    @Override
    public long getItemId(int position) {
        return title.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return title.size();
    }


    private static class MovieHolder extends RecyclerView.ViewHolder{

        TextView titleTextView,
                 descriptionTextView;

        ImageView coverImageView,
                  threeDotsImageView;
        CardView movieCardView;

        private MovieHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_movie);
            descriptionTextView = itemView.findViewById(R.id.description_movie);
            coverImageView = itemView.findViewById(R.id.cover_movie);
            threeDotsImageView = itemView.findViewById(R.id.threeDotsMovieContainer);
            movieCardView = itemView.findViewById(R.id.movieSearchResultCardView);
        }
    }
}
