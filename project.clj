(defproject lein-plz "0.3.4-SNAPSHOT"
  :description "A Leiningen plugin for adding dependencies to projects quickly."
  :url "http://johnwalker.io/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[rewrite-clj "0.4.12"]
                 [ancient-clj "0.3.6"
                  :exclusions [com.amazonaws/aws-java-sdk-s3]]
                 [com.amazonaws/aws-java-sdk-s3 "1.9.0"
                  :exclusions [joda-time]]
                 [clj-http "1.1.0"]
                 [table "0.4.0"]]
  :eval-in-leiningen true)
