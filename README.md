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

### How much time does it take to post a message if followed by 10, 100, and 500 followers? (average on 30 measures)

| Nb followers  | 10               | 100              | 500              |
|---------------|------------------|------------------|------------------|
| Moyenne en ns | 94219488,6206897 | 90943026,0344828 | 162242807,517241 |
| Moyenne en ms | **94ms**             | **91ms**             | **162ms**            |

 **10 personnes :** 94,219 ms

**100 personnes :** 90,943 ms

**500 personnes :** 162,242 ms

How much time does it take to retrieve the last 10,100 and 500 last messages? (average of 30 measures)

How much “likes” can you do per second ?? (average on 30 measures)

