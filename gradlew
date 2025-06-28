#!/usr/bin/env sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

###############################################################################
##
##  Gradle start up script for UN*X
##
###############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if $cygwin ; then
    [ -n "$APP_HOME" ] &&
        APP_HOME=`cygpath --unix "$APP_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$CLASSPATH" ] &&
        CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# Attempt to find java
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if ! $cygwin && ! $darwin && ! $nonstop ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock;
# also pass JVM runtime options to the system property, along with other properties.
if $darwin ; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Dorg.gradle.appname=$APP_BASE_NAME\""
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin ; then
    APP_HOME=`cygpath --path --windows "$APP_HOME"`
    CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    APP_CLASSPATH=`cygpath --path --windows "$APP_CLASSPATH"`
    CYGWIN_HOME=`cygpath --path --windows "$CYGWIN_HOME"`
fi

# Split up the JVM options found in GRADLE_OPTS
JVM_OPTS=("$DEFAULT_JVM_OPTS")
eval " pasó=($GRADLE_OPTS)"
JVM_OPTS=(${JVM_OPTS[*]} ${pasó[*]})
JVM_OPTS=(${JVM_OPTS[*]})
# Add GRADLE_OPTS to JAVA_OPTS to make sure this is passed to the exec
# code specifically for compiled Groovy code.
JAVA_OPTS=(${JAVA_OPTS[*]} ${pasó[*]})
JAVA_OPTS=(${JAVA_OPTS[*]})


# Set up the command line
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Preserve quoting of arguments
set -- "$@"

# Execute Gradle
# shellcheck disable=SC2086
exec "$JAVACMD" "${JVM_OPTS[@]}" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"

# Since this script is intended to be run for a long time, it's a good
# idea to disown it from the shell, so it doesn't get killed when the
# shell exits. This is especially important when this script is run
# from a terminal that is closed after the script is started.
#
# The following code does this, but only if the script is run from a
# terminal. If the script is run from a cron job, or some other

# non-interactive context, then it's not necessary to disown it.
#
# A common way to check if the script is run from a terminal is to
# check if the STDIN file descriptor is a terminal.
if [ -t 0 ] ; then
  # If STDIN is a terminal, then we are running from a terminal, so
  # disown the process.
  #
  # The `disown` command is a shell builtin, so it's not available in
  # all shells. If it's not available, then we just ignore the error.
  disown >/dev/null 2>&1
fi

# If the script is run from a terminal, then it's a good idea to also
# redirect STDOUT and STDERR to /dev/null, so that the script doesn't
# print anything to the terminal. This is especially important if the
# script is run from a terminal that is closed after the script is
# started.
#
# The following code does this, but only if the script is run from a
# terminal. If the script is run from a cron job, or some other
# non-interactive context, then it's not necessary to redirect STDOUT
# and STDERR.
if [ -t 0 ] ; then
  # If STDIN is a terminal, then we are running from a terminal, so
  # redirect STDOUT and STDERR to /dev/null.
  exec >/dev/null 2>&1
fi
