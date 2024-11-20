This file explains how to set up and run the application

Java Version Used: java version "23.0.1"
Selenium version Used: 4.26.0
Apache Commons csv version used: 1.12.0
Chrome version 131 is used and its respective driver version.
The new Project is created using a Maven Project on eclipse, 
The pom.xml file was setup for the various dependencies, as seen in repo
The Java file that runs the code is present under: StockOutlier/src/main/java/Stock.java
The Chromedriver Path and the Sample Data Files are present in my Local as mentioned in the Code
Once the system is setup, if we run the Java file, we should be able to spot some outliers
(This may not happen in the first run as every time the code runs a random row is selected, so might need running a couple of times for the outliers file to be generated.)
The outliers file will be Generated in the same Project folder and will be visible in eclipse under the Project explorer view
There are a few sample outliers file that was created after running the code attached too: like that of FLTR LSE_outliers.csv and TSLA_outliers.csv
