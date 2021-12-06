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
