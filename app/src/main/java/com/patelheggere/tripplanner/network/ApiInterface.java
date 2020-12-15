package com.patelheggere.tripplanner.network;



import com.patelheggere.tripplanner.model.APIResponseModel;
import com.patelheggere.tripplanner.model.BeneficiaryModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    /*
     * Retrofit get annotation with our URL
     * And our method that will return us details of student.
     */

   /* //mine AIzaSyD_Zbbwx7aYQaAWnl5O2Dv4-6r2G3dhEUI
    //ind AIzaSyDexSpfSK4WI1XnxQCuusnateV57knMJww
    @GET("api/place/nearbysearch/json?sensor=true&rankby=distance&key=AIzaSyDexSpfSK4WI1XnxQCuusnateV57knMJww")
    Call<Place> getNearbyPlaces(@Query("types") String type, @Query("location") String location);
    //Call<Place> getNearbyPlaces(@Query("location") String location);

    @GET("api/place/nearbysearch/json?sensor=true&rankby=distance&key=AIzaSyDexSpfSK4WI1XnxQCuusnateV57knMJww")
    Call<Place> getNearbyPlacesWithToken(@Query("location") String location, @Query("pagetoken") String token);*/

    // with type
    //Call<Place> getNearbyPlaces(@Query("types") String type, @Query("location") String location);

   // Call<Place> getNearbyPlaces(@Query("types") String type, @Query("location") String location, @Query("radius") int radius);

   // @GET("beneficiary/getByMobile.php")
  //  Call<BeneficiaryModel> getByMobile(@Query("mobile") String mobile);

/*    @GET("getTaskByExeId.php")
    Call<List<AssignedTasksModel>> getTaskAssignedToExe(@Query("id") String id);

    @GET("VerifyUser.php")
    Call<ExecVerifyModel> verifyUser(@Query("uname") String uname, @Query("pwd") String pwd);

    @GET("getAllProducts.php")
    Call<List<ProductsOnlyModel>> getAllProducts(@Query("name") String name);

    @GET("getProductDetails.php")
    Call<List<ProductDetails>> getProductDetails(@Query("pid") String name);

 */

    @GET("GetTalukByAC")
    Call<APIResponseModel> GetTalukByAC(@Query("AC_Code") String ac);


    @GET("GetHobliByAC")
    Call<APIResponseModel> GetHobliByAC(@Query("AC_Code") String ac);


    @GET("GetACPolygon")
    Call<APIResponseModel> GetACPolygon();

    @GET("GetBoothByAC")
    Call<APIResponseModel> GetBoothByAC(@Query("AC_Code") String ac, @Query("Booth") String status);

    @GET("beneficiary/GetBoothLeaders.php")
    Call<List<BeneficiaryModel>> getboothLeaders(@Query("booth_no") int id);

}
