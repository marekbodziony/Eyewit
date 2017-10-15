package marekbodziony.eyewit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class HelpMeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_me);
        getSupportActionBar().hide();   // hide actionbar
    }
}
