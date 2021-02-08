import org.gradle.api.JavaVersion.VERSION_1_8

group = "cs-stats-collector"
version = "1.5"

val springVersion: String by extra

plugins {
    java
    id("org.springframework.boot") version "2.1.12.RELEASE"
//    id("io.spring.dependency-management") version "1.0.10.RELEASE"
}

subprojects {
    apply(plugin = "java")
}

tasks {
    // omit version in output jar name
    bootJar {
        archiveVersion.set("")
        exclude("application.properties")
        exclude("application-dev.properties")
    }
    // ensure encoding for non-linux environment
    compileJava { options.encoding = "UTF-8" }
    compileTestJava { options.encoding = "UTF-8" }
}

allprojects {
    extra["springVersion"] = "2.1.12.RELEASE"
    extra["joinFacesVersion"] = "4.0.12"

    repositories {
        mavenCentral()
        maven {
            setUrl("https://repository.primefaces.org")
        }
    }

// Правильные версии всех зависимостей узнавать тут:
// http://search.maven.org/classic/remotecontent?filepath=org/springframework/boot/spring-boot-dependencies/2.1.12.RELEASE/spring-boot-dependencies-2.1.12.RELEASE.pom
    dependencies {
        val lombokVer = "1.18.6"
        compileOnly("org.projectlombok:lombok:$lombokVer");
        annotationProcessor("org.projectlombok:lombok:$lombokVer");
        testCompile("org.projectlombok:lombok:$lombokVer");
        testAnnotationProcessor("org.projectlombok:lombok:$lombokVer");

//        compile("org.apache.commons:commons-math3:3.6.1")
//        compile("org.apache.commons:commons-collections4:4.3")
//        compile("commons-codec:commons-codec:1.11")
        compile("org.apache.commons:commons-lang3:3.8.1")
//        compile("commons-io:commons-io:2.6")

//        compile("org.springframework.boot:spring-boot-actuator:$springVersion")
        compile("org.springframework.boot:spring-boot-starter-security:$springVersion")
        compile("org.springframework.boot:spring-boot-starter-jooq:$springVersion")
        compile("org.springframework.boot:spring-boot-starter-cache:$springVersion")
        compile("mysql:mysql-connector-java:8.0.21")
//        compile("com.github.ben-manes.caffeine:caffeine:2.6.2")

        compile("org.springframework.boot:spring-boot-starter-web:$springVersion")
    }
    configure<JavaPluginConvention> {
        sourceCompatibility = VERSION_1_8
    }
}

val developmentOnly = configurations.create("developmentOnly")
configurations.runtimeClasspath.get().extendsFrom(developmentOnly)

dependencies {
//    compile("io.hawt:hawtio-springboot:2.6.0") // compatible with spring boot 2.1.12.RELEASE
    developmentOnly("org.springframework.boot:spring-boot-devtools:$springVersion")

    subprojects.forEach {
        compile(project(":${it.name}"))
    }

    /* test */
//    testCompile("org.apache.commons:commons-collections4:4.3")
    testCompile("org.springframework.boot:spring-boot-test:$springVersion")
    testCompile("org.springframework.boot:spring-boot-test-autoconfigure:$springVersion")
}