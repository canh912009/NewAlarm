package app.alarm.ui.detail.tone;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class AlarmTonePagerAdapter extends FragmentPagerAdapter {
    private int mNumOfTabs;

    public AlarmTonePagerAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.mNumOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                RingTonesFragment ringTonesFragment = new RingTonesFragment();
                return ringTonesFragment;
            case 1:
                SongsFragment songsFragment = new SongsFragment();
                return songsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
