//package com.example.kishorebaktha.mongodb;
//
//import android.content.Context;
//import android.os.AsyncTask;
//import android.os.StrictMode;
//import android.widget.Toast;
//
//import com.mongodb.BasicDBObject;
//import com.mongodb.MongoClient;
//import com.mongodb.MongoClientURI;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoCursor;
//import com.mongodb.client.MongoDatabase;
//
///**
// * Created by KISHORE BAKTHA on 1/26/2018.
// */
//public class BackgroundWriter extends AsyncTask<Void,Void,Void> {
//    Context context;
//    String data="",dataParsed="",singleParsed="";
//
//    public BackgroundWriter(Context context) {
//        this.context = context;
//    }
//
//    @Override
//    protected Void doInBackground(Void... params) {
//        try {
//            MongoClientURI uri  = new MongoClientURI("mongodb://kbak:123456@ds155634.mlab.com:55634/sample");
//            MongoClient client = new MongoClient(uri);
//            if (android.os.Build.VERSION.SDK_INT > 9)
//            {
//                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//                StrictMode.setThreadPolicy(policy);
//            }
//            MongoDatabase db = client.getDatabase(uri.getDatabase());
//            MongoCollection<BasicDBObject> collection = db.getCollection("barcode", BasicDBObject.class);
//
//            long len = collection.count();
//
//            BasicDBObject document = new BasicDBObject();
//            document.put("name", "mkyong");
//            document.put("age", 30);
//            collection.insertOne(document);
//            Toast.makeText(context,"inserted",Toast.LENGTH_SHORT).show();
//            MongoCursor iterator = collection.find().iterator();
//            while (iterator.hasNext()) {
//                System.out.println(iterator.next());
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }
//
//    @Override
//    protected void onPostExecute(Void aVoid) {
//        super.onPostExecute(aVoid);
//    }
//}
