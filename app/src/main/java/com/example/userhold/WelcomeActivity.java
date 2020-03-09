package com.example.userhold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        final Button dev_button = findViewById(R.id.developbutton);
        dev_button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View c){
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                WelcomeActivity.this.startActivity(intent);
            }
        });

        final Button test_button = findViewById(R.id.testbutton);
        test_button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View c){
                Intent intent = new Intent(WelcomeActivity.this, TestActivity.class);
                WelcomeActivity.this.startActivity(intent);
            }
        });
    }
}
