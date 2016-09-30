/*******************************************************************************
 * Copyright © Microsoft Open Technologies, Inc.
 *
 * All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 * ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
 * PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
 *
 * See the Apache License, Version 2.0 for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/
package com.microsoft.aad.adal4jsample;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;

import javax.naming.ServiceUnavailableException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;

public class BasicFilter implements Filter {

    public static final String STATES = "states";
    public static final String STATE = "state";
    public static final Integer STATE_TTL = 180;
    public static final String FAILED_TO_VAL_MSG = "Failed to validate data received from Authorization service - ";
    private String clientId = "";
    private String clientSecret = "";
    private String tenant = "";
    private String authority;

    public void destroy() {

    }

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            try {
                String currentUri = httpRequest.getRequestURL().toString();
                String fullUrl = currentUri
                        + (httpRequest.getQueryString() != null ? "?" + httpRequest.getQueryString() : "");
                // check if user has a AuthData in the session
                if (!AuthHelper.isAuthenticated(httpRequest)) {
                    if (AuthHelper.containsAuthenticationData(httpRequest)) {
                        processAuthenticationData(httpRequest, currentUri, fullUrl);
                    } else {
                        // not authenticated
                        sendAuthRedirect(httpRequest, httpResponse);
                        return;
                    }
                } else {
                    checkAccessTokenExpirationDate(httpRequest, currentUri);
                }
            }
            catch (AuthenticationException authException){
                // something went wrong (like expiration or revocation of token)
                // we should invalidate AuthData stored in session and redirect to Authorization server
                removePrincipalFromSession(httpRequest);
                sendAuthRedirect(httpRequest, httpResponse);
                return;
            }
            catch (Throwable exc) {
                httpResponse.setStatus(500);
                request.setAttribute("error", exc.getMessage());
                request.getRequestDispatcher("/error.jsp").forward(request, response);
            }
        }
        chain.doFilter(request, response);
    }

    private void checkAccessTokenExpirationDate(HttpServletRequest httpRequest, String currentUri) throws Throwable {
        // check access token expiration date
        AuthenticationResult authData = AuthHelper.getAuthSessionObject(httpRequest);
        if(authData.getExpiresOnDate().before(new Date())){
            authData = getAccessTokenFromRefreshToken(authData.getRefreshToken(), currentUri);
        }
        setSessionPrincipal(httpRequest, authData);
    }

    private void processAuthenticationData(HttpServletRequest httpRequest, String currentUri, String fullUrl)
            throws Throwable {
        Map<String, String> params = new HashMap();
        for (String key : httpRequest.getParameterMap().keySet()) {
            params.put(key, httpRequest.getParameterMap().get(key)[0]);
        }
        validateState(httpRequest.getSession(), params.get(STATE));

        AuthenticationResponse authResponse = AuthenticationResponseParser.parse(new URI(fullUrl), params);
        if (AuthHelper.isAuthenticationSuccessful(authResponse)) {
            AuthenticationSuccessResponse oidcResponse = (AuthenticationSuccessResponse) authResponse;
            validateOIDCAuthResponseMatchesCodeFlow(oidcResponse);
            AuthenticationResult result = getAccessToken(oidcResponse.getAuthorizationCode(), currentUri);

            setSessionPrincipal(httpRequest, result);
        } else {
            AuthenticationErrorResponse oidcResponse = (AuthenticationErrorResponse) authResponse;
            throw new Exception(String.format("Request for auth code failed: %s - %s",
                    oidcResponse.getErrorObject().getCode(),
                    oidcResponse.getErrorObject().getDescription()));
        }
    }

    private void sendAuthRedirect(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        httpResponse.setStatus(302);

        // use state parameter to validate response from Authorization server
        String state = UUID.randomUUID().toString();
        storeStateInSession(httpRequest.getSession(), state);

        String currentUri = httpRequest.getRequestURL().toString();
        httpResponse.sendRedirect(getRedirectUrl(currentUri, state));
    }

    /**
     * make sure that state is stored in the session,
     * delete it from session - should be used only once
     * @param session
     * @param stateInAuthData
     * @throws Exception
     */
    private void validateState(HttpSession session, String stateInAuthData) throws Exception {
        if (stateInAuthData == null || !isStateInSession(session, stateInAuthData)){
            throw new Exception(FAILED_TO_VAL_MSG + "could not validate state");
        }
    }


    private void validateOIDCAuthResponseMatchesCodeFlow(AuthenticationSuccessResponse oidcResponse) throws Exception {
        if(oidcResponse.getIDToken() != null || oidcResponse.getAccessToken() != null ||
                oidcResponse.getAuthorizationCode() == null){
            throw new Exception(FAILED_TO_VAL_MSG + "unexpected set of artifacts received");
        }
    }

    private boolean isStateInSession(HttpSession session, String state){
        Map<String, Date> states =  (Map<String, Date>)session.getAttribute(STATES);
        if(states != null){
            eliminateExpiredStates(states);
            if(states.containsKey(state)){
                states.remove(state);
                return true;
            }
        }
        return false;
    }

    private void storeStateInSession(HttpSession session, String state){
        if(session.getAttribute(STATES) == null){
            session.setAttribute(STATES, new HashMap<String, Date>());
        }
        ((Map<String, Date>)session.getAttribute(STATES)).put(state, new Date());
    }

    private void eliminateExpiredStates(Map<String, Date> map){
        Iterator<Map.Entry<String, Date>> it = map.entrySet().iterator();

        Date currTime = new Date();
        while (it.hasNext()){
            Map.Entry<String, Date> entry = it.next();
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(currTime.getTime() - entry.getValue().getTime());

            if(diffInMinutes > STATE_TTL){
                it.remove();
            }
        }
    }

    private AuthenticationResult getAccessTokenFromRefreshToken(
            String refreshToken, String currentUri) throws Throwable {
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext(authority + tenant + "/", true,
                    service);
            Future<AuthenticationResult> future = context
                    .acquireTokenByRefreshToken(refreshToken, new ClientCredential(clientId, clientSecret), null, null);
            result = future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new ServiceUnavailableException("authentication result was null");
        }
        return result;
    }

    private AuthenticationResult getAccessToken(
            AuthorizationCode authorizationCode, String currentUri)
            throws Throwable {
        String authCode = authorizationCode.getValue();
        ClientCredential credential = new ClientCredential(clientId,
                clientSecret);
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext(authority + tenant + "/", true,
                    service);
            Future<AuthenticationResult> future = context
                    .acquireTokenByAuthorizationCode(authCode, new URI(
                            currentUri), credential, null);
            result = future.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new ServiceUnavailableException("authentication result was null");
        }
        return result;
    }

    private void setSessionPrincipal(HttpServletRequest httpRequest,
                                     AuthenticationResult result) {
        httpRequest.getSession().setAttribute(AuthHelper.PRINCIPAL_SESSION_NAME, result);
    }

    private void removePrincipalFromSession(HttpServletRequest httpRequest) {
        httpRequest.getSession().removeAttribute(AuthHelper.PRINCIPAL_SESSION_NAME);
    }

    private String getRedirectUrl(String currentUri, String state)
            throws UnsupportedEncodingException {
        String redirectUrl = authority
                + this.tenant
                + "/oauth2/authorize?response_type=code&scope=openid&response_mode=form_post&redirect_uri="
                + URLEncoder.encode(currentUri, "UTF-8") + "&client_id="
                + clientId + "&resource=https%3a%2f%2fgraph.windows.net"
                + "&state=" + state;

        return redirectUrl;
    }

    public void init(FilterConfig config) throws ServletException {
        clientId = config.getInitParameter("client_id");
        authority = config.getServletContext().getInitParameter("authority");
        tenant = config.getServletContext().getInitParameter("tenant");
        clientSecret = config.getInitParameter("secret_key");
    }

}
