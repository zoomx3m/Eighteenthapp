package com.example.x_ray.eighteenthapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class DataFragment extends Fragment {
    private List<Marker> listOfMarkers;
    static final String MARKER_LIST_KEY = "marker_list";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            listOfMarkers = (List<Marker>) savedInstanceState.getSerializable(MARKER_LIST_KEY);
        }
        if(listOfMarkers == null){
            listOfMarkers = new LinkedList();
        }
        setRetainInstance(true);
        return null;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(MARKER_LIST_KEY, (Serializable) listOfMarkers);

    }

    public List<Marker> getListOfMarkers() {
        return listOfMarkers;
    }
}
