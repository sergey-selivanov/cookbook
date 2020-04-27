pipeline{
    agent none
    parameters {
        string(name: 'TARGETS', defaultValue: 'exe', description: 'exe deb rpm pkg')
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
    // c:\bin\jdk-custom-env jdk && c:\bin\jpackage-env && gradlew -PinstallerType=exe clean jpackage
                    script{
                        //bat 'c:/bin/jdk-custom-env jdk && gradlew clean distZip jpackage'
                        // -PenvironmentName=some
                        bat 'c:/bin/jdk-custom-env jdk && c:/bin/jpackage-env && gradlew -PinstallerType=exe clean distZip jpackage'
                    }

                    archiveArtifacts '**/build/distributions/*.zip, **/build/jpackage/*.exe'
                }
            }

            stage('mac pkg'){
                when{
                    beforeAgent true
                    expression { "${params.TARGETS}".contains('pkg') }
                }
                agent { label 'macos' }

                steps{
                    checkout scm

    // dmg only can be built from desktop
    // cd ~/jenkins/jenkins/workspace/cookbook-local
    // sh gradlew -PinstallerType=dmg clean jpackage
                    script{
                        sh 'sh gradlew -PinstallerType=pkg clean jpackage'
                    }

                    archiveArtifacts '**/build/jpackage/*.pkg'
                }
            }

            stage('deb'){   // ubuntu
                when{
                    beforeAgent true
                    expression { "${params.TARGETS}".contains('deb') }
                }
                agent { label 'ubuntu' }

                steps{
                    checkout scm

    //-PenvironmentName=some
                    script{
                        sh 'sh gradlew -PinstallerType=deb clean jpackage'
                    }

                    archiveArtifacts '**/build/jpackage/*.deb'
                }
            }

            stage('rpm'){   // ora linux
                when{
                    beforeAgent true
                    expression { "${params.TARGETS}".contains('rpm') }
                }
                agent { label 'rh' }

                steps{
                    checkout scm

    //-PenvironmentName=some
                    script{
                        sh 'sh gradlew -PinstallerType=rpm clean jpackage'
                    }

                    archiveArtifacts '**/build/jpackage/*.rpm'
                }
            }

        }

        }
    }

}
