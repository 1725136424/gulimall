pipeline {
  agent {
    node {
      label 'maven'
    }
  }
  parameters {
    string(name:'TAG_NAME',defaultValue: 'v1.0',description:'')
    string(name:'PROJECT_NAME',defaultValue: 'gulimall-cart',description:'')
    string(name:'BRANCH_NAME',defaultValue: 'master', description: '')
  }
  environment {
     DOCKER_CREDENTIAL_ID = 'harbor-id'
     GITEE_CREDENTIAL_ID = 'gitee-id'
     KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
     REGISTRY = 'hub.wanjiahao.site'
     DOCKERHUB_NAMESPACE = 'gulimall'
     GITEE_ACCOUNT = 'haogege1'
     SONAR_CREDENTIAL_ID = 'sonar-token'
  }
  stages {
    stage('拉取代码') {
      steps {
        git(credentialsId: 'gitee-id', url: 'https://gitee.com/haogege1/gulimall.git', branch: '$BRANCH_NAME', changelog: true, poll: false)
      }
    }

   /* stage('代码质量分析') {
            steps {
              container ('maven') {
                withCredentials([string(credentialsId: "$SONAR_CREDENTIAL_ID", variable: 'SONAR_TOKEN')]) {
                  withSonarQubeEnv('sonar') {
                   sh "echo 当前目录为 `pwd`"
                   sh "echo 正在编译项目"
                   sh "mvn clean install -Dmaven.test.skip=true"
                   sh "mvn sonar:sonar -gs `pwd`/mvn-settings.xml -Dsonar.login=$SONAR_TOKEN"
                  }
                }
                timeout(time: 1, unit: 'HOURS') {
                  waitForQualityGate abortPipeline: true
                }
              }
            }
        } */

    stage ('构建镜像-推送镜像') {
            steps {
                container ('maven') {
                    sh 'mvn -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml clean package'
                    sh 'cd $PROJECT_NAME && docker build --no-cache -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
                    withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                        sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
                        sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
                    }
                }
            }
        }

    stage('推送最新镜像'){
           when{
             branch 'master'
           }
           steps{
                container ('maven') {
                  sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
                  sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
                }
           }
        }

     stage('部署到开发环境') {
          when{
            branch 'master'
          }
          steps {
            input(id: "deploy-to-dev-${PROJECT_NAME}", message: '是否部署到开发环境?')
            kubernetesDeploy(configs: "${PROJECT_NAME}/deploy/**", enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
          }
        }

    stage('发布版本'){
          when{
            expression{
              return params.TAG_NAME =~ /v.*/
            }
          }
          steps {
              container ('maven') {
                input(id: "release-image-with-tag-$PROJECT_NAME", message: '是否发布最新镜像?')
                  withCredentials([usernamePassword(credentialsId: "$GITEE_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh 'git config --global user.email "1725136424@qq.com" '
                    sh 'git config --global user.name "wanjiahao" '
                    sh 'git tag -a $TAG_NAME -m "$TAG_NAME" '
                    sh "git push http://$GIT_USERNAME:$GIT_PASSWORD@gitee.com/$GITEE_ACCOUNT/gulimall.git --tags --ipv4"
                  }
                sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$TAG_NAME '
                sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$TAG_NAME '
          }
          }
        }
  }
}