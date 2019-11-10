package edu.uw.gavz63.tcss450.phishapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;
import android.os.Bundle;
import me.pushy.sdk.Pushy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Pushy.listen(this);

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("type")) {
                Navigation.findNavController(this, R.id.nav_host_fragment)
                        .setGraph(R.navigation.nav_graph, getIntent().getExtras());
            }
        }
    }
}
