package com.amirov.jirareporter.jira;

import com.amirov.jirareporter.Reporter;
import com.amirov.jirareporter.RunnerParamsProvider;
import com.amirov.jirareporter.teamcity.TeamCityXMLParser;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.domain.Comment;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.Transition;
import com.atlassian.jira.rest.client.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

import java.net.URI;
import java.net.URISyntaxException;

public class JIRAClient {

    public static JiraRestClient getRestClient() {
        System.setProperty("jsse.enableSNIExtension", RunnerParamsProvider.sslConnectionIsEnabled());
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI jiraServerUri = null;
        try {
            jiraServerUri = new URI(RunnerParamsProvider.getJiraServerUrl());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return factory.createWithBasicHttpAuthentication(jiraServerUri, RunnerParamsProvider.getJiraUser(), RunnerParamsProvider.getJiraPassword());
    }

    public static Issue getIssue() {
        Promise<Issue> issue = null;
        try {
            issue = getRestClient().getIssueClient().getIssue(Reporter.getIssueId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return issue.claim();
    }

    public static String getIssueStatus(){
        return getIssue().getStatus().getName();
    }

    private static Iterable<Transition> getTransitions (){
        return getRestClient().getIssueClient().getTransitions(getIssue().getTransitionsUri()).claim();
    }

    private static Transition getTransition(String transitionName){
        return getTransitionByName(getTransitions(), transitionName);
    }

    public static TransitionInput getTransitionInput(String transitionName){
        TeamCityXMLParser parser = new TeamCityXMLParser();
        return new TransitionInput(getTransition(transitionName).getId(), Comment.valueOf(parser.getTestResultText()));
    }

    private static Transition getTransitionByName(Iterable<Transition> transitions, String transitionName) {
        for (Transition transition : transitions) {
            if (transition.getName().equals(transitionName)) {
                return transition;
            }
        }
        return null;
    }
}
