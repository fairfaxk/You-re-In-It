package com.youreinit.fairfaxk.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlaceDetails;
import com.youreinit.fairfaxk.myapplication.models.Bathroom;
import com.youreinit.fairfaxk.myapplication.models.BathroomDetails;
import com.youreinit.fairfaxk.myapplication.models.Code;
import com.youreinit.fairfaxk.myapplication.parsers.BathroomDetailsParser;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CustomWindowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_window);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);


        //Get the place ID of our current object
        Intent intent = getIntent();
        String place_id = intent.getStringExtra("PLACE_ID");

        String API_KEY = "AIzaSyCYEpFebQpUbBorbLsnIxUugIyuBtXy00A";


        PlaceDetails placeDetails = new PlaceDetails();

        GeoApiContext geoAPI = new GeoApiContext.Builder().apiKey(API_KEY).build();

        //Get the place Details
        try {
            placeDetails = new PlaceDetailsRequest(geoAPI).placeId(place_id).await();
        }
        catch (ApiException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        actionBar.setTitle(placeDetails.name);

        final String lat = Double.toString(placeDetails.geometry.location.lat);
        final String longitude = Double.toString(placeDetails.geometry.location.lng);

        //Add navigation
        Button button = findViewById(R.id.navigate);
        button.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + longitude + "&mode=w");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
        //Get the bathroom details
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://powerful-falls-22457.herokuapp.com/api/bathroom-details/findBathroomByPlaceId/" + place_id;
        final DecimalFormat df = new DecimalFormat("#.##");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        BathroomDetails bathroomDetails = BathroomDetailsParser.parseBathroomDetails(response);

                        if(bathroomDetails!=null){
                            //Set mens room data
                            Bathroom mensroom = bathroomDetails.getMensRoom();

                            CheckBox mens = findViewById(R.id.mens);
                            mens.setChecked(mensroom.isExists());
                            if(mensroom.isExists()) {

                                CheckBox mensAccessible = findViewById(R.id.menshandicap);
                                mensAccessible.setChecked(mensroom.isHandicap());

                                //find average rating
                                List<Double> ratings = mensroom.getRatings();
                                double sum = 0;
                                for (double d : ratings) {
                                    sum += d;
                                }
                                double avg = 0;
                                if(ratings.size()>0)
                                    avg = sum / ratings.size();

                                StringBuffer text = new StringBuffer("Rating: " + Double.toString(Double.valueOf(df.format(avg))));

                                SpannableStringBuilder str = new SpannableStringBuilder(text);

                                str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                TextView mensrating = findViewById(R.id.mensrating);
                                mensrating.setText(str);

                                //Get top 3 codes
                                List<Code> menscodes = mensroom.getCodes();

                                Collections.sort(menscodes, new CodeComparator());
                                int len = menscodes.size();

                                if (len >= 3) {
                                    TextView code1 = findViewById(R.id.mcode1);
                                    code1.setText(menscodes.get(0).getNumber());

                                    TextView code2 = findViewById(R.id.mcode2);
                                    code2.setText(menscodes.get(1).getNumber());

                                    TextView code3 = findViewById(R.id.mcode3);
                                    code3.setText(menscodes.get(2).getNumber());
                                }

                                if (len == 2) {
                                    TextView code1 = findViewById(R.id.mcode1);
                                    code1.setText(menscodes.get(0).getNumber());

                                    TextView code2 = findViewById(R.id.mcode2);
                                    code2.setText(menscodes.get(1).getNumber());

                                    TextView code3 = findViewById(R.id.mcode3);
                                    code3.setVisibility(View.GONE);
                                }

                                if (len == 1) {
                                    TextView code1 = findViewById(R.id.mcode1);
                                    code1.setText(menscodes.get(0).getNumber());

                                    TextView code2 = findViewById(R.id.mcode2);
                                    code2.setVisibility(View.GONE);

                                    TextView code3 = findViewById(R.id.mcode3);
                                    code3.setVisibility(View.GONE);
                                }

                                if (len == 0) {
                                    TextView code1 = findViewById(R.id.mcode1);
                                    code1.setText("No data to show");

                                    TextView code2 = findViewById(R.id.mcode2);
                                    code2.setVisibility(View.GONE);

                                    TextView code3 = findViewById(R.id.mcode3);
                                    code3.setVisibility(View.GONE);
                                }
                            }
                            else{
                                //If there is no mens room show no data
                                TextView mensrating = findViewById(R.id.mensrating);
                                mensrating.setText("No Data for Men's Room");

                                CheckBox mensAccessible = findViewById(R.id.menshandicap);
                                mensAccessible.setVisibility(View.GONE);

                                TextView code = findViewById(R.id.menscodes);
                                code.setVisibility(View.GONE);
                                
                                TextView code1 = findViewById(R.id.mcode1);
                                code1.setVisibility(View.GONE);

                                TextView code2 = findViewById(R.id.mcode2);
                                code2.setVisibility(View.GONE);

                                TextView code3 = findViewById(R.id.mcode3);
                                code3.setVisibility(View.GONE);
                            }

                            //Set Womens room data
                            Bathroom womensroom = bathroomDetails.getWomensRoom();

                            CheckBox womens = findViewById(R.id.womens);
                            womens.setChecked(womensroom.isExists());
                            
                            if(womensroom.isExists()) {
                                CheckBox womensAccessible = findViewById(R.id.womenshandicap);
                                womensAccessible.setChecked(womensroom.isHandicap());

                                //find average rating
                                List<Double> ratings = womensroom.getRatings();
                                double sum = 0;
                                for (double d : ratings) {
                                    sum += d;
                                }
                                double avg = 0;
                                if(ratings.size()>0)
                                    avg = sum / ratings.size();

                                StringBuffer text = new StringBuffer("Rating: " + Double.toString(Double.valueOf(df.format(avg))));

                                SpannableStringBuilder str = new SpannableStringBuilder(text);

                                str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                TextView womensrating = findViewById(R.id.womensrating);
                                womensrating.setText(str);

                                //Get top 3 codes
                                List<Code> womenscodes = womensroom.getCodes();

                                Collections.sort(womenscodes, new CodeComparator());
                                int len = womenscodes.size();

                                if (len >= 3) {
                                    TextView code1 = findViewById(R.id.wcode1);
                                    code1.setText(womenscodes.get(0).getNumber());

                                    TextView code2 = findViewById(R.id.wcode2);
                                    code2.setText(womenscodes.get(1).getNumber());

                                    TextView code3 = findViewById(R.id.wcode3);
                                    code3.setText(womenscodes.get(2).getNumber());
                                }

                                if (len == 2) {
                                    TextView code1 = findViewById(R.id.wcode1);
                                    code1.setText(womenscodes.get(0).getNumber());

                                    TextView code2 = findViewById(R.id.wcode2);
                                    code2.setText(womenscodes.get(1).getNumber());

                                    TextView code3 = findViewById(R.id.wcode3);
                                    code3.setVisibility(View.GONE);
                                }

                                if (len == 1) {
                                    TextView code1 = findViewById(R.id.wcode1);
                                    code1.setText(womenscodes.get(0).getNumber());

                                    TextView code2 = findViewById(R.id.wcode2);
                                    code2.setVisibility(View.GONE);

                                    TextView code3 = findViewById(R.id.mcode3);
                                    code3.setVisibility(View.GONE);
                                }

                                if (len == 0) {
                                    TextView code1 = findViewById(R.id.wcode1);
                                    code1.setText("No data to show");

                                    TextView code2 = findViewById(R.id.wcode2);
                                    code2.setVisibility(View.GONE);

                                    TextView code3 = findViewById(R.id.wcode3);
                                    code3.setVisibility(View.GONE);
                                }
                            }
                            else{
                                //If there is no womens room show no data
                                TextView womensrating = findViewById(R.id.womensrating);
                                womensrating.setText("No Data for Women's Room");

                                CheckBox womensAccessible = findViewById(R.id.womenshandicap);
                                womensAccessible.setVisibility(View.GONE);

                                TextView code = findViewById(R.id.womenscodes);
                                code.setVisibility(View.GONE);

                                TextView code1 = findViewById(R.id.wcode1);
                                code1.setVisibility(View.GONE);

                                TextView code2 = findViewById(R.id.wcode2);
                                code2.setVisibility(View.GONE);

                                TextView code3 = findViewById(R.id.wcode3);
                                code3.setVisibility(View.GONE);
                            }

                            //Set Gender Neutral Data
                            Bathroom gender_neutral_room = bathroomDetails.getGenderNeutral();

                            CheckBox gender_neutral_ = findViewById(R.id.gender_neutral);
                            gender_neutral_.setChecked(gender_neutral_room.isExists());
                            
                            if(gender_neutral_room.isExists()) {
                                CheckBox gender_neutral_Accessible = findViewById(R.id.gender_neutral_handicap);
                                gender_neutral_Accessible.setChecked(gender_neutral_room.isHandicap());

                                //find average rating
                                List<Double> ratings = gender_neutral_room.getRatings();
                                double sum = 0;
                                for (double d : ratings) {
                                    sum += d;
                                }
                                double avg = 0;
                                if(ratings.size()>0)
                                    avg = sum / ratings.size();

                                StringBuffer text = new StringBuffer("Rating: " + Double.toString(Double.valueOf(df.format(avg))));

                                SpannableStringBuilder str = new SpannableStringBuilder(text);

                                str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                                TextView gender_neutral_rating = findViewById(R.id.gender_neutral_rating);
                                gender_neutral_rating.setText(str);

                                //Get top 3 codes
                                List<Code> gender_neutral_codes = gender_neutral_room.getCodes();

                                Collections.sort(gender_neutral_codes, new CodeComparator());
                                int len = gender_neutral_codes.size();

                                if (len >= 3) {
                                    TextView code1 = findViewById(R.id.gcode1);
                                    code1.setText(gender_neutral_codes.get(0).getNumber());

                                    TextView code2 = findViewById(R.id.gcode2);
                                    code2.setText(gender_neutral_codes.get(1).getNumber());

                                    TextView code3 = findViewById(R.id.gcode3);
                                    code3.setText(gender_neutral_codes.get(2).getNumber());
                                }

                                if (len == 2) {
                                    TextView code1 = findViewById(R.id.gcode1);
                                    code1.setText(gender_neutral_codes.get(0).getNumber());

                                    TextView code2 = findViewById(R.id.gcode2);
                                    code2.setText(gender_neutral_codes.get(1).getNumber());

                                    TextView code3 = findViewById(R.id.gcode3);
                                    code3.setVisibility(View.GONE);
                                }

                                if (len == 1) {
                                    TextView code1 = findViewById(R.id.gcode1);
                                    code1.setText(gender_neutral_codes.get(0).getNumber());

                                    TextView code2 = findViewById(R.id.gcode2);
                                    code2.setVisibility(View.GONE);

                                    TextView code3 = findViewById(R.id.mcode3);
                                    code3.setVisibility(View.GONE);
                                }

                                if (len == 0) {
                                    TextView code1 = findViewById(R.id.gcode1);
                                    code1.setText("No data to show");

                                    TextView code2 = findViewById(R.id.gcode2);
                                    code2.setVisibility(View.GONE);

                                    TextView code3 = findViewById(R.id.gcode3);
                                    code3.setVisibility(View.GONE);
                                }
                            }
                            else{
                                //No gender neutral data
                                TextView gender_neutral_rating = findViewById(R.id.gender_neutral_rating);
                                gender_neutral_rating.setText("No Data for Gender Neutral Restroom");

                                CheckBox gender_neutral_Accessible = findViewById(R.id.gender_neutral_handicap);
                                gender_neutral_Accessible.setVisibility(View.GONE);

                                TextView code = findViewById(R.id.gender_neutral_codes);
                                code.setVisibility(View.GONE);

                                TextView code1 = findViewById(R.id.gcode1);
                                code1.setVisibility(View.GONE);

                                TextView code2 = findViewById(R.id.gcode2);
                                code2.setVisibility(View.GONE);

                                TextView code3 = findViewById(R.id.gcode3);
                                code3.setVisibility(View.GONE);
                            }
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e("ERROR: ", "Error with request to API");

                        //If there is no mens room show no data
                        TextView mensrating = findViewById(R.id.mensrating);
                        mensrating.setText("No Data for Men's Room");

                        CheckBox mensAccessible = findViewById(R.id.menshandicap);
                        mensAccessible.setVisibility(View.GONE);

                        TextView code = findViewById(R.id.menscodes);
                        code.setVisibility(View.GONE);

                        TextView code1 = findViewById(R.id.mcode1);
                        code1.setVisibility(View.GONE);

                        TextView code2 = findViewById(R.id.mcode2);
                        code2.setVisibility(View.GONE);

                        TextView code3 = findViewById(R.id.mcode3);
                        code3.setVisibility(View.GONE);

                        TextView womensrating = findViewById(R.id.womensrating);
                        womensrating.setText("No Data for Women's Room");

                        CheckBox womensAccessible = findViewById(R.id.womenshandicap);
                        womensAccessible.setVisibility(View.GONE);

                        TextView wcode = findViewById(R.id.womenscodes);
                        wcode.setVisibility(View.GONE);

                        TextView wcode1 = findViewById(R.id.wcode1);
                        wcode1.setVisibility(View.GONE);

                        TextView wcode2 = findViewById(R.id.wcode2);
                        wcode2.setVisibility(View.GONE);

                        TextView wcode3 = findViewById(R.id.wcode3);
                        wcode3.setVisibility(View.GONE);

                        TextView gender_neutral_rating = findViewById(R.id.gender_neutral_rating);
                        gender_neutral_rating.setText("No Data for Gender Neutral Restroom");

                        CheckBox gender_neutral_Accessible = findViewById(R.id.gender_neutral_handicap);
                        gender_neutral_Accessible.setVisibility(View.GONE);

                        TextView gcode = findViewById(R.id.gender_neutral_codes);
                        gcode.setVisibility(View.GONE);

                        TextView gcode1 = findViewById(R.id.gcode1);
                        gcode1.setVisibility(View.GONE);

                        TextView gcode2 = findViewById(R.id.gcode2);
                        gcode2.setVisibility(View.GONE);

                        TextView gcode3 = findViewById(R.id.gcode3);
                        gcode3.setVisibility(View.GONE);
                    }
                });
        queue.add(jsonObjectRequest);
        /**
         * Add data to the view
         */

        //Add the name of the facility
        StringBuffer text = new StringBuffer("Name: " + placeDetails.name);

        SpannableStringBuilder str = new SpannableStringBuilder(text);

        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView nameView = findViewById(R.id.nameView);
        nameView.setText(str);

        //Adding the address
        text = new StringBuffer("Address: " + placeDetails.formattedAddress);

        str = new SpannableStringBuilder(text);

        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView addressView = findViewById(R.id.address);
        addressView.setText(str);

        //Adding the opening hours
        text = new StringBuffer("Opening Hours: ");

        str = new SpannableStringBuilder(text);

        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, text.indexOf(":"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView hoursView = findViewById(R.id.hours);
        hoursView.setText(str);

        String[] hours = placeDetails.openingHours.weekdayText;

        for(String s : hours){
            if(s.contains("Monday")){
                TextView monday = findViewById(R.id.monday);
                monday.setText(s);
            }
            else if(s.contains("Tuesday")){
                TextView tuesday = findViewById(R.id.tuesday);
                tuesday.setText(s);
            }
            else if(s.contains("Wednesday")){
                TextView wednesday = findViewById(R.id.wednesday);
                wednesday.setText(s);
            }
            else if(s.contains("Thursday")){
                TextView thursday = findViewById(R.id.thursday);
                thursday.setText(s);
            }
            else if(s.contains("Friday")){
                TextView friday = findViewById(R.id.friday);
                friday.setText(s);
            }
            else if(s.contains("Saturday")){
                TextView saturday = findViewById(R.id.saturday);
                saturday.setText(s);
            }
            else if(s.contains("Sunday")){
                TextView sunday = findViewById(R.id.sunday);
                sunday.setText(s);
            }
        }

        CheckBox mens = findViewById(R.id.mens);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class CodeComparator implements Comparator<Code> {
        public int compare(Code a, Code b){
            return new Integer(a.getVotes()).compareTo(new Integer(b.getVotes()));
        }
    }
}
