package com.fsck.k9.NewClasslar;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class KeyServer {

    private static final String USER_AGENT                  = "Mozilla/5.0";

    private static final String KEYSERVER_LOOKUP_ADDRESS            = "https://keyserver.ubuntu.com/pks/lookup?search=";
    private static final String KEYSERVER_LOOKUP_PUBLIC_KEY_ADDRESS            = "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x";

    private static String publicKey;

    public String getKeyServerPublicKey(String search) {
        try {
            Log.e( "PublicKey: ", new keyServerLookupPublicKey().execute(search).get());
            return new keyServerLookupPublicKey().execute(search).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    private class keyServerLookupPublicKey extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                final  String search = strings[0].replaceAll(" ", "%20");

                Log.e("Where" ,    "LookupPublicKey ");

                //construct the address
                String address = KEYSERVER_LOOKUP_PUBLIC_KEY_ADDRESS + search;
                URL url = new URL(address);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");

                //add request header
                conn.setRequestProperty("User-Agent", USER_AGENT);

                int responseCode = conn.getResponseCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);
                Log.e("Nerede yeni" , String.valueOf(responseCode));

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                System.out.println(response.toString());
                //System.out.println(Pattern.compile(response.toString()));
                publicKey = publicKeySplitAnswer(String.valueOf(response));
                //split(String.valueOf(response));


            } catch (MalformedURLException e) {
                //we should never encounter this
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("LookupPublicKey", publicKey);
            return publicKey;
        }
        @Override
        protected void onPostExecute(String result) {
            //if you had a ui element, you could display the title
            Log.e("PUBLIC", "onPostExecute: "+result);
        }
    }

    private static String publicKeySplitAnswer(String answerFromWebsite) {
        //String startOfBlock = "keyserver.ubuntu.com";
        //String endOfBlock = "-----END PGP PUBLIC KEY BLOCK-----";
        String startOfBlock = "<pre>";
        String endOfBlock = "</pre>";
        int startIndex = answerFromWebsite.indexOf(startOfBlock) + startOfBlock.length();
        int endIndex = answerFromWebsite.indexOf(endOfBlock);
        String result = answerFromWebsite.substring(startIndex,endIndex);
        Log.e("PublicKey", result);
        return result;
    }

    public ArrayList<String> keyServerLookup(String search) {
        try {
            Log.e("Server", "LookupResult: "+ new keyServerLookupSplit().execute(search).get());
            return new keyServerLookupSplit().execute(search).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    private class keyServerLookupSplit extends AsyncTask<String, String, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            String title ="";
            Document doc = null;
            ArrayList<String> result = new ArrayList<String>();
            try {
                Log.e("LookupSplit String", strings[0]);
                doc = Jsoup.connect(KEYSERVER_LOOKUP_ADDRESS + strings[0]+ "&fingerprint=on&op=index").get();

                title = doc.title();

                Elements links = doc.select("pre");
                title = links.toString();
                for (Element link : links) {

                    result.add(link.text());
                    //System.out.println("\nText:" + link.text());
                    //Log.e("Dizi", String.valueOf(result));

                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("istek", "doInBackground: "+e);
            }catch (Exception e){
                Log.d("istek", "doInBackground: "+e);
            }
            //Log.e("Dizi", String.valueOf(result));
            return result;
        }
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            //if you had a ui element, you could display the title
            Log.d("İstek", "onPostExecute: "+result);
        }
    }

}
