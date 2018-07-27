package com.youreinit.fairfaxk.myapplication.parsers;

import com.youreinit.fairfaxk.myapplication.models.Bathroom;
import com.youreinit.fairfaxk.myapplication.models.BathroomDetails;
import com.youreinit.fairfaxk.myapplication.models.Code;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BathroomDetailsParser {
    public static BathroomDetails parseBathroomDetails(JSONObject response){
        BathroomDetails bathroomDetails = new BathroomDetails();
        if(response==null){
            return null;
        }

        //set placeid
        try {
            String place_id = response.getString("placeId");
            bathroomDetails.setPlaceId(place_id);
        }
        catch (JSONException e) {
            return bathroomDetails;
        }

        //set mens room
        try {
            JSONObject mensObject = response.getJSONObject("mensRoom");

            Bathroom mensRoom = parseBathroom(mensObject);
            bathroomDetails.setMensRoom(mensRoom);

        } catch (JSONException e) {
            bathroomDetails.setMensRoom(new Bathroom());
        }

        //set womens room
        try {
            JSONObject womensObject = response.getJSONObject("womensRoom");

            Bathroom womensRoom = parseBathroom(womensObject);
            bathroomDetails.setWomensRoom(womensRoom);

        } catch (JSONException e) {
            bathroomDetails.setWomensRoom(new Bathroom());
        }

        //set gender neutral
        try {
            JSONObject genderNeutralObject = response.getJSONObject("genderNeutral");

            Bathroom genderNeutralRoom = parseBathroom(genderNeutralObject);
            bathroomDetails.setGenderNeutral(genderNeutralRoom);

        } catch (JSONException e) {
            bathroomDetails.setGenderNeutral(new Bathroom());
        }


        return bathroomDetails;
    }

    private static Bathroom parseBathroom(JSONObject response){
        Bathroom bathroom = new Bathroom();

        Boolean exists;
        Boolean accessible;
        List<Double> ratings;
        List<Code> codes;

        //set exists
        try {
            exists = response.getBoolean("exists");
        } catch (JSONException e) {
            exists = new Boolean(false);
        }

        //set accessible
        try {
            accessible = response.getBoolean("handicap");
        } catch (JSONException e) {
            accessible = new Boolean(false);
        }

        //set ratings
        try{
            JSONArray rates = response.getJSONArray("ratings");
            ratings = new ArrayList<Double>();
            for(int i = 0; i< rates.length(); i++){
                ratings.add(rates.getDouble(i));
            }
        } catch (JSONException e){
             ratings = new ArrayList<Double>();
        }

        //set codes
        try{
            JSONArray code = response.getJSONArray("codes");
            codes = new ArrayList<Code>();

            for(int i = 0; i<code.length(); i++){
                JSONObject obj = code.getJSONObject(i);

                String number = obj.getString("number");
                int votes = obj.getInt("votes");
                //TODO Set the dates

                Code c = new Code();
                c.setNumber(number);
                c.setVotes(votes);
                codes.add(c);
            }
        } catch (JSONException e){
            codes = new ArrayList<Code>();
        }

        bathroom.setExists(exists);
        bathroom.setHandicap(accessible);
        bathroom.setCodes(codes);
        bathroom.setRatings(ratings);
        return bathroom;
    }
}
