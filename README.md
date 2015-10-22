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

### Step 2: Download node.js for your platform

To successfully use this sample, you need a working installation of [Node.js](https://nodejs.org/).

### Step 3: Download the Sample application and modules

Next, clone the sample repo and install the project's NPM dependencies.

From your shell or command line:

* `$ git clone git@github.com:Azure-Samples/active-directory-java-webapi-headless.git`
* `$ cd active-directory-java-webapi-headless`
* `$ mvn archetype:generate -DgroupId=com.microsoft.azure -DartifactId=adal4jsample -DinteractiveMode=false`

### Step 5: Configure your web app using constants.java

Provided for simplicity, `constants.java` contains some simple variables. In a 
real application, it would be best to use environment variables or [Azure Key Vault](https://azure.microsoft.com/en-us/services/key-vault/) 
to store credentials securely, outside of your source control system.

### Step 6: Run the application

* `$ java adal4jsample.jar`


### You're done!

You will have a server successfully running on `http://localhost:3000`.

### Acknowledgements

We would like to acknowledge the folks who own/contribute to the following projects for their support of Azure Active Directory and their libraries that were used to build this sample. In places where we forked these libraries to add additional functionality, we ensured that the chain of forking remains intact so you can navigate back to the original package. Working with such great partners in the open source community clearly illustrates what open collaboration can accomplish. Thank you!

