gitlog-gradle-plugin
====================

Plugin to generate a simple log of changes based on tags

Usage
====================
```groovy
buildscript {
    repositories {
        maven { url 'http://nexus.malinskiy.com/content/groups/public/' }
    }
}

apply plugin 'gitlog'
```

```bash
./gradlew generateGitLog
```

Result is gitlog.html and gitlog.md files

Prerequisites
====================
Your git logs should conform to the following:
Each major release should be tagged to something like semantic versioning.

v1.0.0
v0.9.4
v0.9.3
v0.9.1
...


This will be expanded into

Commits that are after v1.0.0
v1.0.0
-------------
Commits that are after v0.9.4 but before v1.0.0
v0.9.4
-------------
Commits that are after v0.9.3 but before v0.9.4
v0.9.3
-------------
Commits that are after v0.9.1 but before v0.9.3
....


JIRA links (Optional)
====================
Define JIRA_ROOT and JIRA_PROJECT_KEY in your gradle.properties file in the root folder to replace
$KEY-$ISSUE_NUMBER with meaningful link
