name := "sample-scala"


libraryDependencies ++= Seq(
  "org.jinq" % "jinq-jpa-scala" % "1.8.12",
  "org.apache.derby" % "derby" % "10.10.1.1",
  "org.eclipse.persistence" % "eclipselink" % "2.5.1"
//  "org.hibernate" % "hibernate-entitymanager" % "4.3.5.Final"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")