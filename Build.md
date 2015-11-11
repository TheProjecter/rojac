# Build from sources #

For whom do not want to wait downloadable distributive.

## Requirements ##
  1. [JDK 1.6](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  1. [Ant 1.7 or higher](http://ant.apache.org/bindownload.cgi)
  1. Command-line [subversion](http://subversion.apache.org/packages.html) client

## Instruction ##

  * Check out latest sources from SVN repository into a new empty folder: `svn co http://rojac.googlecode.com/svn/trunk/app/ c:\rojac`
  * Go into the folder (c:\rojac) and execute command `ant make-product`
  * After successful building you can found generated distributive in a created dist folder.
  * Enjoy.