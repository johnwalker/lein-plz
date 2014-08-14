(ns leiningen.plz
  (:require [ancient-clj.core :as anc]
            [clojure.java.io :as io]
            [rewrite-clj.zip :as z]
            [rewrite-clj.zip.indent :refer [indent]]))

(def fallback-abbreviation-map
  {:org.clojure/clojure         #{"clojure" "clj"}
   :org.clojure/clojurescript   #{"clojurescript" "cljs"}
   :org.clojure/core.async      #{"core.async" "async"}
   :org.clojure/core.cache      #{"core.cache" "cache"}
   :org.clojure/core.logic      #{"core.logic" "logic"}
   :org.clojure/core.match      #{"core.match" "match"}
   :org.clojure/core.memoize    #{"core.memoize" "memoize"}
   :org.clojure/core.typed      #{"core.typed" "typed"}
   :org.clojure/data.json       #{"data.json" "json"}
   :org.clojure/data.xml        #{"data.xml" "xml"}
   :org.clojure/java.jdbc       #{"java.jdbc" "jdbc"}
   ;; Databases for jdbc
   :org.apache.derby/derby      #{"derby"}
   :hsqldb/hsqldb               #{"hsqldb"}
   :mysql/mysql-connector-java  #{"mysql"}
   :net.sourceforge.jtds/jtds   #{"jtds"}
   :postgresql/postgresql       #{"postgresql" "postgres"}
   :org.xerial/sqlite-jdbc      #{"sqlite"}

   :compojure                   #{"compojure"}
   :hiccup                      #{"hiccup"}
   :ring                        #{"ring"}})


(defn collect-dependencies
  "Why did I write this

  Oh yeah"
  [p]
  (-> p
      (z/find-value z/next :dependencies)
      z/right
      z/sexpr))

(defn keyword->str [k]
  (str (when-let [n (namespace k)]
         (str n "/")) (name k)))

(defn abbreviation->dependency-str
  "Hahaha yeah ;). 1.7.0 will clean this up."
  [abbreviation-map abbrev]
  (-> (filter
       (fn [[dependency set-of-abbrevs]]
         (when (set-of-abbrevs abbrev)
           dependency))
       abbreviation-map)
      first
      first
      keyword->str))

(defn to-updated-pair [s]
  [s (anc/latest-version-string! {:snapshots? false} s)])

(defn plz
  "STOP LOOKING AT ME"
  [project & args]
  (let [plz-options (:plz project)
        [action & abbreviations]  args]
    (when-not (seq plz-options)
      (println "No options specified in system profiles.clj."))
    (when (and (= "add" action) (seq abbreviations))
      (let [root                   (:root project)
            project-file           (str root "/" "project.clj")
            am                     (merge fallback-abbreviation-map
                                          (cond (string? plz-options) ((comp read-string slurp) plz-options)
                                                (or (vector? plz-options) (list? plz-options)) (apply merge (mapv (comp read-string slurp) plz-options))
                                                (map? plz-options) plz-options
                                                :else (when (seq plz-options)
                                                        (println "I have no idea how this triggered...file a bug report here:")
                                                        (println "https://github.com/johnwalker/lein-plz"))))
            suggested-dependencies (map (partial abbreviation->dependency-str
                                                 am)
                                        abbreviations)

            prj                     (z/of-file project-file)

            prj-map                 (z/find-value prj z/next 'defproject)

            current-dependency-pairs (collect-dependencies prj-map)

            current-dependency-strs  (mapv first current-dependency-pairs)

            new-dependencies (mapv (comp to-updated-pair symbol)
                                   (remove (into #{} current-dependency-strs)
                                           suggested-dependencies))]
        (try
          (with-open [w (io/writer project-file)]
            (binding [*out* w]
              (z/print-root
               (reduce (fn [z new-dep]
                         (-> z
                             (z/insert-right new-dep)
                             z/right
                             (z/prepend-newline)
                             (indent 17)))
                       (-> prj-map (z/find-value :dependencies)
                           z/right
                           z/down
                           z/right)
                       new-dependencies))
              (.flush w)))
          (catch Exception e
            (println (.getMessage e))))))))
