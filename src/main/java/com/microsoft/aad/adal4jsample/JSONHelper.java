/*******************************************************************************
 // Copyright (c) Microsoft Corporation.
 // All rights reserved.
 //
 // This code is licensed under the MIT License.
 //
 // Permission is hereby granted, free of charge, to any person obtaining a copy
 // of this software and associated documentation files(the "Software"), to deal
 // in the Software without restriction, including without limitation the rights
 // to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
 // copies of the Software, and to permit persons to whom the Software is
 // furnished to do so, subject to the following conditions :
 //
 // The above copyright notice and this permission notice shall be included in
 // all copies or substantial portions of the Software.
 //
 // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 // IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 // FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
 // AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 // LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 // OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 // THE SOFTWARE.
 ******************************************************************************/
package com.microsoft.aad.adal4jsample;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * This class provides the methods to parse JSON Data from a JSON Formatted
 * String.
 *
 * @author Azure Active Directory Contributor
 */
public class JSONHelper {

    private static Logger logger = Logger.getLogger(JSONHelper.class);

    JSONHelper() {
        // PropertyConfigurator.configure("log4j.properties");
    }

    /**
     * This method parses an JSON Array out of a collection of JSON Objects
     * within a string.
     *
     * @param jsonObject The JSON String that holds the collection.
     * @return An JSON Array that would contains all the collection object.
     */
    public static JSONArray fetchDirectoryObjectJSONArray(JSONObject jsonObject) {
        return jsonObject.optJSONObject("responseMsg").optJSONArray("value");
    }

    /**
     * This method parses an JSON Object out of a collection of JSON Objects
     * within a string
     *
     * @return An JSON Object that would contains the DirectoryObject.
     */
    public static JSONObject fetchDirectoryObjectJSONObject(JSONObject jsonObject) {
        return jsonObject.optJSONObject("responseMsg");
    }

    /**
     * This method parses the skip token from a json formatted string.
     *
     * @param jsonObject The JSON Formatted String.
     * @return The skipToken.
     */
    public static String fetchNextSkiptoken(JSONObject jsonObject) {
        String skipToken;
        // Parse the skip token out of the string.
        skipToken = jsonObject.optJSONObject("responseMsg").optString("odata.nextLink");

        if (!skipToken.equalsIgnoreCase("")) {
            // Remove the unnecessary prefix from the skip token.
            int index = skipToken.indexOf("$skiptoken=") + ("$skiptoken=").length();
            skipToken = skipToken.substring(index);
        }
        return skipToken;
    }

    public static String fetchDeltaLink(JSONObject jsonObject) {
        String deltaLink = "";
        // Parse the skip token out of the string.
        deltaLink = jsonObject.optJSONObject("responseMsg").optString("aad.deltaLink");

        if (deltaLink == null || deltaLink.length() == 0) {
            deltaLink = jsonObject.optJSONObject("responseMsg").optString("aad.nextLink");
            logger.info("deltaLink empty, nextLink ->" + deltaLink);
        }

        if (!deltaLink.equalsIgnoreCase("")) {
            // Remove the unnecessary prefix from the skip token.
            int index = deltaLink.indexOf("deltaLink=") + ("deltaLink=").length();
            deltaLink = deltaLink.substring(index);
        }
        return deltaLink;
    }

    /**
     * This method would create a string consisting of a JSON document with all
     * the necessary elements set from the HttpServletRequest request.
     *
     * @param request The HttpServletRequest
     * @return the string containing the JSON document.
     */
    public static String createJSONString(HttpServletRequest request, String controller) {
        JSONObject obj = new JSONObject();
        try {
            final Class<?> aClass = Class.forName(
                    "com.microsoft.windowsazure.activedirectory.sdk.graph.models." + controller
            );
            Field[] allFields = aClass.getDeclaredFields();

            String[] allFieldStr = new String[allFields.length];

            for (int i = 0; i < allFields.length; i++) {
                allFieldStr[i] = allFields[i].getName();
            }

            List<String> allFieldStringList = Arrays.asList(allFieldStr);
            Enumeration<String> fields = request.getParameterNames();

            while (fields.hasMoreElements()) {

                String fieldName = fields.nextElement();
                String param = request.getParameter(fieldName);

                if (allFieldStringList.contains(fieldName)) {
                    if (param == null || param.length() == 0) {
                        if (!fieldName.equalsIgnoreCase("password")) {
                            obj.put(fieldName, JSONObject.NULL);
                        }
                    } else {
                        if (fieldName.equalsIgnoreCase("password")) {
                            obj.put("passwordProfile", new JSONObject("{\"password\": \"" + param + "\"}"));
                        } else {
                            obj.put(fieldName, param);
                        }
                    }
                }
            }
        } catch (JSONException | SecurityException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    /**
     * @return string format of this JSON obje
     */
    public static String createJSONString(String key, String value) {

        JSONObject obj = new JSONObject();
        try {
            obj.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj.toString();
    }

    /**
     * This is a generic method that copies the simple attribute values from an
     * argument jsonObject to an argument generic object.
     *
     * @param jsonObject The jsonObject from where the attributes are to be copied.
     * @param destObject The object where the attributes should be copied into.
     * @throws Exception Throws a Exception when the operation are unsuccessful.
     */
    public static <T> void convertJSONObjectToDirectoryObject(JSONObject jsonObject, T destObject) throws Exception {

        // Get the list of all the field names.
        Field[] fields = destObject.getClass().getDeclaredFields();

        // For all the declared field.
        for (Field field : fields) {
            // If the field is of type String, that is
            // if it is a simple attribute.
            if (field.getType().equals(String.class)) {
                // Invoke the corresponding set method of the destObject using
                // the argument taken from the jsonObject.
                final String setterName = String.format("set%s", WordUtils.capitalize(field.getName()));

                destObject.getClass()
                        .getMethod(setterName, new Class[]{String.class})
                        .invoke(destObject, jsonObject.optString(field.getName()));
            }
        }
    }

    public static JSONArray joinJSONArrays(JSONArray a, JSONArray b) {
        JSONArray comb = new JSONArray();

        for (int i = 0; i < a.length(); i++) {
            comb.put(a.optJSONObject(i));
        }
        for (int i = 0; i < b.length(); i++) {
            comb.put(b.optJSONObject(i));
        }
        return comb;
    }

}
