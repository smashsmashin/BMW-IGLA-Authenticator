package com.example.nfciauth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AuthorIdActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_id);

        TextView authorIdStatusTextView = findViewById(R.id.author_id_status_text_view);
        authorIdStatusTextView.setText("Author ID Enabled Successfully!");

        // You can add more logic here if needed for the "Author ID" feature
    }
}
