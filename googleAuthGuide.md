we essentially have a few steps needed before you can actually run the google backend api. 

1) ensure you are added to the google cloud side as an **OWNER**
2) ensure that within your application.properties, is filled in, to do this, visit: https://console.cloud.google.com/apis/credentials?project=climatesync-915316

3) go and create your own google auth key, and create a key using desktop. 

4) copy paste the secret + key into /Weather-App/src/main/resources/application.properties 

5) in terminal type:  mvn clean 
6) also run: mvn install

7) you should be now able to run the ClimateSyncApplication.java (next to main. )

8) while the app is running, visit the OAuth2 URL in your browser: http://localhost:8080/oauth2/authorization/google