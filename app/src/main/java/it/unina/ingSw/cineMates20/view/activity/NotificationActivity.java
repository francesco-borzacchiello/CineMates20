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

import org.jetbrains.annotations.NotNull;

import it.unina.ingSw.cineMates20.R;
import it.unina.ingSw.cineMates20.controller.NotificationController;
import it.unina.ingSw.cineMates20.view.adapter.ReportNotificationAdapter;
import it.unina.ingSw.cineMates20.view.fragment.PendingFriendsNotificationFragment;
import it.unina.ingSw.cineMates20.view.fragment.ReportNotificationFragment;

public class NotificationActivity extends AppCompatActivity {

    private NotificationController notificationController;
    private PendingFriendsNotificationFragment pendingFriendsNotificationFragment;
    private ReportNotificationFragment reportNotificationFragment;
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

        notificationController = NotificationController.getNotificationControllerInstance();
        notificationController.setNotificationActivity(this);

        initializeGraphicsComponents();
    }

    private void initializeGraphicsComponents() {
        setContentView(R.layout.activity_notification);

        ViewPager2 viewPager2 = findViewById(R.id.notificationsViewPager);
        viewPager2.setAdapter(notificationController.getViewPager2Adapter());

        /* Forza pre-caricamento di entrambe le pagine disponibili,
           il numero deve essere maggiore della metà dei fragment disponibili (2) */
        viewPager2.setOffscreenPageLimit(3);

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
                    badgeDrawableTabFriends.setVisible(false);
                }
                else {
                    tab.setText("Segnalazioni");
                    tab.setIcon(R.drawable.ic_report);
                    badgeDrawableTabReports = tab.getOrCreateBadge();
                    badgeDrawableTabReports.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                    badgeDrawableTabReports.setBadgeTextColor(ContextCompat.getColor(getApplicationContext(), R.color.lightBlue));
                    //badgeDrawableTabReports.setNumber(134);
                    badgeDrawableTabReports.setMaxCharacterCount(3);
                    badgeDrawableTabReports.setVisible(false);
                }
            }
        );
        tabLayoutMediator.attach();
    }

    public void decreaseFriendsNotificationsBadgeNumber() {
        runOnUiThread(()-> {
            badgeDrawableTabFriends.setNumber(badgeDrawableTabFriends.getNumber() - 1);
            if (badgeDrawableTabFriends.getNumber() == 0)
                badgeDrawableTabFriends.setVisible(false);
        });
    }

    public void decreaseReportNotificationsBadgeNumber() {
        runOnUiThread(()-> {
            badgeDrawableTabReports.setNumber(badgeDrawableTabReports.getNumber() - 1);
            if (badgeDrawableTabReports.getNumber() == 0)
                badgeDrawableTabReports.setVisible(false);
        });
    }

    public void initializeFriendRequestsNotificationAdapter(PendingFriendsNotificationFragment fragment) {
        pendingFriendsNotificationFragment = fragment;
        notificationController.initializeFriendRequestsNotificationAdapter();
    }

    public void initializeReportNotificationAdapter(ReportNotificationFragment fragment) {
        reportNotificationFragment = fragment;
        notificationController.initializeReportNotificationAdapter();
    }

    public void setFriendsNotificationsRecyclerView(@NotNull RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        if(adapter.getItemCount() == 0)
            showEmptyFriendsNotificationPage(true);
        else
            runOnUiThread(() -> {
                pendingFriendsNotificationFragment.setFriendsNotificationRecyclerView(adapter);

                if (adapter.getItemCount() > 0) {
                    badgeDrawableTabFriends.setVisible(true);
                    badgeDrawableTabFriends.setNumber(adapter.getItemCount());
                }
            });
    }

    public void setReportNotificationsRecyclerView(@NotNull ReportNotificationAdapter adapter) {
        if(adapter.getItemCount() == 0)
            showEmptyReportsNotificationPage(true);
        else
            runOnUiThread(()-> {
                reportNotificationFragment.setReportNotificationRecyclerView(adapter);

                if(adapter.getItemCount() > 0) {
                    badgeDrawableTabReports.setVisible(true);
                    badgeDrawableTabReports.setNumber(adapter.getItemCount());
                }
            });
    }

    public void showEmptyFriendsNotificationPage(boolean show) {
        pendingFriendsNotificationFragment.showEmptyNotificationsPage(show);
    }

    public void showEmptyReportsNotificationPage(boolean show) {
        reportNotificationFragment.showEmptyNotificationsPage(show);
    }
}