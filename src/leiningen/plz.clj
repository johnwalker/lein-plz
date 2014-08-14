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
   :org.clojure/data.edn        #{"data.edn" "edn"}
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

(defn determine-case [prj-map]
  (if-let [z-deps (-> prj-map (z/find-value :dependencies))]
    (let [z-deps-v (-> z-deps z/right)
          s-deps-v (z/sexpr z-deps-v)]
      [(if (seq s-deps-v)
         (if (vector? s-deps-v)
           :some-vector
           :some-sequence)
         (if (vector? s-deps-v)
           :empty-vector
           :something-else)) z-deps-v])
    [:no-dep-key prj-map]))

(defn conj-deps
  "Lots in here to pull out..."
  [prj-map deps]
  (let [[k zpr] (determine-case prj-map)]
    (try [true
          (case k
            :some-vector (let [zpr' (-> zpr z/down z/rightmost)
                               present-deps (into #{} (map (comp keyword first)
                                                           (z/sexpr zpr)))]
                           (reduce (fn [z new-dep]
                                     (-> z
                                         (z/insert-right new-dep)
                                         (z/append-newline)
                                         (z/right)
                                         (indent 16)))
                                   zpr'
                                   (distinct
                                    (map (fn [[k v]] [(-> k
                                                          keyword->str
                                                          symbol) v])
                                         (remove (fn [[k v]]
                                                   (present-deps (keyword k))) deps)))))

            :empty-vector (let [deps' (distinct
                                       (map (fn [[k v]]
                                              [(-> k
                                                   keyword->str
                                                   symbol) v])
                                            deps))]
                            (reduce (fn [z new-dep]
                                      (-> z
                                          (z/insert-right new-dep)
                                          (z/append-newline)
                                          (z/right)
                                          (indent 16)))
                                    (-> zpr
                                        (z/replace [(first deps')])
                                        (z/down))
                                    (rest deps')))
            :some-sequence (ex-info (str ":some-sequence "
                                         "Post an issue if project.clj is valid: "
                                         "https://github.com/johnwalker/lein-plz"))
            :something-else (ex-info (str ":something-else "
                                          "Post an issue if project.clj is valid "
                                          "https://github.com/johnwalker/lein-plz")))]
         (catch Exception e
           [false (.getMessage e)]))))

(defn plz
  "STOP LOOKING AT ME"
  [project & args]
  (let [plz-options (:plz project)
        [action & abbreviations] args]
    (when-not (seq plz-options)
      (println "No options specified in system profiles.clj."))
    (when (and (= "add" action) (seq abbreviations))
      (let [root         (:root project)
            project-file (str root "/" "project.clj")
            project-file-contents (slurp project-file)
            am           (merge fallback-abbreviation-map
                                (cond (string? plz-options)
                                      (-> plz-options
                                          slurp
                                          read-string)

                                      (or (vector? plz-options)
                                          (list? plz-options))
                                      (apply merge (mapv
                                                    (comp
                                                     read-string
                                                     slurp)
                                                    plz-options))

                                      (map? plz-options) plz-options
                                      :else (when (seq plz-options)
                                              (println "merge: ")
                                              (println "File a bug report here: ")
                                              (println "https://github.com/johnwalker/lein-plz")
                                              (println)
                                              (println plz-options))))
            suggested-dependencies (map (partial abbreviation->dependency-str am)
                                        abbreviations)

            prj                     (z/of-string project-file-contents)

            prj-map                 (z/find-value prj z/next 'defproject)

            ;; TODO: improve efficiency here
            new-dependencies (mapv (comp to-updated-pair symbol)
                                   suggested-dependencies)]
        (let [[success result] (conj-deps prj-map new-dependencies)]
          (if success
            (let [output (with-out-str (z/print-root result))]
              (if (>= (count output) (count project-file-contents))
                ;; Good, nothing shrunk !
                (spit project-file output)

                ;; Output shrunk ... aborting write
                (do (println "shrink: ")
                    (println "File a bug report here: ")
                    (println "https://github.com/johnwalker/lein-plz")
                    (println)
                    (println project-file-contents)
                    (println output))))
            (do
              (println "Something went wrong.")
              (println "File a bug report with this message here:")
              (println "https://github.com/johnwalker/lein-plz")
              (println [success result]))))))))
