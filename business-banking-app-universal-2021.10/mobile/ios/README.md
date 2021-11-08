# Business Banking Kiskstarter App

## Important Note

In order to detect debuggers attached to the app a c file was added to the project, this shouldn't affect your release process, but if your app gets rejected due to this, please remove file jb_protect.c from your project and implement a different solution

## Getting Started

### Requirements

- [Xcode 12+](https://developer.apple.com/xcode/)
- [Cocoapods](https://cocoapods.org/)
- [Cocoapods-art](https://github.com/jfrog/cocoapods-art)
- [NPM](https://npmjs.com/)
- [bb-mobile](https://community.backbase.com/documentation/mobile-sdk/latest/mobile_cli)

> Note: Minimum required version for cocoapods is `1.10`.
> Please make sure that you’re using the right version by running `gem which cocoapods`.
```
gem install cocoapods
gem install cocoapods-art
```
  

### Environment Setup

Follow these two steps if you are setting up the project for the first time, otherwise, skip to the next section.

#### A - Automatic setup

Run the script

```
./init.sh
```

#### B - Manual setup

##### 1- Set artifactory credentials
   
repo-art uses authentication as specified in your standard [netrc](https://www.gnu.org/software/inetutils/manual/html_node/The-_002enetrc-file.html) file.
  
- Log in to [Repo](https://repo.backbase.com/) and click on your name in the top right.
  
- Enter your password again and click `Unlock`.
  
- Next to the `Encrypted Password` field, copy your encrypted password by clicking the `Copy encrypted password to clipboard` button.
  
- Open **~/.netrc** on your computer. If this file does not exist, create it.
  
- Add the following 3 lines to the file, replacing <username> with your artifactory username and <encrypted password> with the encrypted password you just copied.

```
machine repo.backbase.com
  login <username>
  password <encrypted password>
```
  

##### 2- Add and update artifactory repositories

```sh
pod repo-art add bbrepo3 https://repo.backbase.com/api/pods/ios3
pod repo-art add bbrepo-retail3 https://repo.backbase.com/api/pods/ios-retail3
pod repo-art add bbrepo-business https://repo.backbase.com/api/pods/ios-business
pod repo-art add bbrepo-identity https://repo.backbase.com/api/pods/ios-identity
pod repo-art add bbrepo-engagement-channels https://repo.backbase.com/api/pods/ios-engagement-channels
pod repo-art add bbrepo-mobile-notifications https://repo.backbase.com/api/pods/ios-mobile-notifications
```
  

#### Update the repositories 

```
pod repo-art update bbrepo3
pod repo-art update bbrepo-retail3
pod repo-art update bbrepo-business
pod repo-art update bbrepo-identity
pod repo-art update bbrepo-engagement-channels
pod repo-art update bbrepo-mobile-notifications
```
  

#### Adding the Business Universal App's dependency

All the necessary dependencies for the Business Universal App are distributed through one main dependency: `BusinessUniversalApp`.

The latest Business Universal App available is `2.1.1`. Please make sure that you're using the right version in your `podfile`
```
pod 'BusinessUniversalApp', '2.1.1'
```
  

#### Install dependencies
> Within the project directory:
```
pod install
```

This will download all the sub-dependencies including journeys, design system, clients, etc.
Do a clean build and run the app.

## Documentation
For more information about Business Banking including documentation, reference and release notes, visit [Community](https://community.backbase.com/)

[Business Banking Mobile](https://backbase.atlassian.net/wiki/spaces/F5/pages/1489634166/Documentation)

## License
Backbase License. See [LICENSE](https://stash.backbase.com/projects/BUS/repos/business-banking-collection-ios/browse/LICENSE) for more info.
