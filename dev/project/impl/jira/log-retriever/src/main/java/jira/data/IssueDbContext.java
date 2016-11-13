package jira.data;

import java.sql.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import database.entity.*;
import database.manager.DatabaseManager;

public class IssueDbContext {

	private DatabaseManager dbm;

	public IssueDbContext() {
		dbm = new DatabaseManager();
	}

	public void addNewJiraIssue(JiraIssue jiraIssue) {
		dbm.persist(jiraIssue);
	}

	public boolean addProjectIfNotExists(String projectName) {

		JiraProject jiraProject = new JiraProject();
		jiraProject.setProjectName(projectName);
		System.out.println(this.getJiraProject(projectName));
		if (this.getJiraProject(projectName) == null) {
			dbm.persist(jiraProject);
			return true;
		}
		return false;
	}

	public IssuePriority addIssuePriorityIfNotExists(String priorityName) {

		IssuePriority issuePriority = new IssuePriority();
		issuePriority.setPriorityName(priorityName);

		if (this.getIssuePriority(priorityName) == null) {
			dbm.persist(issuePriority);
			return issuePriority;
		}
		return null;
	}

	public IssueResolution addIssueResolutionIfNotExists(String resolutionName) {

		IssueResolution issueResulution = new IssueResolution();
		issueResulution.setResolutionName(resolutionName);

		if (this.getIssueResolution(resolutionName) == null) {
			dbm.persist(issueResulution);
			return issueResulution;
		}
		return null;
	}

	public IssueStatus addIssueStatusIfNotExists(String statusName) {

		IssueStatus issueStatus = new IssueStatus();
		issueStatus.setStatusName(statusName);

		if (this.getIssueStatus(statusName) == null) {
			dbm.persist(issueStatus);
			return issueStatus;
		}
		return null;
	}

	public IssueType addIssueTypeIfNotExists(String typeName) {

		IssueType issueType = new IssueType();
		issueType.setTypeName(typeName);

		if (this.getIssueType(typeName) == null) {
			dbm.persist(issueType);
			return issueType;
		}
		return null;
	}

	public Assignee addAssigneeIfNotExists(String assigneeName) {

		Assignee assignee = new Assignee();
		assignee.setName(assigneeName);

		if (this.getAssignee(assigneeName) == null) {
			dbm.persist(assignee);
			return assignee;
		}
		return null;
	}

	public IssueReporter addIssueReporterIfNotExists(String reporterName) {

		IssueReporter issueReporter = new IssueReporter();
		issueReporter.setFullName(reporterName);

		if (this.getIssueReporter(reporterName) == null) {
			dbm.persist(issueReporter);
			return issueReporter;
		}
		return null;
	}

	public IssueComment addIssueCommentIfNotExists(String content, Date addedAt, String addedBy,
			JiraIssue jiraIssueNew) {

		IssueComment issueComment = new IssueComment();
		issueComment.setContent(content);
		issueComment.setAddedAt(addedAt);
		issueComment.setAddedBy(addedBy);
		issueComment.setJiraIssue(jiraIssueNew);

		if (this.getCommentIssue(content, jiraIssueNew) == null) {
			dbm.persist(issueComment);
			return issueComment;
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Assignee getAssignee(String assigneeName) {
		Criteria criteria = dbm.getSession().createCriteria(Assignee.class);
		List assignees = criteria.add(Restrictions.eq("name", assigneeName)).list();
		if (assignees.size() >= 1) {
			return (Assignee) assignees.get(0);
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	public IssueComment getCommentIssue(String content, JiraIssue jiraIssue) {
		Criteria criteria = dbm.getSession().createCriteria(IssueComment.class);
		criteria.add(Restrictions.eq("content", content));
		criteria.add(Restrictions.eq("jiraIssueNew", jiraIssue));
		List comments = criteria.list();
		if (comments.size() >= 1) {
			return (IssueComment) comments.get(0);
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	public IssuePriority getIssuePriority(String priorityName) {
		Criteria criteria = dbm.getSession().createCriteria(IssuePriority.class);
		List priorities = criteria.add(Restrictions.eq("priorityName", priorityName)).list();
		if (priorities.size() >= 1) {
			return (IssuePriority) priorities.get(0);
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	public IssueReporter getIssueReporter(String reporterName) {
		Criteria criteria = dbm.getSession().createCriteria(IssueReporter.class);
		List reporters = criteria.add(Restrictions.eq("fullName", reporterName)).list();
		if (reporters.size() >= 1) {
			return (IssueReporter) reporters.get(0);
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	public IssueResolution getIssueResolution(String resolutionName) {
		Criteria criteria = dbm.getSession().createCriteria(IssueResolution.class);
		List resolutions = criteria.add(Restrictions.eq("resolutionName", resolutionName)).list();
		if (resolutions.size() >= 1) {
			return (IssueResolution) resolutions.get(0);
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	public IssueStatus getIssueStatus(String statusName) {
		Criteria criteria = dbm.getSession().createCriteria(IssueStatus.class);
		List statuses = criteria.add(Restrictions.eq("statusName", statusName)).list();
		if (statuses.size() >= 1) {
			return (IssueStatus) statuses.get(0);
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	public IssueType getIssueType(String typeName) {
		Criteria criteria = dbm.getSession().createCriteria(IssueType.class);
		List types = criteria.add(Restrictions.eq("typeName", typeName)).list();
		if (types.size() >= 1) {
			return (IssueType) types.get(0);
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	public JiraIssue getJiraIssue(String code, JiraProject project) {
		Criteria criteria = dbm.getSession().createCriteria(JiraIssue.class);
		criteria.add(Restrictions.eq("code", code));
		criteria.add(Restrictions.eq("jiraProject", project));
		List issues = criteria.list();
		if (issues.size() >= 1) {
			return (JiraIssue) issues.get(0);
		}

		return null;
	}

	@SuppressWarnings("rawtypes")
	public JiraProject getJiraProject(String projectName) {
		Criteria criteria = dbm.getSession().createCriteria(JiraProject.class);
		List projects = criteria.add(Restrictions.eq("projectName", projectName)).list();
		if (projects.size() >= 1) {
			System.out.println(projects.get(0));
			return (JiraProject) projects.get(0);
		}

		return null;
	}

	public void initDbm() {
		dbm.init();
	}

	public void closeDbm() {
		dbm.close();
	}

}
