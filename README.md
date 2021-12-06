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

### How much time does it take to post a message if followed by 10, 100, and 500 followers? 

| Nb followers  | 10               | 100              | 500              |
|---------------|------------------|------------------|------------------|
| Moyenne en ns | 94219488,6206897 | 90943026,0344828 | 162242807,517241 |
| Moyenne en ms | **94ms**             | **91ms**             | **162ms**            |

 **10 personnes :** 94,219 ms

**100 personnes :** 90,943 ms

**500 personnes :** 162,242 ms

### How much time does it take to retrieve the last 10,100 and 500 last posts? 

| Nb followers  | 10               | 100              | 500              |
|---------------|------------------|------------------|------------------|
| Moyenne en ms | **120ms**             | **181ms**             | **420ms**            |

 **10 posts :** 120,345 ms

**100 posts :** 180,655 ms

**500 posts :** 419,862 ms

### How much “likes” can you do per second ? 

| Nb like/sec  | 30               | 60              | 90              |
|---------------|------------------|------------------|------------------|
| Taux de réussite | 100%             | 95%           | 85%           |

*Pas sûr du résultat obtenu car beaucoup de difficulté à tester*

