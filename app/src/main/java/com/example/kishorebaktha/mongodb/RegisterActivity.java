package com.example.kishorebaktha.mongodb;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class RegisterActivity extends AppCompatActivity {
    EditText empname,empcode,desig,ext,pass,email;
    MongoClientURI uri;
    MongoClient mongoClient;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        empname=(EditText)findViewById(R.id.empname2);
        empcode=(EditText)findViewById(R.id.empcode2);
        desig=(EditText)findViewById(R.id.desig);
        ext=(EditText)findViewById(R.id.phoneext);
        pass=(EditText)findViewById(R.id.pass);
        email=(EditText)findViewById(R.id.email);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                uri  = new MongoClientURI("mongodb://kbak:123456@ds155634.mlab.com:55634/sample");
                mongoClient = new MongoClient(uri);
                // use test as a datbase,use your database here
            }
        });
        thread.start();
    }

    public void registeremp(View view) {
        progressDialog = ProgressDialog.show(this,"Sending request","Please wait...",false,false);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                // fetch the collection object ,car is used here,use your own
                BasicDBObject document = new BasicDBObject();
                document.put("username",empname.getText().toString());
                document.put("password",pass.getText().toString());
                document.put("empcode",empcode.getText().toString());
                document.put("desig",desig.getText().toString());
                document.put("phoneext",ext.getText().toString());
                document.put("email",email.getText().toString());
                document.put("type","staff");
                MongoDatabase db2 = mongoClient.getDatabase(uri.getDatabase());
                MongoCollection<BasicDBObject> collection = db2.getCollection("users", BasicDBObject.class);
                collection.insertOne(document);
                String email2=email.getText().toString();
                String subject = "Registration";
                String message ="Your registration is successful"+"\n"+
                        "Username:"+empname.getText().toString()+"\n"+
                        "Employee code:"+empcode.getText().toString()+"\n"+
                        "Designation:"+desig.getText().toString()+"\n"+
                        "Phone Ext:"+ext.getText().toString()+"\n";
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
                    mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email2));
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
            }
        });
        thread.start();
        progressDialog.dismiss();
        Toast.makeText(getApplicationContext(),"Registered successfully",Toast.LENGTH_SHORT).show();
        Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);

    }
}
