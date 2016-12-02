#!groovy
{ -> {
	stage 'build'
	node {
		checkout scm
		sh './gradlew clean build -x test -x check'
	}

	stage 'test'
	node {
		try {
			sh './gradlew test'
		} finally {
			step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/test/TEST-*.xml'])
			step([$class: 'JacocoPublisher'])
		}
	}

	stage 'check'
	node {
		try {
			sh './gradlew check --continue'
		} catch(e) {
			currentBuild.result = "UNSTABLE"
		} finally {
			step([$class: 'CheckStylePublisher', pattern: "build/reports/checkstyle/main.xml"])
			step([$class: 'FindBugsPublisher', pattern: "build/reports/findbugs/main.xml"])
			step([$class: 'PmdPublisher', pattern: "build/reports/pmd/main.xml"])
		}
	}

	if(env.BRANCH_NAME == "master") {
		stage 'upload'
		node {
			sh './gradlew upload'
		}
	}
}
