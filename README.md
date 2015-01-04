# Some basic S3 Tools

I have a client's S3 API cred.s, but no AWS login, I need to set some S3 bucket
prop.s.. CORS stuff. Client isn't about.

This should be a quick tool to setup some rules.

## Building

    gradle build
    gradle jar
    gradle fatJar

## Running (via gradle)

    gradle run -PappArgs="..."

## Running (from jar)

    java -jar build/libs/S3Tools-fat-0.2.jar ...
