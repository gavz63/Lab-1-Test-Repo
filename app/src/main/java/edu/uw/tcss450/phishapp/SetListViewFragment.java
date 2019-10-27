package edu.uw.tcss450.phishapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import edu.uw.tcss450.phishapp.blog.BlogPost;
import edu.uw.tcss450.phishapp.setlist.SetList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetListViewFragment extends Fragment {
    
    private SetList mSetList;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_set_list_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        mSetList = (SetList) args.getSerializable(getString(R.string.blog_key));

        TextView dateView = view.findViewById(R.id.set_list_date);
        dateView.setText(mSetList.getDate());
        TextView locationView = view.findViewById(R.id.set_list_location);
        locationView.setText(mSetList.getLocation());
        TextView dataView = view.findViewById(R.id.set_list_data);
        dataView.setText(mSetList.getData());
        TextView notesView = view.findViewById(R.id.set_list_data);
        notesView.setText(mSetList.getNotes());

        Button butt = view.findViewById(R.id.button_url);
        butt.setOnClickListener(this::followUrl);
    }

    private void followUrl(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mSetList.getUrl()));
        startActivity(i);
    }
}
