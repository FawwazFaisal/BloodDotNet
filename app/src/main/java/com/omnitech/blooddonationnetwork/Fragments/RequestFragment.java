package com.omnitech.blooddonationnetwork.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omnitech.blooddonationnetwork.Adapter.BloodType;
import com.omnitech.blooddonationnetwork.Adapter.BloodTypeAdapter;
import com.omnitech.blooddonationnetwork.CreateNew;
import com.omnitech.blooddonationnetwork.Flags;
import com.omnitech.blooddonationnetwork.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class RequestFragment extends Fragment {
    public static final String Age = "Age";
    public static final String Gender = "Gender";
    public static final String Contact = "Contact";
    private static final String Email = "Email";
    private static final String Name = "Name";
    private static final String ID = "ID";
    View view;
    Spinner spinner;
    EditText quantity;
    ArrayList<String> items;
    Button confirm;
    Boolean spinnerSelected = false;
    String BloodTypeSelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.request_fragment, container, false);
        spinner = view.findViewById(R.id.blood_type);
        quantity = view.findViewById(R.id.quantity_request);
        confirm = view.findViewById(R.id.confirm_request);

        final ArrayList<BloodType> pojoItems = new ArrayList<>();
        items = new ArrayList<>();
        items.add("A+");
        items.add("A-");
        items.add("B+");
        items.add("B-");
        items.add("AB+");
        items.add("AB-");
        items.add("O+");
        items.add("O-");
        final BloodTypeAdapter adapter = new BloodTypeAdapter(getActivity(), R.layout.spinner_item_layout, pojoItems);
        for (String Item : items) {
            BloodType pojo = new BloodType(Item);
            pojoItems.add(pojo);
        }
        spinner.setAdapter(adapter);

//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, items);
//        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSelected = true;
                BloodType currentItem = (BloodType)parent.getSelectedItem();
                BloodTypeSelected = currentItem.getBloodType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addToDB();
        return view;
    }

    private void addToDB() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String id = sharedPreferences.getString(ID, "");
        final String name = sharedPreferences.getString(Name, "");
        final String email = sharedPreferences.getString(Email, "");
        final String gender = sharedPreferences.getString(Gender, "");
        final String contact = sharedPreferences.getString(Contact, "");
        final String age = sharedPreferences.getString(Age, "");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(quantity.getText()) && spinnerSelected) {
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                    HashMap<String, String> object = new HashMap<>();
                    object.put("Name", name);
                    object.put("Email", email);
                    object.put("Age", age);
                    object.put("Gender", gender);
                    object.put("Contact", contact);
                    object.put("Quantity", quantity.getText().toString());
                    object.put("BloodType", BloodTypeSelected);
                    object.put("Latitude",String.valueOf(((CreateNew)getActivity()).getLatitude()));
                    object.put("Longitude",String.valueOf(((CreateNew)getActivity()).getLongitude()));
                    object.put("TaggedByOthers","");
                    object.put("TaggedByMe","");
                    String prefix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                    String suffix = String.valueOf(new Random().nextInt(999));
                    final String docId = prefix + suffix;

                    SharedPreferences.Editor edit = sharedPreferences.edit();
                    edit.putString("isRequester", "True");
                    edit.putString("activeID",docId);
                    edit.apply();
                    FirebaseFirestore.getInstance().collection("Request").document(docId).set(object).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                                startActivity(new Intent(getActivity(), Flags.class));
                                FirebaseFirestore.getInstance().collection("Users").document(id).update("isRequester","True","activeID",docId);
                            }
                        }
                    });
                }
                else{
                    Snackbar.make(getView(),"Missing Information",Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    }).setTextColor(getResources().getColor(R.color.colorAccent)).show();
                }
            }
        });
    }
}
