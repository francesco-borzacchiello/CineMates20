package it.unina.ingSw.cineMates20.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import it.unina.ingSw.cineMates20.R;

public class ActorMovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<String> nameAndSurname,
                               movieNameAndSurname,
                               linkImage;

    public ActorMovieAdapter(Context context, List<String> nameAndSurname, List<String> movieNameAndSurname, List<String> linkImage) {
        this.context = context;
        this.nameAndSurname = nameAndSurname;
        this.movieNameAndSurname = movieNameAndSurname;
        this.linkImage = linkImage;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.movie_actor_container, parent, false);
        return new ActorMovieAdapter.ActorHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getClass() != ActorMovieAdapter.ActorHolder.class)
            throw new IllegalArgumentException("Holder non valido!");

        ActorHolder actorHolder = (ActorMovieAdapter.ActorHolder)holder;
        actorHolder.nomeCognomeTextView.setText(nameAndSurname.get(position));
        actorHolder.nomeCognomeFilmTextView.setText(movieNameAndSurname.get(position));

        String firstPath = context.getResources().getString(R.string.first_path_poster_image);
        if(linkImage.get(position) != null && !linkImage.get(position).equals(""))
            Picasso.get().load(firstPath +
                    linkImage.get(position)).resize(270, 360).noFade()
                    .into(actorHolder.actorImageView,
                    new Callback() {
                        @Override
                        public void onSuccess() {
                            actorHolder.actorImageView.setAlpha(0f);
                            actorHolder.actorImageView.animate().setDuration(300).alpha(1f).start();
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });
    }

    @Override
    public long getItemId(int position) {
        return nameAndSurname.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return nameAndSurname.size();
    }

    private static class ActorHolder extends RecyclerView.ViewHolder {

        TextView nomeCognomeTextView,
                 nomeCognomeFilmTextView;

        ImageView actorImageView;

        private ActorHolder(@NonNull View itemView) {
            super(itemView);
            nomeCognomeTextView = itemView.findViewById(R.id.actor_name);
            nomeCognomeFilmTextView = itemView.findViewById(R.id.actor_movie_name);
            actorImageView = itemView.findViewById(R.id.actor_cover);
        }
    }
}
