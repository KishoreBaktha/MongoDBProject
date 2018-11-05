package com.example.kishorebaktha.mongodb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class forgotActivity extends AppCompatActivity {
    EditText forgotemail;
    DBCollection coll;
    int success=0;
    ProgressDialog progressDialog;
    final Handler mHandler = new Handler();
    DBCursor cursor;
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            progressDialog.dismiss();
            if(success==1)
            {
                Toast.makeText(getApplicationContext(),"Check your email for details",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Invalid Email",Toast.LENGTH_SHORT).show();
                forgotemail.setText("");
            }
            success=0;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        forgotemail=(EditText)findViewById(R.id.forgotemail);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                MongoClientURI uri  = new MongoClientURI("mongodb://kbak:123456@ds155634.mlab.com:55634/sample");
                MongoClient client = new MongoClient(uri);
                // use test as a datbase,use your database here
                DB db = client.getDB("sample");
                coll = db.getCollection("users");
            }
        });
        thread.start();
    }

    public void forgot(View view) {

        progressDialog = ProgressDialog.show(this,"Verifying..","Please wait...",false,false);
        final String email=forgotemail.getText().toString();
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                cursor=coll.find();
                while (cursor.hasNext())
                {
                    DBObject doc2 = cursor.next();
                    if(email.equals(doc2.get("email")))
                    {
                        String subject = "User details";
                        String message = "Username:"+doc2.get("username")+"\n"+
                                "Password:"+doc2.get("password")+"\n";
                        Properties props = new Properties();
                        //Configuring properties for gmail
                        // If you are not using gmail you may need to change the values
                        props.put("mail.smtp.host", "smtp.gmail.com");
                        props.put("mail.smtp.socketFactory.port", "465");
                        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                        props.put("mail.smtp.auth", "true");
                        props.put("mail.smtp.port", "465");
                        // Creating a new session
                        Session session = Session.getDefaultInstance(props,
                                new javax.mail.Authenticator() {
                                    //Authenticating the password
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(Config.EMAIL, Config.PASSWORD);
                                    }
                                });

//Creating MimeMessage object
                        try {
                            MimeMessage mm = new MimeMessage(session);
                            MimeMessage mm2=new MimeMessage(session);
                            //Setting sender address
                            mm.setFrom(new InternetAddress(Config.EMAIL));
                            // Adding receiver
                            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
                            // Adding subject
                            mm.setSubject(subject);
                            // Adding message
                            mm.setText(message);
                            Transport.send(mm);
                        }
                        catch (AddressException e) {
                            e.printStackTrace();
                        } catch (MessagingException e) {
                            e.printStackTrace();
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
}
