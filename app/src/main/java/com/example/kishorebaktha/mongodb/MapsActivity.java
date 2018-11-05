package com.example.kishorebaktha.mongodb;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    Double Latitude,Longitude;
    Marker[] markers;
    String [] latarray,longarray;
    LatLng[] locations;
    private ProgressDialog progressDialog;
  int count=0,index;
    MongoClientURI uri;
    MongoClient mongoClient;
    DB db;
  int numoftimes=0;
    String Dept,Loc,System_serialnum,Purchasedate,Price,System_make;
    final Handler mHandler = new Handler();
    // Create runnable for posting
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
           // progressDialog.dismiss();
            progressDialog.dismiss();
            showDialog(MapsActivity.this,Dept,Loc,System_serialnum,Purchasedate,Price);
        }
    };

    private void showDialog(MapsActivity mainActivity, String dept, String loc, String system_serialnum, String purchasedate, String price)
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                uri = new MongoClientURI("mongodb://kbak:123456@ds155634.mlab.com:55634/sample");
                mongoClient = new MongoClient(uri);
                // use test as a datbase,use your database here
                db = mongoClient.getDB("sample");
            }
        });
        thread.start();
        Intent intent=getIntent();
        Bundle b=this.getIntent().getExtras();
         latarray=b.getStringArray("Latarray");
         longarray=b.getStringArray("Longarray");
        Toast.makeText(getApplicationContext(),intent.getStringExtra("Latitude")+intent.getStringExtra("Longitude"),Toast.LENGTH_LONG).show();
        Latitude=Double.parseDouble(intent.getStringExtra("Latitude"));
        Longitude=Double.parseDouble(intent.getStringExtra("Longitude"));
         index=Integer.parseInt(intent.getStringExtra("index"));
        markers=new Marker[index];
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(Latitude,Longitude);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Your Location")).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 18.0f ) );
        for(int i=0;i<index;i++)
        {
            if(!latarray[i].equals(String.valueOf(Latitude)))
            {
                LatLng location=new LatLng(Double.parseDouble(latarray[i]),Double.parseDouble(longarray[i]));
                markers[count]=mMap.addMarker(new MarkerOptions().position(location));
                markers[count].setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                count++;
            }
        }
        Toast.makeText(getApplicationContext(),"Click the marker to view details",Toast.LENGTH_SHORT).show();
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        progressDialog = ProgressDialog.show(this,"Sending request","Please wait...",false,false);
        LatLng position=marker.getPosition();
        Latitude=position.latitude;
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                //code to do the HTTP request
                // fetch the collection object ,car is used here,use your own
                DBCollection coll = db.getCollection("FAR");
                DBObject query = new BasicDBObject("Latitude",String.valueOf(Latitude));
                DBObject doc = coll.findOne(query);
                Dept=doc.get("Dept").toString();
                Loc=doc.get("Loc").toString();
                System_serialnum=doc.get("System_serialnum").toString();
                Purchasedate=doc.get("Purchasedate").toString();
                Price=doc.get("Price").toString();
                //dialog.dismiss();
                mHandler.postDelayed(mUpdateResults,0);
                //      showDialog(MainActivity.this,Dept,Loc,System_serialnum,Purchasedate,Price);
            }
        });
        thread.start();
        return false;
    }
}
