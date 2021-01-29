package it.unina.ingSw.cineMates20.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import it.unina.ingSw.cineMates20.view.fragment.PendingFriendsNotificationFragment;
import it.unina.ingSw.cineMates20.view.fragment.ReportNotificationFragment;

public class NotificationPagerAdapter extends FragmentStateAdapter {

    public NotificationPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PendingFriendsNotificationFragment();
        }
        return new ReportNotificationFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
