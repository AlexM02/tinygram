# Tinygram

Alex MAINGUY - Lucas LELIÈVRE - Thomas LAPIERRE

## Commandes de déploiement

``` mvn clean package```

```mvn endpoints-framework:openApiDocs```  

```gcloud endpoints services deploy target/openapi-docs/openapi.json  ```

```mvn appengine:deploy ```

## Datastore
### Entité Friend
<img src="https://github.com/AlexM02/tinygram/blob/main/image-readme/friend-kind.png" width="450"/>

### Entité Post
<img src="https://github.com/AlexM02/tinygram/blob/main/image-readme/post-kind.png" width="450"/>

## Benchmark

How much time does it take to post a message if followed by 10, 100, and 500 followers? (average on 30 measures)

10 personnes : 
91,078839 ms 

100 personnes : 

500 personnes :

How much time does it take to retrieve the last 10,100 and 500 last messages? (average of 30 measures)

How much “likes” can you do per second ?? (average on 30 measures)

