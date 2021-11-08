#!/bin/bash

REPO="repo.backbase.com"

echo "Verifying credentials for repo $REPO...";

if ! `grep -q "$REPO" ~/.netrc` ; then
    echo "Provide your credentials for $REPO"
    echo "Username:"
    read LOGIN
    echo "Password:"
    read PASSWORD
    cat >> ~/.netrc <<- EOM
machine $REPO
login $LOGIN
password $PASSWORD
EOM
fi

echo "Checking for required tools...";

if `gem list -i "cocoapods"`; then
    echo "cocoapods gem is installed!";
else
    gem install --user-install "cocoapods"  
fi

if `gem list -i "cocoapods-art"`; then
    echo "cocoapods-art gem is installed!";
else
    gem install --user-install "cocoapods-art"  
fi

if ! [ -x "$(command -v brew)" ]; then
    echo "brew is installed!";
else
    /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
fi

if ! [ -x "$(command -v npm)" ]; then
    echo "npm is installed!";
else
    brew install node
fi

npm i -g @bb-cli/bb-mobile

echo "Checking for required repos...";

if [ ! -d ~/.cocoapods/repos-art/bbrepo3 ]; then
    pod repo-art add bbrepo3 https://repo.backbase.com/api/pods/ios3
fi

if [ ! -d ~/.cocoapods/repos-art/bbrepo-retail3 ]; then
    pod repo-art add bbrepo-retail3 https://repo.backbase.com/api/pods/ios-retail3
fi

if [ ! -d ~/.cocoapods/repos-art/bbrepo-business ]; then
    pod repo-art add bbrepo-business https://repo.backbase.com/api/pods/ios-business
fi

if [ ! -d ~/.cocoapods/repos-art/bbrepo-identity ]; then
    pod repo-art add bbrepo-identity https://repo.backbase.com/api/pods/ios-identity
fi

if [ ! -d ~/.cocoapods/repos-art/bbrepo-engagement-channels ]; then
    pod repo-art add bbrepo-engagement-channels https://repo.backbase.com/api/pods/ios-engagement-channels
fi

if [ ! -d ~/.cocoapods/repos-art/bbrepo-mobile-notifications ]; then
    pod repo-art add bbrepo-mobile-notifications https://repo.backbase.com/api/pods/ios-mobile-notifications
fi

echo "Updating repos..."

pod repo-art update bbrepo3
pod repo-art update bbrepo-retail3
pod repo-art update bbrepo-business
pod repo-art update bbrepo-identity
pod repo-art update bbrepo-engagement-channels
pod repo-art update bbrepo-mobile-notifications

echo "Installing dependencies..."

pod install


read -p "Do you want to setup SSL Pinning? (y/n) " -n 1 -r
echo    # (optional) move to a new line
if [[ $REPLY =~ ^[Yy]$ ]]; then
    # do dangerous stuff
    echo "Domain:"
    read DOMAIN
    echo "Downloading certificate for $DOMAIN..."
    openssl s_client -showcerts -servername $DOMAIN -host $DOMAIN -port 443 </dev/null 2>/dev/null | openssl x509 -outform DER > certificate.der
    echo "Certificate downloaded certificate.der, please add this file to your project"
    echo "and add the following node to your config.json under security"
    cat << EOF
"sslPinning": {
            "certificates": [
                "certificate.der"
            ],
            "checkChain": true
        }
EOF
fi