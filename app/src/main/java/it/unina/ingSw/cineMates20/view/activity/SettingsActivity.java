package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.SettingsController;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.view.fragment.SettingsFragment;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class SettingsActivity extends AppCompatActivity {

    private SettingsController settingsController;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView fotoProfilo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsController = SettingsController.getSettingsControllerInstance();
        settingsController.setSettingsActivity(this);
        HomeController.getHomeControllerInstance().setSettingsActivity(this);

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
        setContentView(R.layout.settings_activity);
        setToolbar();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.frameLayoutFragmentImpostazioni, new SettingsFragment(this));
        transaction.commit();

        configureNavigationDrawer();
    }

    private void configureNavigationDrawer() {
        drawerLayout = findViewById(R.id.settingsDrawerLayout);
        navigationView = findViewById(R.id.settingsNavigationView);
        navigationView.setItemIconTintList(null);

        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);
        fotoProfilo = navigationView.getHeaderView(0).findViewById(R.id.imageProfile);

        runOnUiThread(() -> {
            nomeTextView.setText(User.getLoggedUser(this).getNome());
            cognomeTextView.setText(User.getLoggedUser(this).getCognome());

            String profilePictureUrl = User.getUserProfilePictureUrl();
            if(profilePictureUrl != null)
                refreshProfilePicture(profilePictureUrl);
        });
    }

    private void refreshProfilePicture(String imageUrl) {
        Picasso.get().load(imageUrl).memoryPolicy(MemoryPolicy.NO_CACHE)
                .networkPolicy(NetworkPolicy.NO_CACHE).resize(75, 75).noFade()
                .into(fotoProfilo,
                        new Callback() {
                            @Override
                            public void onSuccess() {}

                            @Override
                            public void onError(Exception e) {}
                        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setTitle("Impostazioni");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);
        menu.findItem(R.id.searchItem).setVisible(false);
        menu.findItem(R.id.notificationItem).setVisible(false);
        menu.findItem(R.id.shareItem).setVisible(false);
        setNavigationViewActionListener();

        return true;
    }

    private void setNavigationViewActionListener() {
        //Listener per icone del NavigationView
        navigationView.setNavigationItemSelectedListener(
                item -> {
                    Runnable r = HomeController.getHomeControllerInstance().getNavigationViewOnOptionsItemSelected(this, item.getItemId());
                    try {
                        r.run();
                    }catch(NullPointerException e){
                        Utilities.stampaToast(SettingsActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
                    }
                    return false;
                }
        );
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = settingsController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(SettingsActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        String profilePicUrl = User.getUserProfilePictureUrl();
        if(profilePicUrl != null)
            refreshProfilePicture(profilePicUrl);
    }

    public void closeDrawerLayout() {
        if(drawerLayout != null)
            runOnUiThread(()-> drawerLayout.closeDrawer(GravityCompat.START));
    }

    public void openDrawerLayout() {
        if(drawerLayout != null)
            runOnUiThread(()-> drawerLayout.openDrawer(GravityCompat.START));
    }

    public void filterPreferencesChanged(boolean enabled) {
        settingsController.setFilterPreference(enabled);
    }

    public void notificationPreferencesChanged(boolean enabled) {
        settingsController.setNotificationPreference(enabled);
    }
}