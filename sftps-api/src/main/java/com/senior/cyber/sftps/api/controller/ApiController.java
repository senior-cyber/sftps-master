package com.senior.cyber.sftps.api.controller;


import com.google.crypto.tink.Aead;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.senior.cyber.frmk.common.function.HttpExtension;
import com.senior.cyber.frmk.common.pki.CertificateUtils;
import com.senior.cyber.frmk.common.pki.PublicKeyUtils;
import com.senior.cyber.sftps.api.SecretUtils;
import com.senior.cyber.sftps.api.repository.KeyRepository;
import com.senior.cyber.sftps.api.repository.LogRepository;
import com.senior.cyber.sftps.api.repository.UserRepository;
import com.senior.cyber.sftps.api.tink.MasterAead;
import com.senior.cyber.sftps.api.tink.WebHook;
import com.senior.cyber.sftps.dao.entity.Key;
import com.senior.cyber.sftps.dao.entity.Log;
import com.senior.cyber.sftps.dao.entity.User;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jasypt.util.password.PasswordEncryptor;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@RestController
public class ApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    protected static final String USER_ID = "USER_ID";
    protected static final String KEY_ID = "KEY_ID";

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PasswordEncryptor passwordEncryptor;

    @Autowired
    protected KeyRepository keyRepository;

    @Autowired
    protected LogRepository logRepository;

    @Autowired
    protected MasterAead masterAead;

    @Autowired
    protected CloseableHttpClient client;

    @DeleteMapping(path = "/**")
    public void delete(@RequestHeader(value = "Authorization", required = false) String authorization, HttpServletRequest request, HttpServletResponse response) throws CertificateException, IOException {
        authentication(authorization, request, response);

        HttpSession session = request.getSession(true);

        Optional<User> optionalUser = this.userRepository.findById((long) session.getAttribute(USER_ID));
        Key key = null;
        if (session.getAttribute(KEY_ID) != null) {
            Optional<Key> optionalKey = this.keyRepository.findById((long) session.getAttribute(KEY_ID));
            key = optionalKey.orElse(null);
        }

        User userObject = optionalUser.orElse(null);
        if (userObject == null) {
            session.removeAttribute(USER_ID);
            session.removeAttribute(KEY_ID);
            response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else {
            if (!userObject.isEnabled()) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
        }

        if (key != null && !key.isEnabled()) {
            session.removeAttribute(USER_ID);
            session.removeAttribute(KEY_ID);
            response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String pathInfo = request.getRequestURI();

        String homeDirectory = FilenameUtils.normalizeNoEndSeparator(userObject.getHomeDirectory(), true);
        String fn = FilenameUtils.normalizeNoEndSeparator(new File(homeDirectory, pathInfo).getAbsolutePath(), true);

        if (!fn.startsWith(homeDirectory)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (fn.equals(homeDirectory)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        File file = new File(fn);
        FileUtils.deleteQuietly(file);

        Log log = new Log();
        log.setCreatedAt(new Date());
        log.setEventType("Deleted");
        log.setUserDisplayName(userObject.getDisplayName());
        if (key != null) {
            log.setKeyName(key.getName());
        }
        if (file.isFile()) {
            log.setSize(file.length());
        }
        log.setSrcPath(fn.substring(homeDirectory.length()));
        logRepository.save(log);
        WebHook.report(this.client, this.userRepository, this.masterAead, log, userObject, key);
    }

    @PutMapping(path = "/**")
    public void put(@RequestHeader(value = "Authorization", required = false) String authorization, HttpServletRequest request, HttpServletResponse response) throws GeneralSecurityException, IOException {
        authentication(authorization, request, response);

        HttpSession session = request.getSession(true);

        Optional<User> optionalUser = this.userRepository.findById((long) session.getAttribute(USER_ID));
        Key key = null;
        if (session.getAttribute(KEY_ID) != null) {
            Optional<Key> optionalKey = this.keyRepository.findById((long) session.getAttribute(KEY_ID));
            key = optionalKey.orElse(null);
        }

        User userObject = optionalUser.orElse(null);
        if (userObject == null) {
            session.removeAttribute(USER_ID);
            session.removeAttribute(KEY_ID);
            response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else {
            if (!userObject.isEnabled()) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
        }

        if (key != null && !key.isEnabled()) {
            session.removeAttribute(USER_ID);
            session.removeAttribute(KEY_ID);
            response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String pathInfo = request.getRequestURI();

        String homeDirectory = FilenameUtils.normalizeNoEndSeparator(userObject.getHomeDirectory(), true);
        String fn = FilenameUtils.normalizeNoEndSeparator(new File(homeDirectory, pathInfo).getAbsolutePath(), true);

        if (!fn.startsWith(homeDirectory)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        String dek = userObject.getDek();
        String black_secret = userObject.getSecret();

        String white_secret = null;
        if (dek != null && !"".equals(dek) && black_secret != null && !"".equals(black_secret)) {
            Aead aeadDek = KeysetHandle.read(JsonKeysetReader.withString(dek), masterAead).getPrimitive(Aead.class);
            white_secret = new String(aeadDek.decrypt(Base64.getDecoder().decode(black_secret), "".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        }

        byte[] originalDictionary = null;
        if (white_secret != null) {
            LOGGER.info("white_secret [{}]", white_secret);
            originalDictionary = SecretUtils.buildOriginToFake(white_secret);
        }

        File file = new File(fn);
        try (FileOutputStream outputStream = FileUtils.openOutputStream(file)) {
            InputStream inputStream = request.getInputStream();
            if (originalDictionary != null) {
                byte[] buffer = IOUtils.byteArray(IOUtils.DEFAULT_BUFFER_SIZE);
                int n;
                while (IOUtils.EOF != (n = inputStream.read(buffer))) {
                    byte[] data = SecretUtils.translate(originalDictionary, buffer);
                    outputStream.write(data, 0, n);
                }
            } else {
                IOUtils.copy(request.getInputStream(), outputStream);
            }
        }

        Log log = new Log();
        log.setCreatedAt(new Date());
        log.setEventType("Uploaded");
        log.setUserDisplayName(userObject.getDisplayName());
        if (key != null) {
            log.setKeyName(key.getName());
        }
        log.setSize(file.length());
        log.setSrcPath(fn.substring(homeDirectory.length()));
        logRepository.save(log);
        WebHook.report(this.client, this.userRepository, this.masterAead, log, userObject, key);
    }

    @GetMapping(path = "/**")
    public void get(@RequestHeader(value = "Authorization", required = false) String authorization, HttpServletRequest request, HttpServletResponse response) throws IOException, GeneralSecurityException {
        String pathInfo = request.getRequestURI();
        if (pathInfo.equals("/___folder.gif")) {
            response.setContentType("image/gif");
            try (InputStream stream = ApiController.class.getResourceAsStream("/folder.gif")) {
                IOUtils.copy(stream, response.getOutputStream());
            }
            return;
        } else if (pathInfo.equals("/___unknown.gif")) {
            response.setContentType("image/gif");
            try (InputStream stream = ApiController.class.getResourceAsStream("/unknown.gif")) {
                IOUtils.copy(stream, response.getOutputStream());
            }
            return;
        } else if (pathInfo.equals("/___blank.gif")) {
            response.setContentType("image/gif");
            try (InputStream stream = ApiController.class.getResourceAsStream("/blank.gif")) {
                IOUtils.copy(stream, response.getOutputStream());
            }
            return;
        } else if (pathInfo.equals("/___back.gif")) {
            response.setContentType("image/gif");
            try (InputStream stream = ApiController.class.getResourceAsStream("/back.gif")) {
                IOUtils.copy(stream, response.getOutputStream());
            }
            return;
        }

        authentication(authorization, request, response);

        HttpSession session = request.getSession(true);

        Optional<User> optionalUser = this.userRepository.findById((long) session.getAttribute(USER_ID));
        Key key = null;
        if (session.getAttribute(KEY_ID) != null) {
            Optional<Key> optionalKey = this.keyRepository.findById((long) session.getAttribute(KEY_ID));
            key = optionalKey.orElse(null);
        }

        String address = HttpExtension.lookupHttpAddress(request);

        User userObject = optionalUser.orElse(null);
        if (userObject == null) {
            session.removeAttribute(USER_ID);
            session.removeAttribute(KEY_ID);
            response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else {
            if (!userObject.isEnabled()) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
        }

        if (key != null && !key.isEnabled()) {
            session.removeAttribute(USER_ID);
            session.removeAttribute(KEY_ID);
            response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        String homeDirectory = FilenameUtils.normalizeNoEndSeparator(userObject.getHomeDirectory(), true);
        String fn = FilenameUtils.normalizeNoEndSeparator(new File(homeDirectory, pathInfo).getAbsolutePath(), true);

        if (!fn.startsWith(homeDirectory)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        File file = new File(fn);
        if (file.isDirectory()) {
            response.setContentType("text/html");
            PrintWriter writer = response.getWriter();
            writer.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">\n");
            writer.write("<html>\n");
            writer.write("  <head>\n");
            if (homeDirectory.equals(fn)) {
                writer.write("    <title>Index of /</title>\n");
            } else {
                writer.write("    <title>Index of " + fn.substring(homeDirectory.length()) + "</title>\n");
            }
            writer.write("  </head>\n");

            writer.write("  <body>\n");
            if (homeDirectory.equals(fn)) {
                writer.write("    <h1>Index of /</h1>\n");
            } else {
                writer.write("    <h1>Index of " + fn.substring(homeDirectory.length()) + "</h1>\n");
            }
            writer.write("    <table>\n");
            writer.write("      <tr><th valign=\"top\"><img src=\"" + address + "/___blank.gif\" alt=\"[ICO]\"></th><th><a href=\"?C=N;O=D\">Name</a></th><th><a href=\"?C=M;O=A\">Last modified</a></th><th><a href=\"?C=S;O=A\">Size</a></th><th><a href=\"?C=D;O=A\">Description</a></th></tr>\n");
            writer.write("      <tr><th colspan=\"5\"><hr></th></tr>\n");
            if (!homeDirectory.equals(fn)) {
                String p = FilenameUtils.normalizeNoEndSeparator(file.getParentFile().getAbsolutePath(), true) + "/";
                writer.write("      <tr><td valign=\"top\"><img src=\"" + address + "/___back.gif\" alt=\"[PARENTDIR]\"></td><td><a href=\"" + p.substring(homeDirectory.length()) + "\">Parent Directory</a></td><td>&nbsp;</td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
            }
            File[] temps = file.listFiles();
            if (temps != null) {
                List<File> children = new ArrayList<>(Arrays.asList(temps));
                children.sort(Comparator.comparing(File::getName));
                for (File child : children) {
                    if (child.isDirectory()) {
                        writer.write("      <tr><td valign=\"top\"><img src=\"" + address + "/___folder.gif\" alt=\"[DIR]\"></td><td><a href=\"" + child.getName() + "/\">" + child.getName() + "/</a></td><td align=\"right\">" + DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(new Date(child.lastModified())) + "  </td><td align=\"right\">  - </td><td>&nbsp;</td></tr>\n");
                    }
                    if (child.isFile()) {
                        writer.write("      <tr><td valign=\"top\"><img src=\"" + address + "/___unknown.gif\" alt=\"[   ]\"></td><td><a href=\"" + child.getName() + "\">" + child.getName() + "</a></td><td align=\"right\">" + DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT.format(new Date(child.lastModified())) + "  </td><td align=\"right\"> " + FileUtils.byteCountToDisplaySize(child.length()) + "</td><td>&nbsp;</td></tr>\n");
                    }
                }
            }
            writer.write("      <tr><th colspan=\"5\"><hr></th></tr>\n");
            writer.write("    </table>\n");
            writer.write("  </body>\n");
            writer.write("</html>\n");
        } else if (file.isFile()) {
            String dek = userObject.getDek();
            String black_secret = userObject.getSecret();

            String white_secret = null;
            if (dek != null && !"".equals(dek) && black_secret != null && !"".equals(black_secret)) {
                Aead aeadDek = KeysetHandle.read(JsonKeysetReader.withString(dek), masterAead).getPrimitive(Aead.class);
                white_secret = new String(aeadDek.decrypt(Base64.getDecoder().decode(black_secret), "".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
            }

            byte[] fakeDictionary = null;
            if (white_secret != null) {
                LOGGER.info("white_secret [{}]", white_secret);
                fakeDictionary = SecretUtils.buildFakeToOrigin(white_secret);
            }

            response.setContentType("application/octet-stream");
            response.setContentLengthLong(file.length());
            try (InputStream inputStream = FileUtils.openInputStream(file)) {
                OutputStream outputStream = response.getOutputStream();
                if (fakeDictionary != null) {
                    byte[] buffer = IOUtils.byteArray(IOUtils.DEFAULT_BUFFER_SIZE);
                    int n;
                    while (IOUtils.EOF != (n = inputStream.read(buffer))) {
                        byte[] data = SecretUtils.translate(fakeDictionary, buffer);
                        outputStream.write(data, 0, n);
                    }
                } else {
                    IOUtils.copy(inputStream, outputStream);
                }
            }
            Log log = new Log();
            log.setCreatedAt(new Date());
            log.setEventType("Downloaded");
            log.setUserDisplayName(userObject.getDisplayName());
            if (key != null) {
                log.setKeyName(key.getName());
            }
            log.setSize(file.length());
            log.setSrcPath(fn.substring(homeDirectory.length()));
            logRepository.save(log);
            WebHook.report(this.client, this.userRepository, this.masterAead, log, userObject, key);
        }
    }

    protected void authentication(String authorization, HttpServletRequest request, HttpServletResponse response) throws IOException, CertificateException {
        HttpSession session = request.getSession(true);
        if (session.getAttribute(USER_ID) == null) {
            Long userId = null;
            Long keyId = null;
            if (authorization == null || "".equals(authorization)) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }

            X509Certificate[] certificates = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
            X509Certificate certificate = null;

            if (certificates != null && certificates.length >= 1) {
                certificate = certificates[0];
            }

            if (!StringUtils.upperCase(authorization).startsWith("BASIC")) {
                throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
            }

            if (authorization.length() <= "Basic ".length()) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }

            String base64 = StringUtils.substring(authorization, "Basic ".length());
            String login_pwd = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            int colon = StringUtils.indexOf(login_pwd, ':');
            String login = StringUtils.substring(login_pwd, 0, colon);
            String pwd = StringUtils.substring(login_pwd, colon + 1);

            if ("".equals(pwd) && certificate == null) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }

            Optional<User> optionalUser = this.userRepository.findByLogin(login);
            if (optionalUser.isEmpty()) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }

            User userObject = optionalUser.orElse(null);

            if (!userObject.isEnabled()) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }

            boolean authenticated = false;

            userId = userObject.getId();

            if (!"".equals(pwd)) {
                if (this.passwordEncryptor.checkPassword(pwd, userObject.getPassword())) {
                    userObject.setLastSeen(LocalDate.now().toDate());
                    userRepository.save(userObject);
                    authenticated = true;
                }
            }

            if (!authenticated && certificate != null) {
                PublicKey a = PublicKeyUtils.read(PublicKeyUtils.write(certificate.getPublicKey()));
                List<Key> keys = this.keyRepository.findByUser(userObject);
                if (!keys.isEmpty()) {
                    for (Key key : keys) {
                        X509Certificate c = CertificateUtils.read(key.getCertificate());
                        PublicKey b = PublicKeyUtils.read(PublicKeyUtils.write(certificate.getPublicKey()));
                        if (key.isEnabled() && a.equals(b)) {
                            keyId = key.getId();

                            userObject.setLastSeen(LocalDate.now().toDate());
                            userRepository.save(userObject);

                            key.setLastSeen(LocalDate.now().toDate());
                            keyRepository.save(key);

                            authenticated = true;
                            break;
                        }
                    }
                }
            }

            if (!authenticated) {
                response.setHeader("WWW-Authenticate", "Basic realm=\"Authentication\"");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }

            session.setAttribute(USER_ID, userId);
            session.setAttribute(KEY_ID, keyId);
        }
    }

}
