package com.omnitech.blooddonationnetwork;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

public class Details extends AppCompatActivity {
    public static final String ID = "ID";
    public static final String activeID = "activeID";
    public static final String taggedByMe = "taggedByMe";
    private static final String isRequester = "isRequester";
    private static final String isDonator = "isDonator";
    boolean isEvent1Executed = false;
    boolean isEvent2Executed = false;
    boolean isEvent3Executed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        TextView Contact = findViewById(R.id.contact_details);
        TextView Email = findViewById(R.id.email_details);
        TextView Name = findViewById(R.id.name_details);
        TextView Age = findViewById(R.id.age_details);
        TextView Gender = findViewById(R.id.gender_details);
        TextView Type = findViewById(R.id.type_details);
        TextView Qty = findViewById(R.id.qty_details);
        TextView Time = findViewById(R.id.time_details);
        ImageView bgImg = findViewById(R.id.details_imgview);
        Button Delete = findViewById(R.id.delete_details);
        Button Call = findViewById(R.id.call_details);
        Button Mail = findViewById(R.id.mail_details);
        Button Tag = findViewById(R.id.tag_details);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String AuthEmail = preferences.getString("Email", "");

        final Bundle bundle = getIntent().getExtras();

        final String Collection = bundle.getString("Collection");
        final String markerID = bundle.getString("markerID");
        final String contact = bundle.getString("Contact");
        String qty = Qty.getText().toString() + ": " + bundle.getString("Quantity");

        if (bundle.containsKey("Time")) {
            String time = Time.getText().toString() + ": " + bundle.getString("Time");
            Time.setVisibility(View.VISIBLE);
            Time.setText(time);
            bgImg.invalidateDrawable(getDrawable(R.drawable.logo));
            bgImg.setImageResource(R.drawable.syringe);
        }
        String age = bundle.getString("Age");
        String name = bundle.getString("Name");
        final String email = bundle.getString("Email");
        String gender = bundle.getString("Gender");
        String type = bundle.getString("BloodType");


        double latitude = Double.parseDouble(bundle.getString("Latitude"));
        double longitude = Double.parseDouble(bundle.getString("Longitude"));

        if (AuthEmail.equals(email)) {
            Delete.setVisibility(View.VISIBLE);
            Tag.setVisibility(View.GONE);
            Mail.setEnabled(false);
            Call.setEnabled(false);

            ConstraintLayout.LayoutParams callLayoutParams = (ConstraintLayout.LayoutParams) Call.getLayoutParams();
            ConstraintLayout.LayoutParams mailLayoutParams = (ConstraintLayout.LayoutParams) Mail.getLayoutParams();
            callLayoutParams.width = 1;
            mailLayoutParams.width = 1;
            mailLayoutParams.height = 1;
            callLayoutParams.height = 1;
            callLayoutParams.leftMargin = 0;
            mailLayoutParams.leftMargin = 0;
            mailLayoutParams.topToBottom = Call.getId();
            mailLayoutParams.topMargin = 62;
            Call.setLayoutParams(callLayoutParams);
            Mail.setLayoutParams(mailLayoutParams);
        }

        //make tag button disabled if the taggedByOthers field contains the activeID of the user viewing this marker detail
        ArrayList<String> taggedByOthersList = new ArrayList<>();
        if (!bundle.getString("TaggedByOthers").isEmpty() && bundle.getString("TaggedByOthers").split(";").length > 1) {
            for (String string : bundle.getString("TaggedByOthers").split(";")) {
                taggedByOthersList.add(string);
            }

        } else if (!bundle.getString("TaggedByOthers").isEmpty()) {
            taggedByOthersList.add(bundle.getString("TaggedByOthers"));
        }
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Details.this);
        if (taggedByOthersList.contains(sharedPreferences.getString(activeID, ""))) {
            Tag.setEnabled(false);
            Tag.setClickable(false);
            Tag.setAlpha(.5f);
        }

        Contact.setText(contact);
        Email.setText(email);
        Name.setText(name);
        Age.setText(age);
        Gender.setText(gender);
        Type.setText(type);
        Qty.setText(qty);


        Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contact));
                startActivity(intent);
            }
        });
        Mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmail(email);
            }
        });

        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Before deleting, make sure that the IDs in taggedByOthers in the respective collection is fetched
                //and all the IDs' TaggedByMe is updated
                String IsDonor = sharedPreferences.getString(isDonator, "");
                //find taggedByMe in collection opposite to that this user is in
                final String collection = (IsDonor.equals("False")) ? "Donation" : "Request";
                ArrayList<String> taggedByOthersList = new ArrayList<>();
                String taggedByOthersStrings = bundle.getString("TaggedByOthers");
                if (!taggedByOthersStrings.isEmpty() && taggedByOthersStrings.split(";").length > 1) {
                    for (String s : taggedByOthersStrings.split(";")) {
                        taggedByOthersList.add(s);
                    }
                    for (final String id : taggedByOthersList) {
                        FirebaseFirestore.getInstance().collection(collection).document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                String taggedByMeStrings = documentSnapshot.getString("TaggedByMe");
                                ArrayList<String> taggedByMeList = new ArrayList<>();
                                if (taggedByMeStrings.split(";").length > 1) {
                                    for (String s : taggedByMeStrings.split(";")) {
                                        taggedByMeList.add(s);
                                    }
                                    taggedByMeList.remove(markerID);
                                } else {
                                    taggedByMeList.add(taggedByMeStrings);
                                    taggedByMeList.remove(markerID);
                                }

                                //convert modified taggedByMeList to ; separated string
                                String taggedbyme = "";
                                if (taggedByMeList.size() == 1) {
                                    taggedbyme = taggedByMeList.get(0);
                                } else if (taggedByMeList.size() > 1) {
                                    taggedbyme = taggedByMeList.get(0);
                                    for (String s : taggedByMeList.subList(1, (taggedByMeList.size() - 1))) {
                                        taggedbyme += ";" + s;
                                    }
                                }
                                FirebaseFirestore.getInstance().collection(collection).document(id).update("TaggedByMe", taggedbyme);
                            }
                        });
                    }
                } else if (!taggedByOthersList.isEmpty()) {
                    taggedByOthersList.add(taggedByOthersStrings);
                    for (final String id : taggedByOthersList) {
                        FirebaseFirestore.getInstance().collection(collection).document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                if (!isEvent3Executed) {
                                    String taggedByMeString = documentSnapshot.getString("TaggedByMe");
                                    ArrayList<String> taggedByMeList = new ArrayList<>();
                                    if (taggedByMeString.split(";").length > 1) {
                                        for (String s : taggedByMeString.split(";")) {
                                            taggedByMeList.add(s);
                                        }
                                        taggedByMeList.remove(markerID);
                                    } else {
                                        taggedByMeList.add(taggedByMeString);
                                        taggedByMeList.remove(markerID);
                                    }

                                    //convert modified taggedByMeList to ; separated string
                                    String taggedbyme = "";
                                    if (taggedByMeList.size() == 1) {
                                        taggedbyme = taggedByMeList.get(0);
                                    } else if (taggedByMeList.size() > 1) {
                                        taggedbyme = taggedByMeList.get(0);
                                        for (String s : taggedByMeList.subList(1, (taggedByMeList.size() - 1))) {
                                            taggedbyme += ";" + s;
                                        }
                                    }
                                    FirebaseFirestore.getInstance().collection(collection).document(id).update("TaggedByMe", taggedbyme);
                                    isEvent3Executed = true;
                                }
                            }
                        });
                    }
                }

                FirebaseFirestore.getInstance().collection(Collection).document(markerID).delete();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Details.this);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.remove("activeID");
                edit.remove("taggedByMe");
                edit.putString("isRequester", "False");
                edit.putString("isDonator", "False");
                edit.apply();
                String id = sharedPreferences.getString("ID", "");
                FirebaseFirestore.getInstance().collection("Users").document(id).update("isRequester", "False", "isDonator", "False", "activeID", "");

                Intent intent = new Intent(Details.this, Flags.class);
                startActivity(intent);
            }
        });
        Tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                //Adding this Donation/Request ID to the taggedByMe field of the user logged in in Users collection
                final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Details.this);
                final String userID = sharedPreferences.getString(ID, "");
                final String ActiveID = sharedPreferences.getString(activeID, "");

                //First get the previous tags in TaggedByMe then append this markerID to them with ; in Collection
                String IsDonor = sharedPreferences.getString(isDonator, "");

                final String collection = (IsDonor.equals("True")) ? "Donation" : "Request";

                FirebaseFirestore.getInstance().collection(collection).document(ActiveID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null && !isEvent1Executed) {
                            String tagList = documentSnapshot.getString("TaggedByMe");
                            if (tagList.isEmpty()) {
                                tagList += markerID;
                            } else {
                                tagList += ";" + markerID;
                            }
                            sharedPreferences.edit().putString(taggedByMe, tagList).apply();
                            FirebaseFirestore.getInstance().collection(collection).document(ActiveID).update("TaggedByMe", tagList);
                            isEvent1Executed = true;
                        }
                    }
                });

                //Second get the previous tags in TaggedByOthers then append this ActiveID to them with ; in Requester/Donor table
                FirebaseFirestore.getInstance().collection(Collection).document(markerID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null && !isEvent2Executed) {
                            String tagList = documentSnapshot.getString("TaggedByOthers");
                            if (tagList.isEmpty()) {
                                tagList += ActiveID;
                            } else {
                                tagList += ";" + ActiveID;
                            }
                            FirebaseFirestore.getInstance().collection(Collection).document(markerID).update("TaggedByOthers", tagList);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            isEvent2Executed = true;
                            startActivity(new Intent(Details.this, Flags.class));
                        }
                    }
                });
            }
        });
    }

    public void sendEmail(String email) {
        String subject = ("BloodDotNet NEW REQUEST");

        Intent emailSelectorIntent = new Intent(Intent.ACTION_SENDTO);
        emailSelectorIntent.setData(Uri.parse("mailto:"));

        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please visit the app to view more details");
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        emailIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        emailIntent.setSelector(emailSelectorIntent);
        startActivityForResult(Intent.createChooser(emailIntent, "Choose an Email client"), 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK || resultCode == RESULT_CANCELED) {
            Intent intent = new Intent(this, Flags.class);
            startActivity(intent);
            finish();
        }
    }
}
