package edu.uw.gavz63.tcss450.phishapp;

import edu.uw.gavz63.tcss450.phishapp.model.Credentials;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SuccessFragment extends Fragment {

    private Credentials mCredentials;
    private String mJwt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HomeActivityArgs args = HomeActivityArgs.fromBundle(getArguments());
        mCredentials = args.getCredentials();
        mJwt = args.getJwt();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView label = view.findViewById(R.id.success_email);
        String text = label.getText().toString();
        label.setText(mCredentials.getEmail());
        Log.d("JWT", mJwt);
    }
}
