package com.omnitech.blooddonationnetwork;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omnitech.blooddonationnetwork.Adapter.LoginViewAdapter;
import com.omnitech.blooddonationnetwork.Fragments.Login;
import com.omnitech.blooddonationnetwork.Fragments.Register;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;
    public static final String ID = "ID";
    public static final String Age = "Age";
    public static final String Gender = "Gender";
    public static final String Contact = "Contact";
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final String Email = "Email";
    private static final String Name = "Name";
    public static final String isDonator = "isDonator";
    public static final String isRequester = "isRequester";
    public static final String activeID = "activeID";
    private static final int ERROR_DIALOGUE_REQUEST = 102;

    ViewPager viewPager;
    TabLayout tabLayout;
    Toolbar toolbar;

    ProgressDialog progressDialog;
    boolean isValid = true;
    public FirebaseAuth firebaseAuth;
    public FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean mLocationPermissionGranted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar_main);
        tabLayout = findViewById(R.id.tablayout_main);
        viewPager = findViewById(R.id.view_pager_main);

        getLocationPermissions();
        firebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mfirebaseUser = firebaseAuth.getCurrentUser();
                if (mfirebaseUser != null) {
                    if (isServicesOK() && mLocationPermissionGranted) {
                        Intent intent = new Intent(MainActivity.this, Flags.class);
                        startActivity(intent);
                    } else if (!mLocationPermissionGranted)
                        Toast.makeText(getApplicationContext(), "Please Enable Location Permission First", Toast.LENGTH_SHORT).show();
                    getLocationPermissions();
                    return;
                }
            }
        };

        setSupportActionBar(toolbar);
        viewPager.setAdapter(setAdapter());
        tabLayout.setupWithViewPager(viewPager);
    }

    private PagerAdapter setAdapter() {
        LoginViewAdapter adapter = new LoginViewAdapter(getSupportFragmentManager());
        adapter.addFragment(new Login(), "Login");
        adapter.addFragment(new Register(), "Register");
        return adapter;
    }

    public boolean CheckConnectivity(View view) {
        boolean netState;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        netState = networkInfo != null && networkInfo.isConnected();
        if (!netState) {
            Snackbar.make(view, "Please connect to the internet", Snackbar.LENGTH_LONG)
                    .setAction("CLOSE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EnableWifi(view);
                        }
                    })
                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                    .show();
            return false;
        }

        if (!mLocationPermissionGranted) {
            Toast.makeText(getApplicationContext(), "Please Allow Location Permission First", Toast.LENGTH_SHORT).show();
            getLocationPermissions();
            return false;
        } else {
            return true;
        }
    }
    private void EnableWifi(View view) {
        Snackbar.make(view, "Please connect to the internet", Snackbar.LENGTH_INDEFINITE)
                .setAction("ENABLE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 2);
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }

    public boolean SignInUser(String email, String pass) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("..Please wait..");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    progressDialog.dismiss();
                    isValid = false;
                } else if(task.isSuccessful() && mLocationPermissionGranted){
                    FirebaseFirestore.getInstance().collection("Users").document(firebaseAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot doc = task.getResult();
                                if (doc.exists()) {
                                    SharedPreferences sharedPreferences = PreferenceManager
                                            .getDefaultSharedPreferences(MainActivity.this);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(Name, doc.getString("name"));
                                    editor.putString(Contact, doc.getString("contact"));
                                    editor.putString(Email, doc.getString("email"));
                                    editor.putString(Age, doc.getString("age"));
                                    editor.putString(Gender, doc.getString("gender"));
                                    editor.putString(ID, doc.getId());
                                    editor.putString(isDonator, doc.getString("isDonator"));
                                    editor.putString(isRequester, doc.getString("isRequester"));
                                    editor.putString(activeID, doc.getString("activeID"));
                                    editor.apply();

                                    if (isServicesOK()) {
                                        Intent intent = new Intent(MainActivity.this, Flags.class);
                                        progressDialog.dismiss();
                                        startActivity(intent);
                                    }
                                }
                            }
                        }
                    });
                }
                else if(!mLocationPermissionGranted){
                    getLocationPermissions();
                }
            }
        });
        return isValid;
    }

    public boolean isServicesOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and user can make map request
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOGUE_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "you cant make map request", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isServicesOK() && mLocationPermissionGranted) {
            firebaseAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    public void getLocationPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    else
                        mLocationPermissionGranted = true;
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (networkInfo.isConnected() && networkInfo != null) {
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        }
    }
}