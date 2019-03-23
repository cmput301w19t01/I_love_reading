package com.example.libo.myapplication.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.libo.myapplication.Activity.RequestDetailActivity;
import com.example.libo.myapplication.Model.Request;
import com.example.libo.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class RequestFragment extends Fragment {

    private static final String TAG = "RequestDatabase";

    private TextView userNameTextView;
    private ListView requestList;
    private int currentIndex;
    private ArrayList<Request> requests;
    private ArrayAdapter arrayAdapter;
    private DatabaseReference requestDatabseRef;
    private DatabaseReference borrowedRef;
    private DatabaseReference AllbooksRef;
    private String userid;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.request_page,container,false);
        userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestDatabseRef = FirebaseDatabase.getInstance().getReference("requests").child(userid);
        borrowedRef = FirebaseDatabase.getInstance().getReference("borrowedBooks");
        AllbooksRef = FirebaseDatabase.getInstance().getReference("books").child(userid);
        Log.d(TAG,"The current ref is   " + requestDatabseRef.toString());

        requestDatabseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,"The current news is   " + dataSnapshot.toString());
                for(DataSnapshot newds : dataSnapshot.getChildren()) {
                    Log.d(TAG,"The current news is   " + newds.toString());
                    Request request = newds.getValue(Request.class);
                    requests.add(request);
                }
                arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, requests);
                requestList.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        requestList = getActivity().findViewById(R.id.request_listview);
        Request request1 = new Request("ybai5","123","b@gmail.com",true, Calendar.getInstance().getTime());
        Request request2 = new Request("ybai5","123","b@gmail.com",false, Calendar.getInstance().getTime());
        requests = new ArrayList<>();
        requests.add(request1);
        requests.add(request2);


        requestList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Request request = requests.get(i);
                currentIndex = i;
                Intent intent = new Intent(getContext(), RequestDetailActivity.class);
                intent.putExtra("request", request);
                startActivityForResult(intent, 0);
            }
        });

        requestList.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                return true;
            }


        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (0): {
                if (resultCode == Activity.RESULT_OK) {
                    if (data.getStringExtra("result").equals("accept")) {
                        Request request = requests.get(currentIndex);
                        String borrowerId = request.getSenderId();
                        String bookID = request.getBookId();
                        request.setBorrowed(true);
                        borrowedRef.child(borrowerId).child(bookID).setValue(bookID);

                    }
                    if (data.getStringExtra("result").equals("deny")) {
                        requests.remove(currentIndex);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
