package com.example.kishorebaktha.mongodb;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.nexmo.client.NexmoClient;
import com.nexmo.client.auth.AuthMethod;
import com.nexmo.client.auth.TokenAuthMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
//BackgroundWriter backgroundWriter;
    EditText text;
   // private static final int request_camera=1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
   // private ZXingScannerView scannerView;
   SurfaceView cameraview;
    TextView result;
    EditText dept,make,empname,empcode;
    String desig,phoneext;
    Dialog dialog;
    AuthMethod auth = new TokenAuthMethod("b74672bd", "e764e53751a8f30d");
    NexmoClient client = new NexmoClient(auth);
    Spinner spinner,spinner2;
    String category,type;
    String [] latarray=new String[100];
    String [] longarray=new String[100];
    int index=0;
    private ProgressDialog progressDialog;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    MongoClientURI uri;
    MongoClient mongoClient;
    DB db;
//    SharedPreferences settings = getSharedPreferences("user_mode", Context.MODE_PRIVATE);
//    String username = settings.getString("username", "");
    String Latitude,Longitude;
    String Dept,Loc,System_serialnum,Purchasedate,Price,System_make,emailuser,email;
    final int requestcamera = 1001;
    final Handler mHandler = new Handler();
    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            progressDialog.dismiss();
            showDialog(MainActivity.this,Dept,Loc,System_serialnum,Purchasedate,Price);
        }
    };
    final Handler mHandler2 = new Handler();
    final Handler mHandler3 = new Handler();
    final Handler mHandler4 = new Handler();
    // Create runnable for posting
    final Runnable mUpdateResults2 = new Runnable() {
        public void run() {
            progressDialog.dismiss();
            Intent intent=getIntent();
            empname.setText(intent.getStringExtra("username"));
            empcode.setText(intent.getStringExtra("empcode"));
            desig=intent.getStringExtra("desig");
            emailuser=intent.getStringExtra("email");
            phoneext=intent.getStringExtra("phoneext");
            dept.setText(Dept);
            make.setText(System_make);
            empname.setFocusable(false);
            empcode.setFocusable(false);
            dept.setFocusable(false);
            make.setFocusable(false);
            //Toast.makeText(getApplicationContext(),"Plese wait....",Toast.LENGTH_LONG).show();
            int width = (int) (MainActivity.this.getResources().getDisplayMetrics().widthPixels * 0.95);
            // set height for dialog
            int height = (int) (MainActivity.this.getResources().getDisplayMetrics().heightPixels * 0.95);
            dialog.getWindow().setLayout(width, height);
            dialog.show();

        }
    };
    final Runnable mUpdateResults3 = new Runnable() {
        public void run() {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),"done",Toast.LENGTH_LONG).show();
          Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
            intent.putExtra("Latitude",Latitude);
            intent.putExtra("Longitude",Longitude);
            intent.putExtra("index",String.valueOf(index));
            Bundle b=new Bundle();
            b.putStringArray("Latarray", latarray);
            b.putStringArray("Longarray", longarray);
            intent.putExtras(b);
            startActivity(intent);
        }
    };
    final Runnable mUpdateResults4 = new Runnable() {
        public void run() {
            progressDialog.dismiss();
            Toast.makeText(getApplicationContext(),"Updated Sucessfully",Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(),"Login Again To See The Changes",Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraview = (SurfaceView) findViewById(R.id.cameraview);
        result = (TextView) findViewById(R.id.textview);
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).
                setRequestedPreviewSize(640, 480).setAutoFocusEnabled(true).build();
        cameraview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.CAMERA}, requestcamera);
                    return;
                }
                try {
                    cameraSource.start(cameraview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                cameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcodes=detections.getDetectedItems();
                if(qrcodes.size()!=0)
                {
                    result.post(new Runnable() {
                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            result.setText(qrcodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });
            if (android.os.Build.VERSION.SDK_INT > 9)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                uri  = new MongoClientURI("mongodb://kbak:123456@ds155634.mlab.com:55634/sample");
                 mongoClient = new MongoClient(uri);
                // use test as a datbase,use your database here
                 db = mongoClient.getDB("sample");
            }
        });
            thread.start();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.logout_id:
//                SharedPreferences settings = getSharedPreferences("user_mode", Context.MODE_PRIVATE);
//                SharedPreferences.Editor editor = settings.edit();
//                editor.remove("username");
//                editor.remove("password");
//                editor.apply();
//                editor.commit();
                Toast.makeText(getApplicationContext(),"Successfully logged out",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                break;
            default: return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
        case requestcamera: {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    return;
                }
                try {
                    cameraSource.start(cameraview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        break;
    }
}
    public void send(View view) {
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                // fetch the collection object ,car is used here,use your own
                DBCollection coll = db.getCollection("FAR");
                DBObject query = new BasicDBObject("Barcode", result.getText().toString());
                DBObject doc = coll.findOne(query);
                String Dept = doc.get("Dept").toString();
                String Loc = doc.get("Loc").toString();
                String System_type = doc.get("System_type").toString();
                String System_make = doc.get("System_make").toString();
                String System_modelnum = doc.get("System_modelnum").toString();
                String System_serialnum = doc.get("System_serialnum").toString();
                String Monitor = doc.get("Monitor").toString();
                final String Config = doc.get("Config").toString();
                String Purchasenum = doc.get("Purchasenum").toString();
                String Purchasedate = doc.get("Purchasedate").toString();
                String Invoicenum = doc.get("Invoicenum").toString();
                String Invoicedate = doc.get("Invoicedate").toString();
                String Price = doc.get("Price").toString();
                BasicDBObject document = new BasicDBObject();
                document.put("Dept", Dept);
                document.put("Loc", Loc);
                document.put("System_type", System_type);
                document.put("System_make", System_make);
                document.put("System_modelnum", System_modelnum);
                document.put("System_serialnum", System_serialnum);
                document.put("Monitor", Monitor);
                document.put("Config", Config);
                document.put("Purchasenum", Purchasenum);
                document.put("Purchasedate", Purchasedate);
                document.put("Invoicenum", Invoicenum);
                document.put("Invoicedate", Invoicedate);
                document.put("Price", Price);
                MongoDatabase db2 = mongoClient.getDatabase(uri.getDatabase());
                MongoCollection<BasicDBObject> collection = db2.getCollection("assets", BasicDBObject.class);
                collection.insertOne(document);
                String email ="kishorebaktha@gmail.com";
                String subject = "New Data";
                String message ="New Data has been added"+"\n";
                sendemail(email,subject,message);
            }
        });
        thread.start();
        Toast.makeText(getApplicationContext(), "Inserted Successfully", Toast.LENGTH_SHORT).show();
                        }

    private void showDialog(MainActivity mainActivity, String dept, String loc, String system_serialnum, String purchasedate, String price)
    {
        final Dialog dialog = new Dialog(mainActivity);
        dialog.setContentView(R.layout.dialog);
        dialog.setTitle("Details");
        EditText department=(EditText)dialog.findViewById(R.id.dept);
        EditText location=(EditText)dialog.findViewById(R.id.loc);
        EditText serialnum=(EditText)dialog.findViewById(R.id.serialnum);
        EditText purchasedatetext=(EditText)dialog.findViewById(R.id.purchasedate);
        EditText pricetext=(EditText)dialog.findViewById(R.id.price);
        department.setText(dept);
        location.setText(loc);
        serialnum.setText(system_serialnum);
        purchasedatetext.setText(purchasedate);
        pricetext.setText(price);
        department.setFocusable(false);
        location.setFocusable(false);
        serialnum.setFocusable(false);
        purchasedatetext.setFocusable(false);
        pricetext.setFocusable(false);
        department.setGravity(Gravity.CENTER_HORIZONTAL);
        location.setGravity(Gravity.CENTER_HORIZONTAL);
        serialnum.setGravity(Gravity.CENTER_HORIZONTAL);
        purchasedatetext.setGravity(Gravity.CENTER_HORIZONTAL);
        pricetext.setGravity(Gravity.CENTER_HORIZONTAL);
        int width = (int) (mainActivity.getResources().getDisplayMetrics().widthPixels * 0.95);
        // set height for dialog
        int height = (int) (mainActivity.getResources().getDisplayMetrics().heightPixels * 0.85);
        dialog.getWindow().setLayout(width, height);
        dialog.show();
       // progressDialog.dismiss();
    }


    public void complaint(View view) {
        progressDialog = ProgressDialog.show(this,"Sending request","Please wait...",false,false);
         dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.complaint);
        dialog.setTitle("Complaint");
         empname=(EditText)dialog.findViewById(R.id.empname);
         empcode=(EditText)dialog.findViewById(R.id.empcode);
        dept=(EditText)dialog.findViewById(R.id.dept);
        make=(EditText)dialog.findViewById(R.id.loc);
        spinner=(Spinner)dialog.findViewById(R.id.spinner1);
        spinner2=(Spinner)dialog.findViewById(R.id.spinner2);
        final EditText problemdesc=(EditText)dialog.findViewById(R.id.probdesc);
        Button submit=(Button)dialog.findViewById(R.id.submit);
        List<String> categories = new ArrayList<String>();
        categories.add("Please select an option");
        categories.add("Software");
        categories.add("Hardware");
        categories.add("Network");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                DBCollection coll = db.getCollection("FAR");
                DBObject query = new BasicDBObject("Barcode", result.getText().toString());
                DBObject doc = coll.findOne(query);
                 Dept=doc.get("Dept").toString();
                 System_make=doc.get("System_make").toString();
                mHandler2.postDelayed(mUpdateResults2,0);

            }
        });
        thread.start();
        spinner2.setOnItemSelectedListener(this);
        final long min = 000000000001l;
        final long max = 999999999999l;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                final long random = getRandom(min,max);
                String ticketnum="TT"+random;
                BasicDBObject document = new BasicDBObject();
                document.put("Ticket_Number",ticketnum);
                document.put("User",empname.getText().toString());
                document.put("Department",dept.getText().toString());
                document.put("Make",make.getText().toString());
                document.put("Category",category);
                document.put("Type",type);
                document.put("Method_Of_Contact","Online");
                document.put("Problem_Description",problemdesc.getText().toString());
                document.put("Assigned_Date_Time","");
                document.put("Severity_Level","");
                document.put("Engineer_Assigned","");
                document.put("Status","");
                document.put("Resolution","");
                document.put("Resolved_Date_Time","");
                document.put("Remarks","");
                        final MongoClientURI uri  = new MongoClientURI("mongodb://kbak:123456@ds155634.mlab.com:55634/sample");
                        final MongoClient mongoClient = new MongoClient(uri);
                        MongoDatabase db2 = mongoClient.getDatabase(uri.getDatabase());
                        MongoCollection<BasicDBObject> collection = db2.getCollection("helpdesks", BasicDBObject.class);
                        collection.insertOne(document);
                        String email ="kishorebaktha@gmail.com";
                        String subject = "New complaint";
                        String message ="Employee name:"+empname.getText().toString()+"\n"+
                                "Employee code:"+empcode.getText().toString()+"\n"+
                                "Department:"+"IT"+"\n"+
                                "Designation:"+desig+"\n"+
                                "Phone Ext:"+phoneext+"\n"+
                                "Category:"+category+"\n"+
                                "Type:"+type+"\n"+
                                "Description:"+problemdesc.getText().toString()+"\n"+
                        "Email:"+emailuser+"\n";
                        sendemail(email,subject,message);
                        String email2 ="kishoreb14053@it.ssn.edu.in";
                        String subject2 = "Your complaint";
                        String message2 ="Employee name:"+empname.getText().toString()+"\n"+
                                "Employee code:"+empcode.getText().toString()+"\n"+
                                "Department:"+"IT"+"\n"+
                                "Designation:"+desig+"\n"+
                                "Phone Ext:"+phoneext+"\n"+
                                "Category:"+category+"\n"+
                                "Type:"+type+"\n"+
                                "Description:"+problemdesc.getText().toString()+"\n"+
                                "Email:"+emailuser+"\n";
                        sendemail(email2,subject2,message2);
                    }
                });
                thread.start();
                Toast.makeText(view.getContext(),"Inserted successfully",Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

    private long getRandom(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min,max);

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            AlertDialog.Builder a_builder = new AlertDialog.Builder(MainActivity.this);
            a_builder.setMessage("do you want to exit?")
                    .setCancelable(false)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ExitActivity.exitApplication(MainActivity.this);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                }
            });
            AlertDialog ab = a_builder.create();
            ab.setTitle("Alert");
            ab.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
         if(parent.getId()==R.id.spinner1)
         category = parent.getItemAtPosition(position).toString();
        if(category.equals("Software")&&parent.getId()==R.id.spinner1)
        {
            Toast.makeText(getApplicationContext(),"Software",Toast.LENGTH_SHORT).show();
            List<String> categories = new ArrayList<String>();
            categories.add("Please select an option");
            categories.add("Email");
            categories.add("E-learning");
            categories.add("Operating System");
            categories.add("Virus");
            categories.add("Installation");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(dataAdapter);
        }

         if(category.equals("Hardware")&&parent.getId()==R.id.spinner1)
        {
            Toast.makeText(getApplicationContext(), "Hardware", Toast.LENGTH_SHORT).show();
            List<String> categories = new ArrayList<String>();
            categories.add("Please select an option");
            categories.add("Desktop");
            categories.add("Laptop");
            categories.add("Keyboard");
            categories.add("Mouse");
            categories.add("Monitor");
            categories.add("Printer");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(dataAdapter);
        }
        if(category.equals("Network")&&parent.getId()==R.id.spinner1)
        {
            Toast.makeText(getApplicationContext(), "Network", Toast.LENGTH_SHORT).show();
            List<String> categories = new ArrayList<String>();
            categories.add("Please select an option");
            categories.add("Internet");
            categories.add("Intranet");
            categories.add("Wired");
            categories.add("Wireless");
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(dataAdapter);
        }
        if(parent.getId()==R.id.spinner2) {
            type = parent.getItemAtPosition(position).toString();
            Toast.makeText(view.getContext(), type, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void locate(View view) {
        progressDialog = ProgressDialog.show(this,"Sending request","Please wait...",false,false);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                DBCollection coll = db.getCollection("FAR");
                DBObject query = new BasicDBObject("Barcode", result.getText().toString());
                DBObject doc = coll.findOne(query);
                String asset=doc.get("Asset").toString();
                DBObject query2 = new BasicDBObject("Asset", asset);
                DBCursor cursor = coll.find(query2);
                while(cursor.hasNext()) {
                    DBObject doc2 = cursor.next();
                    latarray[index]=doc2.get("Latitude").toString();
                    longarray[index]=doc2.get("Longitude").toString();
                    index++;
                }
               Latitude=doc.get("Latitude").toString();
                Longitude=doc.get("Longitude").toString();
                mHandler3.postDelayed(mUpdateResults3,0);
            }
        });
        thread.start();
    }
    public void profile(View view) {
        progressDialog = ProgressDialog.show(this,"Sending request","Please wait...",false,false);
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.profile);
        dialog.setTitle("Profile");
        empname=(EditText)dialog.findViewById(R.id.empname2);
        empcode=(EditText)dialog.findViewById(R.id.empcode2);
        final EditText desig2=(EditText)dialog.findViewById(R.id.desig);
         final EditText phoneext2=(EditText)dialog.findViewById(R.id.phoneext);
        final EditText email2=(EditText)dialog.findViewById(R.id.email);
        Button update=(Button)dialog.findViewById(R.id.update);
        Button cancel=(Button)dialog.findViewById(R.id.cancel);
        Intent intent=getIntent();
        emailuser=intent.getStringExtra("username");
        String empcode2=intent.getStringExtra("empcode");
        desig=intent.getStringExtra("desig");
        phoneext=intent.getStringExtra("phoneext");
        email=intent.getStringExtra("email");
        empname.setText(emailuser);
        empcode.setText(empcode2);
        desig2.setText(desig);
        phoneext2.setText(phoneext);
        email2.setText(email);
        dialog.show();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DBCollection coll = db.getCollection("users");
                        BasicDBObject query = new BasicDBObject().append("username", emailuser);
                        DBObject doc = coll.findOne(query);
                        String em=doc.get("empcode").toString();
                        BasicDBObject document = new BasicDBObject();
                        document.put("$set", new BasicDBObject("empcode",empcode.getText().toString()).append("desig", desig2.getText().toString()).append("phoneext", phoneext2.getText().toString()).append("email", email2.getText().toString()));
                       coll.update(query, document);
                        mHandler4.postDelayed(mUpdateResults4,0);
                    }
                });
                t.start();
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.dismiss();
                dialog.dismiss();
            }
        });
    }
    public void sendemail(String email,String subject,String message)
    {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    //Authenticating the password
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(com.example.kishorebaktha.mongodb.Config.EMAIL, com.example.kishorebaktha.mongodb.Config.PASSWORD);
                    }
                });

//Creating MimeMessage object
        try {
            MimeMessage mm = new MimeMessage(session);
            mm.setFrom(new InternetAddress(com.example.kishorebaktha.mongodb.Config.EMAIL));
            // Adding receiver
            mm.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            // Adding subject
            mm.setSubject(subject);
            // Adding message
            mm.setText(message);
            Transport.send(mm);
//            Toast.makeText(getApplicationContext(),"email sent",Toast.LENGTH_SHORT).show();
        }
        catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

