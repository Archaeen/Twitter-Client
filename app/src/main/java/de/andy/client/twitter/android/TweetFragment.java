package de.andy.client.twitter.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.os.Handler;
import android.widget.Toast;
import android.support.v4.app.Fragment;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;


public class TweetFragment extends Fragment implements ConnectionCallbacks,
                                                       OnConnectionFailedListener,
                                                       LocationListener {

    private int shownItems = 15;
    private int newItemsAmount = 0;
    private int countNewItems = 0;
    private int countDeletedItems = 0;
    private boolean checkNewItem = false;
    private boolean checkDeletedItem = false;
    private int endPosition = shownItems;
    private int itemCounter = 0;
    private int waitingTime = 2000;
    private boolean startCountdown = true;

    private ArrayList<Tweet> tweetArrayList = new ArrayList<Tweet>();
    private ArrayList<Tweet> shownTweetArrayList = new ArrayList<Tweet>();
    private MessageAdapter adapter;
    private InputStream is;
    private final String urlString = "http://www.json-generator.com/api/json/get/bVBCREcPZu";
    private JsonFactory jsonFactory = new JsonFactory();
    private View footer;

    private CharSequence filterCharSequence;

    public static final String INTENT_LATITUDE = "Latitude";
    public static final String INTENT_LONGITUDE = "Longitude";
    public static final String INTENT_USER = "User";
    public static final double NO_POSITION = 100;
    public static final String SAVE_TWEET_LIST = "Tweet List";

    private double lat, lng;
    private GoogleApiClient googleApiClient;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;

    private boolean checkIntentFilter = false;
    private String intentFilterText;
    private EditText etSearch;


    @InjectView(R.id.tweet_list_view)
    ListView tweetsView;

    protected class ViewHolder {
        @InjectView(R.id.et_new_message)
        EditText etMessage;
        @InjectView(R.id.et_new_user)
        EditText etUser;
        @InjectView(R.id.tv_counter)
        TextView tvCount;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location == null) {
            Toast.makeText(getActivity(),
                           getString(R.string.main_activity_searching_position_toast),
                           Toast.LENGTH_SHORT).show();
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } else {
            getCurrentLocation(location);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        getCurrentLocation(location);
    }

    private void getCurrentLocation(Location location) {
        Toast.makeText(getActivity(), getString(R.string.main_activity_found_position_toast), Toast.LENGTH_SHORT)
             .show();
        lat = location.getLatitude();
        lng = location.getLongitude();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            shownTweetArrayList = savedInstanceState.getParcelableArrayList(SAVE_TWEET_LIST);
        }
        adapter = new MessageAdapter(getActivity(), shownTweetArrayList);

        new HandleJson().execute();

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                         .setInterval(10 * 1000)
                                         .setFastestInterval(1000);

        Bundle bundle = this.getActivity().getIntent().getExtras();
        if (bundle != null) {
            checkIntentFilter = true;
            intentFilterText = bundle.getString(Intent.EXTRA_TEXT);
            addNewContent();
        }
    }

    @OnItemClick(R.id.tweet_list_view)
    protected void onContactListItemClick(final android.widget.AdapterView<?> parent, final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View textEntryView = layoutInflater.inflate(R.layout.add_new_content_layout, tweetsView, false);
        final ViewHolder viewHolder = new ViewHolder();

        ButterKnife.inject(viewHolder, textEntryView);
        final Tweet object = (Tweet) parent.getAdapter().getItem(position);
        viewHolder.etUser.setText(object.getUser());
        viewHolder.etMessage.setText(object.getContent());

        showMessageLength(viewHolder.etMessage, viewHolder.tvCount);

        new AlertDialog
                .Builder(getActivity()).setTitle(getString(R.string.main_activity_alert_dialog_edit_content_title))
                                       .setView(textEntryView)
                                       .setPositiveButton(android.R.string.ok,
                                                          new Dialog.OnClickListener() {
                                                              @Override
                                                              public void onClick(DialogInterface dialog,
                                                                                  int which) {
                                                                  String userName = viewHolder.etUser.getText()
                                                                                                     .toString();
                                                                  String message = viewHolder.etMessage.getText()
                                                                                                       .toString();
                                                                  object.setUser(userName);
                                                                  object.setContent(message);
                                                                  adapter.notifyDataSetChanged();
                                                              }
                                                          })
                                       .setNeutralButton(getString(R.string.main_activity_alert_dialog_delete_button),
                                                         new Dialog.OnClickListener() {
                                                             @Override
                                                             public void onClick(DialogInterface dialog,
                                                                                 int which) {
                                                                 new AlertDialog.Builder(getActivity())
                                                                         .setTitle(
                                                                                 getString(R.string.main_activity_alert_dialog_delete_content_title))
                                                                         .setPositiveButton(android.R.string.ok,
                                                                                            new DialogInterface.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(
                                                                                                        DialogInterface dialogInterface,
                                                                                                        int i) {
                                                                                                    adapter.remove(
                                                                                                            object);
                                                                                                    adapter.getFilter()
                                                                                                           .filter(
                                                                                                                   filterCharSequence);
                                                                                                    if (endPosition ==
                                                                                                        shownTweetArrayList
                                                                                                                .size() +
                                                                                                        1) {
                                                                                                        endPosition = shownTweetArrayList
                                                                                                                .size();
                                                                                                    }
                                                                                                    countDeletedItems++;
                                                                                                    adapter.notifyDataSetChanged();
                                                                                                }
                                                                                            })
                                                                         .setNegativeButton(android.R.string.cancel,
                                                                                            new DialogInterface.OnClickListener() {
                                                                                                @Override
                                                                                                public void onClick(
                                                                                                        DialogInterface dialogInterface,
                                                                                                        int i) {
                                                                                                    //nothing
                                                                                                }
                                                                                            })
                                                                         .show();
                                                             }
                                                         })
                                       .setNegativeButton(android.R.string.cancel,
                                                          new Dialog.OnClickListener() {
                                                              @Override
                                                              public void onClick(DialogInterface dialog,
                                                                                  int which) {
                                                                  //nothing
                                                              }
                                                          })
                                       .show();
    }

    @OnItemLongClick(R.id.tweet_list_view)
    protected boolean onContactListItemLongClick(final android.widget.AdapterView<?> parent, final int position) {
        final Tweet object = (Tweet) parent.getAdapter().getItem(position);
        Intent openMapActivityIntent = new Intent(getActivity(), MapActivity.class);
        if (object.getLatitude() == NO_POSITION) {
            Toast.makeText(getActivity(), getString(R.string.main_activity_no_position_saved_toast), Toast.LENGTH_SHORT)
                 .show();
        } else {
            openMapActivityIntent.putExtra(INTENT_LATITUDE, object.getLatitude());
            openMapActivityIntent.putExtra(INTENT_LONGITUDE, object.getLongitude());
            openMapActivityIntent.putExtra(INTENT_USER, object.getUser());
            startActivity(openMapActivityIntent);
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tweet, container, false);
        ButterKnife.inject(this, view);

        tweetsView = (ListView) view.findViewById(R.id.tweet_list_view);
        footer = ((LayoutInflater) this.getActivity()
                                       .getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_layout,
                                                                                                   tweetsView,
                                                                                                   false);
        tweetsView.addFooterView(footer);

        if (savedInstanceState != null) {
            shownTweetArrayList = savedInstanceState.getParcelableArrayList(SAVE_TWEET_LIST);
        }
        tweetsView.setAdapter(adapter);

        tweetsView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                //nothing
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisible, int visibleCount, int totalCount) {
                if (footer.isShown() && startCountdown && firstVisible > 0) {
                    startCountdown = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            itemCounter = itemCounter + shownItems;
                            if (tweetArrayList.size() <= (endPosition + shownItems)) {
                                newItemsAmount = tweetArrayList.size() - endPosition;
                                tweetsView.removeFooterView(footer);
                            }
                            endPosition = endPosition + (newItemsAmount > 0 ? newItemsAmount : shownItems);
                            loadItems(itemCounter);
                            startCountdown = true;
                        }
                    }, waitingTime);
                }
            }
        });

        return view;
    }

    public void loadItems(int counter) {
        int startPosition;
        if (checkNewItem && countNewItems > 0 && !checkDeletedItem && countDeletedItems == 0) {
            startPosition = counter + countNewItems;
        } else if (checkDeletedItem && countDeletedItems > 0 && !checkNewItem && countNewItems == 0) {
            startPosition = counter - countDeletedItems;
        } else if (checkNewItem && countNewItems > 0 && checkDeletedItem && countDeletedItems > 0) {
            startPosition = counter + countNewItems - countDeletedItems;
        } else {
            startPosition = counter;
        }
        for (int i = startPosition; i < endPosition; i++) {
            shownTweetArrayList.add(tweetArrayList.get(i));
        }
        checkNewItem = countNewItems > 0;
        checkDeletedItem = countDeletedItems > 0;
        adapter.notifyDataSetChanged();
    }

    public class HandleJson extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(urlString);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                is = httpResponse.getEntity().getContent();
                JsonParser jsonParser = jsonFactory.createJsonParser(is);

                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    Tweet tweet = new Tweet();

                    while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                        String token = jsonParser.getCurrentName();

                        if ("user".equals(token)) {
                            jsonParser.nextToken();
                            tweet.setUser(jsonParser.getText());
                        }

                        if ("message".equals(token)) {
                            jsonParser.nextToken();
                            tweet.setContent(jsonParser.getText());
                        }

                        if ("latitude".equals(token)) {
                            jsonParser.nextToken();
                            tweet.setLatitude(jsonParser.getDoubleValue());
                        }

                        if ("longitude".equals(token)) {
                            jsonParser.nextToken();
                            tweet.setLongitude(jsonParser.getDoubleValue());
                        }
                    }

                    tweetArrayList.add(tweet);
                }

                jsonParser.close();

            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadItems(itemCounter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_TWEET_LIST, shownTweetArrayList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.activity_main_menu_items, menu);
        final View v = menu.findItem(R.id.menu_item_search).getActionView();
        etSearch = (EditText) v.findViewById(R.id.et_search);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_item_search),
                                                 new MenuItemCompat.OnActionExpandListener() {
                                                     @Override
                                                     public boolean onMenuItemActionExpand(MenuItem item) {
                                                         return true;
                                                     }

                                                     @Override
                                                     public boolean onMenuItemActionCollapse(MenuItem item) {
                                                         adapter.getFilter().filter("");
                                                         etSearch.setText("");
                                                         InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                                                 MainActivity.INPUT_METHOD_SERVICE);
                                                         imm.hideSoftInputFromWindow(getActivity().getCurrentFocus()
                                                                                                  .getWindowToken(),
                                                                                     InputMethodManager.HIDE_NOT_ALWAYS);
                                                         return true;
                                                     }
                                                 });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_add_new_content:
                addNewContent();
                return true;
            case R.id.menu_item_search:
                TextWatcher textWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    }

                    @Override
                    public void onTextChanged(CharSequence filter, int i, int i2, int i3) {
                        adapter.getFilter().filter(filter);
                        filterCharSequence = filter;
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                };
                etSearch.addTextChangedListener(textWatcher);
                return true;
            case R.id.menu_item_disable_position:
                Intent openSettingsActivityIntent = new Intent(getActivity(), PreferencesActivity.class);
                startActivity(openSettingsActivityIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addNewContent() {
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View textEntryView = layoutInflater.inflate(R.layout.add_new_content_layout, tweetsView, false);
        final ViewHolder viewHolder = new ViewHolder();

        ButterKnife.inject(viewHolder, textEntryView);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isSavePositionAllowed = preferences.getBoolean(getString(R.string.activity_preferences_save_position_check_box_key),
                                                               false);

        if (isSavePositionAllowed) {
            googleApiClient.connect();
        } else {
            lat = NO_POSITION;
        }

        if (checkIntentFilter) {
            viewHolder.etMessage.setText(intentFilterText);
            checkIntentFilter = false;
        }

        showMessageLength(viewHolder.etMessage, viewHolder.tvCount);

        new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.main_activity_alert_dialog_new_content_title))
                                              .setView(textEntryView)
                                              .setPositiveButton(android.R.string.ok, new Dialog.OnClickListener() {
                                                  @Override
                                                  public void onClick(DialogInterface dialog, int which) {
                                                      final String userName = viewHolder.etUser.getText().toString();
                                                      final String message = viewHolder.etMessage.getText().toString();
                                                      shownTweetArrayList.add(0,
                                                                              new Tweet(userName, message, lng, lat));
                                                      if (endPosition == shownTweetArrayList.size() - 1) {
                                                          endPosition = shownTweetArrayList.size();
                                                      }
                                                      countNewItems++;
                                                      if (googleApiClient.isConnected()) {
                                                          googleApiClient.disconnect();
                                                      }
                                                      adapter.notifyDataSetChanged();
                                                  }
                                              })
                                              .setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
                                                  @Override
                                                  public void onClick(DialogInterface dialog, int which) {
                                                      if (googleApiClient.isConnected()) {
                                                          googleApiClient.disconnect();
                                                      }
                                                  }
                                              })
                                              .show();
    }

    public void showMessageLength(final EditText etMessage, final TextView tvCount) {
        tvCount.setText(etMessage.getText().toString().length() + " / " + 140);
        final int defaultColor = tvCount.getTextColors().getDefaultColor();
        setCounterDefaultColor(etMessage, tvCount, defaultColor);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                tvCount.setText(etMessage.getText().toString().length() + " / " + 140);
                setCounterDefaultColor(etMessage, tvCount, defaultColor);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        etMessage.addTextChangedListener(textWatcher);
    }

    public void setCounterDefaultColor(EditText etMessage, TextView tvCount, int defaultColor) {
        if (etMessage.getText().toString().length() > 129) {
            tvCount.setTextColor(Color.RED);
        } else {
            tvCount.setTextColor(defaultColor);
        }
    }
}

