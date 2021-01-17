package it.unina.ingSw.cineMates20.view.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.material.navigation.NavigationView;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.FriendsController;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.view.adapter.FriendsAdapter;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class FriendsActivity extends AppCompatActivity {
    private FriendsController friendsController;
    private NavigationView navigationView;
    private DrawerLayout friendsDrawerLayout;
    private Toolbar toolbar;
    private SearchView searchView;
    private Menu menu;
    private ProgressBar progressBar;
    private RecyclerView friendsRecyclerView;
    //Nota: tasto "aggiungi amico" verrà probabilmente rimosso dal mockup

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

        friendsController = FriendsController.getFriendsControllerInstance();
        friendsController.setFriendsActivity(this);
        HomeController.getHomeControllerInstance().setFriendsActivity(this);

        initializeGraphicsComponents();
        configureNavigationDrawer();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_friends);
        setFriendsToolbar();

        progressBar = findViewById(R.id.progressBarFriends);
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);

        friendsController.initializeActivityFriends();
    }

    private void setFriendsToolbar() {
        toolbar = findViewById(R.id.toolbarHeader);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setTitle("Amici");
        }
    }

    private void configureNavigationDrawer() {
        friendsDrawerLayout = findViewById(R.id.friendsDrawerLayout);
        navigationView = findViewById(R.id.friendsNavigationView);
        navigationView.setItemIconTintList(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);

        searchView = (SearchView) menu.findItem(R.id.searchItem).getActionView();
        searchView.setQueryHint("Cerca un utente");

        searchView.setOnQueryTextListener(friendsController.getSearchViewOnQueryTextListener());
        searchView.setOnQueryTextFocusChangeListener(friendsController.getSearchViewOnQueryTextFocusChangeListener());
        searchView.setOnSearchClickListener(friendsController.getOnSearchClickListener());

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
                        Utilities.stampaToast(FriendsActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
                    }
                    return false;
                }
        );
    }

    //Listener per icone della toolbar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Runnable onOptionsItemSelected = friendsController.getOnOptionsItemSelected(item.getItemId());
        try {
            onOptionsItemSelected.run();
            return false;
        }catch(NullPointerException e) {
            Utilities.stampaToast(FriendsActivity.this, "Si è verificato un errore.\nRiprova tra qualche minuto");
        }

        return super.onOptionsItemSelected(item);
    }

    public void hideFriendsProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void showFriendsProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void keepSearchViewExpanded() {
        if(searchView != null) {
            searchView.clearFocus();
            menu.findItem(R.id.searchItem).expandActionView(); //Verrà collassata in onResume()
        }
    }

    public void setLayoutsForFriends(boolean searchIsExpanded) {
        LinearLayout ll = findViewById(R.id.friendsLinearLayout);
        ConstraintLayout cl = findViewById(R.id.friendsConstraintLayout);

        if(searchIsExpanded) {
            ll.setVisibility(View.INVISIBLE);
            cl.setBackgroundColor(getResources().getColor(R.color.lightGray));
        }
        else {
            ll.setVisibility(View.VISIBLE);
            cl.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menu != null && searchView != null) {
            menu.findItem(R.id.searchItem).collapseActionView();
            searchView.setIconified(true);
            searchView.clearFocus();
        }
    }

    public void setFriendsRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        friendsRecyclerView.setAdapter(adapter);

        friendsRecyclerView.setHasFixedSize(true);
        friendsRecyclerView.setItemViewCacheSize(30);

        friendsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
    }

    public void closeDrawerLayout() {
        friendsDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public void openDrawerLayout() {
        friendsDrawerLayout.openDrawer(GravityCompat.START);
    }
}