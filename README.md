[![License](https://img.shields.io/:license-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/atbashEE/atbash-config.svg?branch=master)](https://travis-ci.org/atbashEE/atbash-config)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/be.atbash.config/atbash-config/badge.svg)](https://maven-badges.herokuapp.com/maven-central/be.atbash.config/atbash-config)

# atbash-config
Additional functionality on top of MicroProfile Config 1.x - 3.x

If you still need support for Java 7, please use version v0.9.3

## Atbash configuration extension

By adding the Atbash configuration Maven artifact, you add some additional features as described in the _introduction_ section.

This artifact can be used in combination with the Java 7 ported code but also works with any other MP Config 1.1 compliant implementation.

    <dependency>
        <groupId>be.atbash.config</groupId>
        <artifactId>atbash-config</artifactId>
        <version>${atbash.config.version}</version>
    </dependency>

The list of features is described in the _Atbash configuration features_ section.

When using The Atbash configuration extension with a 'real' implementation, it is advised to exclude the _be.atbash.config:microprofile-config-api_ since these classes are already present (through the dependency on the MP API from the 'real' configuration implementation)

    <dependency>
        <groupId>be.atbash.config</groupId>
        <artifactId>atbash-config</artifactId>
        <version>${atbash.config.version}</version>
        <exclusions>
            <exclusion>
                <groupId>be.atbash.config</groupId>
                <artifactId>microprofile-config-api</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

## User manual

See [here](https://github.com/atbashEE/atbash-config/blob/master/impl/src/main/doc/manual.pdf)