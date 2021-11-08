# {Bank name here} business banking application
This project contains the minimum setup for the Business Banking Universal App. 

## Project setup
After downloading the app for the first time, you will not be able to build it. 
This is due to the fact that you have to setup your Gradle credentials. Note that this also needs to
be done on your automated build server once you have it up and running.

### Set up Gradle credentials
1. Navigate to ~/.gradle/ in your system directory, note the ~ symbol.
2. If it does not already exist, create a file called gradle.properties (touch gradle.properties on the command line)
3. Log in to Repo/Artifactory and click on your name in the top right.
4. Type in your password to get access to your encrypted password. Copy your encrypted password.
5. Add the following lines to the gradle.properties file:

```
artifactsRepoUrl=https://repo.backbase.com/android
artifactsRetailRepoUrl=https://repo.backbase.com/android-retail
artifactsBusinessRepoUrl=https://repo.backbase.com/android-business
artifactsDesignRepoUrl=https://repo.backbase.com/design-android
artifactsIdentityRepoUrl=https://repo.backbase.com/android-identity
artifactsEngagementChannelsRepoUrl=https://artifacts.backbase.com/android-engagement-channels
artifactsMobileNotificationsRepoUrl=https://artifacts.backbase.com/android-mobile-notifications

mvnUser=<your username>
mvnPass=<your encrypted password>
```
You may need to restart Android Studio for it to recognize the new gradle.properties file

