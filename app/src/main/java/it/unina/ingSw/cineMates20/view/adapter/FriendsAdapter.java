package it.unina.ingSw.cineMates20.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.FriendsController;
import it.unina.ingSw.cineMates20.model.UserDB;

public class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<UserDB> friends;
    private final List<Runnable> showUserListeners;
    private UserDB lastClickedUser;
    private int lastClickedItemPosition;

    public FriendsAdapter(Context context, List<UserDB> friends, List<Runnable> showUserListeners) {
        this.context = context;
        this.friends = friends;
        this.showUserListeners = showUserListeners;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.user_container_friend_search, parent, false);
        return new FriendsAdapter.UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getClass() != FriendsAdapter.UserHolder.class)
            throw new IllegalArgumentException("Holder non valido!");

        FriendsAdapter.UserHolder userHolder = (FriendsAdapter.UserHolder)holder;
        userHolder.nameTextView.setText(friends.get(position).getNome());
        userHolder.surnameTextView.setText(friends.get(position).getCognome());
        userHolder.usernameTextView.setText(friends.get(position).getUsername());

        //TODO: adattare questa parte per il recupero dell'immagine da Amazon S3
        /*String firstPath = context.getResources().getString(R.string.first_path_poster_image);
        if(linkImage.get(position) != null)
            Picasso.get().load(firstPath +
                    linkImage.get(position)).resize(270, 360).noFade().into(movieHolder.coverImageView,
                    new Callback() {
                        @Override
                        public void onSuccess() {
                            movieHolder.coverImageView.setAlpha(0f);
                            movieHolder.coverImageView.animate().setDuration(300).alpha(1f).start();
                        }
                        @Override
                        public void onError(Exception e) {}
                    });*/

        userHolder.userConstraintLayout.setOnClickListener(addListenerForUserLayout(position));
    }

    @NotNull
    @Contract(pure = true)
    private View.OnClickListener addListenerForUserLayout(int position) {
        return v -> {
            try{
                showUserListeners.get(position).run();
                lastClickedItemPosition = position;
                lastClickedUser = new UserDB(friends.get(position).getUsername(),
                        friends.get(position).getNome(), friends.get(position).getCognome(),
                        friends.get(position).getEmail(), "utente");
            } catch(NullPointerException ignore) {}
        };
    }

    @Override
    public long getItemId(int position) {
        return showUserListeners.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return showUserListeners.size();
    }

    //Si occupa soltanto dell'eliminazione grafica, non di quella concreta
    public void deleteLastClickedItem() {
        if(friends.size() == 0 || lastClickedItemPosition == -1) return;

        friends.remove(lastClickedItemPosition);
        showUserListeners.remove(lastClickedItemPosition);

        notifyItemRemoved(lastClickedItemPosition);
        notifyItemRangeChanged(lastClickedItemPosition, friends.size());

        if(friends.size() == 0)
            FriendsController.getFriendsControllerInstance().showEmptyFriendsLayout(true);

        lastClickedItemPosition = -1; //Reset
    }

    public UserDB getLastClickedUser() {
        return lastClickedUser;
    }

    private static class UserHolder extends RecyclerView.ViewHolder{

        TextView nameTextView,
                 surnameTextView,
                 usernameTextView;

        ImageView profilePictureImageView;
        ConstraintLayout userConstraintLayout;

        private UserHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.friend_name);
            surnameTextView = itemView.findViewById(R.id.friend_surname);
            usernameTextView = itemView.findViewById(R.id.friend_username);
            profilePictureImageView = itemView.findViewById(R.id.friend_profile_picture);
            userConstraintLayout = itemView.findViewById(R.id.friendsConstraintLayout);
        }
    }
}
