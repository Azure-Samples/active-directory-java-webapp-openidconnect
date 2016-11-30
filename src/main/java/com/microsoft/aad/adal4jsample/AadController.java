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
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.microsoft.aad.adal4j.AuthenticationResult;

@Controller
@RequestMapping("/secure/aad")
public class AadController {

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public String getDirectoryObjects(ModelMap model, HttpServletRequest httpRequest) {
		HttpSession session = httpRequest.getSession();
		AuthenticationResult result = (AuthenticationResult) session.getAttribute(AuthHelper.PRINCIPAL_SESSION_NAME);
		if (result == null) {
			model.addAttribute("error", new Exception("AuthenticationResult not found in session."));
			return "/error";
		} else {
			String data;
			try {
				String idToken = result.getIdToken();

				String tenantId = TokenHelper.GetTenantId(idToken);

				try {
					data = getUsernamesFromGraph(result.getAccessToken(), tenantId);
					model.addAttribute("users", data);
				} catch (IOException ex) {
					model.addAttribute("users", "Error listing all users. Make sure you have the right permissions set in Azure AD");
				}

				model.addAttribute("tenant", tenantId);
				model.addAttribute("userInfo", result.getUserInfo());
			} catch (Exception e) {
				model.addAttribute("error", e);
				return "/error";
			}
		}
		return "/secure/aad";
	}

	private String getUsernamesFromGraph(String accessToken, String tenant) throws Exception {
		URL url = new URL(
				String.format("https://graph.windows.net/%s/users?api-version=2013-04-05", tenant, accessToken));

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// Set the appropriate header fields in the request header.
		conn.setRequestProperty("api-version", "2013-04-05");
		conn.setRequestProperty("Authorization", accessToken);
		conn.setRequestProperty("Accept", "application/json;odata=minimalmetadata");
		String goodRespStr = HttpClientHelper.getResponseStringFromConn(conn, true);
		// logger.info("goodRespStr ->" + goodRespStr);
		int responseCode = conn.getResponseCode();
		JSONObject response = HttpClientHelper.processGoodRespStr(responseCode, goodRespStr);
		JSONArray users;

		users = JSONHelper.fetchDirectoryObjectJSONArray(response);

		StringBuilder builder = new StringBuilder();
		User user;
		for (int i = 0; i < users.length(); i++) {
			JSONObject thisUserJSONObject = users.optJSONObject(i);
			user = new User();
			JSONHelper.convertJSONObjectToDirectoryObject(thisUserJSONObject, user);
			builder.append(user.getUserPrincipalName() + "<br/>");
		}
		return builder.toString();
	}

}
