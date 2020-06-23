package com.omnitech.blooddonationnetwork.Fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.omnitech.blooddonationnetwork.MainActivity;
import com.omnitech.blooddonationnetwork.R;

public class Login extends Fragment {
    View view;
    EditText Email, Password;
    Button login;
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState){

        view = inflater.inflate(R.layout.login_fragment,container,false);
        Email = view.findViewById(R.id.email_login);
        Password = view.findViewById(R.id.pass_login);
        login = view.findViewById(R.id.btn_login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });
        return view;
    }
    public void SignIn() {
        final String email = Email.getText().toString().trim();
        String pass = Password.getText().toString().trim();
        boolean netState = ((MainActivity)getActivity()).CheckConnectivity(view);

        if (TextUtils.isEmpty(email)) {
            Email.setError("Please Enter Email ID");
            Email.requestFocus();
            return;
        }
        if (!ValidateEmail(Email.getText().toString().trim())) {
            Email.setError("Please enter valid email address");
            Email.requestFocus();
            return;
        } else if (TextUtils.isEmpty(pass)) {
            Password.setError("Please Enter Password");
            return;
        }
        else if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)&& netState) {
            if (ValidateEmail(Email.getText().toString().trim())) {
                boolean isValid = ((MainActivity)getActivity()).SignInUser(email,pass);
                if(!isValid){
                    Toast.makeText(getContext(), "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public boolean ValidateEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
