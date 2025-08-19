### To compile and package a JAR file using maven run:
```
mvn compile
mvn package
```

### To rebuild the project run:
```
mvn clean package
```

### To start the project run:
```
java -jar .\target\json-formatter-v02.jar
```
Or double click the generated `json-formatter-v02.jar` file

----------

if using `.jar` dependencies needed to compile and run the project add the following code to the **pom.xml** file:
```xml
<plugins>
    <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.6.0</version>
    <executions>
        <execution>
        <phase>package</phase>
        <goals>
            <goal>shade</goal>
        </goals>
        <configuration>
            <transformers>
            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                <mainClass>json.formatter.app.App</mainClass>
            </transformer>
            </transformers>
        </configuration>
        </execution>
    </executions>
    </plugin>
</plugins>
```

If the error `no main manifest attribute, in .\target\json-formatter-v02.jar` is returned replace the `maven-jar-plugin` code inside the **pom.xml** file with the following code:
```xml
<plugin>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.4.2</version>
    <configuration>
        <archive>
            <manifest>
                <addClasspath>true</addClasspath>
                <mainClass>json.formatter.app.App</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```