package com.patelheggere.tripplanner.activity.ui.dashboard;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.adapter.UsersAdapter;
import com.patelheggere.tripplanner.model.BeneficiaryModel;
import com.patelheggere.tripplanner.network.ApiInterface;
import com.patelheggere.tripplanner.network.RetrofitInstance;
import com.patelheggere.tripplanner.network.RetrofitInstance2;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DashboardFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    private DashboardViewModel dashboardViewModel;
    private View mView;
    private Spinner villageSpinner;
    private List<String> villageName;
    private RecyclerView recyclerView;
    private TextView textViewTotal;
    private ProgressBar progressBar;
    private RadioGroup radioGroup;
    private LinearLayout village_data, boothDetails;
    private EditText editTextBoothNo;
    private Button buttonGetData;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        recyclerView = mView.findViewById(R.id.beneficaryRecyclerView);
        progressBar = mView.findViewById(R.id.progressbar);
        boothDetails = mView.findViewById(R.id.booth_data);
        editTextBoothNo = mView.findViewById(R.id.editText_booth_no);
        buttonGetData = mView.findViewById(R.id.get_booth_data);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE},1);

        setUpNetwork();
        initListeners();
        return mView;
    }

    private void initListeners() {



        buttonGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editTextBoothNo.getText()!=null && editTextBoothNo.getText().length()>0)
                {
                    getBoothData(Integer.parseInt(editTextBoothNo.getText().toString()));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1 : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }
    private void getBoothData(int number) {
        progressBar.setVisibility(View.VISIBLE);
        Call<List<BeneficiaryModel>> listCall = apiInterface.getboothLeaders(number);
        recyclerView.setVisibility(View.GONE);
        listCall.enqueue(new Callback<List<BeneficiaryModel>>() {
            @Override
            public void onResponse(Call<List<BeneficiaryModel>> call, Response<List<BeneficiaryModel>> response) {
                progressBar.setVisibility(View.GONE);
                if(response.isSuccessful())
                {
                    if(response.body().size()>0)
                    {
                        //textViewTotal.setText("Total:"+response.body().size());
                        UsersAdapter usersAdapter = new UsersAdapter(getContext(), response.body());
                        recyclerView.setAdapter(usersAdapter);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        Toast.makeText(getContext(), "No data found for this village", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<BeneficiaryModel>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Some thing wrong while fetching data", Toast.LENGTH_LONG).show();

            }
        });
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private ApiInterface apiInterface;

    private void setUpNetwork()
    {
        RetrofitInstance2 retrofitInstance = new RetrofitInstance2();
        retrofitInstance.setClient();
        apiInterface = retrofitInstance.getClient().create(ApiInterface.class);
    }
}
