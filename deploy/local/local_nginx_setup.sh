echo "Installing nginx"
brew install nginx || apt install -y nginx

echo "Setup nginx configuration"
rm -f /etc/nginx/sites-enabled/moafs.shefer
cp local.nginx.conf /etc/nginx/sites-enabled/moafs.shefer

echo "Registering local domain"
if grep -Fxq "127.0.0.1 moafs.shefer" /etc/hosts
then
    echo "Local domain already registered"
else
  echo "127.0.0.1 moafs.shefer" >> /etc/hosts
fi

echo "Reloading nginx"
nginx -s reload
