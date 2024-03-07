package com.example.androidfacebook.pid;

import static com.example.androidfacebook.login.Login.ServerIP;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.androidfacebook.R;
import com.example.androidfacebook.adapters.PostsListAdapter;
import com.example.androidfacebook.addspages.AddPost;
import com.example.androidfacebook.api.AppDB;
import com.example.androidfacebook.api.PostAPI;
import com.example.androidfacebook.api.PostDao;
import com.example.androidfacebook.api.UserDao;
import com.example.androidfacebook.entities.ClientUser;
import com.example.androidfacebook.entities.DataHolder;
import com.example.androidfacebook.entities.Post;
import com.example.androidfacebook.login.Login;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
    Pid class is the main class for the user to see posts and add new posts
 */
public class Pid extends AppCompatActivity {
    private AppDB appDB;
    private UserDao userDao;
    private ClientUser user;
    private List<Post> postList;
    private PostDao postDao;

    @SuppressLint({"MissingInflatedId", "WrongThread"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pid);
        // Get the user that is in the pid now

        String userId = DataHolder.getInstance().getUserLoggedInID();
//            user= DataHolder.getInstance().getUserLoggedIn();
        String token = DataHolder.getInstance().getToken();
        appDB = Room.databaseBuilder(getApplicationContext(), AppDB.class, "facebookDB")
                .fallbackToDestructiveMigration()
                .build();
        userDao = appDB.userDao();
        final ClientUser[] currentUser = new ClientUser[1];
        CountDownLatch latch = new CountDownLatch(1); // Create a CountDownLatch with a count of 1

        new Thread(() -> {
            currentUser[0] = appDB.userDao().getUserById(userId);
            latch.countDown(); // Decrease the count
        }).start();

        try {
            latch.await(); // Main thread waits here until count reaches zero
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        user = currentUser[0];


        // Get the posts from the data holder
//        List<Post> postList = DataHolder.getInstance().getPostList();
        PostAPI postsApi = new PostAPI(ServerIP);
        postsApi.getAllPosts(token, new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                postList = response.body();
                postDao = appDB.postDao();
                for(Post p:postList){
                    new Thread(() -> {
                        postDao.insert(p);
                    }).start();
                }
                // Set the adapter for the recycler view
                final PostsListAdapter adapter = new PostsListAdapter(Pid.this);
                RecyclerView lstPosts = findViewById(R.id.lstPosts);
                lstPosts.setAdapter(adapter);
                lstPosts.setLayoutManager(new LinearLayoutManager(Pid.this));
                // Set the posts and the user to the adapter
                adapter.setPosts(postList, user);
            }

            @Override
            public void onFailure(retrofit2.Call<List<Post>> call, Throwable t) {
                t.printStackTrace();
            }
        });

//        RecyclerView lstPosts = findViewById(R.id.lstPosts);
//        // Set the adapter for the recycler view
//        final PostsListAdapter adapter = new PostsListAdapter(this);
//        lstPosts.setAdapter(adapter);
//        lstPosts.setLayoutManager(new LinearLayoutManager(this));
//        // Set the posts and the user to the adapter
//        adapter.setPosts(postList, user);

        Button btnAddPost = findViewById(R.id.btnAddPost);
        // When the user clicks on the add post button,
        // the user will be redirected to the add post page
        btnAddPost.setOnClickListener(v -> {
            Intent i = new Intent(this, AddPost.class);
//            DataHolder.getInstance().setPostList(postList);

            startActivity(i);
        });
        ImageButton menuIcon = findViewById(R.id.menuIcon);
        menuIcon.setOnClickListener(v -> showPopupMenu(v));


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clear Room database when the app is closing
        new Thread(() -> {
            userDao.deleteAllUsers();
        }).start();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
    }

    // Show the popup menu
    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.inflate(R.menu.navi_menu);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_darkMode) {
                // Handle dark mode action
                int nightMode = AppCompatDelegate.getDefaultNightMode();
                if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    // Change to dark mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                return true;
            }
            if (id == R.id.action_logOut) {
                // Handle logout action
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
        popupMenu.show();
    }
}