package com.csce4623.ahnelson.restclientexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserActivity extends AppCompatActivity implements OnMapReadyCallback, Callback<List<Post>> {

    private GoogleMap mMap;
    private User user;
    private ListView lvPosts;
    ArrayList<Post> myPostsList;
    PostAdapter myPostsAdapter;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        TextView tvNamePlaceholder = (TextView)findViewById(R.id.tvNamePlaceholder);
        TextView tvUserNamePlaceholder = (TextView)findViewById(R.id.tvUserNamePlaceholder);
        TextView tvEmailPlaceholder = (TextView)findViewById(R.id.tvEmailPlaceholder);
        TextView tvPhonePlaceholder = (TextView)findViewById(R.id.tvPhonePlaceholder);
        TextView tvWebsitePlaceholder = (TextView)findViewById(R.id.tvWebsitePlaceholder);
        lvPosts = (ListView)findViewById(R.id.lvUserPosts);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        ImageView transparent = (ImageView)findViewById(R.id.imagetrans);
        final ScrollView scroll = (ScrollView) findViewById(R.id.scroll);
        transparent.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            Log.d("transparent onTouch", String.valueOf(action));
            switch (action) {
                case MotionEvent.ACTION_DOWN:

                case MotionEvent.ACTION_MOVE:
                    // Disallow ScrollView to intercept touch events.
                    scroll.requestDisallowInterceptTouchEvent(true);
                    // Disable touch on transparent view
                    return false;

                case MotionEvent.ACTION_UP:
                    // Allow ScrollView to intercept touch events.
                    scroll.requestDisallowInterceptTouchEvent(false);
                    return true;

                default:
                    return true;
            }
        });



        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mapFragment != null) {
            transaction.add(R.id.mapsFragmentFrame, mapFragment);
            transaction.commit();
        }

        user = (User) this.getIntent().getSerializableExtra("user");

        tvNamePlaceholder.setText(user.getName());
        tvUserNamePlaceholder.setText(user.getUsername());
        tvEmailPlaceholder.setText(user.getEmail());
        tvPhonePlaceholder.setText(user.getPhone());
        tvWebsitePlaceholder.setText(user.getWebsite());

        Log.d("UserActivity", "Calling mapView getMapAsync");
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("UserActivity", "onMapReady Called");
        if (googleMap != null) {
            mMap = googleMap;
            Address address = user.getAddress();
            Geo location = address.getGeo();
            Log.d("UserActivity", "onMapReady");
            LatLng userLocation = new LatLng(Double.parseDouble(location.getLat()), Double.parseDouble(location.getLng()));
            mMap.addMarker(new MarkerOptions().position(userLocation).title(user.getName() + "'s location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
        }

        startQuery();
    }


    static final String BASE_URL = "https://jsonplaceholder.typicode.com/";

    public void startQuery() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        PostAPI postAPI = retrofit.create(PostAPI.class);
        Call<List<Post>> call = postAPI.loadPostsByUserId(user.getId());
        call.enqueue(this);
    }

    @Override
    public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
        if(response.isSuccessful()) {
            myPostsList = new ArrayList<Post>(response.body());
            myPostsAdapter = new UserActivity.PostAdapter(this, myPostsList);
            lvPosts.setAdapter(myPostsAdapter);

            for (Post post:myPostsList) {
                Log.d("MainActivity","ID: " + post.getId());
            }
        } else {
            System.out.println(response.errorBody());
        }
    }

    @Override
    public void onFailure(Call<List<Post>> call, Throwable t) {
        t.printStackTrace();
    }

    protected class PostAdapter extends ArrayAdapter<Post> {

        public PostAdapter(Context context, ArrayList<Post> posts) {
            super(context, 0, posts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Post post = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_posts_layout, parent, false);
            }
            // Lookup view for data population
            TextView tvId = (TextView) convertView.findViewById(R.id.tvUserPostsId);
            TextView tvTitle = (TextView) convertView.findViewById(R.id.tvUserPostsTitle);
            // Populate the data into the template view using the data object
            tvTitle.setText(post.getTitle());
            tvId.setText(Integer.toString(post.getId()));
            // Return the completed view to render on screen
            return convertView;
        }
    }
}