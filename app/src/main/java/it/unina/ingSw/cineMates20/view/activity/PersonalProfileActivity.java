package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.List;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.PersonalProfileController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class PersonalProfileActivity extends AppCompatActivity {
    private PersonalProfileController personalProfileController;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        OnBackPressedCallback callback = new OnBackPressedCallback(true ) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(0,0);
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        personalProfileController = PersonalProfileController.getPersonalProfileControllerInstance();
        personalProfileController.setPersonalProfileActivity(this);
        HomeController.getHomeControllerInstance().setUserProfileActivity(this);

        initializeGraphicsComponents();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_personal_profile);
        setToolbar();

        TextView nomeTextView = findViewById(R.id.name_personal_profile);
        TextView usernameTextView = findViewById(R.id.username_personal_profile);
        TextView emailTextView = findViewById(R.id.email_personal_profile);

        List<String> userInfo = Utilities.getCurrentUserInformations(this);

        if(userInfo.size() > 3) {
            String fullName = userInfo.get(0) + " " + userInfo.get(1);
            nomeTextView.setText(fullName);
            String username = "@" + userInfo.get(2);
            usernameTextView.setText(username);
            emailTextView.setText(userInfo.get(3));
        }

        configureNavigationDrawer(userInfo);

        //TODO: set listener su ImageView "profileAvatarMenu" per la modifica della foto
    }

    private void configureNavigationDrawer(List<String> userInfo) {
        drawerLayout = findViewById(R.id.userProfileDrawerLayout);
        navigationView = findViewById(R.id.userProfileNavigationView);
        navigationView.setItemIconTintList(null);

        if(userInfo == null || userInfo.size() < 2) return;

        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);

        runOnUiThread(() -> {
            nomeTextView.setText(userInfo.get(0));
            cognomeTextView.setText(userInfo.get(1));
        });
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
        menu.findItem(R.id.notificationItem).setVisible(false);

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
            drawerLayout.closeDrawer(GravityCompat.START);
    }

    public void openDrawerLayout() {
        if(drawerLayout != null)
            drawerLayout.openDrawer(GravityCompat.START);
    }
}