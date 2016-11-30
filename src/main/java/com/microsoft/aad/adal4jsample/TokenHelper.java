package com.microsoft.aad.adal4jsample;

import java.text.ParseException;
import java.util.Map;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;

public class TokenHelper {
	public static String GetTenantId(String idToken) throws ParseException{
		JWT jwt = JWTParser.parse(idToken);
		
        ReadOnlyJWTClaimsSet claims = jwt.getJWTClaimsSet();
        
        String tenantId = claims.getClaim("tid").toString();
        
        return tenantId;
	}
}
