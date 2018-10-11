mayu-text
=========

Text analysis capabilities.

To build:

mvn clean install

To release:

1. change version in pom to released version
2. mvn -Dmaven.test.skip=true -DAWS_DEFAULT_REGION=us-west-2 deploy

Note: you will need to have proper maven creds stored in your ~/.m2/settings.xml file.

3. change version in pom to released version, e.g. x.x.x+1-SNAPSHOT
4. check in new pom file

