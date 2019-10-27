package edu.uw.tcss450.phishapp;

import android.net.Uri;
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
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import edu.uw.tcss450.phishapp.model.Credentials;
import edu.uw.tcss450.phishapp.utils.SendPostAsyncTask;

public class LoginFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Credentials mCredentials;
    private EditText mEmailField;
    private EditText mPasswordField;
    private String mEmailString;
    private String mPasswordString;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

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
        mEmailField.setText("test@test");
        mPasswordField.setText("test123");

        Button b = view.findViewById(R.id.button_login_register);
        b.setOnClickListener(this::onRegisterClicked);

        b = view.findViewById(R.id.button_login_sign_in);
        b.setOnClickListener(this::validateLogin);
    }

    private void validateLogin(View view) {
        mEmailString = mEmailField.getText().toString();
        mPasswordString = mPasswordField.getText().toString();



        if (!anyErrors()) {
            mCredentials = new Credentials.Builder(mEmailString, mPasswordString)
                    .build();

            //build the web service URL
            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_login))
                    .build();

            //build the JSONObject
            JSONObject msg = mCredentials.asJSONObject();

            //instantiate and execute the AsyncTask.
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleLoginOnPre)
                    .onPostExecute(this::handleLoginOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();

        }
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
            mPasswordField.setError("Password should not be empty");
            anyErrors = true;
        } else {
            mPasswordField.setError(null);
        }

        return anyErrors;
    }

    private void onRegisterClicked(View view) {
        NavController nc = Navigation.findNavController(getView());

        nc.navigate(R.id.action_loginFragment_to_registerFragment);
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            Serializable serializable = bundle.getSerializable(getString(R.string.credentials_key));
            if (serializable instanceof Credentials) {
                Credentials cr = (Credentials) serializable;
                String email = cr.getEmail() != null ? cr.getEmail() : "oops";
                String pass = cr.getPassword() != null ? cr.getPassword() : "oops";

                mEmailField.setText(email);
                mPasswordField.setText(pass);
            }
        }
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR",  result);
    }

    /**
     * Handle the setup of the UI before the HTTP call to the webservice.
     */
    private void handleLoginOnPre() {
        getActivity().findViewById(R.id.layout_login_wait).setVisibility(View.VISIBLE);
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleLoginOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_login_success));

            if (success) {
                LoginFragmentDirections
                        .ActionLoginFragmentToHomeActivity homeActivity =
                        LoginFragmentDirections
                                .actionLoginFragmentToHomeActivity(mCredentials);
                homeActivity.setJwt(
                        resultsJSON.getString(
                                getString(R.string.keys_json_login_jwt)));
                Navigation.findNavController(getView())
                        .navigate(homeActivity);
                return;
            } else {
                //Login was unsuccessful. Don’t switch fragments and
                // inform the user
                mEmailField.setError("Login Unsuccessful");
            }
            getActivity().findViewById(R.id.layout_login_wait)
                    .setVisibility(View.GONE);
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR",  result
                    + System.lineSeparator()
                    + e.getMessage());
            getActivity().findViewById(R.id.layout_login_wait)
                    .setVisibility(View.GONE);
            mEmailField.setError("Login Unsuccessful");
        }
    }

}
