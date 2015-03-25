package de.andy.client.twitter.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class MainFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        FragmentTabHost tabHost;

        tabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        tabHost.setup(this.getActivity(), getFragmentManager(), R.id.tab_content);

        tabHost.addTab(tabHost.newTabSpec("tweets").setIndicator(getString(R.string.main_fragment_tab_tweets_title)),
                       TweetFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("profile").setIndicator(getString(R.string.main_fragment_tab_profile_title)),
                       ProfileFragment.class, null);

        return view;
    }
}
