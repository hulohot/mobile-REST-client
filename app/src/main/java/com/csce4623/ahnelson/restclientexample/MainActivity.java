package com.csce4623.ahnelson.restclientexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity {

    ArrayList<Post> myPostList;
    List<User> users = new ArrayList<User>();

    ListView lvPostVList;
    PostAdapter myPostAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvPostVList = (ListView)findViewById(R.id.lvPostList);
        lvPostVList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClicked(parent, view, position,id);
            }
        });
        startQuery();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

    }

    void itemClicked(AdapterView<?> parent, View view, int position, long id){

        Intent myIntent = new Intent(this,PostView.class);
        myIntent.putExtra("postId",myPostList.get(position).getId());
        myIntent.putExtra("postTitle",myPostList.get(position).getTitle());
        myIntent.putExtra("postBody",myPostList.get(position).getBody());
        myIntent.putExtra("userId",myPostList.get(position).getUserId());
        myIntent.putExtra("user", users.get(position));

        startActivity(myIntent);
    }



    static final String BASE_URL = "https://jsonplaceholder.typicode.com/";

    public void startQuery() {

        Debug.startMethodTracing("test");

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        List<Integer> userIds = new ArrayList<Integer>();

        PostAPI postAPI = retrofit.create(PostAPI.class);
        UserAPI userAPI = retrofit.create(UserAPI.class);

        Context greaterContext = this;
        Call<List<Post>> call = postAPI.loadPosts();
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if(response.isSuccessful()) {
                    myPostList = new ArrayList<Post>(response.body());

                    for(int i = 0; i < myPostList.size(); i++) {
                        userIds.add(myPostList.get(i).getUserId());
                    }

                    // Do call for all Users
                    for(int i = 0; i < userIds.size(); i++) {
                        Call<User> userCall = userAPI.loadUserByUserId(userIds.get(i));
                        userCall.enqueue(new Callback<User>() {
                           @Override
                           public void onResponse(Call<User> call, Response<User> response) {
                               if(response.isSuccessful()) {
                                   User user = response.body();
                                   users.add(user);
                               } else {
                                   users.add(null);
                                   System.out.println(response.errorBody());
                               }
                           }
                           @Override
                           public void onFailure(Call<User> call, Throwable t) {
                               t.printStackTrace();
                           }
                       });
                    }

                    myPostAdapter = new PostAdapter(greaterContext,myPostList, users);
                    lvPostVList.setAdapter(myPostAdapter);

                    for (Post post:myPostList) {
                        Log.d("MainActivity","ID: " + post.getId());
                    }
                } else {
                    System.out.println(response.errorBody());
                }
                Debug.stopMethodTracing();
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    protected class PostAdapter extends ArrayAdapter<Post> {
        List<User> users;

        public PostAdapter(Context context, ArrayList<Post> posts, List<User> _users) {
            super(context, 0, posts);
            users = _users;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Post post = getItem(position);
            User user = users.get(position);

            Log.d("PostAdapter", "UserName: " + user.getUsername());
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.post_layout, parent, false);
            }
            // Lookup view for data population
            TextView tvId = (TextView) convertView.findViewById(R.id.tvId);
            TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            TextView tvUserName = (TextView) convertView.findViewById(R.id.tvNamePlaceholder);
            // Populate the data into the template view using the data object
            tvTitle.setText(post.getTitle());
            tvUserName.setText(user.getUsername());
            tvId.setText(Integer.toString(post.getId()));
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
