package main;

import java.util.ArrayList;
import java.util.List;

import database.application.DatabaseApplication;
import database.entity.AssignedIssue;
import database.entity.JiraIssue;
import database.entity.JiraProject;
import filter.AssigneeFilter;
import filter.custom.AnalyzedIssueFilter;
import filter.custom.MinimumIssueDescripionSizeFilter;
import filter.custom.MinimumIssueFilter;
import filter.custom.TimeStampsNotNullFilter;
import filter.custom.lists.SelectedIssuePriorityFilter;
import filter.custom.lists.SelectedIssueTypesFilter;
import inspection.ResultsInspection;
import jaccard.JaccardTextsSimilarity;
import jira.AssigneeIssueSimilarity;
import jira.AssigneeIssues;
import jira.AssigneeTimeResolve;
import jira.IssueResolveTimePredictable;
import jira.IssuesFilter;
import jira.IssuesSimilarity;
import jira.prediction.PredictionPrintable;
import jira.project.ProjectData;
import lucene.CosineTextsSimilarity;
import prediction.IssueResolveTimePredicter;
import printer.PredictionTextComposer;
import results.JiraIssueWithPredictedTimeToResolve;
import results.ResultInspectable;
import retriever.internet.IssueDownloaderMain;
import retriever.project.ProjectRetriever;
import similarity.IssuesSimilarityCalculator;
import utils.properties.PropertiesReader;
import utils.properties.Property;
import utils.time.ResolveTimeCalculator;

/**
 * Contains a set of methods that retrieve issues from Jira (for a given
 * project) and then calculate and display times needed to resolve new issue by
 * concrete developers.
 *
 */
public class PredictionModelViewer
{

	private PropertiesReader propertiesReader;
	private JiraIssue issueFromDb;
	private List<JiraIssue> issuesToVerify;
	private IssuesFilter issuesFilter;
	private IssuesSimilarity issuesSimilarity;
	private DatabaseApplication databaseApplication;
	private PredictionPrintable predictionPrintable;
	private IssueResolveTimePredictable issueResolveTimePredictable;
	private ResultInspectable resultInspectable;

	/**
	 * Creates a new instance of PredictionModelViewer and initializes
	 * propertiesReader variable.
	 * 
	 * @param propertiesReader
	 *            - properties reader, grants access to system configuration
	 *            variables
	 * @see PropertiesReader
	 */
	public PredictionModelViewer(PropertiesReader propertiesReader)
	{
		this.propertiesReader = propertiesReader;
		predictionPrintable = getPredictionPrinter();
	}

	/**
	 * Initializes all necessary variables used to retrieve issues and determine
	 * times needed to resolve new issue by concrete developers.
	 * 
	 * @see PropertiesReader
	 * @see DatabaseApplication
	 * @see JiraIssue
	 */
	public void init()
	{
		new IssueDownloaderMain(propertiesReader).retrieveIssuesFromProjectWithRespectToPropertyFlag(getProjectData(propertiesReader));
		databaseApplication = new DatabaseApplication(propertiesReader);
		issuesToVerify = getPercentageScopeOfJiraIssues();
	}

	/**
	 * Invokes all methods responsible for calculating and printing single issue
	 * prediction.
	 * 
	 * @see JiraIssue
	 * @see IssuesFilter
	 * @see IssuesSimilarity
	 * @see PredictionPrintable
	 * @see IssueResolveTimePredictable
	 * @see ResultInspectable
	 */
	public void calculateSinglePrediction()
	{
		issueFromDb = getJiraIssueFromDb(propertiesReader.getAsInt(Property.PROJECT_ID_JIRA_ISSUE_TO_ANALYZE));
		issuesFilter = getIssuesFilter();
		issuesSimilarity = getIssuesSimilarity();
		issueResolveTimePredictable = getIssueResolveTimePredictable();
		resultInspectable = new ResultsInspection();
		showPrediction();
	}

	/**
	 * Invokes all methods responsible for calculating ad printing prediction
	 * for a percentage scope of issues.
	 * 
	 * @see JiraIssue
	 * @see IssuesFilter
	 * @see IssuesSimilarity
	 * @see PredictionPrintable
	 * @see IssueResolveTimePredictable
	 * @see ResultInspectable
	 */
	public void calculateScopeOfPredictions()
	{
		for (JiraIssue issue : issuesToVerify)
		{
			issueFromDb = issue;
			issuesFilter = getIssuesFilter();
			issuesSimilarity = getIssuesSimilarity();
			issueResolveTimePredictable = getIssueResolveTimePredictable();
			resultInspectable = new ResultsInspection();
			showPrediction();
		}
	}

	private ProjectData getProjectData(PropertiesReader propertiesReader)
	{
		return new ProjectRetriever(propertiesReader).getProjectFromProperties();
	}

	private PredictionPrintable getPredictionPrinter()
	{
		return new PredictionTextComposer(propertiesReader);
	}

	private IssuesFilter getIssuesFilter()
	{
		AssigneeFilter assigneeFilter = new AssigneeFilter(propertiesReader, databaseApplication);
		assigneeFilter.init();
		return assigneeFilter.addFilter(new TimeStampsNotNullFilter()).addFilter(new MinimumIssueDescripionSizeFilter())
				.addFilter(new MinimumIssueFilter()).addFilter(new SelectedIssueTypesFilter()).addFilter(new SelectedIssuePriorityFilter())
				.addFilter(new AnalyzedIssueFilter());
	}

	private IssueResolveTimePredictable getIssueResolveTimePredictable()
	{
		return new IssueResolveTimePredicter();
	}

	private IssuesSimilarity getIssuesSimilarity()
	{
		return new IssuesSimilarityCalculator(propertiesReader, new JaccardTextsSimilarity(propertiesReader), new CosineTextsSimilarity());
	}

	private JiraIssue getJiraIssueFromDb(int jiraIssueId)
	{
		DatabaseApplication dba = new DatabaseApplication(propertiesReader);
		return dba.getJiraIssue(jiraIssueId);
	}

	private List<JiraIssue> getPercentageScopeOfJiraIssues()
	{
		JiraProject jiraProject = databaseApplication.getJiraProject(propertiesReader.getAsString(Property.PROJECT_NAME));
		int percentageScope = propertiesReader.getAsInt(Property.PERCENTAGE_SCOPE_OF_ISSUES);
		return databaseApplication.getPercentageScopeOfJiraIssues(jiraProject, percentageScope);
	}

	/**
	 * Calls methods that display prediction on the console. The output includes
	 * assignee, time needed to resolve an issue, mean squared error and real
	 * time, and assignee that resolved an issue.
	 * 
	 * @see PredictionPrintable
	 */
	public void showPrediction()
	{
		List<AssigneeIssues> assigneesAndTheirIssues = issuesFilter.getAssignedIssues(issueFromDb.getJiraProject());
		AssignedIssue assignedIssue = issueFromDb.getAssignedIssues().iterator().next();
		List<JiraIssueWithPredictedTimeToResolve> issues = new ArrayList<>();
		printNewPrediction();
		for (AssigneeIssues assigneeIssues : assigneesAndTheirIssues)
		{
			showPredictionForAssignee(assigneeIssues, assignedIssue, issues);
		}
		printStatistics(assignedIssue, issues);
	}

	private void printStatistics(AssignedIssue assignedIssue, List<JiraIssueWithPredictedTimeToResolve> issues)
	{
		printRealData(assignedIssue);
		printRootMeanSquaredError(issues);
		printCoefficientOfDetermination(issues);
		printEndPrediction();
	}

	private void printEndPrediction()
	{
		predictionPrintable.println("######## End prediction ########");
	}

	private void printNewPrediction()
	{
		predictionPrintable.println("######## New prediction ########");
	}

	private void printCoefficientOfDetermination(List<JiraIssueWithPredictedTimeToResolve> issues)
	{
		double coefficientOfDetermination = resultInspectable.getCoefficientOfDetermination(issues);
		predictionPrintable.println("Coefficient of determination: " + coefficientOfDetermination);
	}

	private void printRootMeanSquaredError(List<JiraIssueWithPredictedTimeToResolve> issues)
	{
		double rootMeanSquaredError = resultInspectable.getRootMeanSquaredError(issues);
		predictionPrintable.println("Root mean squared error: " + rootMeanSquaredError);
	}

	private void printRealData(AssignedIssue assignedIssue)
	{
		predictionPrintable.println("Real time: " + ResolveTimeCalculator.getResolveTime(assignedIssue));
		predictionPrintable.println("Real assignee: " + assignedIssue.getAssignee().getName());
	}

	private void showPredictionForAssignee(AssigneeIssues assigneeIssues, AssignedIssue assignedIssue,
			List<JiraIssueWithPredictedTimeToResolve> issues)
	{
		AssigneeIssueSimilarity assigneesWithIssueSimilarities = issuesSimilarity.getAssigneesWithIssueSimilarities(assigneeIssues,
				issueFromDb);
		AssigneeTimeResolve prediction = issueResolveTimePredictable.getPrediction(assigneesWithIssueSimilarities);
		JiraIssueWithPredictedTimeToResolve jiraIssueWithPredictedTime = new JiraIssueWithPredictedTimeToResolve(assignedIssue, prediction);
		issues.add(jiraIssueWithPredictedTime);
		double meanSquaredError = resultInspectable.getMeanSquaredError(jiraIssueWithPredictedTime);
		predictionPrintable.printPrediction(prediction, meanSquaredError);
	}

}
