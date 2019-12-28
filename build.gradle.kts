//buildscript {
//    repositories {
//        jcenter()
//    }
//}

plugins {
    kotlin("jvm") version "1.3.61"
//    id("io.gitlab.arturbosch.detekt") version "1.2.2"
}

group = "com.porsche.ecom"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
//    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.amazonaws:aws-lambda-java-core:1.2.0")
    implementation("com.amazonaws:aws-lambda-java-events:2.2.7")
    implementation("software.amazon.awssdk:sns:2.10.35")
    implementation(platform("software.amazon.awssdk:bom:2.5.29"))
    implementation("software.amazon.awssdk:s3")
    implementation("org.apache.poi:poi:4.1.1")
    implementation("com.sun.mail:javax.mail:1.6.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.10.1")

    testImplementation("org.mockito:mockito-all:2.0.2-beta")
    testImplementation("junit:junit:4.12")
}

tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

val fatJar = task("fatJar", type = Jar::class) {
    archiveBaseName.set("${project.name}-fat")
    manifest {
        attributes["Main-Class"] = "com.porsche.ecom.retoure.LambdaApp"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks.build {
    dependsOn(fatJar)
}
