package it.unina.ingSw.cineMates20.view.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.unina.ingSw.cineMates20.BuildConfig;
import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.PersonalProfileController;
import it.unina.ingSw.cineMates20.controller.SettingsController;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class PersonalProfileActivity extends AppCompatActivity {
    private PersonalProfileController personalProfileController;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Uri profileImageUri;
    private ImageView profilePictureImageView;
    private ImageView navMenuProfilePictureImageView;
    private MenuItem notificationItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        personalProfileController = PersonalProfileController.getPersonalProfileControllerInstance();
        personalProfileController.setPersonalProfileActivity(this);
        HomeController.getHomeControllerInstance().setUserProfileActivity(this);

        initializeGraphicsComponents();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(drawerLayout.isOpen())
                    closeDrawerLayout();
                else {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_personal_profile);
        setToolbar();

        TextView nomeTextView = findViewById(R.id.name_personal_profile);
        nomeTextView.setSelected(true);
        TextView usernameTextView = findViewById(R.id.username_personal_profile);
        usernameTextView.setSelected(true);
        TextView emailTextView = findViewById(R.id.email_personal_profile);
        emailTextView.setSelected(true);

        runOnUiThread(() -> {
            String fullName = User.getLoggedUser(this).getNome() + " " + User.getLoggedUser(this).getCognome();
            nomeTextView.setText(fullName);
            String username = "@" + User.getLoggedUser(this).getUsername();
            usernameTextView.setText(username);
            emailTextView.setText(User.getLoggedUser(this).getEmail());
        });

        profilePictureImageView = findViewById(R.id.profileAvatarMenu);
        ImageView pencilImageView = findViewById(R.id.editAvatarPencilMenu);

        profilePictureImageView.setOnClickListener(personalProfileController.getEditProfilePictureOnClickListener());
        pencilImageView.setOnClickListener(personalProfileController.getEditProfilePictureOnClickListener());

        String profilePictureUrl = User.getUserProfilePictureUrl();
        configureNavigationDrawer(profilePictureUrl);
        refreshProfilePicture(profilePictureUrl);
    }

    private void configureNavigationDrawer(String profilePictureUrl) {
        drawerLayout = findViewById(R.id.userProfileDrawerLayout);
        navigationView = findViewById(R.id.userProfileNavigationView);
        navigationView.setItemIconTintList(null);

        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);
        navMenuProfilePictureImageView = navigationView.getHeaderView(0).findViewById(R.id.imageProfile);

        runOnUiThread(() -> {
            nomeTextView.setText(User.getLoggedUser(this).getNome());
            cognomeTextView.setText(User.getLoggedUser(this).getCognome());

            refreshProfilePicture(profilePictureUrl);
        });
    }

    private void refreshProfilePicture(String imageUrl) {
        if(imageUrl != null) {
            Picasso.get().load(imageUrl).memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE).resize(75, 75).noFade()
                    .into(navMenuProfilePictureImageView,
                            new Callback() {
                                @Override
                                public void onSuccess() {}

                                @Override
                                public void onError(Exception e) {}
                            });

            Picasso.get().load(imageUrl).memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE).resize(140, 140).noFade()
                    .into(profilePictureImageView,
                            new Callback() {
                                @Override
                                public void onSuccess() {
                                    profilePictureImageView.setAlpha(0f);
                                    profilePictureImageView.animate().setDuration(100).alpha(1f).start();
                                }

                                @Override
                                public void onError(Exception e) {}
                            });
        }
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setTitle("Profilo");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);
        menu.findItem(R.id.searchItem).setVisible(false);
        setNavigationViewActionListener();

        notificationItem = menu.findItem(R.id.notificationItem);

        if(SettingsController.getSettingsControllerInstance().isNotificationSyncEnabled()) {
            ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);
            scheduleTaskExecutor.scheduleAtFixedRate(this::setUpNotificationIcon, 0, 15, TimeUnit.SECONDS);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == personalProfileController.getPickImageCode() && resultCode == RESULT_OK) {
            profileImageUri = data.getData();

            profilePictureImageView.setImageURI(profileImageUri);
            navMenuProfilePictureImageView.setImageURI(profileImageUri);
            personalProfileController.uploadNewProfilePicture();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == personalProfileController.getPickImageCode()) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                personalProfileController.launchGalleryIntentPicker();
            else if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Hai rifiutato la concessione di questa autorizzazione.\n" +
                                "Devi concedere il permesso in \"Permessi\" nelle impostazioni\n del tuo dispositivo, prima di poter selezionare una foto.",
                        Snackbar.LENGTH_LONG).setAction("Impostazioni", view ->
                        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.parse("package:" + BuildConfig.APPLICATION_ID))));

                View snackbarView = snackbar.getView();
                TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setMaxLines(6);
                snackbar.setDuration(6000);
                snackbar.show();
            }
        }
        else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public Uri getProfileImageUri() {
        return profileImageUri;
    }

    private void setUpNotificationIcon() {
        new Thread(()-> runOnUiThread(()-> {
            if(User.getTotalUserNotificationCount() > 0)
                notificationItem.setIcon(R.drawable.ic_notifications_on);
            else
                notificationItem.setIcon(R.drawable.ic_notifications);
        })).start();
    }

    private void setNavigationViewActionListener() {
        //Listener per icone del NavigationView
        navigationView.setNavigationItemSelectedListener(
            item -> {
                Runnable r = HomeController.getHomeControllerInstance().getNavigationViewOnOptionsItemSelected(this, item.getItemId());
                try {
                    r.run();
                }catch(NullPointerException e){
                    Utilities.stampaToast(PersonalProfileActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
                }
                return false;
            }
        );
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = personalProfileController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(PersonalProfileActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
        }

        return super.onOptionsItemSelected(item);
    }

    public void closeDrawerLayout() {
        if(drawerLayout != null)
            runOnUiThread(()-> drawerLayout.closeDrawer(GravityCompat.START));
    }

    public void openDrawerLayout() {
        if(drawerLayout != null)
            runOnUiThread(()-> drawerLayout.openDrawer(GravityCompat.START));
    }
}