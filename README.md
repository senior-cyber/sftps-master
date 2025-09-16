# 1. Create your own root CA (private key + self-signed cert)

```shell
openssl genpkey -algorithm RSA -out ca.key -pkeyopt rsa_keygen_bits:4096
```

```shell
openssl req -x509 -new -key ca.key -sha256 -days 3650 -subj "/C=US/ST=State/L=City/O=YourOrg/OU=YourUnit/CN=MyRootCA" -out ca.pem
```

# 2. Generate your server’s private key (PKCS#8 PEM)

```shell
openssl genpkey -algorithm RSA -out privkey.pem -pkeyopt rsa_keygen_bits:2048
```

# 3. Produce a CSR for your server cert

# Replace “your.server.domain” with the actual hostname or IP

```shell
openssl req -new -key privkey.pem -subj "/C=US/ST=State/L=City/O=YourOrg/OU=ServerDept/CN=your.server.domain" -out server.csr
```

# 4. Create an extensions file to include SAN (SubjectAltName)

```shell
cat > ssl.ext <<EOF
subjectAltName = DNS:your.server.domain, IP:127.0.0.1
EOF
```

# 5. Sign the CSR with your CA, producing the server cert

```shell
openssl x509 -req -in server.csr -CA ca.pem -CAkey ca.key -CAcreateserial -out server.crt -days 825 -sha256 -extfile ssl.ext
```

# 6. Build your “fullchain.pem” by concatenating the server cert + CA cert

```shell
cat server.crt ca.pem > fullchain.pem
```

# P12 File

```shell
openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -name alias -out tls.p12 -passout pass:changeit
```

# TrustStore

```shell
keytool -import -alias alias -file ca.pem -keystore ca.jks -storepass changeit -noprompt
```