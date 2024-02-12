package com.example.androidfacebook.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfacebook.R;
import com.example.androidfacebook.entities.Post;
import java.util.*;
public class PostsListAdapter extends RecyclerView.Adapter<PostsListAdapter.PostViewHolder>{
    class PostViewHolder extends RecyclerView.ViewHolder{
        private final TextView tvAuthor;
        private final TextView tvContent;
        private final ImageView ivPic;

        private PostViewHolder(View itemView){
            super(itemView);
            tvAuthor=itemView.findViewById(R.id.tvAuthor);
            tvContent=itemView.findViewById(R.id.tvContent);
            ivPic=itemView.findViewById(R.id.ivPic);

        }

    }
    private final LayoutInflater mInflater;
    private List<Post> posts;

    public PostsListAdapter(Context context){mInflater=LayoutInflater.from(context);}

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent,int viewType){
        View itemView = mInflater.inflate(R.layout.post_item,parent,false);
        return new PostViewHolder(itemView);
    }
    public void onBindViewHolder(PostViewHolder holder,int position){
        if(posts!=null){
            final Post current = posts.get(position);
            holder.tvAuthor.setText(current.getUser().getDisplayName());
            holder.tvContent.setText(current.getInitialText());
            holder.ivPic.setImageResource(current.getPictures());
        }
    }
    public void setPosts(List<Post> s){
        posts = s;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount(){
        if(posts!=null){
            return posts.size();
        }
        else{
            return 0;
        }
    }
    public List<Post> getPosts() {return posts;}
}
