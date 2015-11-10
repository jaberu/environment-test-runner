# Environment Test Runner
Motivation is to allow to run unit and integration tests with environment specific settings.
Basically there is a lot of frameworks out there already supporting like Spring ... etc.

However I had the need to do an integration with no such framework available, why this project was created.

## Test integration
Integration into test is done by annotation. Environment-specific settings can be incuded by test class property. The property is marked with the @PropertyValue annotation. An example looks like that:

```java
@PropertyValue Anwendung
@RunWith(EnvironmentTestRunner.class)
@ResourceBundle("test")
public class MyTest {

    @PropertyValue("publication")
    private String publication;

    @PropertyValue("test.key")
    private String testKey;

	...
}
```
The annotation value is the name of the property what should be set. To be applied, the test needs to be executed by the EnvironmentTestRunner. Before executing the test, the runner will check the test class for all property with the @PropertyValue annotation and inject the values.
The values can be injected from different sources.
###ResourceBundles
One source us the classic java resource bundle. Therefore the @ResourceBundle annotation can be used at class level.
Those bundles can be created for different environments. For the example above the test looks in classpath for a file named ``/test.properties``.
If the test is running in a specific environment, also existence of ``/<environment>/test.properties`` checked. If such a file exists the properties from ``/test.properties`` are overriddn by those from ``/<environment>/
###System Properties
A second way for applying settings is the system properties from command line directly. This is useful for integrating the tests in jenkins for example. By this mechanism also the environment is given.

Take care, if you're using grade, that way is not working directly. By default gradle is not applying the jvm system properties to the gradle vm.
If you start your tests with gradle you have to extent your ``build.gradle`` by something like this:
###Gradle Konfiguration
```gradle
task unitTest( type: Test ) {
  systemProperties['publication'] = System.getProperty("publication")
  systemProperties['stage'] = System.getProperty("stage")
}
```
In the example only two properties are copied into the gradle vm. If you want more, you need to add them.