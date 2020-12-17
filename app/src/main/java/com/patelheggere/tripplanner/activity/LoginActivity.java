package com.patelheggere.tripplanner.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.model.APIResponseModel;
import com.patelheggere.tripplanner.network.ApiInterface;
import com.patelheggere.tripplanner.network.RetrofitInstance;
import com.patelheggere.tripplanner.utils.SharedPrefsHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.patelheggere.tripplanner.utils.Constants.FIRST_TIME;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ApiInterface apiInterface;
    private TextInputEditText inputEditTextMobile, inputEditTextPassword;
    private Button mButtonLogin;
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActionBar = getSupportActionBar();
        if(mActionBar!=null)
        {
            mActionBar.setTitle("Login");
        }
        if(SharedPrefsHelper.getInstance().get(FIRST_TIME))
        {
            startActivity(new Intent(LoginActivity.this, MatantaraActivity.class));
        }
        else {
            setContentView(R.layout.activity_login);
            setUpNetwork();
            inputEditTextMobile = findViewById(R.id.et_phone_login);
            inputEditTextPassword = findViewById(R.id.et_pwd_login);
            mButtonLogin = findViewById(R.id.btn_login_submit);
            mButtonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Call<APIResponseModel> call = apiInterface.Login(inputEditTextMobile.getText().toString().trim(), inputEditTextPassword.getText().toString().trim());
                    call.enqueue(new Callback<APIResponseModel>() {
                        @Override
                        public void onResponse(Call<APIResponseModel> call, Response<APIResponseModel> response) {
                            if (response.body().isStatus()) {
                                SharedPrefsHelper.getInstance().save(FIRST_TIME, true);
                                startActivity(new Intent(LoginActivity.this, MatantaraActivity.class));
                            }
                        }

                        @Override
                        public void onFailure(Call<APIResponseModel> call, Throwable t) {

                        }
                    });
                }
            });
        }
    }

    private void setUpNetwork() {
        RetrofitInstance retrofitInstance = new RetrofitInstance();
        retrofitInstance.setClient();
        apiInterface = retrofitInstance.getClient().create(ApiInterface.class);
    }
}