package com.omnitech.blooddonationnetwork;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.omnitech.blooddonationnetwork.Adapter.InfoWindowAdapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Flags extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    public static final String Age = "Age";
    public static final String Gender = "Gender";
    public static final String Type = "Type";
    public static final String Contact = "Contact";
    public static final String activeID = "activeID";
    public static final String taggedByMe = "taggedByMe";
    private static final String Email = "Email";
    private static final String isRequester = "isRequester";
    private static final String isDonator = "isDonator";
    private static final String Name = "Name";
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    DrawerLayout drawer;
    NavigationView nav;
    View navHeader;
    ImageView image;
    String email, name, age, type, contact, gender;
    SupportMapFragment mapView;
    FusedLocationProviderClient locationClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    LocationManager locationManager;
    boolean locationPermission = false;
    Location currLoc;
    FloatingActionButton Floater;
    Button TagBtn;
    ProgressBar progressBar;

    ArrayList<Marker> markers = new ArrayList<>();
    ArrayList<MarkerOptions> markerOptions = new ArrayList<>();
    Bundle Flag2DetailsBundle = new Bundle();
    boolean isEventExecuted = false;
    boolean isEvent2Executed = false;
    boolean isShowingTags = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flags);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        Toolbar toolbar = findViewById(R.id.toolbar_flags);
        drawer = findViewById(R.id.drawer_layout);
        nav = findViewById(R.id.nav_view);
        navHeader = nav.inflateHeaderView(R.layout.nav_header);
        image = navHeader.findViewById(R.id.nav_header_image);
        mapView = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        locationClient = new FusedLocationProviderClient(this);
        Floater = findViewById(R.id.float_btn_flags);
        TagBtn = findViewById(R.id.tag_btn_details);
        progressBar = findViewById(R.id.progressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        email = preferences.getString(Email, "");
        name = preferences.getString(Name, "");
        age = preferences.getString(Age, "");
        gender = preferences.getString(Gender, "");
        type = preferences.getString(Type, "");
        contact = preferences.getString(Contact, "");

        if (gender.equals("Female")) {
            image.setImageResource(R.drawable.female_logo);
        } else {
            image.setImageResource(R.drawable.male_logo);
        }

        //Fetch all the IDs that are tagged by logged in user:
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String ActiveID = sharedPreferences.getString(activeID, "");
        String IsDonor = sharedPreferences.getString(isDonator, "");
        final String collection = (IsDonor.equals("True")) ? "Donation" : "Request";

        if (!ActiveID.isEmpty()) {
            FirebaseFirestore.getInstance().collection(collection).document(ActiveID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if (!isEvent2Executed) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(taggedByMe, documentSnapshot.getString("TaggedByMe"));
                        editor.apply();
                    }
                    isEvent2Executed = true;
                }
            });
        }

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(Flags.this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        nav.setNavigationItemSelectedListener(this);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        boolean locationStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!locationStatus){
            if(locationPermission){
                EnableGPS(drawer);
            }
            else{
                getLocationPermissions();
                EnableGPS(drawer);
            }
        }
        else{
            fetchLocation();
        }
    }
    private void EnableGPS(View view) {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        Snackbar.make(view, "Please turn on Locations", Snackbar.LENGTH_INDEFINITE)
                .setAction("ENABLE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 3);
                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }
    private boolean getLocationPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ActivityCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermission = true;
        } else {
            ActivityCompat.requestPermissions(this, permissions, 4);
        }
        return  locationPermission;
    }

    public void fetchLocation() {
        progressBar.setVisibility(View.VISIBLE);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(final LocationResult result) {
                locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        currLoc = location;
                        Floater.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Flags.this, CreateNew.class);
                                intent.putExtra("Latitude", currLoc.getLatitude());
                                intent.putExtra("Longitude", currLoc.getLongitude());
                                startActivity(intent);
                            }
                        });
                        mapView.getMapAsync(Flags.this);
                    }
                });
            }
        };
        locationRequest = new LocationRequest();
        locationRequest.setNumUpdates(1);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.setMyLocationEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Animate(googleMap);
        FetchAll(googleMap);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String IsDonor = sharedPreferences.getString(isDonator, "");
        final String collection = ((IsDonor.equals("False")) ? "Donation" : "Request");
        final String TaggedByMeStrings = sharedPreferences.getString(taggedByMe, "");
        if(!TaggedByMeStrings.isEmpty()){
            TagBtn.setVisibility(View.VISIBLE);
        }
        else if(!TaggedByMeStrings.isEmpty()){
            TagBtn.setVisibility(View.GONE);
        }


        TagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TaggedByMeStrings.isEmpty() && !isShowingTags) {
                    showTagged(googleMap, collection, TaggedByMeStrings, IsDonor);
                    isShowingTags = true;
                    TagBtn.setText("Show All");
                } else if (!TaggedByMeStrings.isEmpty() && isShowingTags) {
                    FetchAll(googleMap);
                    isShowingTags = false;
                    TagBtn.setText("Show Tagged");
                }
            }
        });


        googleMap.setInfoWindowAdapter(new InfoWindowAdapter(this));
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.hideInfoWindow();
                final Intent intent = new Intent(Flags.this, Details.class);
                //adding key-value of marker to show details of in details view:
                final String Collection = marker.getTitle().split(",")[2];
                final String markerID = marker.getTitle().split(",")[1];
                isEventExecuted = false;
                FirebaseFirestore.getInstance().collection(Collection).document(markerID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null && documentSnapshot.getData() != null && !isEventExecuted) {
                            for (String K : documentSnapshot.getData().keySet()) {
                                Flag2DetailsBundle.putString(K, (String) documentSnapshot.getData().get(K));
                            }
                            Flag2DetailsBundle.putString("Collection", Collection);
                            Flag2DetailsBundle.putString("markerID", markerID);
                            intent.putExtras(Flag2DetailsBundle);
                            startActivity(intent);
                            isEventExecuted = true;
                        }

                    }
                });
            }
        });
    }

    public void showTagged(GoogleMap googleMap, String collection, String TaggedByMeStrings, String IsDonator) {
        for (Marker m : markers) {
            m.remove();
        }
        markers.clear();
        markerOptions.clear();
        if (TaggedByMeStrings.split(";").length > 1) {
            for (final String id : TaggedByMeStrings.split(";")) {
                try {
                    markerOptions = (ArrayList<MarkerOptions>) new DBT3().execute(collection, id, IsDonator).get();
                } catch (Exception e) {

                }
                Toast.makeText(Flags.this, String.valueOf(markerOptions.size()), Toast.LENGTH_SHORT).show();
            }
        } else if (TaggedByMeStrings.split(";").length == 1) {
            try {
                markerOptions = (ArrayList<MarkerOptions>) new DBT4().execute(collection, TaggedByMeStrings, IsDonator).get();
            } catch (Exception e) {

            }
            Toast.makeText(Flags.this, String.valueOf(markerOptions.size()), Toast.LENGTH_SHORT).show();
        }
        for (MarkerOptions markerOption : markerOptions) {
            markers.add(googleMap.addMarker(markerOption));
        }
    }

    public void FetchAll(GoogleMap googleMap) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String Id = sharedPreferences.getString(activeID, "");
        String IsRequester = sharedPreferences.getString(isRequester, "");
        final String IsDonator = sharedPreferences.getString(isDonator, "");
        for(Marker marker:markers){
            marker.remove();
        }
        markers.clear();
        markerOptions.clear();
        if (IsRequester.equals("True")) {
            try {
                markerOptions = (ArrayList<MarkerOptions>) new DBT1().execute("Request", Id).get();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            for (MarkerOptions markerOption : markerOptions) {
                markers.add(googleMap.addMarker(markerOption));
            }
            FetchDonations(googleMap);
        } else if (IsDonator.equals("True")) {
            try {
                markerOptions = (ArrayList<MarkerOptions>) new DBT2().execute("Donation", Id).get();
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            for (MarkerOptions markerOption : markerOptions) {
                markers.add(googleMap.addMarker(markerOption));
            }
            FetchRequests(googleMap);
        } else {
            Floater.setVisibility(View.VISIBLE);
        }
    }

    private void FetchRequests(final GoogleMap googleMap) {
        markers.clear();
        try {
            markerOptions = (ArrayList<MarkerOptions>) new DBT5().execute("Request").get();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        for (MarkerOptions markerOptions : markerOptions) {
            markers.add(googleMap.addMarker(markerOptions));
        }

    }

    private void FetchDonations(final GoogleMap googleMap) {
        markers.clear();

        try {
            markerOptions = (ArrayList<MarkerOptions>) new DBT6().execute("Donation").get();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        for (MarkerOptions markerOptions : markerOptions) {
            markers.add(googleMap.addMarker(markerOptions));
        }
        Toast.makeText(this, String.valueOf(markers.size()), Toast.LENGTH_SHORT).show();
    }

    private void Animate(final GoogleMap googleMap) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                googleMap.setMaxZoomPreference(19);
                googleMap.setMinZoomPreference(12);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.GONE);
            }
        };
        final Handler handler = new Handler();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLoc.getLatitude(), currLoc.getLongitude()), 17), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                handler.postDelayed(runnable, 1000);
            }

            @Override
            public void onCancel() {

            }
        });

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPreferences.edit().clear().apply();
            startActivity(new Intent(this, MainActivity.class));
        }
        return false;
    }

    public class DBT1 extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Task<DocumentSnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().
                    collection((String) objects[0]).document((String) objects[1]).get();

            ArrayList<MarkerOptions> markers = new ArrayList<>();
            try {
                DocumentSnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                double mylat = Double.parseDouble(documentSnapshot.getString("Latitude"));
                double mylng = Double.parseDouble(documentSnapshot.getString("Longitude"));
                String myCollection = (String) objects[0];
                String myBT = documentSnapshot.getString("BloodType");
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(mylat, mylng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.request)).title("TAP FOR DETAILS," + objects[1] + "," + myCollection).snippet(myBT);
                markers.add(markerOptions);

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return markers;
        }
    }

    public class DBT2 extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Task<DocumentSnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().
                    collection((String) objects[0]).document((String) objects[1]).get();

            ArrayList<MarkerOptions> markers = new ArrayList<>();
            try {
                DocumentSnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                double mylat = Double.parseDouble(documentSnapshot.getString("Latitude"));
                double mylng = Double.parseDouble(documentSnapshot.getString("Longitude"));
                String myCollection = (String) objects[0];
                String myBT = documentSnapshot.getString("BloodType");
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(mylat, mylng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.donation)).title("TAP FOR DETAILS," + objects[1] + "," + myCollection).snippet(myBT);
                markers.add(markerOptions);

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return markers;
        }
    }

    public class DBT3 extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Task<DocumentSnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().
                    collection((String) objects[0]).document((String) objects[1]).get();

            ArrayList<MarkerOptions> markers = new ArrayList<>();
            try {
                DocumentSnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                double lat = Double.parseDouble(documentSnapshot.getString("Latitude"));
                double lng = Double.parseDouble(documentSnapshot.getString("Longitude"));
                String isDonor = (String) objects[2];
                String myBT = documentSnapshot.getString("BloodType");
                if (isDonor.equals("True")) {
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.request_marked)).title("TAP FOR DETAILS," + objects[1] + "," + objects[0]).snippet(myBT);
                    markers.add(markerOptions);
                } else {
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.donation_marked)).title("TAP FOR DETAILS," + objects[1] + "," + objects[0]).snippet(myBT);
                    markers.add(markerOptions);
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return markers;
        }
    }

    public class DBT4 extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Task<DocumentSnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().
                    collection((String) objects[0]).document((String) objects[1]).get();

            ArrayList<MarkerOptions> markers = new ArrayList<>();
            try {
                DocumentSnapshot documentSnapshot = Tasks.await(documentSnapshotTask);
                double lat = Double.parseDouble(documentSnapshot.getString("Latitude"));
                double lng = Double.parseDouble(documentSnapshot.getString("Longitude"));
                String isDonor = (String) objects[2];
                String myBT = documentSnapshot.getString("BloodType");
                if (isDonor.equals("True")) {
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.request_marked)).title("TAP FOR DETAILS," + objects[1] + "," + objects[0]).snippet(myBT);
                    markers.add(markerOptions);
                } else {
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.drawable.donation_marked)).title("TAP FOR DETAILS," + objects[1] + "," + objects[0]).snippet(myBT);
                    markers.add(markerOptions);
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return markers;
        }
    }

    public class DBT5 extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {


            Task<QuerySnapshot> querySnapshotTask = FirebaseFirestore.getInstance().collection((String) objects[0]).get();

            ArrayList<MarkerOptions> markers = new ArrayList<>();
            try {
                QuerySnapshot querySnapshot = Tasks.await(querySnapshotTask);
                if (querySnapshotTask.isSuccessful() && querySnapshotTask.getResult() != null) {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        double Latitude = Double.parseDouble(document.get("Latitude").toString());
                        double Longitude = Double.parseDouble(document.get("Longitude").toString());
                        MarkerOptions marker = new MarkerOptions()
                                .position(new LatLng(Latitude, Longitude))
                                .snippet("Blood Type: " + document.get("BloodType"))
                                .title("TAP FOR DETAILS," + document.getId() + ",Request")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.request));
                        markers.add(marker);
                    }

                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return markers;
        }
    }

    public class DBT6 extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Task<QuerySnapshot> querySnapshotTask = FirebaseFirestore.getInstance().
                    collection((String) objects[0]).get();

            ArrayList<MarkerOptions> markers = new ArrayList<>();
            try {
                QuerySnapshot querySnapshot = Tasks.await(querySnapshotTask);
                if (querySnapshotTask.isSuccessful() && querySnapshot != null) {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        double Latitude = Double.parseDouble(document.get("Latitude").toString());
                        double Longitude = Double.parseDouble(document.get("Longitude").toString());
                        MarkerOptions marker = new MarkerOptions()
                                .position(new LatLng(Latitude, Longitude))
                                .snippet("Blood Type: " + document.get("BloodType"))
                                .title("TAP FOR DETAILS," + document.getId() + ",Donation")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.donation));
                        markers.add(marker);
                    }
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return markers;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3) {
            boolean locationStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!locationStatus) {
                EnableGPS(drawer);
            }else {
                startActivity(new Intent(Flags.this,Flags.class));
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 4) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                locationPermission = true;
            }
        }
    }
}
