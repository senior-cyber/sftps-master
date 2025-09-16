# sftps-api

#### nano /opt/apps/sftps-master/sftps-api/run.sh

```text
#!/usr/bin/env bash

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export JAVA_HOME="/opt/apps/java/21"

cd $DIR

$JAVA_HOME/bin/java -jar sftps-api.jar --spring.config.location=file:./
```

#### sudo nano /etc/systemd/system/sftps-api.service

```text
[Unit]
Description=sftps-api
After=network-online.target

[Service]
Type=simple
Restart=always
RestartSec=15
User=socheat
Group=socheat
WorkingDirectory=/opt/apps/sftps-master/sftps-api
ExecStart=/opt/apps/sftps-master/sftps-api/run.sh
StartLimitInterval=15

[Install]
WantedBy=multi-user.target
```

```shell
sudo chmod 755 /etc/systemd/system/sftps-api.service
sudo systemctl enable sftps-api
sudo systemctl daemon-reload
sudo systemctl start sftps-api
sudo systemctl status sftps-api
```