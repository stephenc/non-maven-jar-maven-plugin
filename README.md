Non-Maven Jar Maven Plugin
==========================

There is a rare situation that this plugin aims to address. Here is the use case:

  * You have a dependency on a third-party JAR
  * You cannot convince the authors of that third-party JAR to publish the JAR file to a Maven repository
    (either an internal repository if the authors are within your organization or a repository like
    [Central](http://repo.maven.apache.org/maven2/) where they are outside your organization)
  * You are unwilling to either have an internal Maven repository or take part in the process for uploading
    their dependencies to a repository like [Central](http://repo.maven.apache.org/maven2/)

Now good citizens would solve this issue by changing their stance to the last point in that use-case, but if you wish
to insist on being a bad citizen, the Non-Maven Jar Maven Plugin is here to help you get your build done...

- - -

__BY USING THIS PLUGIN YOU ACKNOWLEDGE THAT YOU ARE A BAD CITIZEN OF THE MAVEN ECOSYSTEM.__

- - -

How to use this plugin
----------------------

Firstly, you will be using a multi-module build. For each non-maven dependency you will need a `pom.xml` file and a
directory to hold that file. In that directory you can either have a `src` sub-directory and put the `.jar` file in
that `src` directory, or you can type a little more in your `pom.xml` and put the `.jar` file beside the `pom.xml`.
So the directory tree could look something like

    pom.xml
    dep1/
        pom.xml
        src/
            dep1.jar
    dep2/
        pom.xml
        dep2.jar
    main/
        pom.xml
        src/
            main/
                java/
                    ...

If all your `pom.xml` files inherit from the root `pom.xml` (this is the simplest for you) then you will need to
add the following to your root `pom`

    <project>
        ...
        <build>
            ...
            <plugins>
                ...
                <plugin>
                    <groupId>com.github.stephenc.nonmavenjar</groupId>
                    <artifactId>non-maven-jar-maven-plugin</artifactId>
                    ...
                    <extensions>true</extensions>
                    ...
                </plugin>
                ...
            </plugins>
            ...
        </build>
        ...
    </project>

So that Maven is aware of the `non-maven-jar` packaging type. If you don't or cannot add that to your root `pom.xml`
(for example in the case where the child `pom.xml` files do not use the root `pom.xml` as their parent) then
you will need to flag the extension in each of the child `pom.xml` files that have
`<packaging>non-maven-jar</packaging>`

Continuing the above example, the `dep1/pom.xml` would look something like this:

    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <parent>
            <groupId>com.bar.foo</groupId>
            <artifactId>manchu-parent</artifactId>
            <version>1.0-SNAPSHOT</version>
        </parent>

        <artifactId>manchu-dep1</artifactId>
        <packaging>non-maven-jar</packaging>

    </project>

Where the auto-magic will be used to pick up the one and only JAR file from `dep1/src` and bind that into the reactor.

The `dep2/pom.xml` needs some configuration to specify where the JAR file is, as it is not using the defaults, so
would look something like this:

    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <parent>
            <groupId>com.bar.foo</groupId>
            <artifactId>manchu-parent</artifactId>
            <version>1.0-SNAPSHOT</version>
        </parent>

        <artifactId>manchu-dep2</artifactId>
        <packaging>non-maven-jar</packaging>

        <dependencies>
            <!-- this needs dep1 as a transitive dependency and we are trying not to be completely bad citizens -->
            <dependency>
                <groupId>com.bar.foo</groupId>
                <artifactId>manchu-dep1</artifactId>
                <version>1.0-SNAPSHOT</version>
            </dependency>
        </dependencies>

        <build>
            <plugins>
                <plugin>
                    <groupId>com.github.stephenc.nonmavenjar</groupId>
                    <artifactId>non-maven-jar-maven-plugin</artifactId>
                    <configuration>
                        <jarFile>${basedir}/dep2.jar</jarFile>
                    </configuration>
                </plugin>
            </plugins>
        </build>

    </project>

And finally the `main/pom.xml` can just list the dependencies as if they were ordinary dependencies:

    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <parent>
            <groupId>com.bar.foo</groupId>
            <artifactId>manchu-parent</artifactId>
            <version>1.0-SNAPSHOT</version>
        </parent>

        <artifactId>manchu-main</artifactId>

        <dependencies>
            <dependency>
                <groupId>com.bar.foo</groupId>
                <artifactId>manchu-dep2</artifactId>
                <version>1.0-SNAPSHOT</version>
            </dependency>
        </dependencies>

    </project>


Rational for releasing this plugin
----------------------------------

While this is a bad way to do things, it is less evil than the other hacks such as abusing `system` scope or
adding a `<repository>` which is pointing to a `file:///` URI. Also if used correctly this plugin can make
transitioning from a bad citizen to a good citizen a lot easier as one needs only remove the
`<packaging>non-maven-jar</packaging>` and submit the JAR and `pom.xml` as part of an upload bundle for
[Central](http://repo.maven.apache.org/maven2/) (in other words those upload bundles are less work than you fear)

[![Build Status](https://buildhive.cloudbees.com/job/stephenc/job/non-maven-jar-maven-plugin/badge/icon)](https://buildhive.cloudbees.com/job/stephenc/job/non-maven-jar-maven-plugin/)
