package edu.uw.gavz63.tcss450.phishapp;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.gavz63.tcss450.phishapp.model.Credentials;
import edu.uw.gavz63.tcss450.phishapp.utils.SendPostAsyncTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Credentials mCredentials;
    private EditText mFirstNameField;
    private EditText mLastNameField;
    private EditText mNicknameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mPasswordConfirmField;
    private String mFirstNameString;
    private String mLastNameString;
    private String mNicknameString;
    private String mEmailString;
    private String mPasswordString;
    private String mPasswordConfirmString;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
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
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFirstNameField = view.findViewById(R.id.first_name);
        mLastNameField = view.findViewById(R.id.last_name);
        mNicknameField = view.findViewById(R.id.username);
        mEmailField = view.findViewById(R.id.register_email);
        mPasswordField = view.findViewById(R.id.register_pass);
        mPasswordConfirmField = view.findViewById(R.id.register_re_pass);

        Button b = view.findViewById(R.id.button_register_register);
        b.setOnClickListener(this::validateRegistration);
    }

    private void validateRegistration(View view) {
        mFirstNameString = mFirstNameField.getText().toString();
        mLastNameString = mLastNameField.getText().toString();
        mNicknameString = mNicknameField.getText().toString();
        mEmailString = mEmailField.getText().toString();
        mPasswordString = mPasswordField.getText().toString();
        mPasswordConfirmString = mPasswordConfirmField.getText().toString();

        if (!anyErrors()) {
            mCredentials = new Credentials.Builder(mEmailString, mPasswordString)
                    .addFirstName(mFirstNameString)
                    .addLastName(mLastNameString)
                    .addUsername(mNicknameString)
                    .build();

            Uri uri = new Uri.Builder()
                    .scheme("https")
                    .appendPath(getString(R.string.ep_base_url))
                    .appendPath(getString(R.string.ep_register))
                    .build();

            //build the JSONObject
            JSONObject msg = mCredentials.asJSONObject();

            //instantiate and execute the AsyncTask.
            new SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleRegisterOnPre)
                    .onPostExecute(this::handleRegisterOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        }
    }

    private boolean anyErrors() {
        boolean anyErrors = false;

        if (!mFirstNameString.equals("")) {
            mFirstNameField.setError(null);
        } else {
            mFirstNameField.setError("First Name cannot be empty");
            anyErrors = true;
        }

        if (!mLastNameString.equals("")) {
            mLastNameField.setError(null);
        } else {
            mLastNameField.setError("Last Name cannot be empty");
            anyErrors = true;
        }

        if (!mNicknameString.equals("")) {
            mNicknameField.setError(null);
        } else {
            mNicknameField.setError("Nickname cannot be empty");
            anyErrors = true;
        }

        //If email does not contain exactly one '@'
        if (mEmailString.length() - mEmailString.replace("@", "").length() != 1) {
            //If email is empty
            if (mEmailString.equals("")) {
                mEmailField.setError("Email cannot be empty");
            } else {
                mEmailField.setError("Please enter a valid email");
            }
            anyErrors = true;
        } else {
            mEmailField.setError(null);
        }

        if (mPasswordString.length() < 6) {
            if (mPasswordString.equals("")) {
                mPasswordField.setError("Password cannot be empty");
            } else {
                mPasswordField.setError("Your password must be 6 or more characters");
            }
            anyErrors = true;
        } else {
            mPasswordField.setError(null);
        }
        if (!mPasswordString.equals(mPasswordConfirmString)) {
            mPasswordConfirmField.setError("Passwords do not match");
            anyErrors = true;
        }

        return anyErrors;
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
    private void handleRegisterOnPre() {
        getActivity().findViewById(R.id.layout_register_wait).setVisibility(View.VISIBLE);
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleRegisterOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success =
                    resultsJSON.getBoolean(
                            getString(R.string.keys_json_register_success));

            if (success) {
                RegisterFragmentDirections.ActionRegisterFragmentToHomeActivity homeActivity =
                        RegisterFragmentDirections
                                .actionRegisterFragmentToHomeActivity(mCredentials);
                homeActivity.setJwt("Will get token later");
                Navigation.findNavController(getView())
                        .navigate(homeActivity);
                //Remove this Activity from the backstack. Do not allow nav back to register
                getActivity().finish();
                return;
            } else {
                //Login was unsuccessful. Don’t switch fragments and
                // inform the user
                String err =
                        resultsJSON.getString(
                                getString(R.string.keys_json_register_err));
                if (err.startsWith("Missing required")) {
                    mFirstNameField.setError(err);
                } else if (err.startsWith("Key (username)")){
                    mNicknameField.setError("Username is not available.");
                } else if (err.startsWith("Key (email)")){
                    mEmailField.setError("Email is already in use.");
                }
            }
            getActivity().findViewById(R.id.layout_register_wait)
                    .setVisibility(View.GONE);
        } catch (JSONException e) {
            //It appears that the web service did not return a JSON formatted
            //String or it did not have what we expected in it.
            Log.e("JSON_PARSE_ERROR",  result
                    + System.lineSeparator()
                    + e.getMessage());
            getActivity().findViewById(R.id.layout_register_wait)
                    .setVisibility(View.GONE);
            mFirstNameField.setError("JSONException");
        }
    }
}
