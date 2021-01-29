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

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.controller.NotificationController;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class InformationActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Menu menu;

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

        HomeController.getHomeControllerInstance().setInformationActivity(this);

        initializeGraphicsComponents();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_information);
        setToolbar();

        configureNavigationDrawer(User.getLoggedUser(this));
    }

    private void configureNavigationDrawer(UserDB user) {
        drawerLayout = findViewById(R.id.informationDrawerLayout);
        navigationView = findViewById(R.id.informationNavigationView);
        navigationView.setItemIconTintList(null);

        if(user == null) return;

        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);

        runOnUiThread(() -> {
            nomeTextView.setText(user.getNome());
            cognomeTextView.setText(user.getCognome());
        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setTitle("Informazioni");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);
        menu.findItem(R.id.searchItem).setVisible(false);
        setUpNotificationIcon(menu);
        setNavigationViewActionListener();

        this.menu = menu;

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
                    Utilities.stampaToast(InformationActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
                }
                return false;
            }
        );
    }

    private void setUpNotificationIcon(@NotNull Menu menu) {
        new Thread(()-> runOnUiThread(()-> {
            MenuItem notificationItem = menu.findItem(R.id.notificationItem);
            if(User.getTotalUserNotificationCount() > 0)
                notificationItem.setIcon(R.drawable.ic_notifications_on);
            else
                notificationItem.setIcon(R.drawable.ic_notifications);
        })).start();
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            runOnUiThread(()-> drawerLayout.openDrawer(GravityCompat.START));
        else if(item.getItemId() == R.id.notificationItem &&
                !Utilities.checkNullActivityOrNoConnection(this)) {
            NotificationController.getNotificationControllerInstance().start(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(menu != null)
            setUpNotificationIcon(menu);
    }

    public void closeDrawerLayout() {
        if(drawerLayout != null)
            runOnUiThread(()-> drawerLayout.closeDrawer(GravityCompat.START));
    }
}