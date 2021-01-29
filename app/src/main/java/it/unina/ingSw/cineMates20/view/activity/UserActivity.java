package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.UserController;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class UserActivity extends AppCompatActivity {
    private UserController userController;

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
        //ImageView profilePicture = findViewById(R.id.userProfilePicture);

        if(userController.isParentFriendsActivity()) {
            setContentView(R.layout.activity_user_friend_profile);
        }
        else if(userController.isParentNotificationsActivity()) {
            setContentView(R.layout.activity_user);
        }
        else { //parent è SearchFriendsActivity
            //if(userController.isUserFriendshipPending() || !userController.isFriendProfile())
                setContentView(R.layout.activity_user);
            //else setContentView(R.layout.activity_user_friend_profile);
        }

        initializeUser(userController.getActualUser());
        setUserToolbar();
        setButtonListeners();
    }

    private void initializeUser(@NotNull UserDB user) {
        TextView nome = findViewById(R.id.user_name);
        TextView username = findViewById(R.id.user_username);

        String fullName = user.getNome() + " " + user.getCognome();
        nome.setText(fullName);

        username.setText(user.getUsername());

        /*TODO: Occorrerà settare anche immagine tramite un metodo di UserDB che restituisce il link,
                se anche il link della foto fosse su Cognito, se ne occuperà FriendsAdapter di
                settarlo correttamente in uno UserDB */
        //...set foto profilePicture tramite user.getImageLink()
    }

    private void setUserToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            ab.setTitle("Username"); //TODO: da modificare con username reale preso dal controller da UserDB
        }
    }

    private void setButtonListeners() {
        if(userController.isFriendProfile()) {
            Button removeFriendButton = findViewById(R.id.removeFriendButton);
            removeFriendButton.setOnClickListener(userController.getRemoveFriendOnClickListener());
            Button joinedMoviesButton = findViewById(R.id.joinedMoviesButton);
            joinedMoviesButton.setOnClickListener(userController.getJoinedMoviesOnClickListener());
        }
        else {
            //if(!userController.isUserFriendshipPending()) //Vale anche per notificationsActivity quando si clicca su un utente in attesa di essere accettato/rifiutato
                Button addUserButton = findViewById(R.id.addUserButton);
                addUserButton.setOnClickListener(userController.getAddFriendOnClickListener());
            //else disableAddFriendButton()
        }
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