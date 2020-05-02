Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
$CURRENT_DATE = GET-DATE -Format "yyyy/MM/dd"
$CURRENT_TIME = get-date -format "hh:mm:ss"
$LESSON = pwd | Select-Object | %{$_.ProviderPath.Split("\")[-1]}
mvn clean package "-Dmaven.test.skip=true";
$JAR_PATH = Resolve-Path ./target/spring*.jar
java -jar "-Dspring.batch.job.names=delivery_parcel_job" $JAR_PATH "item=shoes" "packing_date(date)=$CURRENT_DATE" "lesson=$LESSON" "time=$CURRENT_TIME";
pause;
