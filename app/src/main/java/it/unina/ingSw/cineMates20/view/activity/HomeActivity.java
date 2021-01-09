package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class HomeActivity extends AppCompatActivity {


    private NavigationView navigationView;
    private DrawerLayout homeDrawerLayout;
    private HomeController homeController;
    private Toolbar toolbar;
    private SearchView searchView;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        homeController = HomeController.getHomeControllerInstance();
        homeController.setHomeActivity(this);

        initializeGraphicsComponents();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_home);

        setToolBar();
        configureNavigationDrawer();
    }

    private void setToolBar() {
        toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setTitle("Home");
        }
    }

    private void configureNavigationDrawer() {
        homeDrawerLayout = findViewById(R.id.navMenuDrawerLayout);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setItemIconTintList(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);

        searchView = (SearchView) menu.findItem(R.id.searchItem).getActionView();
        searchView.setQueryHint("Cerca un film");

        searchView.setOnQueryTextListener(homeController.getSearchViewOnQueryTextListener());

        setNavigationViewActionListener();
        this.menu = menu;

        return true;
    }

    private void setNavigationViewActionListener() {
        //Listener per icone del NavigationView
        navigationView.setNavigationItemSelectedListener(
                item -> {
                    Runnable r = homeController.getNavigationViewOnOptionsItemSelected(item.getItemId());
                    try {
                        r.run();
                    }catch(NullPointerException e){
                        Utilities.stampaToast(HomeActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
                    }
                    return false;
                }
        );
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = homeController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(HomeActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
        }

        return super.onOptionsItemSelected(item);
    }

    public DrawerLayout getDrawerLayout() {
        return homeDrawerLayout;
    }

    public void hideSearchView() {
        menu.findItem(R.id.searchItem).collapseActionView();
        searchView.setIconified(true);
        searchView.clearFocus();
    }
}