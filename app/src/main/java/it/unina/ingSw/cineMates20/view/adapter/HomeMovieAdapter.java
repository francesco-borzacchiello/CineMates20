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

public class HomeMovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<String> title, linkImage;
    private final List<Runnable> movieCardListeners;

    public HomeMovieAdapter(Context context, List<String> nameAndSurname,
                            List<String> linkImage, List<Runnable> movieCardListeners) {
        this.context = context;
        this.title = nameAndSurname;
        this.linkImage = linkImage;
        this.movieCardListeners = movieCardListeners;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.movie_container_home, parent, false);
        return new HomeMovieAdapter.MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getClass() != HomeMovieAdapter.MovieHolder.class)
            throw new IllegalArgumentException("Holder non valido!");

        MovieHolder movieHolder = (HomeMovieAdapter.MovieHolder)holder;
        movieHolder.titleTextView.setText(title.get(position));

        String firstPath = context.getResources().getString(R.string.first_path_poster_image);
        if(linkImage.get(position) != null && !linkImage.get(position).equals(""))
            Picasso.get().load(firstPath +
                    linkImage.get(position)).resize(270,360).noFade().into(movieHolder.movieImageView,
                    new Callback() {
                        @Override
                        public void onSuccess() {
                            movieHolder.movieImageView.setAlpha(0f);
                            movieHolder.movieImageView.animate().setDuration(300).alpha(1f).start();
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });

        //Set listener CardView
        movieHolder.movieCardView.setOnClickListener(addListenerForMovieCard(position));
    }

    @NotNull
    @Contract(pure = true)
    private View.OnClickListener addListenerForMovieCard(int position) {
        return listener -> {
            try{
                movieCardListeners.get(position).run();
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

    private static class MovieHolder extends RecyclerView.ViewHolder {

        TextView titleTextView;
        ImageView movieImageView;
        CardView movieCardView;

        private MovieHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.home_movie_title);
            movieImageView = itemView.findViewById(R.id.home_movie_cover);
            movieCardView = itemView.findViewById(R.id.homeMovieCardView);
        }
    }
}
