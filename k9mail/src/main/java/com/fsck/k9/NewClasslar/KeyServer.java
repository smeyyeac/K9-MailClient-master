package com.fsck.k9.NewClasslar;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.nio.charset.StandardCharsets.UTF_8;


public class KeyServer {

    private static final String USER_AGENT                  = "Eposta";

    private static final String KEYSERVER_LOOKUP_ADDRESS            = "https://keyserver.ubuntu.com/pks/lookup?search=";
    private static final String KEYSERVER_LOOKUP_PUBLIC_KEY_ADDRESS            = "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x";

    private static String publicKey;

    private final static String URL_POST_KEY_ENDPOINT = "https://keyserver.ubuntu.com/pks/add";
    private static final char PARAMETER_DELIMITER = '&';
    private static final char PARAMETER_EQUALS_CHAR = '=';

    //Servera Yükleme

    public static  void publishPublicKey (String publicKey) {
        final String pubKey = publicKey;
        new Thread () {

            public void run() {

                try {
                    Log.w("Getir pub", "yayınla");
                    HashMap<String,String> hmParams = new HashMap<String,String>();
                    hmParams.put("keytext",pubKey);
                    String queryString = createQueryStringForParameters(hmParams);

                    URL url = new URL(URL_POST_KEY_ENDPOINT);
                    HttpURLConnection client = null;
                    client = (HttpURLConnection) url.openConnection();
                    client.setRequestMethod("POST");
                    client.setFixedLengthStreamingMode(queryString.getBytes().length);
                    client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    client.setDoOutput(true);
                    client.setDoInput(true);
                    client.setReadTimeout(20000);
                    client.setConnectTimeout(30000);

                    PrintWriter out = new PrintWriter(client.getOutputStream());
                    out.print(queryString);
                    out.close();


                    // handle issues
                    int statusCode = client.getResponseCode();
                    if (statusCode != HttpURLConnection.HTTP_OK) {
                        // throw some exception
                        Log.w("PGP","key did not upload: " + statusCode);
                    }


                    client.disconnect();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
            }
        }.start();
    }

    public static String createQueryStringForParameters(Map<String, String> parameters) {
        StringBuilder parametersAsQueryString = new StringBuilder();
        if (parameters != null) {
            boolean firstParameter = true;

            for (String parameterName : parameters.keySet()) {
                if (!firstParameter) {
                    parametersAsQueryString.append(PARAMETER_DELIMITER);
                }

                parametersAsQueryString.append(parameterName)
                        .append(PARAMETER_EQUALS_CHAR)
                        .append(URLEncoder.encode(
                                parameters.get(parameterName)));

                firstParameter = false;
            }
        }
        return parametersAsQueryString.toString();
    }


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
