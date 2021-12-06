package com.webcloud.tinygram;

public class GoogleObject {

    public String email ;
    public String familyName;
    public String givenName ;
    public String imageUrl ;
    public String name;

    public GoogleObject() {
    }

    public GoogleObject(String email, String familyName, String givenName, String imageUrl, String name) {
        this.email = email;
        this.familyName = familyName;
        this.givenName = givenName;
        this.imageUrl = imageUrl;
        this.name = name;
    }
}
