package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.FriendsController;
import it.unina.ingSw.cineMates20.controller.HomeController;
import it.unina.ingSw.cineMates20.model.User;
import it.unina.ingSw.cineMates20.model.UserDB;
import it.unina.ingSw.cineMates20.view.util.Utilities;

public class FriendsActivity extends AppCompatActivity {
    private FriendsController friendsController;
    private NavigationView navigationView;
    private DrawerLayout friendsDrawerLayout;
    private SearchView searchView;
    private Menu menu;
    private ProgressBar progressBar;
    private RecyclerView friendsRecyclerView;
    private TextView emptyFriendsListTextView;

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

        friendsController = FriendsController.getFriendsControllerInstance();
        friendsController.setFriendsActivity(this);
        HomeController.getHomeControllerInstance().setFriendsActivity(this);

        initializeGraphicsComponents();
        configureNavigationDrawer();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_friends);
        setToolbar();

        progressBar = findViewById(R.id.progressBarFriends);
        friendsRecyclerView = findViewById(R.id.friendsRecyclerView);
        emptyFriendsListTextView = findViewById(R.id.emptyFriendsListTextView);

        friendsController.initializeActivityFriendsAdapter();
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarHeader);
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

        TextView nomeTextView = navigationView.getHeaderView(0).findViewById(R.id.nomeUtenteNavMenu);
        TextView cognomeTextView = navigationView.getHeaderView(0).findViewById(R.id.cognomeUtenteNavMenu);

        runOnUiThread(() -> {
            UserDB user = User.getLoggedUser(this);
            nomeTextView.setText(user.getNome());
            cognomeTextView.setText(user.getCognome());
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.header_navigation_menu, menu);

        searchView = (SearchView) menu.findItem(R.id.searchItem).getActionView();
        searchView.setQueryHint("Cerca un utente");

        setUpNotificationIcon(menu);

        searchView.setOnQueryTextListener(friendsController.getSearchViewOnQueryTextListener());
        searchView.setOnQueryTextFocusChangeListener(friendsController.getSearchViewOnQueryTextFocusChangeListener());
        searchView.setOnSearchClickListener(friendsController.getOnSearchClickListener());

        setNavigationViewActionListener();
        this.menu = menu;

        return true;
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

    public void showFriendsProgressBar(boolean show) {
        runOnUiThread(()-> {
            if (show)
                progressBar.setVisibility(View.VISIBLE);
            else
                progressBar.setVisibility(View.INVISIBLE);
        });
    }

    public void keepSearchViewExpanded() {
        if(searchView != null) {
            runOnUiThread(()-> {
                searchView.clearFocus();
                menu.findItem(R.id.searchItem).expandActionView(); //Verrà collassata in onResume()
            });
        }
    }

    public void setLayoutsForFriends(boolean searchIsExpanded) {
        LinearLayout ll = findViewById(R.id.friendsLinearLayout);
        ConstraintLayout cl = findViewById(R.id.friendsConstraintLayout);

        runOnUiThread(()-> {
            if(searchIsExpanded) {
                ll.setVisibility(View.INVISIBLE);
                cl.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.lightGray));
            }
            else {
                ll.setVisibility(View.VISIBLE);
                cl.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            }
        });
    }

    //Nasconde la tastiera alla pressione di un elemento che non sia essa stessa o una text box
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean ret = super.dispatchTouchEvent(event);
        Utilities.hideKeyboard(this, event);
        return ret;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (menu != null && searchView != null) {
            runOnUiThread(()-> {
                menu.findItem(R.id.searchItem).collapseActionView();
                searchView.setIconified(true);
                searchView.clearFocus();
            });

            //La struttura potrebbe essere stata invalidata, per cui si reinizializza il RecyclerView degli amici
            friendsController.initializeActivityFriendsAdapter();
        }
        if(menu != null)
            setUpNotificationIcon(menu);
    }

    public void setFriendsRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        runOnUiThread(()-> {
            friendsRecyclerView.setAdapter(adapter);

            friendsRecyclerView.setItemViewCacheSize(30);

            friendsRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            friendsRecyclerView.setFocusable(false);
        });
    }

    public void closeDrawerLayout() {
        runOnUiThread(()-> friendsDrawerLayout.closeDrawer(GravityCompat.START));
    }

    public void openDrawerLayout() {
        runOnUiThread(()-> friendsDrawerLayout.openDrawer(GravityCompat.START));
    }

    public void showEmptyFriendsLayout(boolean show) {
        runOnUiThread(()-> {
            if(show)
                emptyFriendsListTextView.setVisibility(View.VISIBLE);
            else
                emptyFriendsListTextView.setVisibility(View.GONE);
        });
    }
}