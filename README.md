# pipeline-test

Test Grooovy-script in Jenkins Pipeline

## Setup instructions

1. In Jenkins, go to Manage Jenkins &rarr; System &rarr; Global Trusted Pipeline Libraries

    - Name: `pipeline-test`

2. Then create a Jenkins job with the following pipeline (note that the underscore `_` is not a typo):

    ```
    @Library('pipeline-test')_

    stage('Demo') {

      echo 'Hello World'
   
      sayHello 'Gepard'

    }
    ```

