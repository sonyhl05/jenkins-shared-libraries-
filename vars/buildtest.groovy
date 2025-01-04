def buildProject() {
    echo 'Building project with Maven...'
    sh 'mvn clean package'
}

def checkoutCode() {
    echo 'Checking out code...'
    checkout scm
}

def cleanupProcesses() {
    echo 'Cleaning up processes...'
    sh 'pkill -f "mvn spring-boot:run" || true'
}

def cleanupWorkspace() {
    echo 'Cleaning up the workspace...'
    deleteDir() // Deletes all files in the current workspace
}

def runSpringBootApp() {
    echo 'Running Spring Boot application...'
    sh 'nohup mvn spring-boot:run &'
    sleep(time: 15, unit: 'SECONDS')

    def localIp = sh(script: "hostname -I | awk '{print \$1}'", returnStdout: true).trim()
    echo "The application is running and accessible at: http://${localIp}:8080"
}

def setupJava17() {
    echo 'Setting up Java 17...'
    sh 'sudo apt update'
    sh 'sudo apt install -y openjdk-17-jdk'
}

def setupMaven() {
    echo 'Setting up Maven...'
    sh 'sudo apt install -y maven'
}

def stopSpringBootApp() {
    echo 'Gracefully stopping the Spring Boot application...'
    sh 'mvn spring-boot:stop'
}

def tagBuild(String tagName, String message = 'Build tagging') {
    echo "Tagging the build with tag: ${tagName}"
    
    sh """
        git tag -a ${tagName} -m '${message}'
        git push origin ${tagName}
    """
}

def uploadArtifact(String artifactPath) {
    echo 'Uploading artifact...'
    archiveArtifacts artifacts: artifactPath, allowEmptyArchive: true
}

def validateAppRunning() {
    echo 'Validating that the app is running...'
    def response = sh(script: 'curl --write-out "%{http_code}" --silent --output /dev/null http://localhost:8080', returnStdout: true).trim()
    if (response == "200") {
        echo 'The app is running successfully!'
    } else {
        echo "The app failed to start. HTTP response code: ${response}"
        error("The app did not start correctly!")
    }
}
