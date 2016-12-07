package similarity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import database.application.DatabaseApplication;
import database.entity.JiraIssue;
import jira.AssigneeIssueSimilarity;
import jira.AssigneeIssues;
import jira.IssuesSimilarity;
import jira.JiraIssueSimilarity;
import lucene.CosineTextsSimilarity;
import similarity.exceptions.SimilarityRangeException;
import utils.properties.PropertiesReader;
import utils.properties.Property;

public class IssuesSimilarityCalculator implements IssuesSimilarity, TextsSimilarity
{
	private DatabaseApplication dba;
	private IssuesSimilarityHelper ish;
	private CosineTextsSimilarity cts;
	private PropertiesReader propertiesReader;
	private Logger logger;

	public IssuesSimilarityCalculator(PropertiesReader propertiesReader)
	{
		this.propertiesReader = propertiesReader;
		this.dba = new DatabaseApplication(propertiesReader);
		this.ish = new IssuesSimilarityHelper();
	}
	
	@Override
	public List<AssigneeIssueSimilarity> getIssuesSimilarityList(JiraIssue jiraIssue, List<AssigneeIssues> assigneeIssues)
	{
		List<AssigneeIssueSimilarity> assigneeSimilarityList = new ArrayList<AssigneeIssueSimilarity>();
		List<JiraIssueSimilarity> jiraIssueSimilarities = new ArrayList<JiraIssueSimilarity>();

		for(AssigneeIssues asi : assigneeIssues)
		{
			for(JiraIssue issue : asi.getAssignedJiraIssues())
			{
				if (issue.getJiraIssueId() != jiraIssue.getJiraIssueId())
					jiraIssueSimilarities.add(new JiraIssueSimilarity(issue, getIssuesSimilarity(jiraIssue, issue)));
			}
			assigneeSimilarityList.add(new AssigneeIssueSimilarity(asi.getAssignee(), jiraIssueSimilarities));
			jiraIssueSimilarities = null;
		}
		dba.closeSession();
		
		return assigneeSimilarityList;
	}

	public double getIssuesSimilarity(JiraIssue issue1, JiraIssue issue2)
	{
		return propertiesReader.getAsDouble(Property.SUMMARY_WEIGHT) * getSimilarity(issue1.getSummary(), issue2.getSummary())
				+ propertiesReader.getAsDouble(Property.DESCRIPTION_WEIGHT) * getSimilarity(issue1.getDescription(), issue2.getDescription())
				+ propertiesReader.getAsDouble(Property.COMMENTS_WEIGHT) * getSimilarity(issue1.getSummary(), ish.collectIssueComments(issue2).toString());
	}

	@Override
	public double getSimilarity(String text1, String text2) 
	{
		double similarity = 0.0;
		try
		{
			logger = Logger.getLogger(IssuesSimilarityCalculator.class.getName());
			if(text1 != null && text2 != null)
			{
				cts = new CosineTextsSimilarity(text1, text2);
				similarity = cts.getSimilarity();
			}
			if(similarity < 0 || similarity > 1)
				throw new SimilarityRangeException();
		} catch(SimilarityRangeException ex)
		{
			logger.error(ex);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return similarity;
	}

}
