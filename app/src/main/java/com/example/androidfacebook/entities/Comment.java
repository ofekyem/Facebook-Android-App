package com.example.androidfacebook.entities;
import java.util.*;
public class Comment {
    private int id;
    private String text;
    private User user;

    public Comment(int id,String text,User user){
        this.id=id;
        this.text=text;
        this.user=user;
    }
}