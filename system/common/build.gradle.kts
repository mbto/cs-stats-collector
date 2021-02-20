import dev.bombinating.gradle.jooq.*
import org.jooq.meta.jaxb.SchemaMappingType

dependencies {
    jooqRuntime("mysql:mysql-connector-java:8.0.21")
}

plugins {
    // https://github.com/bombinating/jooq-gradle-plugin
    id("dev.bombinating.jooq-codegen") version "1.7.0"
}

jooq { // TODO: run task
    version = "3.11.10" //todo: if migrates to new spring-boot version, can use new code-generator version 3.13.+
    jdbc {
        driver = "com.mysql.cj.jdbc.Driver"
        url = "jdbc:mysql://127.0.0.1:3306/"
        username = "root"
        password = "root"
    }
    generator {
        database {
            name = "org.jooq.meta.mysql.MySQLDatabase"

            val collector = SchemaMappingType();
            collector.inputSchema = "collector"

            val csstats = SchemaMappingType();
            csstats.inputSchema = "csstats"

            schemata = listOf(collector, csstats)

            forcedTypes {
                forcedType {
                    name = "BOOLEAN"
                    types = "(?i:TINYINT UNSIGNED)"
                }
            }
        }
        target {
            directory = "$projectDir/src/main/java"
            packageName = "ru.csdm.stats.common.model"
            encoding = "UTF-8"
            isClean = true
        }
        generate {
            isDaos = false
            isRoutines = false
            isPojos = true
            isPojosEqualsAndHashCode = true
            isValidationAnnotations = true
            isJavaTimeTypes = true
        }
    }
}
// Auto-generation works before of compileJava, but I start the generation task manually
// tasks.getByName("compileJava").dependsOn(tasks.getByName("jooq"))