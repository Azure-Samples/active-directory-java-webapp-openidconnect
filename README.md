---
services: active-directory
platforms: java
author: brandwe
---

# Integrating Azure AD into a Java web application

This Java app will give you with a quick and easy way to set up a Web application in Java using OAuth2. The sample included in the download is designed to run on any platform and tested with Java 8.

We've released all of the source code for this example in GitHub under an MIT license, so feel free to clone (or even better, fork!) and provide feedback on the forums.


## Quick Start

Getting started with the sample is easy. It is configured to run out of the box with minimal setup.

### Step 1: Register an Azure AD Tenant

To use this sample you will need a Azure Active Directory Tenant. If you're not sure what a tenant is or how you would get one, read [What is an Azure AD tenant](http://technet.microsoft.com/library/jj573650.aspx)? or [Sign up for Azure as an organization](http://azure.microsoft.com/documentation/articles/sign-up-organization/). These docs should get you started on your way to using Azure AD.

### Step 2: Download Java (7 and above) for your platform 

To successfully use this sample, you need a working installation of [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Maven](https://maven.apache.org).

### Step 3: Download the Sample application and modules

Next, clone the sample repo and install the project's dependencies.

From your shell or command line:

* `$ git clone https://github.com/Azure-Samples/active-directory-java-webapp-openidconnect.git`
* `$ cd active-directory-java-webapp-openidconnect`
* `$ mvn compile -DgroupId=com.microsoft.azure -DartifactId=adal4jsample -DinteractiveMode=false`

### Step 5: Configure your web app using web.xml

Provided for simplicity, `web.xml` in the webapp/WEB-INF/ folder contains values for you to fill in with your tenant information.

### Step 6: Package and then deploy the adal4jsample.war file.

From your shell or command line:

* `$ mvn package`

This will generate a `adal4jsample.war` file in your /targets directory. Deploy this war file using Tomcat or any other J2EE container solution. This WAR will automatically be hosted at `http://<yourserverhost>:<yourserverport>/adal4jsample/`

Example: `http://localhost:8080/adal4jsample/`


### You're done!

Click on "Sign-in" to start the process of logging in.

### Acknowledgements

We would like to acknowledge the folks who own/contribute to the following projects for their support of Azure Active Directory and their libraries that were used to build this sample. In places where we forked these libraries to add additional functionality, we ensured that the chain of forking remains intact so you can navigate back to the original package. Working with such great partners in the open source community clearly illustrates what open collaboration can accomplish. Thank you!


