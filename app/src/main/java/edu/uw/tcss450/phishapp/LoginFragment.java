package edu.uw.tcss450.phishapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import java.io.Serializable;
import edu.uw.tcss450.phishapp.model.Credentials;

// Joel's comment 10/08/2019 - 2
//Hi Joel
//HIIIIIII JOEEEEL
public class LoginFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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

        Button b = view.findViewById(R.id.button_login_register);
        b.setOnClickListener(v -> this.onRegisterClicked());

        b = view.findViewById(R.id.button_login_sign_in);
        b.setOnClickListener(v -> this.validateLogin(view));
    }

    private void validateLogin(View view) {
        EditText emailView = view.findViewById(R.id.login_email);
        EditText passView = view.findViewById(R.id.login_pass);

        boolean emailErrors = emailErrors(emailView);
        boolean passErrors = passwordErrors(passView);

        if (!emailErrors && !passErrors) {
            Credentials.Builder builder =
                    new Credentials
                            .Builder(emailView.getText().toString(),
                            passView.getText().toString());

            onLoginSuccess(builder.build(), "");
        }
    }

    private boolean emailErrors(EditText theEmailField) {
        String email = theEmailField.getText().toString();
        boolean toReturn = false;

        if (!email.contains("@")) {
            theEmailField.setError("Please enter a valid email (Use an @)");
            if (email.equals("")) {
                theEmailField.setError("Email cannot be empty");
            }
            toReturn = true;
        } else {
            theEmailField.setError(null);
        }

        return toReturn;
    }

    private boolean passwordErrors(EditText thePasswordField) {
        String pass = thePasswordField.getText().toString();
        boolean toReturn = false;

        if (pass.equals("")) {
            thePasswordField.setError("Password cannot be empty");
            toReturn = true;
        } else {
            thePasswordField.setError(null);
        }

        return toReturn;
    }
//Spooky scary skeletons
// Send shivers down your spine.

    private void onLoginSuccess(Credentials theCredentials, String jwt) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(getString(R.string.credentials_key), theCredentials);

        NavController nc = Navigation.findNavController(getView());

        NavDestination nd = nc.getCurrentDestination();

        if (nd.getId() != R.id.loginFragment) {
            nc.navigateUp();
        } else {
            nc.navigate(R.id.action_loginFragment_to_successFragment, bundle);
        }
    }

    private void onRegisterClicked() {
        NavController nc = Navigation.findNavController(getView());

        NavDestination nd = nc.getCurrentDestination();

        if (nd.getId() != R.id.loginFragment) {
            nc.navigateUp();
        } else {
            nc.navigate(R.id.action_loginFragment_to_registerFragment);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            Serializable serializable = bundle.getSerializable(getString(R.string.credentials_key));
            if (serializable instanceof Credentials) {
                View v = getView();
                EditText emailView = v.findViewById(R.id.login_email);
                EditText passView = v.findViewById(R.id.login_pass);

                Credentials cr = (Credentials) serializable;
                String email = cr.getEmail() != null ? cr.getEmail() : "oops";
                String pass = cr.getPassword() != null ? cr.getPassword() : "oops";

                emailView.setText(email);
                passView.setText(pass);
            }
        }
    }
}
