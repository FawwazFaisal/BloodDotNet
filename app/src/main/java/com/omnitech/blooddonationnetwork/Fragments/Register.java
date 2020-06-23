package com.omnitech.blooddonationnetwork.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.FocusFinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.omnitech.blooddonationnetwork.MainActivity;
import com.omnitech.blooddonationnetwork.R;

import java.util.HashMap;

public class Register extends Fragment {
    View view;
    EditText Email, Password, Contact, Name, Gender, Age;
    Button Register;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){
        view = inflater.inflate(R.layout.register_fragment, container, false);
        Email = view.findViewById(R.id.email_reg);
        Password = view.findViewById(R.id.pass_reg);
        Contact = view.findViewById(R.id.contact_reg);
        Name = view.findViewById(R.id.name_reg);
        Gender = view.findViewById(R.id.gender_reg);
        Age = view.findViewById(R.id.age_reg);
        Register = view.findViewById(R.id.btn_reg);
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register(Email.getText().toString(), Password.getText().toString(), Name.getText().toString(), Contact.getText().toString(), Age.getText().toString(), Gender.getText().toString());
            }
        });

        return view;

    }

    private void register(final String email , final String pass,final String name,final String contact,final String age,final String gender) {
        if(!email.isEmpty() || !pass.isEmpty() || !name.isEmpty() || !contact.isEmpty() || !age.isEmpty() || !gender.isEmpty())
        {
            boolean netState = ((MainActivity)getActivity()).CheckConnectivity(view);
            if(netState){
                final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                firebaseAuth.removeAuthStateListener(((MainActivity)getActivity()).mAuthStateListener);
                firebaseAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Register.setClickable(false);
                                    Register.setEnabled(false);
                                    Register.setAlpha(0.5f);
                                    HashMap<String, Object> user = new HashMap<String, Object>();
                                    user.put("email", email);
                                    user.put("name", name);
                                    user.put("contact", contact);
                                    user.put("age", age);
                                    user.put("gender", gender);
                                    user.put("isRequester","False");
                                    user.put("isDonator","False");
                                    user.put("activeID","");

                                    FirebaseFirestore.getInstance().collection("Users").document(firebaseAuth.getUid()).set(user)
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    firebaseAuth.signOut();
                                                    ((MainActivity)getActivity()).firebaseAuth.signOut();
                                                    startActivity(new Intent(getActivity(),MainActivity.class));
                                                    Toast.makeText(getContext(), "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            ((MainActivity)getActivity()).firebaseAuth.signOut();
                                            firebaseAuth.signOut();
                                            startActivity(new Intent(getActivity(),MainActivity.class));

                                            final ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.view_pager_main);
                                            viewPager.setCurrentItem(0);
                                        }
                                    });
                                } else {
                                    Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
        else {
            Toast.makeText(getContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
        }

    }
}
