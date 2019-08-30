![mongobee](https://raw.githubusercontent.com/mongobee/mongobee/master/misc/mongobee_min.png)

[![Build Status](https://travis-ci.org/mongobee/mongobee.svg?branch=master)](https://travis-ci.org/mongobee/mongobee) [![Coverity Scan Build Status](https://scan.coverity.com/projects/2721/badge.svg)](https://scan.coverity.com/projects/2721) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.mongobee/mongobee/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mongobee/mongobee) [![Licence](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/mongobee/mongobee/blob/master/LICENSE)
---


**mongobee** is a Java tool which helps you to *manage changes* in your MongoDB and *synchronize* them with your application.
The concept is very similar to other db migration tools such as [Liquibase](http://www.liquibase.org) or [Flyway](http://flywaydb.org) but *without using XML/JSON/YML files*.

The goal is to keep this tool simple and comfortable to use.


**mongobee** provides new approach for adding changes (change sets) based on Java classes and methods with appropriate annotations.

## Getting started

### Add a dependency

With Maven
```xml
<dependency>
  <groupId>com.github.mongobee</groupId>
  <artifactId>mongobee-philips</artifactId>
  <version>0.15</version>
</dependency>
```
With Gradle
```groovy
compile 'com.github.mongobee:mongobee:0.5'
```

### Usage 

```java
Mongobee runner = new Mongobee("mongodb://YOUR_DB_HOST:27017/DB_NAME");
runner.setDbName("yourDbName");         // host must be set if not set in URI
runner.setChangeLogsScanPackage(
     "com.example.yourapp.changelogs"); // package to scan for changesets

runner.execute();         //  ------> starts migration changesets
```

Above examples provide minimal configuration. `Mongobee` object provides some other possibilities (setters) to make the tool more flexible:

```java
runner.setChangelogCollectionName(logColName);   // default is dbchangelog, collection with applied change sets
runner.setLockCollectionName(lockColName);       // default is mongobeelock, collection used during migration process
runner.setEnabled(shouldBeEnabled);              // default is true, migration won't start if set to false
```

### Creating change logs

`ChangeLog` contains bunch of `ChangeSet`s. `ChangeSet` is a single task (set of instructions made on a database). In other words `ChangeLog` is a class annotated with `@ChangeLog` and containing methods annotated with `@ChangeSet`.

```java 
package com.example.yourapp.changelogs;

@ChangeLog
public class DatabaseChangelog {
  
  @ChangeSet(order = "001", id = "someChangeId", author = "testAuthor")
  public void importantWorkToDo(DB db){
     // task implementation
  }


}
```
#### @ChangeLog

Class with change sets must be annotated by `@ChangeLog`. There can be more than one change log class but in that case `order` argument should be provided:

```java
@ChangeLog(order = "001")
public class DatabaseChangelog {
  //...
}
```
ChangeLogs are sorted alphabetically by `order` argument and changesets are applied due to this order.

#### @ChangeSet

Method annotated by @ChangeSet is taken and applied to the database. History of applied change sets is stored in a collection called `dbchangelog` (by default) in your MongoDB

##### Annotation parameters:

`order` - string for sorting change sets in one changelog. Sorting in alphabetical order, ascending. It can be a number, a date etc.

`id` - name of a change set, **must be unique** for all change logs in a database

`author` - author of a change set

`runAlways` - _[optional, default: false]_ changeset will always be executed but only first execution event will be stored in dbchangelog collection

##### Defining ChangeSet methods
Method annotated by `@ChangeSet` can have one of the following definition:

```java
@ChangeSet(order = "001", id = "someChangeWithoutArgs", author = "testAuthor")
public void someChange1() {
   // method without arguments can do some non-db changes
}

@ChangeSet(order = "002", id = "someChangeWithMongoDatabase", author = "testAuthor")
public void someChange2(MongoDatabase db) {
  // type: com.mongodb.client.MongoDatabase : original MongoDB driver v. 3.x, operations allowed by driver are possible
  // example: 
  MongoCollection<Document> mycollection = db.getCollection("mycollection");
  Document doc = new Document("testName", "example").append("test", "1");
  mycollection.insertOne(doc);
}

```