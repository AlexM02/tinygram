package com.webcloud.tinygram;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.google.appengine.repackaged.com.google.datastore.v1.PropertyFilter;
import com.google.apphosting.datastore.DatastoreV4;

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

    // -------- GESTION DES UTILISATEURS -------

    /**
     * Create user in datastore
     * @param friend
     * @return
     */
    @ApiMethod(name = "addFriend", path = "friend", httpMethod = HttpMethod.POST)
    public Entity addFriend(GoogleObject friend) {
        Entity e = new Entity("Friend", friend.email);
        e.setProperty("email", friend.email);
        e.setProperty("firstName", friend.givenName);
        e.setProperty("lastName", friend.familyName);
        e.setProperty("name", friend.name);
        e.setProperty("imageUrl",friend.imageUrl);
        e.setProperty("followers", new ArrayList<String>());
        e.setProperty("following", new ArrayList<String>());
        e.setProperty("cptFollowing", 0);
        e.setProperty("cptFollower", 0);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(e);
        return e;
    }

    /**
     * Get a user
     * @param email
     * @param friend
     * @return
     */
    @ApiMethod(name = "friend", path = "friend/{email}", httpMethod = HttpMethod.POST)
    public Entity friend(@Named("email")  String email, GoogleObject friend)  {
        if(!email.equals(friend.email)){
            return null;
        }
        Key key = KeyFactory.createKey("Friend", friend.email);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity e ;
        try{
            e = datastore.get(key);
        }catch(EntityNotFoundException ex){
            e = addFriend(friend);
        }
        datastore.put(e);
        return e;
    }

    /**
     * Follow someone
     * @param email
     * @param follow
     * @return
     * @throws EntityNotFoundException
     */
    @ApiMethod(name = "addFollow", path = "friend/{email}", httpMethod = HttpMethod.PUT)
    public Entity addFollow(@Named("email") String email,@Named("follow") String follow) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Key key = KeyFactory.createKey("Friend", email);
        Entity e = datastore.get(key);
        ArrayList<String> list = (ArrayList<String>) e.getProperty("following");
        if (list == null) {
            list = new ArrayList<String>();
        }
        if(!list.contains(follow)){
            list.add(follow);
            e.setProperty("following", list);
            e.setProperty("cptFollowing", (long) e.getProperty("cptFollowing")+1);
            datastore.put(e);
        }



        Key key2 = KeyFactory.createKey("Friend", follow);
        Entity e2 = datastore.get(key2);
        ArrayList<String> listFollowers = (ArrayList<String>) e.getProperty("followers");
        if (listFollowers == null) {
            listFollowers = new ArrayList<String>();
        }
        if(!listFollowers.contains(email)){
            listFollowers.add(email);
            e2.setProperty("followers", listFollowers);
            e2.setProperty("cptFollower", (long) e.getProperty("cptFollower")+1);
            datastore.put(e2);
        }

        return e;
    }

    /**
     * Récupérer des amis non suivis par l'utilisateur
     * @param email
     * @return
     */
    @ApiMethod(name = "friendsToFollow", path = "friends/{email}", httpMethod = HttpMethod.GET)
    public List<Entity> friendsToFollow(@Named("email") String email){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        ArrayList<Entity> result = new ArrayList<>();

        Query q = new Query("Friend")
                .setFilter(new FilterPredicate("followers", FilterOperator.NOT_EQUAL, email)
                );
        PreparedQuery pq = datastore.prepare(q);
        result.addAll(pq.asList(FetchOptions.Builder.withLimit(50)));
        return result;
    }



    // --------- GESTION DES POSTS --------

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
