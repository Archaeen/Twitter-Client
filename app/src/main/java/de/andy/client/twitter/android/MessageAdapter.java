package de.andy.client.twitter.android;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MessageAdapter extends ArrayAdapter<Tweet> implements Filterable {

    private final ArrayList<Tweet> originalTweetArrayList;
    private Activity activity;
    private ArrayList<Tweet> tweetArrayList;
    private Filter tweetFilter;

    public MessageAdapter(Activity activity, ArrayList<Tweet> tweetArrayList) {
        super(activity, R.layout.list_items, tweetArrayList);
        this.activity = activity;
        this.tweetArrayList = tweetArrayList;
        this.originalTweetArrayList = tweetArrayList;
    }

    @Override
    public Filter getFilter() {
        if (tweetFilter == null) {
            tweetFilter = new TweetFilter();
        }
        return tweetFilter;
    }

    @Override
    public Tweet getItem(int position) {
        return tweetArrayList.get(position);
    }

    static class ViewHolder {
        @InjectView(R.id.tv_user_list) TextView tvUser;
        @InjectView(R.id.tv_content_list) TextView tvContent;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public View getView(int position, View view, ViewGroup parent) {
        View rowView = view;
        ViewHolder viewHolder;

        if (rowView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.list_items, null);

            viewHolder = new ViewHolder(rowView);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        viewHolder.tvUser.setText(tweetArrayList.get(position).getUser());
        viewHolder.tvContent.setText(tweetArrayList.get(position).getContent());

        return rowView;
    }

    @Override
    public int getCount() {
        return tweetArrayList.size();
    }

    private class TweetFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (!TextUtils.isEmpty(constraint)) {
                ArrayList<Tweet> filterTweetArrayList = new ArrayList<>();

                for (int i = 0; i < originalTweetArrayList.size(); i++) {
                    if (originalTweetArrayList.get(i).getUser().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                        originalTweetArrayList.get(i).getContent().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filterTweetArrayList.add(originalTweetArrayList.get(i));
                    }
                }

                results.values = filterTweetArrayList;
                results.count = filterTweetArrayList.size();

            } else {
                results.values = originalTweetArrayList;
                results.count = originalTweetArrayList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            tweetArrayList = (ArrayList<Tweet>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}
