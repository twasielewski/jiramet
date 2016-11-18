CREATE DATABASE JIRA;

USE JIRA;

CREATE TABLE ISSUE_REPORTER(
ISSUE_REPORTER_ID INT( 10 ) AUTO_INCREMENT PRIMARY KEY NOT NULL,
FULL_NAME VARCHAR( 150 ) NOT NULL
);

CREATE TABLE ISSUE_TYPE(
ISSUE_TYPE_ID INT( 10 ) AUTO_INCREMENT PRIMARY KEY NOT NULL,
TYPE_NAME VARCHAR( 150 ) UNIQUE NOT NULL
);

CREATE TABLE ISSUE_RESOLUTION(
ISSUE_RESOLUTION_ID INTEGER( 10 ) AUTO_INCREMENT PRIMARY KEY NOT NULL ,
RESOLUTION_NAME VARCHAR( 150 ) UNIQUE NOT NULL
);

CREATE TABLE ISSUE_PRIORITY(
ISSUE_PRIORITY_ID INT( 10 ) AUTO_INCREMENT PRIMARY KEY NOT NULL ,
PRIORITY_NAME VARCHAR( 150 ) UNIQUE NOT NULL
);

CREATE TABLE JIRA_PROJECT(
JIRA_PROJECT_ID INT( 10 ) AUTO_INCREMENT PRIMARY KEY NOT NULL ,
PROJECT_NAME VARCHAR( 150 ) NOT NULL
);

CREATE TABLE JIRA_ISSUE (
    JIRA_ISSUE_ID INT(10) AUTO_INCREMENT PRIMARY KEY NOT NULL,
    PROJECT_ID INTEGER(10),
    ISSUE_REPORTER_ID INTEGER(10),
    ISSUE_PRIORITY_ID INTEGER(10),
    ISSUE_TYPE_ID INTEGER(10),
    ISSUE_RESOLUTION_ID INTEGER(10),
    CODE VARCHAR(50) NOT NULL,
    ISSUE_STATUS VARCHAR(20),
    CREATED_AT TIMESTAMP NULL DEFAULT NULL,
    FIRST_RESPONSE_DATE TIMESTAMP NULL DEFAULT NULL,
    DESCRIPTION TEXT,
    PURPOSE VARCHAR(1) NOT NULL DEFAULT 'T',
    FOREIGN KEY (PROJECT_ID) REFERENCES JIRA_PROJECT(JIRA_PROJECT_ID),
    FOREIGN KEY (ISSUE_REPORTER_ID) REFERENCES ISSUE_REPORTER(ISSUE_REPORTER_ID),
    FOREIGN KEY (ISSUE_PRIORITY_ID) REFERENCES ISSUE_PRIORITY(ISSUE_PRIORITY_ID),
    FOREIGN KEY (ISSUE_TYPE_ID) REFERENCES ISSUE_TYPE(ISSUE_TYPE_ID),
    FOREIGN KEY (ISSUE_RESOLUTION_ID) REFERENCES ISSUE_RESOLUTION(ISSUE_RESOLUTION_ID)
);

CREATE TABLE ISSUE_COMMENT (
ISSUE_COMMENT_ID INT(10) AUTO_INCREMENT PRIMARY KEY NOT NULL,
JIRA_ISSUE_ID INT(10),
CONTENT VARCHAR(500),
ADDED_AT TIMESTAMP NULL DEFAULT NULL,
ADDED_BY VARCHAR(150),
FOREIGN KEY (JIRA_ISSUE_ID) REFERENCES JIRA_ISSUE(JIRA_ISSUE_ID)
);


CREATE TABLE ASSIGNEE(
ASSIGNEE_ID INT( 10 ) AUTO_INCREMENT PRIMARY KEY NOT NULL ,
NAME VARCHAR( 150 ) NOT NULL
);

CREATE TABLE ASSIGNED_ISSUE(
ASSIGNED_ISSUE_ID INT( 10 ) AUTO_INCREMENT PRIMARY KEY NOT NULL ,
JIRA_ISSUE_ID INT( 10 ) NOT NULL,
ASSIGNEE_ID INT( 10 ) NOT NULL,
RESOLVED_AT TIMESTAMP NULL DEFAULT NULL,
FOREIGN KEY ( JIRA_ISSUE_ID ) REFERENCES JIRA_ISSUE( JIRA_ISSUE_ID ) ,
FOREIGN KEY ( ASSIGNEE_ID ) REFERENCES ASSIGNEE( ASSIGNEE_ID ) 
);
