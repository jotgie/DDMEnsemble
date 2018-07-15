#!/bin/bash


# compile & create JAR
"C:\Program Files\Java\jdk1.7.0_80\bin\java" -Dmaven.multiModuleProjectDirectory=C:\Users\Józef\Desktop\PWr\magisterskie\PracaMagisterska\moa2\moa\moa "-Dmaven.home=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2017.2.5\plugins\maven\lib\maven3" "-Dclassworlds.conf=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2017.2.5\plugins\maven\lib\maven3\bin\m2.conf" "-javaagent:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2017.2.5\lib\idea_rt.jar=57727:C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2017.2.5\bin" -Dfile.encoding=UTF-8 -classpath "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2017.2.5\plugins\maven\lib\maven3\boot\plexus-classworlds-2.5.2.jar" org.codehaus.classworlds.Launcher -Didea.version=2017.2.5 package -DskipTests


# execute JAR for each *.arff file in DIR
DIR="./src/main/resources/datasets"
OUTPUT="./src/main/resources/results_analysis/results"
FILES=$(find ${DIR} -type f -name "*.arff")
for i in ${FILES}
do
  echo "-----------------------------------"
  echo "File ${i}"
  echo "-----------------------------------"
  java -cp target/moa-2012.09-SNAPSHOT.jar -javaagent:target/sizeofag-1.0.0.jar moa.DoTask "LearnModel -l (moa.classifiers.drift.MultipleClassifierDrift) -s (ArffFileStream -f ${i}) -m 100000 -O C:\Users\Józef\Documents\asd.moa"
  mv ${OUTPUT}"/detectionResult.txt" "${OUTPUT}/` basename "${i}"`.txt"
done
