#!groovy
{ ->
	node {
		stage 'build'
		checkout scm
		sh './gradlew clean build -x test -x check'
	
		stage 'test'
		try {
			sh './gradlew test'
		} finally {
			try {
				step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/test/TEST-*.xml', allowEmptyResults: true])
				step([$class: 'JacocoPublisher'])
			} catch(e) {
				println e	
			}
		}

		stage 'check'
		try {
			sh './gradlew check --continue'
		} catch(e) {
			currentBuild.result = "UNSTABLE"
		} finally {
			step([$class: 'CheckStylePublisher', pattern: "build/reports/checkstyle/main.xml"])
			step([$class: 'FindBugsPublisher', pattern: "build/reports/findbugs/main.xml"])
			step([$class: 'PmdPublisher', pattern: "build/reports/pmd/main.xml"])
		}

		stage 'upload'
		if(env.BRANCH_NAME == "master")
			sh './gradlew upload'
	}
}
