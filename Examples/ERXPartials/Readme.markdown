# ERXPartials Test / Example

## Getting Started

1. create the database
	- Change into the ERXPartialBaseModel directory

	> _GVCDev:ERXPartialBaseModel daspinall$ __ant fb.recreate___
	>
	> Buildfile: /Volumes/Data/Development/GitHub/ERXPartials/wonder/Tests/ERXPartials/ERXPartialBaseModel/build.xml
	> 
	> init.properties:
	> 
	>    fb.recreate:
	>      [exec] connect to ERXPartials user _system;
	>      [exec] Cannot connect to ERXPartials@localhost
	>      [exec] Database is not running
	>      [exec] stop database;
	>      [exec] No current session.
	>      [exec] delete database ERXPartials;
	>      [exec] Cannot delete database ERXPartials@localhost;
	>      [exec] Reason: Database is unknown
	>      [exec] create database ERXPartials;
	>      [exec] connect to ERXPartials user _system;
	>      [exec] Auto committing is on: SET COMMIT TRUE;
	>      [exec] create user erxpartial;
	>      [exec] set password test user erxpartial;
	>      [exec] create schema erxpartial authorization erxpartial;
	>      [exec] disconnect all;
	>      [exec] 
	>
	> BUILD SUCCESSFUL
	>
	> Total time: 1 second
	>
	> _GVCDev:ERXPartialBaseModel daspinall$_

2. compile


