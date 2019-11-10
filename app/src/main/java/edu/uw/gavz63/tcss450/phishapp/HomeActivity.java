package edu.uw.gavz63.tcss450.phishapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import edu.uw.gavz63.tcss450.phishapp.blog.BlogPost;
import edu.uw.gavz63.tcss450.phishapp.model.ChatMessageNotification;
import edu.uw.gavz63.tcss450.phishapp.model.Credentials;
import edu.uw.gavz63.tcss450.phishapp.setlist.SetList;
import edu.uw.gavz63.tcss450.phishapp.utils.GetAsyncTask;
import edu.uw.gavz63.tcss450.phishapp.utils.PushReceiver;
import me.pushy.sdk.Pushy;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private String mJwToken;
    private Credentials mCredentials;

    private ColorFilter mDefault;
    private HomePushMessageReceiver mPushMessageReceiver;

    private ChatMessageNotification mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_blog, R.id.nav_setlists, R.id.nav_chat)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.setGraph(R.navigation.mobile_navigation, getIntent().getExtras());
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        HomeActivityArgs args = HomeActivityArgs.fromBundle(getIntent().getExtras());
        mJwToken = args.getJwt();
        mCredentials = args.getCredentials();

        if (args.getChatMessage() != null) {
            mMessage = args.getChatMessage();
            MobileNavigationDirections.ActionGlobalChatFragment directions =
                    MobileNavigationDirections.actionGlobalChatFragment();
            directions.setJwt(mJwToken);
            directions.setEmail(mCredentials.getEmail());
            directions.setMessage(mMessage);
            navController.navigate(directions);
        }
        navigationView.setNavigationItemSelectedListener(this::onNavigationSelected);
        mDefault = toolbar.getNavigationIcon().getColorFilter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPushMessageReceiver == null) {
            mPushMessageReceiver = new HomePushMessageReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        registerReceiver(mPushMessageReceiver, iFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPushMessageReceiver != null){
            unregisterReceiver(mPushMessageReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void handleBlogGetOnPostExecute(final String result) {
        //parse JSON

        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_blogs_response))) {
                JSONObject response = root.getJSONObject(
                        getString(R.string.keys_json_blogs_response));
                if (response.has(getString(R.string.keys_json_blogs_data))) {
                    JSONArray data = response.getJSONArray(
                            getString(R.string.keys_json_blogs_data));

                    BlogPost[] blogs = new BlogPost[data.length()];
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject jsonBlog = data.getJSONObject(i);

                        blogs[i] = new BlogPost.Builder(
                                jsonBlog.getString(
                                        getString(R.string.keys_json_blogs_pubdate)),
                                jsonBlog.getString(
                                        getString(R.string.keys_json_blogs_title)))
                                .addTeaser(jsonBlog.getString(
                                        getString(R.string.keys_json_blogs_teaser)))
                                .addUrl(jsonBlog.getString(
                                        getString(R.string.keys_json_blogs_url)))
                                .build();
                    }

                    MobileNavigationDirections.ActionGlobalNavBlog directions
                            = BlogFragmentDirections.actionGlobalNavBlog(blogs);

                    Navigation.findNavController(this, R.id.nav_host_fragment)
                            .navigate(directions);
                } else {
                    Log.e("ERROR!", "No data array");
                }
            } else {
                Log.e("ERROR!", "No response");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    private void handleSetListGetOnPostExecute(final String result) {
        //parse JSON

        try {
            JSONObject root = new JSONObject(result);
            if (root.has(getString(R.string.keys_json_blogs_response))) {
                JSONObject response = root.getJSONObject(
                        getString(R.string.keys_json_blogs_response));
                if (response.has(getString(R.string.keys_json_blogs_data))) {
                    JSONArray data = response.getJSONArray(
                            getString(R.string.keys_json_blogs_data));

                    SetList[] setLists = new SetList[data.length()];
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject jsonSetList = data.getJSONObject(i);

                        setLists[i] = new SetList.Builder(
                                jsonSetList.getString(
                                        getString(R.string.keys_json_setlists_date)),
                                jsonSetList.getString(
                                        getString(R.string.keys_json_setlists_location)))
                                .addVenue(jsonSetList.getString(
                                        getString(R.string.keys_json_setlists_venue)))
                                .addUrl(jsonSetList.getString(
                                        getString(R.string.keys_json_setlists_url)))
                                .addData(jsonSetList.getString(
                                        getString(R.string.keys_json_setlists_data)))
                                .addNotes(jsonSetList.getString(
                                        getString(R.string.keys_json_setlists_notes)))
                                .build();
                    }


                    MobileNavigationDirections.ActionGlobalNavSetlists directions =
                            SetListFragmentDirections.actionGlobalNavSetlists(setLists);

                    Navigation.findNavController(this, R.id.nav_host_fragment)
                            .navigate(directions);
                } else {
                    Log.e("ERROR!", "No data array");
                }
            } else {
                Log.e("ERROR!", "No response");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
    }

    private boolean onNavigationSelected(final MenuItem menuItem) {
        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment);
        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                MobileNavigationDirections.ActionGlobalNavHome home =
                        SuccessFragmentDirections.actionGlobalNavHome(mCredentials);
                home.setCredentials(mCredentials);
                home.setJwt(mJwToken);
                navController.navigate(R.id.nav_home, getIntent().getExtras());
                break;
            case R.id.nav_blog:
                Uri blogUri = new Uri.Builder()
                        .scheme("https")
                        .appendPath(getString(R.string.ep_base_url))
                        .appendPath(getString(R.string.ep_phish))
                        .appendPath(getString(R.string.ep_blog))
                        .appendPath(getString(R.string.ep_get))
                        .build();

                new GetAsyncTask.Builder(blogUri.toString())
                        .onPostExecute(this::handleBlogGetOnPostExecute)
                        .addHeaderField("authorization", mJwToken) //add the JWT as a header
                        .build().execute();
                break;
            case R.id.nav_setlists:
                Uri setListUri = new Uri.Builder()
                        .scheme("https")
                        .appendPath(getString(R.string.ep_base_url))
                        .appendPath(getString(R.string.ep_phish))
                        .appendPath(getString(R.string.ep_setlists))
                        .appendPath(getString(R.string.ep_recent))
                        .build();
                new GetAsyncTask.Builder(setListUri.toString())
                        .onPostExecute(this::handleSetListGetOnPostExecute)
                        .addHeaderField("authorization", mJwToken) //add the JWT as a header
                        .build().execute();
                break;
            case  R.id.nav_chat:
                MobileNavigationDirections.ActionGlobalChatFragment chatFragment =
                        ChatFragmentDirections.actionGlobalChatFragment();

                chatFragment.setJwt(mJwToken);
                chatFragment.setEmail(mCredentials.getEmail());
                chatFragment.setMessage(mMessage);

                navController.navigate(chatFragment);

                //We've clicked on the chat, reset the hamburger icon color
                ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon().setColorFilter(mDefault);


        }
        //Close the drawer
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
        return true;
    }

    private void logout() {
        new DeleteTokenAsyncTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // Deleting the Pushy device token must be done asynchronously. Good thing
    // we have something that allows us to do that.
    class DeleteTokenAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            //since we are already doing stuff in the background, go ahead
            //and remove the credentials from shared prefs here.
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);

            prefs.edit().remove(getString(R.string.keys_prefs_password)).apply();
            prefs.edit().remove(getString(R.string.keys_prefs_email)).apply();

            //unregister the device from the Pushy servers
            Pushy.unregister(HomeActivity.this);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //close the app
            finishAndRemoveTask();

            //or close this activity and bring back the Login
//            Intent i = new Intent(this, MainActivity.class);
//            startActivity(i);
//            //Ends this Activity and removes it from the Activity back stack.
//            finish();
        }
    }

    /**
     * A BroadcastReceiver that listens for messages sent from PushReceiver
     */
    private class HomePushMessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            NavController nc =
                    Navigation.findNavController(HomeActivity.this, R.id.nav_host_fragment);
            NavDestination nd = nc.getCurrentDestination();
            if (nd.getId() != R.id.nav_chat) {

                if (intent.hasExtra("SENDER") && intent.hasExtra("MESSAGE")) {

                    String sender = intent.getStringExtra("SENDER");
                    String messageText = intent.getStringExtra("MESSAGE");

                    mMessage = new ChatMessageNotification
                            .Builder(sender,messageText).build();

                    //change the hamburger icon to red alerting the user of the notification
                    ((Toolbar) findViewById(R.id.toolbar)).getNavigationIcon()
                            .setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

                    Log.d("HOME", sender + ": " + messageText);
                }
            }
        }
    }

}
