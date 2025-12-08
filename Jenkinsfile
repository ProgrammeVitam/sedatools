pipeline {
    agent {
        label 'build'
    }

    tools {
        jdk 'java11'
        maven 'maven-3.9'
    }

    environment {
        MVN_BASE = "/usr/local/maven/bin/mvn"
        MVN_COMMAND = "${MVN_BASE} --settings ${pwd()}/.ci/settings.xml --show-version --batch-mode --errors --fail-at-end -DinstallAtEnd=true -DdeployAtEnd=true "
        DEPLOY_GOAL = " " // Deploy goal used by maven ; typically "deploy" for master* branches & "" (nothing) for everything else (we don't deploy) ; keep a space so can work in other branches than develop
        CI = credentials("app-jenkins")
        SERVICE_SONAR_URL = credentials("service-sonar-url")
        SERVICE_NEXUS_URL = credentials("service-nexus-url")
        SERVICE_CHECKMARX_URL = credentials("service-checkmarx-url")
        SERVICE_REPO_SSHURL = credentials("repository-connection-string")
        SERVICE_GIT_URL = credentials("service-gitlab-url")
        SERVICE_PROXY_HOST = credentials("http-proxy-host")
        SERVICE_PROXY_PORT = credentials("http-proxy-port")
        MAILEXTRACT_GIT_URL=credentials("mailextract-gitlab-url")
        SERVICE_NOPROXY = credentials("http_nonProxyHosts")
        GITHUB_ACCOUNT_TOKEN = credentials("vitam-prg-token")
    }

    triggers {
        upstream(upstreamProjects: 'build-mailextractor', threshold: hudson.model.Result.SUCCESS)
    }

   stages {

       stage("Tools configuration") {
           steps {
               echo "Workspace location : ${env.WORKSPACE}"
               echo "Branch : ${env.GIT_BRANCH}"
               // default behavior
               writeFile file: 'deploy_goal.txt', text: "${env.DEPLOY_GOAL}"
           }
       }

        // Override the default maven deploy target when on master (publish on nexus)
        stage("Computing maven target") {
            when {
                anyOf {
                    branch "master"
                    tag pattern: "^[1-9]+\\.[0-9]+\\.[0-9]+-?[0-9]*\$", comparator: "REGEXP"
                }
            }
            environment {
                DEPLOY_GOAL = "deploy"
                MASTER_BRANCH = "true"
            }
            steps {
                script {
                    // overwrite file content with one more goal
                    writeFile file: 'deploy_goal.txt', text: "${env.DEPLOY_GOAL}"
                 }
                echo "We are on master branch (${env.GIT_BRANCH}) ; deploy goal is \"${env.DEPLOY_GOAL}\""
            }
        }

        stage ("Execute unit tests") {
            when {
                not{
                    branch "PR*"
                }
            }
            steps {
                sh '$MVN_COMMAND -f pom.xml clean test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage ("Execute unit tests when pull request") {
            when {
                branch "PR*"
            }
            steps {
                githubNotify status: "PENDING", description: "Building & testing", credentialsId: "vitam-prg-token"
                sh '$MVN_COMMAND -f pom.xml clean test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
                success {
                    githubNotify status: "SUCCESS", description: "Build successul", credentialsId: "vitam-prg-token"
                }
                failure {
                    githubNotify status: "ERROR", description: "Build failed", credentialsId: "vitam-prg-token"
                }
                unstable {
                    githubNotify status: "ERROR", description: "Build unstable", credentialsId: "vitam-prg-token"
                }
                aborted {
                    githubNotify status: "FAILURE", description: "Build canceled", credentialsId: "vitam-prg-token"
                }
                unsuccessful {
                    githubNotify status: "ERROR", description: "Build unsuccessful", credentialsId: "vitam-prg-token"
                }
            }
        }

        stage("Build") {
            when {
                not{
                    branch "PR*"
                }
            }
            environment {
                DEPLOY_GOAL = readFile("deploy_goal.txt")
            }
            steps {
                sh '$MVN_COMMAND -f pom.xml -Dmaven.test.skip=true -DskipTests=true clean package javadoc:aggregate-jar $DEPLOY_GOAL'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
