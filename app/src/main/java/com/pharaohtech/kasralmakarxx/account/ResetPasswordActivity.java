package com.pharaohtech.kasralmakarxx.account;

import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.pharaohtech.kasralmakarxx.R;
import com.squareup.picasso.Picasso;

public class ResetPasswordActivity extends AppCompatActivity {

    private ImageView forgetBg;
    private EditText forgetEmail;
    private Button forgetBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mAuth = FirebaseAuth.getInstance();
        forgetBg = findViewById(R.id.forgetBg);
        forgetBtn = findViewById(R.id.forgetBtn);
        Picasso.get().load(R.drawable.bg_login2).into(forgetBg);
        reset();
    }

    private void reset(){
        forgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgetEmail = findViewById(R.id.forgetEmail);
                String email = forgetEmail.getText().toString();
                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(ResetPasswordActivity.this, R.string.resetPlease, Toast.LENGTH_SHORT).show();
                }else
                {
                    mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ResetPasswordActivity.this, R.string.resetSucess, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ResetPasswordActivity.this, R.string.resetFaliure, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
