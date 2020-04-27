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


            stage('win'){
                when{
                    beforeAgent true
                    expression { "${params.TARGETS}".contains('exe') }
                }
                agent { label 'wix' }

                steps{
                    checkout scm

    // run witn builduser logged in, and run 'gradlew jpackage' manually first, or wix will fail
                    script{
                        //bat 'c:/bin/jdk-custom-env jdk && gradlew clean distZip jpackage'
                        // -PenvironmentName=some
                        bat 'c:/bin/jdk-custom-env jdk && c:/bin/jpackage-env && gradlew -PtargetPlatform=some clean distZip jpackage'
                    }

                    archiveArtifacts '**/build/distributions/*.zip, **/build/jpackage/*.exe'
                }
            }

            stage('mac'){
                when{
                    beforeAgent true
                    expression { "${params.TARGETS}".contains('pkg') }
                }
                agent { label 'macos' }

                steps{
                    checkout scm

    //-PenvironmentName=some
                    script{
                        sh 'sh gradlew -PtargetPlatform=some clean jpackage'
                    }

                    archiveArtifacts '**/build/jpackage/*.pkg'
                }
            }

        }

        }
    }

}
