# android-photo-helper

 <!-- Replace the 2 instances of "ios-template" in each of the links below with
      the name of your project in Jenkins. -->
[![Build Status](http://ci.intrepid.io:8080/buildStatus/icon?job=android-photo-helper)](http://ci.intrepid.io:8080/job/android-photo-helper/)
[![Coverage](http://ci.intrepid.io:9913/jenkins/cobertura/android-photo-helper/)](http://ci.intrepid.io:8080/job/android-photo-helper/cobertura/)

This library wraps the default photo picker on Android with an easy-to-use API.
___
# Table of Contents

1. [Building](#building)
	2. [Onboarding](#onboarding)
	3. [Running](#running)
2. [Testing](#testing)
3. [Release](#release)
	4. [Quirks](#quirks)
	5. [Known Bugs](#known-bugs)
6. [Architecture](#architecture)
	7. [Data Flow](#data-flow)
	8. [Core Technology #1](#core-technology-1)
	9. [Core Technology #2](#core-technology-2)
	8. [Third Party Libraries](#third-party-libraries)
9. [History](#history)

___

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
Intended use model:<br/>
1. The developer will instantiate the PhotoHelper in their Fragment class, and then pass all arguments from the following methods to the corresponding PhotoHelper methods:
 - onActivityResult()
 - onCreate()
 - onRequestPermissionsResult()
 - onSaveInstanceState()<br/>


2. When the user is ready to display the photo picker, the Fragment should then call showImagePicker().

I created the Helper interface since this allows for slightly more abstraction (in case of different PhotoHelper implementations), but also makes it easier to read which methods you must override and call delegate-style from your Fragment.

## Data Flow
## Core Technology 1
## Core Technology 2
## Third Party Libraries
___

# History
Library initially developed by Matthew Groves on 2/10/2017.
