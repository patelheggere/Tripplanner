package com.patelheggere.tripplanner.activity.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.patelheggere.tripplanner.R;
import com.patelheggere.tripplanner.activity.MapActivity;
import com.patelheggere.tripplanner.model.APIResponseModel;
import com.patelheggere.tripplanner.model.TalukByAcModel;
import com.patelheggere.tripplanner.network.ApiInterface;
import com.patelheggere.tripplanner.network.RetrofitInstance;
import com.patelheggere.tripplanner.network.RetrofitInstance2;
import com.patelheggere.tripplanner.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private View mView;
    private Spinner AcSpinner, HobliSpinner;
    private Button acMap, HobliMap, BoothMap;
    private List<String> HobliNames, AcNames;
    private String selectedHobliCode, selectedHobliName;
    private String selectedAc;

    private String polyMap;
    private List<TalukByAcModel> talukByAcModelList;
    private TalukByAcModel selectedTalukData;
    private TextInputEditText editTextBooth;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_home, container, false);
        initView();
        initData();
        initListener();
        return mView;
    }

    private void initListener() {
        AcSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0)
                {
                    selectedAc = "005";
                    polyMap = kudachiPoly;
                    getHoblis("005");
                }
                else
                {
                    polyMap = chitPoly;
                    selectedAc = "040";
                    getHoblis("040");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        HobliSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedHobliCode = talukByAcModelList.get(i).getKGISHobliCode();
                selectedHobliName = talukByAcModelList.get(i).getKGISTHobliName();
                selectedTalukData = talukByAcModelList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        HobliMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(polyMap!=null && selectedTalukData!=null) {
                    Intent intent = new Intent(getContext(), MapActivity.class);
                    intent.putExtra("DATA", selectedTalukData);
                    intent.putExtra("MAP", polyMap);
                    startActivity(intent);
                }
            }
        });

        BoothMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBooth(selectedAc, editTextBooth.getText().toString());
            }
        });

    }

    private void initView() {
        AcSpinner = mView.findViewById(R.id.sp_ac);
        HobliSpinner = mView.findViewById(R.id.sp_hobli);
        acMap = mView.findViewById(R.id.ac_showmap);
        HobliMap = mView.findViewById(R.id.hobli_showmap);
        BoothMap = mView.findViewById(R.id.booth_showmap);
        editTextBooth = mView.findViewById(R.id.et_booth);
        progressBar = mView.findViewById(R.id.progresBar);
    }

    String kudachiPoly, chitPoly;

    private void initData() {
        setUpNetwork();
        AcNames = new ArrayList<>();
        AcNames.add("Kudachi");
        AcNames.add("Chittapura");
        ArrayAdapter aa = new ArrayAdapter(getContext(), R.layout.spinner_item, AcNames);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        AcSpinner.setAdapter(aa);
       polyMap =  SharedPrefsHelper.getInstance().get("KUDACHI_POLY");

        if(polyMap==null){
            getACPoly();
        }
        else
        {
            kudachiPoly = polyMap;
            chitPoly = SharedPrefsHelper.getInstance().get("CHIT_POLY");
        }
    }

    private void getBooth(String ac, String booth) {
        progressBar.setVisibility(View.VISIBLE);

        final Call<APIResponseModel> responseModelCall = apiInterface.GetBoothByAC(ac, booth);
        responseModelCall.enqueue(new Callback<APIResponseModel>() {
            @Override
            public void onResponse(Call<APIResponseModel> call, Response<APIResponseModel> response) {
                progressBar.setVisibility(View.GONE);

                List<TalukByAcModel> talukByAcModelList = response.body().getTalukByAcList();
                if(talukByAcModelList!=null && talukByAcModelList.size()>0){
                    if(polyMap!=null ) {
                        Intent intent = new Intent(getContext(), MapActivity.class);
                        intent.putExtra("DATA", talukByAcModelList.get(0));
                        intent.putExtra("MAP", polyMap);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onFailure(Call<APIResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);

            }
        });
    }

    private void getACPoly()
    {
        progressBar.setVisibility(View.VISIBLE);
        final Call<APIResponseModel> responseModelCall = apiInterface.GetACPolygon();
        responseModelCall.enqueue(new Callback<APIResponseModel>() {
            @Override
            public void onResponse(Call<APIResponseModel> call, Response<APIResponseModel> response) {
                progressBar.setVisibility(View.GONE);

                List<TalukByAcModel> talukByAcModelList = response.body().getTalukByAcList();
                if(talukByAcModelList!=null && talukByAcModelList.size()>0){
                  kudachiPoly = talukByAcModelList.get(0).getPolyGon();
                  chitPoly = talukByAcModelList.get(1).getPolyGon();
                  SharedPrefsHelper.getInstance().save("KUDACHI_POLY", kudachiPoly);
                  SharedPrefsHelper.getInstance().save("CHIT_POLY", chitPoly);
                    polyMap = kudachiPoly;
                }
            }

            @Override
            public void onFailure(Call<APIResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);

            }
        });
    }
    private void getHoblis(String ac) {
        progressBar.setVisibility(View.VISIBLE);
        final Call<APIResponseModel> responseModelCall = apiInterface.GetHobliByAC(ac);
        responseModelCall.enqueue(new Callback<APIResponseModel>() {
            @Override
            public void onResponse(Call<APIResponseModel> call, Response<APIResponseModel> response) {
                progressBar.setVisibility(View.GONE);

                talukByAcModelList = response.body().getTalukByAcList();
                HobliNames = new ArrayList<>();
                if(talukByAcModelList!=null && talukByAcModelList.size()>0){
                    for(int i=0; i<talukByAcModelList.size(); i++)
                    {
                        HobliNames.add(talukByAcModelList.get(i).getKGISTHobliName()+"("+talukByAcModelList.get(i).getKGISTalukName()+")");
                    }
                    ArrayAdapter aa = new ArrayAdapter(getContext(), R.layout.spinner_item, HobliNames);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    HobliSpinner.setAdapter(aa);
                }
            }

            @Override
            public void onFailure(Call<APIResponseModel> call, Throwable t) {
                progressBar.setVisibility(View.GONE);

            }
        });
    }

    private ApiInterface apiInterface, apiInterface2;

    private void setUpNetwork() {
        RetrofitInstance retrofitInstance = new RetrofitInstance();
        retrofitInstance.setClient();
        apiInterface = retrofitInstance.getClient().create(ApiInterface.class);

        RetrofitInstance2 retrofitInstance2 = new RetrofitInstance2();
        retrofitInstance2.setClient();
        apiInterface2 = retrofitInstance2.getClient().create(ApiInterface.class);

    }
}