pipeline{
    agent any
    parameters {
        string(name: 'TARGETS', defaultValue: 'exe', description: 'exe deb rpm dmg')
    }
    options{
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages{
        stage('win'){
            agent { label 'wix' }
            when{ expression { "${params.TARGETS}".contains('exe') } }
            steps{
                checkout scm

                script{
                    //bat 'c:/bin/jdk-custom-env jdk && gradlew clean distZip jpackage'
                    bat 'c:/bin/jdk-custom-env jdk && gradlew -PenvironmentName=some -PtargetPlatform=some distZip jpackage'
                }

                archiveArtifacts '**/build/distributions/*.zip, **/build/jpackage/*.exe'
            }
        }
        stage('dmg'){
            agent { label 'macos' }
            when{ expression { "${params.TARGETS}".contains('dmg') } }
            steps{
                checkout scm

                script{
                    sh 'gradlew -PenvironmentName=some -PtargetPlatform=some jpackage'
                }

                archiveArtifacts '**/build/jpackage/*.dmg'
            }
        }
    }

}
