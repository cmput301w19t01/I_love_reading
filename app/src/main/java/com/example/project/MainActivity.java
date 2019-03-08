package com.example.project;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button start_button;
    private Button view_Button;
    private Book test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> Classification = new ArrayList<String>();
        test = new Book("", "", "", false,"", Classification);
        setContentView(R.layout.activity_main);
        start_button = (Button) findViewById(R.id.button_item_view);
        view_Button = (Button) findViewById(R.id.button_view);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ItemView = new Intent(MainActivity.this, ItemViewActivity.class); // set the intent to start next activity
                ItemView.putExtra("BookName", test.getBookName()); // Put the info of the book to next activity
                ItemView.putExtra("AuthorName", test.getAuthorName());
                ItemView.putExtra("ID", test.getID());
                ItemView.putExtra("status", test.getStatus());
                ItemView.putExtra("edit",true);
                ItemView.putExtra("Description", test.getDescription());
                ItemView.putExtra("ClassificationArray", test.getClassification());
                startActivityForResult(ItemView, 1); // request code 0 means we are allowing the user to edit the book
            }
        });

        view_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ItemView = new Intent(MainActivity.this, ItemViewActivity.class); // set the intent to start next activity
                ItemView.putExtra("BookName", test.getBookName()); // Put the info of the book to next activity
                ItemView.putExtra("AuthorName", test.getAuthorName());
                ItemView.putExtra("ID", test.getID());
                ItemView.putExtra("status", test.getStatus());
                ItemView.putExtra("edit",false);
                ItemView.putExtra("Description", test.getDescription());
                ItemView.putExtra("ClassificationArray", test.getClassification());
                ItemView.putExtra("BookCover", test.getBookCover());

                startActivityForResult(ItemView, 0); // request code 0 means we are looking for if the user decide to borrow the book
            }
        });


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (0): { // In the case that we are looking for if the user is trying to borrow book
                if (resultCode == Activity.RESULT_OK) {
                    // TODO Extract the data returned from the child Activity.
                    if (data.getStringExtra("borrow").equals("true")) {
                        this.test.setStatus(true);
                        /* The book is now borrowed, update your information



                         */
                    }
                    if (data.getStringExtra("watchlist").equals("true")){

                        /* The Book is now added to watchlist, update your information



                        */
                    }
                }
            }
            case (1): {// we are looking for the new information that the user edited the book.
                if (resultCode == Activity.RESULT_OK) {
                    String order = data.getStringExtra("do");
                    if (order.equals("edit")) {
                        test.setBookName(data.getStringExtra("BookName"));
                        test.setAuthorName(data.getStringExtra("AuthorName"));
                        test.setDescription(data.getStringExtra("Description"));
                        test.setClassification(data.getStringArrayListExtra("ClassificationArray"));
                        test.setBookCover(data.getBundleExtra("BookCover"));
                        test.setAuthorName(order);
                    }
                }
                }
                }
    }


    public void set_test(String BookName, String ID, String AuthorName, Boolean Status){
        // used to test for bugs
        this.test.setAuthorName(AuthorName);
        this.test.setBookName(BookName);
        this.test.setID(ID);
        this.test.setStatus(Status);
    }

}


