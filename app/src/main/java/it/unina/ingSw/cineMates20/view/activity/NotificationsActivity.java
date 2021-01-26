package it.unina.ingSw.cineMates20.view.activity;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.NotificationsController;
import it.unina.ingSw.cineMates20.view.fragment.PendingFriendsNotificationsFragment;

public class NotificationsActivity extends AppCompatActivity {

    private NotificationsController notificationsController;
    private PendingFriendsNotificationsFragment pendingFriendsNotificationsFragment;
    private BadgeDrawable badgeDrawableTabFriends,
                          badgeDrawableTabReports;

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

        notificationsController = NotificationsController.getNotificationControllerInstance();
        notificationsController.setNotificationsActivity(this);

        initializeGraphicsComponents();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_notification);

        ViewPager2 viewPager2 = findViewById(R.id.notificationsViewPager);
        viewPager2.setAdapter(notificationsController.getViewPager2Adapter());

        TabLayout tabLayout = findViewById(R.id.notificationsTabLayout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2,
            (tab, position) -> {
                if(position == 0) {
                    tab.setText("Richieste di amicizia");
                    tab.setIcon(R.drawable.ic_pending);
                    badgeDrawableTabFriends = tab.getOrCreateBadge();
                    badgeDrawableTabFriends.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    badgeDrawableTabFriends.setBadgeTextColor(ContextCompat.getColor(getApplicationContext(), R.color.lightBlue));
                    badgeDrawableTabFriends.setMaxCharacterCount(3);
                }
                else {
                    tab.setText("Segnalazioni");
                    tab.setIcon(R.drawable.ic_report);
                    badgeDrawableTabReports = tab.getOrCreateBadge();
                    badgeDrawableTabReports.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    badgeDrawableTabReports.setBadgeTextColor(ContextCompat.getColor(getApplicationContext(), R.color.lightBlue));
                    badgeDrawableTabReports.setVisible(true);
                    badgeDrawableTabReports.setNumber(134);
                    badgeDrawableTabReports.setMaxCharacterCount(3);
                }
            }
        );
        tabLayoutMediator.attach();
    }

    public void decreaseFriendsNotificationsBadgeNumber() {
        badgeDrawableTabFriends.setNumber(badgeDrawableTabFriends.getNumber()-1);
        if(badgeDrawableTabFriends.getNumber() == 0)
            badgeDrawableTabFriends.setVisible(false);
    }

    public void initializeNotificationsFriendRequestsAdapter(PendingFriendsNotificationsFragment fragment) {
        pendingFriendsNotificationsFragment = fragment;
        notificationsController.initializeNotificationsFriendRequestsAdapter();
    }

    public void setFriendsNotificationsRecyclerView(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        pendingFriendsNotificationsFragment.setFriendsNotificationsRecyclerView(adapter);

        badgeDrawableTabFriends.setVisible(true);
        badgeDrawableTabFriends.setNumber(adapter.getItemCount());
    }

    public void showEmptyNotificationsPage() {
        pendingFriendsNotificationsFragment.showEmptyNotificationsPage();
    }
}