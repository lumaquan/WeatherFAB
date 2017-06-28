package luismelendezcom.weatherfab;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Weather> weatherList = new ArrayList<>();
    private WeatherArrayAdapter weatherArrayAdapter;
    private ListView weatherListView;
    private RecyclerView weatherRecyclerView;
    private WeatherRecyclerviewAdapter weatherRecyclerviewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prepareList2();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText locationEditText = (EditText) findViewById(R.id.locationEditText);

                Log.d("TATA",locationEditText.getText().toString());
                URL url = createURL(locationEditText.getText().toString());
                Log.d("TATA",url.toString());

                if (url != null) {
                    dismissKeyboard(locationEditText);
                   // GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                   // getLocalWeatherTask.execute(url);
                   Controller controller = new Controller();
                    controller.execute(MainActivity.this,url);
                }
                else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                }

            }
        });



    }

   private void prepareList2(){
       weatherRecyclerView =(RecyclerView)findViewById(R.id.weatherRecyclerview);
       weatherRecyclerviewAdapter = new WeatherRecyclerviewAdapter(this,weatherList);
       Log.d("TATA","Count:  "+ weatherRecyclerviewAdapter.getItemCount());

       RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);

       weatherRecyclerView.setLayoutManager(mLayoutManager);
      // weatherRecyclerView.setItemAnimator(new DefaultItemAnimator());
       weatherRecyclerView.setAdapter(weatherRecyclerviewAdapter);

   }

    private void prepareList() {

        weatherListView = (ListView) findViewById(R.id.weatherListView);
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);



    }

    private void dismissKeyboard(View view) {

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);

    }

    private URL createURL(String city) {


        String apiKey = getString(R.string.api_key);
        String baseUrl =  getString(R.string.web_service_url);
        try {
            String urlString  =baseUrl + URLEncoder.encode(city,"UTF-8")+ "&units=imperial&cnt=16&APPID="+apiKey;
           return new URL(urlString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

          return null;
    }


    private class GetWeatherTask extends AsyncTask<URL,Void,JSONObject>{
        @Override
        protected JSONObject doInBackground(URL... urls) {
            Log.d("TATA",urls[0].toString());

            HttpURLConnection connection = null;

            try {
                connection =(HttpURLConnection)urls[0].openConnection();
                int responseCode = connection.getResponseCode();
                Log.d("TATA",""+connection.getResponseCode());

                if(responseCode==HttpURLConnection.HTTP_OK){
                    StringBuilder builder  = new StringBuilder();

                    try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){

                        String line;
                        while ((line = reader.readLine()) != null) {
                             builder.append(line);
                             }
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new JSONObject(builder.toString());
                }else {
                     Snackbar.make(findViewById(R.id.coordinatorLayout),R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }

            }  catch (Exception e) {
                 Snackbar.make(findViewById(R.id.coordinatorLayout),R.string.connect_error, Snackbar.LENGTH_LONG).show();
                 e.printStackTrace();
                 }
            finally {
                if(connection!=null) connection.disconnect();}
            return null;
        }


        @Override
        protected void onPostExecute(JSONObject weather) {
            convertJSONtoArrayList(weather);
           // weatherArrayAdapter.notifyDataSetChanged();
         //   weatherListView.smoothScrollToPosition(0);
            weatherRecyclerviewAdapter.swapWeatherList(weatherList);

        }
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateList(JsonEvent event){
    convertJSONtoArrayList(event.getForecast());
     weatherRecyclerviewAdapter.swapWeatherList(weatherList);
    }



    private void convertJSONtoArrayList(JSONObject forecast) {
        weatherList.clear();
        try {
            JSONArray list = forecast.getJSONArray("list");

            for (int i = 0; i < list.length(); ++i) {
                JSONObject day = list.getJSONObject(i);
                JSONObject temperatures = day.getJSONObject("temp");
                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);
                weatherList.add(new Weather(
                        day.getLong("dt"),
                        temperatures.getDouble("min"),
                        temperatures.getDouble("max"),
                        day.getDouble("humidity"),
                        weather.getString("description"),
                        weather.getString("icon")));

            }
        }
            catch (JSONException e) {
                 e.printStackTrace();
                 }
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
