package com.paocu.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private static AuthService authService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private Boolean isLogged;
    public static final String TOKEN_KEY = "TOKEN_KEY";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isLogged = false;

        if (authService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://10.0.2.2:8080/") //localhost for emulator
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            authService = retrofit.create(AuthService.class);
        }
    }

    public void login(View view) {
        EditText email = (EditText) findViewById(R.id.useremail);
        EditText password = (EditText) findViewById(R.id.passwordUser);

        final String stringEmail = email.getText().toString();
        final String stringPassword = password.getText().toString();

        if (!stringEmail.matches("")) {
            if (!stringPassword.matches("")) {

                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LoginWrapper loginWrapper = new LoginWrapper(stringEmail, stringPassword);
                            Response<Token> response = authService.login(loginWrapper).execute();
                            Token token = response.body();
                            if (token != null) {
                                SharedPreferences sharedPreferences = context.getSharedPreferences(
                                        getString(R.string.preference_file_key), MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(TOKEN_KEY, token.getAccessToken());
                                editor.commit();

                                isLogged = true;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                if (isLogged) {
                    Intent loginIntent = new Intent(this, MainActivity.class);
                    startActivity(loginIntent);
                }
            } else {
                password.setError("Try again a password");
            }
        } else {
            email.setError("Try again an email");
        }
    }
}
