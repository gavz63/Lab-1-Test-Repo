package edu.uw.gavz63.tcss450.phishapp;


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

import edu.uw.gavz63.tcss450.phishapp.blog.BlogPost;


/**
 * A simple {@link Fragment} subclass.
 */
public class BlogPostFragment extends Fragment {


    private BlogPost mBlogPost;

    public BlogPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blog_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        mBlogPost = (BlogPost) args.getSerializable(getString(R.string.blog_key));

        TextView titleView = view.findViewById(R.id.blog_title);
        titleView.setText(mBlogPost.getTitle());
        TextView dateView = view.findViewById(R.id.blog_date);
        dateView.setText(mBlogPost.getPubDate());
        TextView teaserView = view.findViewById(R.id.blog_teaser);
        teaserView.setText(Html.fromHtml(mBlogPost.getTeaser()));

        Button butt = view.findViewById(R.id.button_full_post);
        butt.setOnClickListener(this::followUrl);
    }

    private void followUrl(View view) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(mBlogPost.getUrl()));
        startActivity(i);
    }

}
