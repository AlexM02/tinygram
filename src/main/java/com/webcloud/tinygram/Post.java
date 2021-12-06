package com.webcloud.tinygram;

public class Post {
    
    public String email;
    public String image;
    public String description;

    public Post() {
    }

    public Post(String email, String image, String description) {
        this.email = email;
        this.image = image;
        this.description = description;
    }
}
