pipeline {
  agent {
    node {
      label 'maven'
    }
  }

   parameters {
       string(name:'TAG_NAME',defaultValue: 'v0.0Beta',description:'请输入版本')
       string(name:'PROJECT_NAME',defaultValue: '',description:'请输入你要构建的项目名称')
   }

   environment {
       DOCKER_CREDENTIAL_ID = 'harbor-id'
       GITEE_CREDENTIAL_ID = 'gitee-id'
       KUBECONFIG_CREDENTIAL_ID = 'demo-kubeconfig'
       REGISTRY = 'registry.cn-shenzhen.aliyuncs.com'
       DOCKERHUB_NAMESPACE = 'gulimall'
       GITEE_ACCOUNT = 'haogege1'
       SONAR_CREDENTIAL_ID = 'sonar-token'
   }

  stages {
      stage('拉取代码') {
        steps {
          git(credentialsId: 'gitee', url: 'https://gitee.com/haogege1/gulimall.git', branch: 'master', changelog: true, poll: false)
          sh 'echo 正在构建项目: $PROJECT_NAME:$TAG_NAME'
        }
      }

      stage('代码质量分析') {
         steps {
             ontainer ('maven') {
                    withCredentials([string(credentialsId: "$SONAR_CREDENTIAL_ID", variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('sonar') {
                       sh 'echo 当前目录是: `pwd`'
                       sh "mvn sonar:sonar -gs `pwd`/mvn-settings.xml -Dsonar.login=$SONAR_TOKEN"
                    }
                    timeout(time: 1, unit: 'HOURS') {
                      waitForQualityGate abortPipeline: true
                    }
              }
         }
      }
    }
}