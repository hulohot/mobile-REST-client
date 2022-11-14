package com.csce4623.ahnelson.restclientexample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class PostView extends Activity implements Callback<List<Comment>>{

    ArrayList<Comment> myCommentsList;
    CommentAdapter myCommentsAdapter;
    ListView lvComments;
    User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);
        TextView tvPostTitle = (TextView)findViewById(R.id.tvPostTitle);
        TextView tvPostBody = (TextView)findViewById(R.id.tvPostBody);
        TextView tvUser = (TextView) findViewById(R.id.tvPostUser);
        tvPostBody.setText(this.getIntent().getStringExtra("postBody"));
        tvPostTitle.setText(this.getIntent().getStringExtra("postTitle"));
        user = (User) this.getIntent().getSerializableExtra("user");
        tvUser.setText(user.getUsername());
        findViewById(R.id.btnMakeComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeNewComment();
            }
        });
        tvUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                intent.putExtra("user", user);

                startActivity(intent);
            }
        });
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
        lvComments = (ListView)findViewById(R.id.lvComments);
        CommentAPI commentAPI = retrofit.create(CommentAPI.class);
        Call<List<Comment>> call = commentAPI.loadCommentByPostId(getIntent().getIntExtra("postId",0));
        call.enqueue(this);
    }

    public void makeNewComment(){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        CommentAPI commentAPI = retrofit.create(CommentAPI.class);
        Call<Comment> call = commentAPI.addCommentToPost(1,"Ethan","embrugge@uark.edu","This technically works");
        call.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                Comment myComment = response.body();
                myCommentsList.add(myComment);
                myCommentsAdapter.notifyDataSetChanged();
                Log.d("PostView","Post Created Successfully at id: " + myComment.getId());
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                Log.d("PostView","Post Not Created");
            }
        });
    }

    @Override
    public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
        if(response.isSuccessful()) {
            myCommentsList = new ArrayList<Comment>(response.body());
            myCommentsAdapter = new PostView.CommentAdapter(this,myCommentsList);
            lvComments.setAdapter(myCommentsAdapter);
            for (Comment comment:myCommentsList) {
                Log.d("MainActivity","ID: " + comment.getId());
            }
        } else {
            System.out.println(response.errorBody());
        }
    }

    @Override
    public void onFailure(Call<List<Comment>> call, Throwable t) {
        t.printStackTrace();
    }

    protected class CommentAdapter extends ArrayAdapter<Comment> {
        public CommentAdapter(Context context, ArrayList<Comment> posts) {
            super(context, 0, posts);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Comment comment = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_layout, parent, false);
            }
            // Lookup view for data population
            TextView tvCommentEmail = (TextView) convertView.findViewById(R.id.tvCommentEmail);
            TextView tvCommentBody = (TextView) convertView.findViewById(R.id.tvCommentBody);
            TextView tvCommentTitle = (TextView) convertView.findViewById(R.id.tvCommentTitle);
            // Populate the data into the template view using the data object
            tvCommentEmail.setText(comment.getEmail());
            tvCommentBody.setText(comment.getBody());
            tvCommentTitle.setText(comment.getName());
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
