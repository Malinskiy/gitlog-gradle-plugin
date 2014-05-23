package com.malinskiy.gradle
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.pegdown.PegDownProcessor

class GitLogTask extends DefaultTask {

    def readTags() {
        def tags = []
        def proc = "git tag -l".execute()
        proc.in.eachLine { line -> tags += line}
        tags.sort {}
        Collections.reverse( tags )
        tags
    }

    def readTagMessage(String tag, String nextTag) {
        def message = []
        def proc = "git log --pretty=format:%s $tag..$nextTag".execute()
        proc.in.eachLine { line ->
            message += parseJiraLinks(line)
        }
        proc.err.eachLine { line -> println line }
        message
    }

    def getFirstCommitHash() {
        def message = []
        def proc = "git rev-list --max-parents=0 HEAD".execute()
        proc.in.eachLine { line ->
            message += line
        }
        proc.err.eachLine { line -> println line }
        message.get(0)
    }

    def parseJiraLinks(String line) {
        Project rootProject = project.getRootProject()
        def jiraRoot = rootProject.hasProperty("JIRA_ROOT") ? rootProject.JIRA_ROOT : null
        def jiraJey =  rootProject.hasProperty("JIRA_PROJECT_KEY") ? rootProject.JIRA_PROJECT_KEY : null
        if( jiraRoot != null && jiraJey != null) {
            return line.replaceAll(/${jiraJey}-(\d+)/, /<a href=\"${jiraRoot}browse\/${jiraJey}-$1\">${jiraJey}-$1<\/a>/)
        }
        line
    }

    @TaskAction
    def generateGitLog() {
        def gitlog = new File('gitlog.md')
        gitlog.delete()
        def versions = "Latest changes"

        def tags = readTags()
        tags.each {tag ->
            versions += "- [$tag](#$tag)\n"
        }

        for(int i = -1; i < tags.size(); i++) {
            def previous = i == -1 ? "HEAD" : tags.get(i)
            def tag = i == (tags.size() - 1) ? getFirstCommitHash() : tags.get(i + 1)

            if(i == -1) {
                gitlog << "# Latest changes<a name='Latest changes'></a>\n"
            } else {
                gitlog << "# ${previous}<a name='${previous}'></a>\n"
            }

            def message = readTagMessage(tag, previous)
            message.each{
                gitlog << "<p class=\"message\">$it</p>"
            }
            gitlog << "\n"
        }

        def pdp = new PegDownProcessor()
        def engine = new SimpleTemplateEngine()
        def template = engine.createTemplate(getClass().getResource('/gitlog.tpl'))

        def tryAgain = true;
        def data
        while(tryAgain == true) {
            try {
                data = [gitlog: pdp.markdownToHtml(new File("gitlog.md").text), application: project.name, versions: pdp.markdownToHtml(versions)]
                tryAgain = false
            } catch (all) {
                //This code sometimes timeouts
                println 'HTML generation timed out. Retrying...'
            }
        }

        def result = template.make(data)
        new File('gitlog.html').withWriter { w ->
            w.write(result)
        }
    }
}