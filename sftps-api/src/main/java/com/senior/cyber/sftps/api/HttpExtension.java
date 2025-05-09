package com.senior.cyber.sftps.api;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

public class HttpExtension {

    public static String lookupHttpAddress(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        StringBuilder address = new StringBuilder();
        if (request.isSecure() && request.getServerPort() == 443) {
            address.append("https://").append(request.getServerName()).append(servletContext.getContextPath());
        } else if (!request.isSecure() && request.getServerPort() == 80) {
            address.append("http://").append(request.getServerName()).append(servletContext.getContextPath());
        } else {
            if (request.isSecure()) {
                address.append("https://");
            } else {
                address.append("http://");
            }
            address.append(request.getServerName()).append(":").append(request.getServerPort())
                    .append(servletContext.getContextPath());
        }
        String result = address.toString();
        if (result.endsWith("/")) {
            return result.substring(0, result.length() - 1);
        } else {
            return result;
        }
    }

}