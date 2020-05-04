Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
$CURRENT_DATE = GET-DATE -Format "yyyy/MM/dd"
$CURRENT_TIME = get-date -format "hh:mm:ss"
$LESSON = pwd | Select-Object | %{$_.ProviderPath.Split("\")[-1]}
mvn clean package "-Dmaven.test.skip=true";
$JAR_PATH = Resolve-Path ./target/spring*.jar
### Usage: "rerun_flowers_job.ps1 roses"  -- when "roses" is passed to ps script the remove_thorns step gets triggered from Spring Batch job
java -jar "-Dspring.batch.job.names=prepare_flowers_job" $JAR_PATH "item=shoes" "packing_date(date)=$CURRENT_DATE" "lesson=$LESSON" "time=$CURRENT_TIME" type=$args;
pause;
