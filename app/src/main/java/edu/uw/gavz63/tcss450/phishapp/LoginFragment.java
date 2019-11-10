package edu.uw.gavz63.tcss450.phishapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.uw.gavz63.tcss450.phishapp.model.ChatMessageNotification;
import edu.uw.gavz63.tcss450.phishapp.model.Credentials;
import me.pushy.sdk.Pushy;

public class LoginFragment extends Fragment {
    private Credentials mCredentials;
    private EditText mEmailField;
    private EditText mPasswordField;
    private String mEmailString;
    private String mPasswordString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEmailField = view.findViewById(R.id.login_email);
        mPasswordField = view.findViewById(R.id.login_pass);
        //Comment out this block before going to prod
//        mEmailField.setText("gavz63@uw.edu");
//        mPasswordField.setText("zeekers63");

        Button b = view.findViewById(R.id.button_login_register);
        b.setOnClickListener(this::onRegisterClicked);

        b = view.findViewById(R.id.button_login_sign_in);
        b.setOnClickListener(this::attemptLogin);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //Retrieve stored credentials from SharedPrefs
        if (prefs.contains(getString(R.string.keys_prefs_email)) &&
                prefs.contains(getString(R.string.keys_prefs_password))) {

            final String email = prefs.getString(getString(R.string.keys_prefs_email), "");
            final String password = prefs.getString(getString(R.string.keys_prefs_password), "");
            //Load the two login EditTexts with the credentials found in SharedPrefs
            EditText emailEdit = getActivity().findViewById(R.id.login_email);
            emailEdit.setText(email);
            EditText passwordEdit = getActivity().findViewById(R.id.login_pass);
            passwordEdit.setText(password);

            doLogin(new Credentials.Builder(
                    emailEdit.getText().toString(),
                    passwordEdit.getText().toString()).build());
        }
    }

    private void onRegisterClicked(View view) {
        NavController nc = Navigation.findNavController(getView());

        nc.navigate(R.id.action_loginFragment_to_registerFragment);
    }

    private void attemptLogin(View view) {
        mEmailString = mEmailField.getText().toString();
        mPasswordString = mPasswordField.getText().toString();

        if (!anyErrors()) {
            doLogin(new Credentials.Builder(
                    mEmailString, mPasswordString)
                    .build());
        }
    }

    private void doLogin(Credentials credentials) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .appendPath(getString(R.string.ep_pushy))
                .build();

        mCredentials = credentials;

        //instantiate and execute the AsyncTask.
        new AttemptLoginTask().execute(uri.toString());
    }

    private boolean anyErrors() {
        boolean anyErrors = false;

        //If email does not contain exactly one '@'
        if (mEmailString.length() - mEmailString.replace("@", "").length() != 1) {
            //If email is empty
            if (mEmailField.equals("")) {
                mEmailField.setError("Email cannot be empty");
            } else {
                mEmailField.setError("Please enter a valid email");
            }
            anyErrors = true;
        } else {
            mEmailField.setError(null);
        }

        if (mPasswordString.equals("")) {
            mPasswordField.setError("Password cannot be empty");
            anyErrors = true;
        } else {
            mPasswordField.setError(null);
        }

        return anyErrors;
    }

    private void saveCredentials(final Credentials credentials) {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //Store the credentials in SharedPrefs
        prefs.edit().putString(getString(R.string.keys_prefs_email), credentials.getEmail()).apply();
        prefs.edit().putString(getString(R.string.keys_prefs_password), credentials.getPassword()).apply();
    }

    class AttemptLoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Activity a = getActivity();
            a.findViewById(R.id.layout_login_wait).setVisibility(View.VISIBLE);
            a.findViewById(R.id.button_login_sign_in).setEnabled(false);
            a.findViewById(R.id.button_login_register).setEnabled(false);
        }

        @Override
        protected String doInBackground(String... urls) {
            //get pushy token
            String deviceToken = "";

            try {
                // Assign a unique token to this device
                deviceToken = Pushy.register(getActivity().getApplicationContext());

                //subscribe to a topic (this is a Blocking call)
                Pushy.subscribe("all", getActivity().getApplicationContext());
            }
            catch (Exception exc) {

                cancel(true);
                // Return exc to onCancelled
                return exc.getMessage();
            }

            //feel free to remove later.
            Log.d("LOGIN", "Pushy Token: " + deviceToken);


            //attempt to log in: Send credentials AND pushy token to the web service
            StringBuilder response = new StringBuilder();
            HttpURLConnection urlConnection = null;

            try {
                URL urlObject = new URL(urls[0]);
                urlConnection = (HttpURLConnection) urlObject.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                urlConnection.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());

                JSONObject message = mCredentials.asJSONObject();
                message.put("token", deviceToken);

                wr.write(message.toString());
                wr.flush();
                wr.close();

                InputStream content = urlConnection.getInputStream();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                String s = "";
                while((s = buffer.readLine()) != null) {
                    response.append(s);
                }
                publishProgress();
            } catch (Exception e) {
                response = new StringBuilder("Unable to connect, Reason: "
                        + e.getMessage());
                cancel(true);
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return response.toString();
        }

        @Override
        protected void onCancelled(String s) {
            super.onCancelled(s);
            getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
            Log.e("LOGIN_ERROR", "Error in Login Async Task: " + s);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {

                Log.d("JSON result",result);
                JSONObject resultsJSON = new JSONObject(result);
                boolean success = resultsJSON.getBoolean("success");


                if (success) {
                    saveCredentials(mCredentials);

                    //Login was successful. Switch to the SuccessFragment.
                    LoginFragmentDirections.ActionLoginFragmentToHomeActivity homeActivity =
                            LoginFragmentDirections
                                    .actionLoginFragmentToHomeActivity(mCredentials);
                    homeActivity.setJwt(resultsJSON.getString(
                            getString(R.string.keys_json_login_jwt)));

                    if (getArguments() != null) {
                        if (getArguments().containsKey("type")) {
                            if (getArguments().getString("type").equals("msg")) {
                                String msg = getArguments().getString("message");
                                String sender = getArguments().getString("sender");

                                ChatMessageNotification chat =
                                        new ChatMessageNotification
                                                .Builder(sender, msg).build();
                                homeActivity.setChatMessage(chat);
                            }
                        }
                    }

                    Navigation.findNavController(getView()).navigate(homeActivity);
                    getActivity().finish();
                    return;
                } else {
                    //Saving the token wrong. Don’t switch fragments and inform the user
                    ((TextView) getView().findViewById(R.id.login_email))
                            .setError("Login Unsuccessful");
                    Activity a = getActivity();
                    a.findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
                    a.findViewById(R.id.button_login_sign_in).setEnabled(true);
                    a.findViewById(R.id.button_login_register).setEnabled(true);
                }
            } catch (JSONException e) {
                //It appears that the web service didn’t return a JSON formatted String
                //or it didn’t have what we expected in it.
                Log.e("JSON_PARSE_ERROR",  result
                        + System.lineSeparator()
                        + e.getMessage());

                ((TextView) getView().findViewById(R.id.login_pass))
                        .setError("Login Unsuccessful");
                Activity a = getActivity();
                a.findViewById(R.id.layout_login_wait).setVisibility(View.GONE);
                a.findViewById(R.id.button_login_sign_in).setEnabled(true);
                a.findViewById(R.id.button_login_register).setEnabled(true);
            }
        }
    }
}
