pipeline{
    agent none
    parameters {
        string(name: 'TARGETS', defaultValue: 'exe', description: 'exe deb rpm')
        string(name: 'ENVIRONMENT', defaultValue: 'production', description: 'production')
    }
    options{
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages{

        stage('parallel'){


        parallel{


            stage('win exe'){
                when{
                    beforeAgent true
                    expression { "${params.TARGETS}".contains('exe') }
                }
                agent { label 'wix' }

                steps{
                    checkout scm

    // run witn builduser logged in, and run 'gradlew jpackage' manually first, or wix will fail
    // cd \jenkins\workspace\cookbook-local
    // c:\bin\jdk-custom-env jdk && gradlew -PinstallerType=exe clean jpackage

    // light.exe : error LGHT0217 : Error executing ICE action 'ICE01'. ... "The Windows Installer Service could not be accessed.
    // https://stackoverflow.com/questions/1064580/wix-3-0-throws-error-217-while-being-executed-by-continuous-integration
    // add builduser to local Administrators group

                    script{
                        //bat 'c:/bin/jdk-custom-env jdk && gradlew clean distZip jpackage'
                        bat "c:/bin/jdk-custom-env jdk && c:/bin/wixenv && gradlew -PinstallerType=exe -PenvironmentName=${params.ENVIRONMENT} clean distZip jpackage"
                    }
// TODO Reckoned version: 0.0.5-dev.0.51+20210124T013814Z on clean git gir in win8vm, report issue
                    archiveArtifacts '**/build/distributions/*.zip, **/build/jpackage/*.exe'
                    //archiveArtifacts '**/build/jpackage/*.exe'
                }
            }

            stage('deb'){   // ubuntu
                when{
                    beforeAgent true
                    expression { "${params.TARGETS}".contains('deb') }
                }
                agent { label 'deb' }

                steps{
                    checkout scm

                    script{
                        sh "sh gradlew -PinstallerType=deb -PenvironmentName=${params.ENVIRONMENT} clean jpackage"
                    }

                    archiveArtifacts '**/build/jpackage/*.deb'
                }
            }

            stage('rpm'){   // ora linux
                when{
                    beforeAgent true
                    expression { "${params.TARGETS}".contains('rpm') }
                }
                agent { label 'rpm' }

                steps{
                    checkout scm

                    script{
                        sh "sh gradlew -PinstallerType=rpm -PenvironmentName=${params.ENVIRONMENT} clean jpackage"
                    }

                    archiveArtifacts '**/build/jpackage/*.rpm'
                }
            }


//            stage('mac pkg'){
//                when{
//                    beforeAgent true
//                    expression { "${params.TARGETS}".contains('pkg') }
//                }
//                agent { label 'macos' }
//
//                steps{
//                    checkout scm
//
//    // dmg only can be built from desktop
//    // cd ~/jenkins/jenkins/workspace/cookbook-local
//    // sh gradlew -PinstallerType=dmg clean jpackage
//                    script{
//                        sh 'sh gradlew -PinstallerType=pkg clean jpackage'
//                    }
//
//                    archiveArtifacts '**/build/jpackage/*.pkg'
//                }
//            }

        }

        }
    }

}
