# thesallab.configuration
## Introduction
thesallab.configuration is used by various projects produced by the Sal Lab, e.g., PPCode3.
## How to build
    mvn clean package install
## How to use
Add the following jvm parameters:

    -Dcx.config.file=/path/to/your/configuration/file
On Windows:

    -Dcx.config.file=X:/path/to/your/configuration/file