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

    // -------- GESTION DES UTILISATEURS -------

    /**
     * Create user in datastore
     * @param friend
     * @return
     */
    @ApiMethod(name = "createUser", path = "friend", httpMethod = HttpMethod.POST)
    public Entity createUser(GoogleObject friend) {
        Entity e = new Entity("Friend", friend.email);
        e.setProperty("email", friend.email);
        e.setProperty("firstName", friend.givenName);
        e.setProperty("lastName", friend.familyName);
        e.setProperty("name", friend.name);
        e.setProperty("imageUrl",friend.imageUrl);
        ArrayList<String> followers = new ArrayList<>();
        followers.add(friend.email);
        e.setProperty("followers", followers);
        e.setProperty("following", new ArrayList<String>());
        e.setProperty("cptFollowing", 0);
        e.setProperty("cptFollower", 0);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(e);
        return e;
    }

    /**
     * Get a user
     * @param friend
     * @return
     */
    @ApiMethod(name = "getUser", path = "getUser", httpMethod = HttpMethod.POST)
    public Entity getUser(GoogleObject friend)  {
        if(friend.email == null || friend.email.equals("")){
            return null;
        }

        Key key = KeyFactory.createKey("Friend", friend.email);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity e ;
        try{
            e = datastore.get(key);
            if(friend.imageUrl != null && !friend.imageUrl.equals("")){
                e.setProperty("imageUrl",friend.imageUrl);
            }
        }catch(EntityNotFoundException ex){
            e = createUser(friend);
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
        Transaction txn = datastore.beginTransaction();

        //Traitement de l'utilisateur qui follow
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
        txn.commit();

        //Traitement de l'utilisateur followé
        txn = datastore.beginTransaction();
        Key key2 = KeyFactory.createKey("Friend", follow);
        Entity e2 = datastore.get(key2);
        ArrayList<String> listFollowers = (ArrayList<String>) e2.getProperty("followers");
        if (listFollowers == null) {
            listFollowers = new ArrayList<String>();
        }
        if(!listFollowers.contains(email)){
            listFollowers.add(email);
            e2.setProperty("followers", listFollowers);
            e2.setProperty("cptFollower", (long) e2.getProperty("cptFollower")+1);
            datastore.put(e2);
        }
        txn.commit();

        /**
         * Ajouté l'utilisateur qui follow dans les listes de diffusions de toutes les photos de l'utilisateur followé
         */
        ArrayList<Entity> result = new ArrayList<>();
        Query q = new Query("Post")
                .setFilter(new FilterPredicate("email", FilterOperator.EQUAL, follow)
                );
        PreparedQuery pq = datastore.prepare(q);
        //On ajoute l'user dans les 20 derniers posts de l'utilisateur qu'il follow
        result.addAll(pq.asList(FetchOptions.Builder.withLimit(20)));
        for(Entity ent : result){
            ArrayList<String> listeDiff = (ArrayList<String>) ent.getProperty("listeDiffusion");
            if(listeDiff == null){
                listeDiff = new ArrayList<>();
            }
            listeDiff.add(email);
            ent.setProperty("listeDiffusion",listeDiff);
            datastore.put(ent);
        }

        return e;
    }

    /**
     * Récupérer des users de l'application
     * @param email
     * @return
     */
    @ApiMethod(name = "friendsToFollow", path = "friends/{email}/{offset}", httpMethod = HttpMethod.GET)
    public List<Entity> friendsToFollow(@Named("email") String email,@Named("offset") int offset) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        ArrayList<Entity> result = new ArrayList<>();

        Key key = KeyFactory.createKey("Friend", email);
        Entity e = datastore.get(key);
        ArrayList<String> following = (ArrayList<String>) e.getProperty("following");

        Query q = new Query("Friend")
                .setFilter(new FilterPredicate("email", FilterOperator.NOT_EQUAL, email)
                );
        PreparedQuery pq = datastore.prepare(q);
        result.addAll(pq.asList(FetchOptions.Builder.withLimit(30).offset(offset)));

        if(following != null){
            for(Entity en : result){
                if(following.contains(en.getProperty("email"))){
                    en.setProperty("isFollowing",true);
                }
            }
        }
        return result;
    }



    // --------- GESTION DES POSTS --------

    @ApiMethod(name = "addPost", path = "post", httpMethod = HttpMethod.POST)
    public Entity addPost(Post post) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity user ;
        Key key = KeyFactory.createKey("Friend", post.email);
        user = datastore.get(key);

        Date d = new Date();
        Entity e = new Entity("Post", d.getTime()+":"+post.email);

        e.setProperty("email", post.email);
        e.setProperty("profilImageLink",user.getProperty("imageUrl"));
        e.setProperty("pseudo", user.getProperty("name"));
        e.setProperty("image", post.image);
        e.setProperty("description", post.description);
        e.setProperty("cptLikes", 0);
        e.setProperty("date", d);
        e.setProperty("listeDiffusion",user.getProperty("followers"));
        e.setProperty("listeAime",new ArrayList<String>());
        datastore.put(e);
        return e;
    }

    @ApiMethod(name = "addLike", path = "post/like", httpMethod = HttpMethod.PUT)
    public Entity addLike(Like like) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity e;
        Key key = KeyFactory.createKey("Post", like.datePhoto+":"+like.emailCreateurPhoto);
        Transaction txn = datastore.beginTransaction();
        e = datastore.get(key);

       ArrayList<String> personWhoLike = (ArrayList<String>) e.getProperty("listeAime");
        if(personWhoLike==null){
            personWhoLike = new ArrayList<String>();
        }
        if(!personWhoLike.contains(like.emailUserQuiLike)){
            personWhoLike.add(like.emailUserQuiLike);
            e.setProperty("cptLikes", (long) e.getProperty("cptLikes") + 1);
        }
        e.setProperty("listeAime",personWhoLike);
        datastore.put(e);
        txn.commit();
        return e;
    }

    @ApiMethod(name = "getPost", path = "post/{email}/{offset}/{limit}", httpMethod = HttpMethod.GET)
    public List<Entity> getPost(@Named("email") String email,@Named("offset") int offset,@Named("limit") int limit){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        ArrayList<Entity> result = new ArrayList<>();

        Query q = new Query("Post")
                .setFilter(new FilterPredicate("listeDiffusion", FilterOperator.EQUAL, email))
                .addSort("date",SortDirection.DESCENDING);

        PreparedQuery pq = datastore.prepare(q);
        result.addAll(pq.asList(FetchOptions.Builder.withLimit(limit).offset(offset)));

        for(Entity en : result){
            if(en.getProperty("listeAime") !=null){
                ArrayList<String> personneQuiAime = (ArrayList<String>) en.getProperty("listeAime");
                if(personneQuiAime.contains(email)){
                    en.setProperty("aAime",true);
                }else{
                    en.setProperty("aAime",false);
                }
            }
            en.removeProperty("listeDiffusion");
            en.removeProperty("listeAime");
        }

        return result;
    }

    //BENCHMARK PART
    @ApiMethod(name = "benchmarkCreateUserXFollowers", path = "benchmark/Xfollowers/{nbFollower}", httpMethod = HttpMethod.GET)
    public Entity createUserXFollowers(@Named("nbFollower") int nbFollower) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        String email = randomEmail();
        GoogleObject go = new GoogleObject(email,"testFamilyName"+email.substring(1,8),
                "testGivenName"+email.substring(1,8),"","name"+email.substring(1,8));
        Entity e = createUser(go);

        ArrayList<String> followers = new ArrayList<>();
        for(int i =0;i<nbFollower;i++){
            String random = randomEmail();
            GoogleObject goBis = new GoogleObject(random,"testFamilyName"+random.substring(1,8),
                    "testGivenName"+random.substring(1,8),"","name"+random.substring(1,8));
            createUser(goBis);
            addFollow(random,email);
            followers.add(random);
            Post post = new Post(random,"https://firebasestorage.googleapis.com/v0/b/tinygram2021.appspot.com" +
                    "/o/thomas18lapierre%40gmail.com%2F8f5e8f92-17bf-4834-a6e7-e521c4b415be?alt=media&token=" +
                    "0d046753-e9ce-4644-ad65-cb2cf405b47b" ,"description test !");
            addPost(post);
        }
        followers.add(email);
        e.setProperty("followers",followers);
        e.setProperty("cptFollower",nbFollower);
        datastore.put(e);

        return e;
    }

    private String randomEmail(){
        String[] firstLetter = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v"};
        String email = firstLetter[(int) (Math.random() * 21)];
        email+= Math.random() * 99 ;
        email+= "@gmail.com";
        return email;
    }

}
