## Preparations to deploy project
! These scripts are just reminders, not really working scripts.

### Install required packeges
```bash
apt update
apt install -y sudo vim nano curl wget zip unzip ffmpeg imagemagick jpegoptim lsof nginx cron
```

### install Java
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 11 #{17 JAVA VERSION} for teamcity agent
sdk install java 17 #{17 JAVA VERSION} for application
```

### Install nvm, node, npm
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.2/install.sh | bash
nvm install 16
```

### Install Teamcity agent
``` bash
mkdir teamcity-agent
cd teamcity-agent
whet https://{TEAMCITY_HOST}/update/buildAgentFull.zip
cp ./conf/buildAgent.dist.properties ./conf/buildAgent.properties
vim ./conf/buildAgent.properties # Set temcity server address
./bin/agent.sh start
```

### Install NGINX
```bash
apt install nginx
vim /etc/nginx/sites-available/moafs.nginx.conf # insert contents of moafs.nginx.conf
ln /etc/nginx/sites-available/moafs.nginx.conf /etc/nginx/sites-enabled/moafs.nginx.conf
ln -sf ./deploy/moafs.nginx.conf /etc/nginx/sites-enabled/moafs.nginx.conf

nginx -t
service nginx start
nginx -s reload
```
