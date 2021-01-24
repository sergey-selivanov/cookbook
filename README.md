# cookbook

Stores and categorizes html pages saved from browser, including referenced images.

Build installers:

    gradlew clean jpackage -PenvironmentName=production -PinstallerType=exe 
    gradlew clean jpackage -PenvironmentName=production -PinstallerType=deb 
    gradlew clean jpackage -PenvironmentName=production -PinstallerType=rpm 
