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

import java.util.List;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.NotificationsController;
import it.unina.ingSw.cineMates20.model.UserDB;

public class PendingFriendsRequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<UserDB> users;
    private final List<Runnable> clickOnUserEvent, acceptRequestEvent, rejectRequestEvent;

    public PendingFriendsRequestsAdapter(Context context, List<UserDB> users, List<Runnable> clickOnUserEvent,
                                         List<Runnable> acceptRequestEvent, List<Runnable> rejectRequestEvent) {
        this.context = context;
        this.users = users;
        this.clickOnUserEvent = clickOnUserEvent;
        this.acceptRequestEvent = acceptRequestEvent;
        this.rejectRequestEvent = rejectRequestEvent;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.friend_notification_container, parent, false);
        return new PendingFriendsRequestsAdapter.FriendNotificationHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getClass() != PendingFriendsRequestsAdapter.FriendNotificationHolder.class)
            throw new IllegalArgumentException("Holder non valido!");

        PendingFriendsRequestsAdapter.FriendNotificationHolder notificationHolder = (PendingFriendsRequestsAdapter.FriendNotificationHolder)holder;
        notificationHolder.nameTextView.setText(users.get(position).getNome());
        notificationHolder.surnameTextView.setText(users.get(position).getCognome());
        notificationHolder.usernameTextView.setText(users.get(position).getUsername());

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

        //Set action listener
        notificationHolder.userCardView.setOnClickListener(addListenerForFriendDetails(position));

        notificationHolder.acceptFriendRequestButton.setOnClickListener(addListenerToAcceptFriendRequest(position));
        notificationHolder.rejectFriendRequestButton.setOnClickListener(addListenerToRejectFriendRequest(position));
    }

    @NonNull
    private View.OnClickListener addListenerForFriendDetails(int position) {
        return v -> {
            try{
                clickOnUserEvent.get(position).run();
            } catch(NullPointerException ignore) {}
        };
    }

    @NonNull
    private View.OnClickListener addListenerToAcceptFriendRequest(int position) {
        return v -> {
            try{
                Runnable r = acceptRequestEvent.get(position);
                deleteItem(position);
                new Thread(r).start();
            } catch(NullPointerException ignore) {}
        };
    }

    @NonNull
    private View.OnClickListener addListenerToRejectFriendRequest(int position) {
        return v -> {
            try{
                Runnable r = rejectRequestEvent.get(position);
                deleteItem(position);
                new Thread(r).start();
            } catch(NullPointerException ignore) {}
        };
    }

    private void deleteItem(int position) {
        users.remove(position);
        clickOnUserEvent.remove(position);
        acceptRequestEvent.remove(position);
        rejectRequestEvent.remove(position);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, users.size());

        if(users.size() == 0)
            NotificationsController.getNotificationControllerInstance().showEmptyNotificationsPage();
    }

    @Override
    public long getItemId(int position) {
        return clickOnUserEvent.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private static class FriendNotificationHolder extends RecyclerView.ViewHolder{

        TextView nameTextView,
                 surnameTextView,
                 usernameTextView;
        ImageView profilePictureImageView,
                  acceptFriendRequestButton,
                  rejectFriendRequestButton;
        CardView userCardView;

        private FriendNotificationHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.notificationFriendName);
            surnameTextView = itemView.findViewById(R.id.notificationFriendSurname);
            usernameTextView = itemView.findViewById(R.id.notificationFriendUsername);
            profilePictureImageView = itemView.findViewById(R.id.notificationFriendProfileImage);
            acceptFriendRequestButton = itemView.findViewById(R.id.acceptFriendRequestButton);
            rejectFriendRequestButton = itemView.findViewById(R.id.rejectFriendRequestButton);
            userCardView = itemView.findViewById(R.id.friendNotificationCardView);
        }
    }
}
