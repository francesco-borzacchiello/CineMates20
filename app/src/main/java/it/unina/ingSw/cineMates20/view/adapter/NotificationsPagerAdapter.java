package it.unina.ingSw.cineMates20.view.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import it.unina.ingSw.cineMates20.view.fragment.PendingFriendsNotificationsFragment;
import it.unina.ingSw.cineMates20.view.fragment.ReportNotificationsFragment;

public class NotificationsPagerAdapter extends FragmentStateAdapter {

    public NotificationsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PendingFriendsNotificationsFragment();
        }
        return new ReportNotificationsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
