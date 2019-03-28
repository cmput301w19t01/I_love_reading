package com.example.libo.myapplication.Activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.libo.myapplication.Adapter.CommentAdapter;
import com.example.libo.myapplication.Model.Comment;
import com.example.libo.myapplication.Model.Request;
import com.example.libo.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * The Item view activity.
 * This Activity shows the detail information of a book
 */
public class ItemViewActivity extends AppCompatActivity {

    private EditText EditTextBookName;
    private EditText EditTextAuthorName;
    private EditText EditTextDescription;
    private TextView TextViewClassification;
    private ImageView ImageViewBookCover;
    private Button BorrowButton;
    private Button ReturnButton;
    private Button WatchListButton;
    private ImageButton AddCommentButton;
    private Intent temp;
    private ListView ListViewComment; // The list View of the comment
    private String[] ItemSet = {"Science Fiction", "Philosophy", "Comedy", "Horror Fiction", "History"}; // Possible selection in classification
    private boolean[] SelectedItemSet;
    private ArrayList<Integer> myUserItems = new ArrayList<>(); //Selected items in terms of binary
    private ArrayList<String> resultClassification= new ArrayList<>(); //Selected items in terms of String
    private ArrayList<Comment> comments;
    private Intent resultIntent = new Intent(); //Initialization of result Intent
    private CommentAdapter adapter; // Adapter for Comment list view

    final int GET_FROM_GALLERY = 2; // result code for getting image from user gallery to set book cover

    final int GET_FROM_COMMENT = 3; // result code for getting new comment
    private DatabaseReference commentsRef;
    private Uri BookCoverUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().setBackgroundDrawableResource(R.drawable.avoid_scale_background);
        final Intent result = getIntent();
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        SelectedItemSet = new boolean[ItemSet.length];
        temp = result;
        /*
        Design of Tool Bar
        */
        setContentView(R.layout.activity_item_view);
        getSupportActionBar().setTitle("Details View");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*
        Initialization of Text view and button.
         */
        EditTextBookName = findViewById(R.id.EditTextBookName);
        EditTextAuthorName = findViewById(R.id.EditTextBookDetail);
        EditTextDescription = findViewById(R.id.EditTextDescriptionContent);
        TextViewClassification = findViewById(R.id.TextViewClassificationSelect);
        ImageViewBookCover = findViewById(R.id.ImageViewBookCover);
        BorrowButton = findViewById(R.id.ButtonRentBook);
        ReturnButton = findViewById(R.id.button_return);
        WatchListButton = findViewById(R.id.ButtonWatchList);
        AddCommentButton = findViewById(R.id.ButtonAddComment);
        ListViewComment = findViewById(R.id.ListViewComments);
        /*
        Get Information of the book from the intent
         */
        String BookName = result.getStringExtra("BookName"); // Get information from the Intent
        String AuthorName = result.getStringExtra("AuthorName");
        String Description = result.getStringExtra("Description");
        //Get the book iD
        String BookId = result.getStringExtra("ID");
        ArrayList<String> ClassificationArray = result.getStringArrayListExtra("ClassificationArray");
        Bitmap BookCover = (Bitmap) result.getParcelableExtra("BookCover"); // Get Book Cover in the format of bitmap
        final Boolean Edit = result.getBooleanExtra("edit",false); // Check if we are allowed to edit the book
        final Boolean Status = result.getBooleanExtra("status",false); //Check if the Book is available for rent

        if (!Edit){ // If we are viewing the info instead of borrowing
            resultIntent.putExtra("borrow","false"); //default setting
            resultIntent.putExtra("watchlist", "false");
            checkStatus(Status); // check if the book can be borrowed
        }

        checkEdit(Edit, BookName, AuthorName, Description, ClassificationArray, BookCover); // show the information of the Book

        comments = new ArrayList<>(); // Initialization of comment array
        //Get Comments from Firebase
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(BookId);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Comment comment = ds.getValue(Comment.class);
                    comments.add(comment);
                }

                adapter = new CommentAdapter(getApplicationContext(), comments);
                ListViewComment.setAdapter(adapter);
                setListViewHeightBasedOnChildren(ListViewComment);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        int buttonCode = result.getIntExtra("ButtonCode", -1);
        if( buttonCode == 0){
            BorrowButton.setVisibility(View.INVISIBLE);
            WatchListButton.setVisibility(View.INVISIBLE);
            ReturnButton.setVisibility(View.VISIBLE);

            ReturnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra("return", true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }


        // Onclick listener for borrow button
        BorrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Status){ //If available
                    resultIntent.putExtra("borrow","true");
                    BorrowButton.setClickable(false);
                    Toast.makeText(getBaseContext(), R.string.BorrowToast,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // Onclick listener for Watchlist button
        WatchListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultIntent.putExtra("watchlist","true");
                WatchListButton.setClickable(false);
                Toast.makeText(getBaseContext(), R.string.WatchListToast,
                        Toast.LENGTH_LONG).show();
            }
        });

        // OnClick listener for BookCover
        ImageViewBookCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery_intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery_intent, GET_FROM_GALLERY);
            }
        });

        // Onclick listener for selecting Classification of the Book
        TextViewClassification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Edit){
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(ItemViewActivity.this);
                    mBuilder.setTitle(R.string.SelectionTile);
                    mBuilder.setMultiChoiceItems(ItemSet, SelectedItemSet, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked) {
                                myUserItems.add(which); }
                            else {
                                myUserItems.remove((Integer.valueOf(which))); } }
                    });
                    mBuilder.setCancelable(false);
                    mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            ArrayList<String> item = new ArrayList<>();
                            for (int i = 0; i < myUserItems.size(); i++) {
                                item.add(ItemSet[myUserItems.get(i)]); }
                            resultClassification = item;
                            TextViewClassification.setText(CombineStringList(item)); }
                    });

                    mBuilder.setNegativeButton("Return", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    mBuilder.setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            for (int i = 0; i < SelectedItemSet.length; i++) {
                                SelectedItemSet[i] = false;
                                myUserItems.clear();
                                resultClassification = new ArrayList<String>();
                                TextViewClassification.setText("");
                            }
                        }
                    });
                    AlertDialog mDialog = mBuilder.create();
                    mDialog.show();
                }
                else{
                    TextViewClassification.setClickable(false);
                }
            }
        });

        // Onclick Listener for Add comment button
        AddCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent CommentIntent= new Intent(ItemViewActivity.this, AddCommentActivity.class);
                startActivityForResult(CommentIntent,GET_FROM_COMMENT);
                ((Activity) ItemViewActivity.this).overridePendingTransition(R.layout.animate_slide_up_enter, R.layout.animate_slide_up_exit);
            }
        });



    }


    // Get result from other activities
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            BookCoverUri = selectedImage;
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                ImageViewBookCover.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (requestCode == GET_FROM_COMMENT && resultCode == Activity.RESULT_OK){
            Intent resultIntent = data;
            Boolean resultCommand = resultIntent.getBooleanExtra("close",true);
            if (!resultCommand) {
                float Rate = resultIntent.getFloatExtra("rate", '0');
                String CommentText = resultIntent.getStringExtra("Comment");
                String UserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName(); //To be done later
                String CommentTime;
                Calendar cal = Calendar.getInstance();
                Date time = cal.getTime();
                DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                String CurrentTime = timeFormat.format(time);
                String CurrentDate = dateFormat.format(time);
                CommentTime = CurrentDate + ' ' + CurrentTime;
                Comment newComment = new Comment(Rate, UserName, CommentTime, CommentText);
                String commentId =  commentsRef.push().getKey();
                commentsRef.child(commentId).setValue(newComment);
            }
        }
    }


    /**
     * Check if the Book is available
     *
     * @param Status the status
     */
    public void checkStatus(Boolean Status){
        // This function checks if the Book is available.
        // If the Book is not available, it Borrow Button will show Unavailable
        if (Status){
            BorrowButton.setText("Unavailable");
            BorrowButton.setEnabled(false);
        }
    }

    /**
     * Display the information of the Book
     *
     * @param Edit                 if we can edit
     * @param BookName            the book name
     * @param AuthorName          the author name
     * @param Description         the description
     * @param ClassificationArray the classification array
     * @param BookCover           the book cover
     */
    public void checkEdit(Boolean Edit, String BookName, String AuthorName, String Description, ArrayList<String> ClassificationArray, Bitmap BookCover){
        // This function checks if the Book is editable
        // If it's editable, text view will be able to edit
        if (Edit){
            EditTextBookName.setEnabled(true);
            EditTextAuthorName.setEnabled(true);
            EditTextDescription.setEnabled(true);
            TextViewClassification.setClickable(true);
            BorrowButton.setVisibility(View.GONE);
            WatchListButton.setVisibility(View.GONE);
            AddCommentButton.setVisibility(View.GONE);
        }
        else{
            EditTextBookName.setCursorVisible(false);
            EditTextBookName.setFocusable(false);
            EditTextAuthorName.setCursorVisible(false);
            EditTextAuthorName.setFocusable(false);
            EditTextDescription.setCursorVisible(false);
            EditTextDescription.setFocusable(false);
            ImageViewBookCover.setEnabled(false);
            TextViewClassification.setClickable(false);
            EditTextBookName.setBackgroundResource(R.drawable.edittext_trans_broader);
            EditTextAuthorName.setBackgroundResource(R.drawable.edittext_trans_broader);
            EditTextDescription.setBackgroundResource(R.drawable.edittext_trans_broader);
        }
        EditTextBookName.setText(BookName);
        EditTextAuthorName.setText(AuthorName);
        EditTextDescription.setText(Description);
        TextViewClassification.setText(CombineStringList(ClassificationArray));
        if (CombineStringList(ClassificationArray) == "" && !Edit){
            TextViewClassification.setText("None");
        }
        if (BookCover != null){
            ImageViewBookCover.setImageBitmap(BookCover);
        }
    }


    // When the return button is pressed
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        // When the return button is pressed. Automatically transfer the required information back
        if (keyCode == KeyEvent.KEYCODE_BACK ) {
            // do something on back.
            Boolean Edit = temp.getBooleanExtra("edit",false);
            if (Edit){
                AlertDialog alertDialog = new AlertDialog.Builder(ItemViewActivity.this).create();
                alertDialog.setTitle("Note: ");
                alertDialog.setMessage("You are quitting the edit view, do you want to save?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                resultIntent.putExtra("do","edit");
                                ImageViewBookCover.buildDrawingCache(); // send the image back
                                Bitmap image= ImageViewBookCover.getDrawingCache();
                                resultIntent.putExtra("BookCover",image);
                                String BookName = EditTextBookName.getText().toString();
                                String AuthorName = EditTextAuthorName.getText().toString();
                                String Description = EditTextDescription.getText().toString();
                                resultIntent.putExtra("BookName",BookName);
                                resultIntent.putExtra("AuthorName", AuthorName);
                                resultIntent.putExtra("Description", Description);
                                resultIntent.putExtra("ClassificationArray", resultClassification);
                                setResult(Activity.RESULT_OK,resultIntent);
                                finish();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Stay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Don't save",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent resultIntent= new Intent();
                                resultIntent.putExtra("do","donotedit");
                                setResult(Activity.RESULT_OK,resultIntent);
                                finish();
                            }
                        });

                alertDialog.show();

            }
            else{
                resultIntent.putExtra("do","test");
                resultIntent.putExtra("Comment",comments);
                setResult(Activity.RESULT_OK,resultIntent);
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Combine string list string.
     *
     * @param my_list the my list
     * @return the string
     */
    public String CombineStringList(ArrayList<String> my_list){
        // The function converts a string arraylist to a string
        String new_string = "";
        for (String temp:my_list){
            new_string = new_string + temp + "/";
        }
        if (new_string.length()>1){
            return new_string.substring(0,new_string.length()-1);
        }
        else
            return "";
    }

    /**
     * Sets list view height based on children.
     *
     * @param listView the list view
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
