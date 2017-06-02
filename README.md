# android-photo-helper

[![Build Status](https://ci.intrepid.io/buildStatus/icon?job=photo-helper-android)](https://ci.intrepid.io/job/photo-helper-android/)
[![Coverage](http://ci.intrepid.io:9913/jenkins/cobertura/hoto-helper-android/)](https://ci.intrepid.io/job/photo-helper-android/cobertura/)

This library wraps the default photo picker on Android with an easy-to-use API.
___
# Table of Contents

1. [Setup](#setup)
2. [Building](#building)
	1. [Onboarding](#onboarding)
	2. [Running](#running)
3. [Testing](#testing)
4. [Release](#release)
	1. [Quirks](#quirks)
	2. [Known Bugs](#known-bugs)
5. [Architecture](#architecture)
	1. [Data Flow](#data-flow)
	2. [Core Technology #1](#core-technology-1)
	3. [Core Technology #2](#core-technology-2)
	4. [Third Party Libraries](#third-party-libraries)
6. [History](#history)

___
# Setup
Add the following lines to your build.gradle file:
```
repositories {
    maven { url "http://sorcerer.intrepid-dev.com:81/archiva/repository/android/" }
}

dependencies {
    compile "io.intrepid.photohelper:photo-helper:0.1.0"
}
```

# Building
## Onboarding
This project doesn't require any other special configuration to run.

## Running
Cannot be run on its own at this time.
___

# Testing
Run unit tests.

# Release
Build it, sign it, :shipit:

## Quirks

## Known Bugs
No bugs. **QA Rules**
___

# Architecture
PhotoContract.java contains the View, Presenter, and PhotoHelper interfaces (for use with the MVP structural pattern).<br/>
Intended use model:

1. The developer will instantiate the PhotoHelper in their Fragment/Activity class, and then pass all arguments from the following methods to the corresponding PhotoHelper methods:

 - onActivityResult()
 - onCreate()
 - onRequestPermissionsResult()
 - onSaveInstanceState()

2. When the user is ready to display the photo picker, the Fragment/Activity should then call showImagePicker().

I created the Helper interface since this allows for slightly more abstraction (in case of different PhotoHelper implementations), but also makes it easier to read which methods you must override and call delegate-style from your Fragment/Activity.

## Data Flow
## Core Technology 1
## Core Technology 2
## Third Party Libraries
___

# History
Library initially developed by Matthew Groves on 2/10/2017.
<br>
0.1.0 - Initial version published to internal maven repo on 3/31/2017
