plugins {
    `java-library`
    `maven-publish`
    jacoco
}

group = "com.didww"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    api("com.github.jasminb:jsonapi-converter:0.14")
    api("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.wiremock:wiremock:3.5.4")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val examples by sourceSets.creating {
    java.srcDir("examples/src/main/java")
    compileClasspath += sourceSets["main"].output + configurations["runtimeClasspath"]
    runtimeClasspath += output + compileClasspath
}

configurations[examples.implementationConfigurationName].extendsFrom(configurations["implementation"])
configurations[examples.runtimeOnlyConfigurationName].extendsFrom(configurations["runtimeOnly"])

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.register<JavaExec>("runExample") {
    group = "application"
    description = "Run an example class. Usage: ./gradlew runExample -PexampleClass=com.didww.examples.BalanceExample"
    classpath = examples.runtimeClasspath

    val exampleClass = providers.gradleProperty("exampleClass")
    doFirst {
        if (!exampleClass.isPresent) {
            throw GradleException("Please provide -PexampleClass=com.didww.examples.<ClassName>")
        }
        mainClass.set(exampleClass.get())
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("DIDWW API Java SDK")
                description.set("Official Java SDK for DIDWW API v3")
                url.set("https://github.com/didww/didww-api-3-java-sdk")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
            }
        }
    }
}
