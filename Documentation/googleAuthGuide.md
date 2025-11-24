we essentially have a few steps needed before you can actually run the google backend api. 

1) ensure you are added to the google cloud side as an **OWNER**
2) ensure that you have your Client ID and Client Secret ready. Visit: https://console.cloud.google.com/apis/credentials?project=climatesync-915316

3) Set the following Environment Variables on your machine or in your IDE run configuration:
   - `GOOGLE_CLIENT_ID`: Your Google Client ID
   - `GOOGLE_CLIENT_SECRET`: Your Google Client Secret

4) in terminal type:  mvn clean 
5) also run: mvn install

6) Run the application (Main.java for the UI).

7) Click the "Connect to Google Calendar" button in the UI. This will open your browser for authentication.
