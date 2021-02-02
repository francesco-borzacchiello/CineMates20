package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.UserController;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class UserActivity extends AppCompatActivity {
    private UserController userController;
    private ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        userController = UserController.getUserControllerInstance();
        userController.setUserActivity(this);

        initializeGraphicsComponents();
    }

    private void initializeGraphicsComponents() {
        if(userController.isUserFriendshipPending() || !userController.isFriendProfile())
            setContentView(R.layout.activity_user);
        else
            setContentView(R.layout.activity_user_friend_profile);

        profilePicture = findViewById(R.id.userProfilePicture);
        initializeUser(userController.getActualUser());
        setUserToolbar();
        setButtonListeners();
    }

    private void initializeUser(@NotNull UserDB user) {
        TextView nomeTextView = findViewById(R.id.user_name);
        nomeTextView.setSelected(true);
        TextView usernameTextView = findViewById(R.id.user_username);
        usernameTextView.setSelected(true);

        String fullName = user.getNome() + " " + user.getCognome();
        String username = "@" + user.getUsername();

        runOnUiThread(() -> {
            nomeTextView.setText(fullName);
            usernameTextView.setText(username);

            String profilePictureUrl = userController.getActualUserProfilePictureUrl();
            if(profilePictureUrl != null)
                Picasso.get().load(profilePictureUrl).memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE).resize(75, 75).noFade()
                    .into(profilePicture,
                          new Callback() {
                              @Override
                              public void onSuccess() {
                                  profilePicture.setAlpha(0f);
                                  profilePicture.animate().setDuration(100).alpha(1f).start();
                              }

                              @Override
                              public void onError(Exception e) {}
                          });
        });
    }

    private void setUserToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            ab.setTitle(userController.getActualUser().getUsername());
        }
    }

    private void setButtonListeners() {
        if(userController.isFriendProfile()) {
            Button removeFriendButton = findViewById(R.id.removeFriendButton);
            removeFriendButton.setOnClickListener(userController.getRemoveFriendOnClickListener());
            Button joinedMoviesButton = findViewById(R.id.joinedMoviesButton);
            joinedMoviesButton.setOnClickListener(userController.getJoinedMoviesOnClickListener());
        }
        else if(!userController.isUserFriendshipPending()) {
                Button addUserButton = findViewById(R.id.addUserButton);
                addUserButton.setOnClickListener(userController.getAddFriendOnClickListener());
        }
        else
            disableAddFriendButton();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = userController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(this, "Si è verificato un errore");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);

        menu.findItem(R.id.searchItem).setVisible(false);
        menu.findItem(R.id.shareItem).setVisible(false);
        menu.findItem(R.id.notificationItem).setIcon(R.drawable.report_user_icon);
        menu.findItem(R.id.notificationItem).setTitle("Segnala");

        menu.findItem(R.id.notificationItem).setOnMenuItemClickListener(userController.getReportItemOnClickListener());

        return true;
    }

    public void disableAddFriendButton() {
        Button addUserButton = findViewById(R.id.addUserButton);
        runOnUiThread(()-> {
            addUserButton.setEnabled(false);
            addUserButton.setBackgroundResource(R.drawable.add_friend_button_disabled);
        });
    }
}