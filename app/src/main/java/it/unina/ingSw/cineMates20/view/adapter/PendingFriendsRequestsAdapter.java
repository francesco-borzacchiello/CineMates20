package it.unina.ingSw.cineMates20.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.NotificationController;
import it.unina.ingSw.cineMates20.model.S3Manager;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.activity.NotificationActivity;

public class PendingFriendsRequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final NotificationActivity notificationActivity;
    private final List<UserDB> users;
    private final List<Runnable> clickOnUserEvent, acceptRequestEvent, rejectRequestEvent;

    public PendingFriendsRequestsAdapter(NotificationActivity notificationActivity, List<UserDB> users, List<Runnable> clickOnUserEvent,
                                         List<Runnable> acceptRequestEvent, List<Runnable> rejectRequestEvent) {
        this.notificationActivity = notificationActivity;
        this.users = users;
        this.clickOnUserEvent = clickOnUserEvent;
        this.acceptRequestEvent = acceptRequestEvent;
        this.rejectRequestEvent = rejectRequestEvent;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(notificationActivity);
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

        String profilePictureUrl = S3Manager.getProfilePictureUrl(users.get(position).getEmail());
        if(profilePictureUrl != null)
            Picasso.get().load(profilePictureUrl).memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE).resize(60, 60)
                    .noFade().into(notificationHolder.profilePictureImageView,
                    new Callback() {
                        @Override
                        public void onSuccess() {
                            notificationHolder.profilePictureImageView.setAlpha(0f);
                            notificationHolder.profilePictureImageView.animate().setDuration(300).alpha(1f).start();
                        }
                        @Override
                        public void onError(Exception e) {}
                    });


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
                new Thread(r).start();
            } catch(NullPointerException ignore) {}
        };
    }

    @NonNull
    private View.OnClickListener addListenerToRejectFriendRequest(int position) {
        return v -> {
            try{
                Runnable r = rejectRequestEvent.get(position);
                new Thread(r).start();
            } catch(NullPointerException ignore) {}
        };
    }

    public void deleteItem(UserDB user) {
        int[] position = new int[1];
        for(UserDB utente: users) {
            if(utente.getEmail().equals(user.getEmail()))
                break;
            position[0]++;
        }

        if(position[0] == users.size())
            return;

        users.remove(position[0]);
        clickOnUserEvent.remove(position[0]);
        acceptRequestEvent.remove(position[0]);
        rejectRequestEvent.remove(position[0]);

        notificationActivity.runOnUiThread(()-> notifyItemRemoved(position[0]));
        notificationActivity.runOnUiThread(()-> notifyItemRangeChanged(position[0], users.size()));

        if(users.size() == 0)
            NotificationController.getNotificationControllerInstance().showEmptyFriendsNotificationPage(true);
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
