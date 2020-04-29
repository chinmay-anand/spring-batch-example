# Spring Database Configurations - DB specific settings

Look for your database.<br/>Copy the associated settings and <br/>Change them locally to suit your environment.</strong>

<strong>There are two places we need to make the DB specific changes.</strong><br/><br/>
`pom.xml`
* Here we need to provide the necessary dependencies so that Spring framework will pull the required jars /drivers etc needed for using the database.

<br/>`application.properties` or `application.yml`
* The database specific connection parameters are specified here to be picked by the Spring Framework for connecting to the database.

Below are the working samples (connectin parametes are purposefully modified - they need to be set as per your environment)
* <table>
    <tr>
        <th>Database</th>
        <th>pom.xml entry</th>
        <th>properties entry</th>
    </tr>
    <tr>
        <td>H2 in-memory database</td>
        <td>
`<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>`
        </td>
        <td># NOT REQUIRED</td>
    </tr>
    <tr>
        <td>Postgres database</td>
        <td>
`<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>`
        </td>
        <td>
spring.jpa.database=postgresql
spring.datasource.platform=postgres
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres_user
spring.datasource.password=postgres_pass
        </td>
    </tr>
    <tr>
        <td>MySQL database</td>
        <td>
`<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>`
        </td>
        <td>
spring.datasource.driverClassName=com.mysql.jdbc.Driver
spring.datasource.platform=mysql
spring.datasource.url=jdbc:mysql://localhost:3306/batch_repo
spring.datasource.username=batch_user
spring.datasource.password=batch_pass
        </td>
    </tr>
</table>
