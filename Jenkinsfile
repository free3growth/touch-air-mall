pipeline {
  agent {
    node {
      label 'maven'
    }
  }
   parameters {
      string(name:'PROJECT_NAME',defaultValue: 'mall-gateway',description:'构建模块')
      string(name:'PROJECT_VERSION',defaultValue: 'v3.29.0',description:'项目版本')
  }
  environment {
        DOCKER_CREDENTIAL_ID = 'aliyunhub-id'
        GITHUB_CREDENTIAL_ID = 'gitee-id'
        KUBECONFIG_CREDENTIAL_ID = 'kubeconfig-id'
        REGISTRY = 'registry.cn-hangzhou.aliyuncs.com'
        DOCKERHUB_NAMESPACE = 'tothemoon'
        GITHUB_ACCOUNT = 'OK12138'
        SONAR_CREDENTIAL_ID='sonar-qube'
        BRANCH_NAME='main'
    }
  stages {
    stage('拉取代码') {
      steps {
        git(url: 'https://gitee.com/OK12138/touch-air-mall.git', credentialsId: 'gitee-id', branch: 'main', changelog: true, poll: false)
        sh 'echo 正在构建 $PROJECT_NAME 版本号：$PROJECT_VERSION 将会提交给 $REGISTRY 镜像仓库'
        container ('maven') {
          sh "mvn clean install -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml"
          }
      }
    }
    stage ('build & push 构建镜像并推送') {
            steps {
                container ('maven') {
                    sh 'mvn -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml clean package'
                    sh 'cd $PROJECT_NAME && docker build -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
                    withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                        sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
                        sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
                        sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
                    }
                }
            }
        }
   stage('部署到k8s') {
          steps {
            input(id: "$PROJECT_NAME", message: "是否将 $PROJECT_NAME 部署到集群中?")
            kubernetesDeploy(configs: "$PROJECT_NAME/deploy/**", enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
          }
        }
   stage('push with tag 打上TAG标签，发布版本'){
          when{
            expression{
              return params.PROJECT_VERSION =~ /v.*/
            }
          }
          steps {
              container ('maven') {
                input(id: 'release-image-with-tag', message: '发布当前版本镜像吗?')
                  withCredentials([usernamePassword(credentialsId: "$GITHUB_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                    sh 'git config --global user.email "kubesphere@yunify.com" '
                    sh 'git config --global user.name "kubesphere" '
                    sh 'git tag -a $PROJECT_VERSION -m "$PROJECT_VERSION" '
                    sh 'git push http://$GIT_USERNAME:$GIT_PASSWORD@gitee.com/$GITHUB_ACCOUNT/touch-air-mall.git --tags --ipv4'
                  }
                sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
                sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
            }
          }
        }
  }
}