---
services: active-directory
platforms: java
author: navyasric
---

# Sign in using Azure AD account into a Java web application

This sample will give you a quick and easy way to set up authentication using Azure AD in a Java web application.
The sample is designed to run on any platform and tested with Java 8.

## Steps to run

### Step 1: Register the sample with Azure AD

To use this sample you will need an Azure Active Directory tenant. If you're not sure what a tenant is or how you would get one, read [this document](https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-howto-tenant).

1. Sign in to the [Azure Portal](https://portal.azure.com/)

2. If you have multiple tenants associated with your Azure account, on the top bar, select your account. Under the **DIRECTORY** list, choose the Active Directory tenant where you wish to register your app.

3. In the left navigation sidebar, select **Azure Active Directory** service. Next, select **App registrations**.

5. Select **New application registration** and provide a friendly name for the app, app type, and sign-on URL:
   **Name**: **Java-WebApp-OpenIDConnect**
   **Application Type**: **Web app / API**
   **Sign-on URL**: `http://localhost:8080/adal4jsample/`
   Select **Create** to register the app.

6. Select **Settings** and on the **Keys** blade, enter a Key description, set a duration and click **Save**. Note down the key value that is displayed before closing the box since it will not be shown again.

7. From the Azure portal, note the following information:

   **The Tenant domain:** See the **App ID URI** base URL under the **Properties** blade. For example: `contoso.onmicrosoft.com`

   The **Application ID** on the registered app blade. For example: `ba74781c2-53c2-442a-97c2-3d60re42f403`

### Step 2: Download Java (7 and above) for your platform

To successfully use this sample, you need a working installation of [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Maven](https://maven.apache.org).

### Step 3: Download the Sample application and modules

Next, clone the sample repo and install the project's dependencies.

From your shell or command line:

* `$ git clone https://github.com/Azure-Samples/active-directory-java-webapp-openidconnect.git`
* `$ cd active-directory-java-webapp-openidconnect`
* `$ mvn compile -DgroupId='com.microsoft.azure' -DartifactId=adal4jsample -DinteractiveMode=false`

### Step 4: Configure your web app using web.xml

Open `web.xml` in the webapp/WEB-INF/ folder. Fill in with your tenant and app registration information noted in registration step.
Replace 'YOUR_TENANT_NAME' with the tenant domain name, 'YOUR_CLIENT_ID' with the Application Id and 'YOUR_CLIENT_SECRET' with the key value noted.

### Step 5: Package and then deploy the adal4jsample.war file.

From your shell or command line, run the command:

* `$ mvn package`

This will generate a `adal4jsample.war` file in your /targets directory. Deploy this war file using Tomcat or any other J2EE container solution. To deploy on Tomcat container, copy the .war file to the webapps folder under your Tomcat installation and then start the Tomcat server.

This WAR will automatically be hosted at `http://<yourserverhost>:<yourserverport>/adal4jsample/`

Example: `http://localhost:8080/adal4jsample/`


### You're done!

Click on "Show users in the tenant" to start the process of logging in.

### Acknowledgements

We would like to acknowledge the folks who own/contribute to the following projects for their support of Azure Active Directory and their libraries that were used to build this sample. In places where we forked these libraries to add additional functionality, we ensured that the chain of forking remains intact so you can navigate back to the original package. Working with such great partners in the open source community clearly illustrates what open collaboration can accomplish. Thank you!
