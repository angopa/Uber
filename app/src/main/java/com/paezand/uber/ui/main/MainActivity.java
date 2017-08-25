package com.paezand.uber.ui.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.paezand.uber.R;
import com.paezand.uber.ui.navigation.ListDistanceActivity;
import com.paezand.uber.ui.navigation.RiderActivity;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static com.paezand.uber.util.Constants.RIDER_OR_DRIVER_USER;

public class MainActivity extends AppCompatActivity {

    private String userSelected;

    @BindView(R.id.select_user_switch)
    protected Switch userTypeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        getSupportActionBar().hide();

        loginAnonymous();
    }

    @OnCheckedChanged(R.id.select_user_switch)
    protected void onSelectUserSwitchToggled(final boolean checked) {
        if (checked) {
            userSelected = "driver";
        } else {
            userSelected = "rider";
        }
    }

    @OnClick(R.id.start_button)
    protected void onStartButtonToggle() {
        if (userTypeSwitch.isChecked()) {
            userSelected = "driver";
        } else {
            userSelected = "rider";
        }
        setUserSelectedValue();
    }

    private void setUserSelectedValue() {
        ParseUser.getCurrentUser().put(RIDER_OR_DRIVER_USER, userSelected);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    loginAnonymous();
                } else {
                    Log.i("Info", "Update failed!");
                }
            }
        });
    }

    private void loginAnonymous() {
        if (ParseUser.getCurrentUser() == null) {
            ParseAnonymousUtils.logIn(new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null) {
                        Log.i("Info", "Anonymous login successful!");
                    } else {
                        Log.i("Info", "Anonymous login failed!");
                    }
                }
            });
        } else {
            Toast.makeText(this, "Welcome " + ParseUser.getCurrentUser().get(RIDER_OR_DRIVER_USER), Toast.LENGTH_SHORT).show();
            if (ParseUser.getCurrentUser().get(RIDER_OR_DRIVER_USER).equals("rider")) {
                startRiderNavigationActivity();
            } else {
                startDriverNavigationActivity();
            }
        }
    }

    private void startDriverNavigationActivity() {
        Intent intent = new Intent(getApplicationContext(), ListDistanceActivity.class);
        startActivity(intent);
    }

    private void startRiderNavigationActivity() {
        Intent intent = new Intent(getApplicationContext(), RiderActivity.class);
        startActivity(intent);
    }
}
