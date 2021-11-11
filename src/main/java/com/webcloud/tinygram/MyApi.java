package com.webcloud.tinygram;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;

@Api(
    name = "myApi",
    version = "v1",
    audiences = "336706060084-q0bhshelogk7vg0rs0dm3163fr99vsri.apps.googleusercontent.com",
  	clientIds = "336706060084-q0bhshelogk7vg0rs0dm3163fr99vsri.apps.googleusercontent.com",
    namespace =
    @ApiNamespace(
        ownerDomain = "tinygram.com",
        ownerName = "tinygram.com",
        packagePath = ""
    )
)

public class MyApi {

    @ApiMethod(name = "addFriend", path = "friend", httpMethod = HttpMethod.POST)
    public Entity addFriend(Friend friend) {
        Entity e = new Entity("Friend", friend.email);
        e.setProperty("email", friend.email);
        e.setProperty("firstName", friend.firstName);
        e.setProperty("lastName", friend.lastName);
        e.setProperty("follow", new ArrayList<String>());
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(e);
        return e;
    }

    @ApiMethod(name = "addFollow", path = "friend/{email}", httpMethod = HttpMethod.PUT)
    public Entity addFollow(@Named("email") String email, Follow follow) throws EntityNotFoundException {
        Key key = KeyFactory.createKey("Friend", email);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity e = datastore.get(key);
        ArrayList<String> list = (ArrayList<String>) e.getProperty("follow");
        if (list == null) {
            list = new ArrayList<String>();
        }
        list.add(follow.follow);
        e.setProperty("follow", list);
        datastore.put(e);
        return e;
    }
    
    @ApiMethod(name = "friend", path = "friend/{email}", httpMethod = HttpMethod.GET)
    public Entity friend(@Named("email") String email) throws EntityNotFoundException {
        Key key = KeyFactory.createKey("Friend", email);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity e = datastore.get(key);
        datastore.put(e);
        return e;
    }

    @ApiMethod(name = "addPost", path = "post", httpMethod = HttpMethod.POST)
    public Entity addPost(Post post) {
        Date d = new Date();
        Entity e = new Entity("Post", post.email + ":" + d);
        e.setProperty("email", post.email);
        e.setProperty("image", post.image);
        e.setProperty("description", post.description);
        e.setProperty("likes", 0);
        e.setProperty("date", d);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(e);
        return e;
    }

    @ApiMethod(name = "putPost", path = "post", httpMethod = HttpMethod.PUT)
    public Entity addLike(Like like) throws EntityNotFoundException {
        Key key = KeyFactory.createKey("Post", like.email + ":" + like.date);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Transaction txn = datastore.beginTransaction();
        Entity e = datastore.get(key);
        long likes = (long) e.getProperty("likes") + 1;
        e.setProperty("likes", likes);
        datastore.put(e);
        txn.commit();
        return e;
    }

    @ApiMethod(name = "post", path = "post/{email}", httpMethod = HttpMethod.GET)
    public List<Entity> post(@Named("email") String email) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key key = KeyFactory.createKey("Friend", email);
        Entity e = datastore.get(key);
        ArrayList<String> list = (ArrayList) e.getProperty("follow");
        ArrayList<Entity> result = new ArrayList<>();

        if (list != null) {
            for (String follow : list) {
                Query q = new Query("Post")
                    .setFilter(new FilterPredicate("email", FilterOperator.EQUAL, follow));
                    //.addSort("date", SortDirection.ASCENDING);
                PreparedQuery pq = datastore.prepare(q);
		        result.addAll(pq.asList(FetchOptions.Builder.withDefaults()));
            }
        }
        return result;
    }
}
