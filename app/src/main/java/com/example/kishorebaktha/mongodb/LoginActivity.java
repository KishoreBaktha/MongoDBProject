package com.example.kishorebaktha.mongodb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class LoginActivity extends AppCompatActivity {
    EditText username,password;
   int success=0;
   ProgressDialog progressDialog;
    DBCursor cursor;
    TextView forgot;
    DBCollection coll,coll2;
    String empcode,desig,phoneext,email;
    final Handler mHandler = new Handler();
    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            progressDialog.dismiss();
            if(success==1&&!username.equals("admin"))
            {
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("username",username.getText().toString());
                intent.putExtra("empcode",empcode);
                intent.putExtra("desig",desig);
                intent.putExtra("phoneext",phoneext);
                intent.putExtra("email",email);
                startActivity(intent);
            }
            else if(success==1&&username.equals("admin"))
            {
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("username",username.getText().toString());
                intent.putExtra("empcode","admin");
                intent.putExtra("desig","admin");
                intent.putExtra("phoneext","admin");
                intent.putExtra("email","admin");
                startActivity(intent);
            }
            else
                Toast.makeText(getApplicationContext(),"Invalid details",Toast.LENGTH_SHORT).show();
            success=0;
        }
    };
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            ExitActivity.exitApplication(this);
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username=(EditText)findViewById(R.id.username);
        password=(EditText)findViewById(R.id.password);
        forgot=(TextView) findViewById(R.id.forgot);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(),forgotActivity.class);
                startActivity(intent);
            }
        });
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                MongoClientURI uri  = new MongoClientURI("mongodb://kbak:123456@ds155634.mlab.com:55634/sample");
                MongoClient client = new MongoClient(uri);
                // use test as a datbase,use your database here
                DB db = client.getDB("sample");
                 coll = db.getCollection("users");
                 coll2=db.getCollection("FAR");
            }
        });
        thread.start();
    }

    public void login(View view) {
        progressDialog = ProgressDialog.show(this,"Verifying..","Please wait...",false,false);
        final String user=username.getText().toString();
        final String pass=password.getText().toString();
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                cursor=coll.find();
            while (cursor.hasNext())
            {
                DBObject doc2 = cursor.next();
                if(user.equals(doc2.get("username"))&&pass.equals(doc2.get("password")))
                {
                    if(!user.equals("admin"))
                    {
                        empcode=doc2.get("empcode").toString();
                        desig=doc2.get("desig").toString();
                        phoneext=doc2.get("phoneext").toString();
                        email=doc2.get("email").toString();
                    }
                    success=1;
                    break;
                }
            }
                mHandler.postDelayed(mUpdateResults,0);
            }
        });
         thread.start();
    }

    public void register(View view) {
        Intent intent=new Intent(getApplicationContext(),RegisterActivity.class);
        startActivity(intent);
    }
}
