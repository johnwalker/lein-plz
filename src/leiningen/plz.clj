(ns leiningen.plz
  (:require [ancient-clj.core :as anc]
            [clojure.java.io :as io]
            [rewrite-clj.zip :as z]
            [rewrite-clj.zip.indent :refer [indent]]))

(def fallback-abbreviation-map
  '{org.clojure/clojure         #{"clojure" "clj"}
    org.clojure/clojurescript   #{"clojurescript" "cljs"}
    org.clojure/core.async      #{"core.async"}
    org.clojure/core.cache      #{"core.cache"}
    org.clojure/core.logic      #{"core.logic"}
    org.clojure/core.match      #{"core.match"}
    org.clojure/core.memoize    #{"core.memoize"}
    org.clojure/core.typed      #{"core.typed"}
    org.clojure/data.json       #{"data.json"}
    org.clojure/data.xml        #{"data.xml"}
    org.clojure/java.jdbc       #{"java.jdbc"}

    compojure                   #{"compojure"}
    hiccup                      #{"hiccup"}
    ring                        #{"ring"}})


(defn lookup-abbrev
  [abbreviation-map abbrev]
  (->> abbreviation-map
       (filter (fn [[dependency set-of-abbrevs]]
                 (when (set-of-abbrevs abbrev)
                   dependency)))
       first first))

(defn to-updated-pair [s]
  [s (anc/latest-version-string! {:snapshots? false} s)])

(defn determine-case [prj-map]
  (if-let [z-deps (z/find-value prj-map :dependencies)]
    (let [z-deps-v (z/right z-deps)
          s-deps-v (z/sexpr z-deps-v)]
      [(if (seq s-deps-v)
         (if (vector? s-deps-v)
           :some-vector
           :some-sequence)
         (if (vector? s-deps-v)
           :empty-vector
           :something-else)) z-deps-v])
    [:no-dep-key prj-map]))

(defn get-deps [[k z]]
  (if (= k :some-vector)
    (set (map first (z/sexpr z)))
    #{}))

(defn insert-dep [z dep]
  (-> z
      (z/insert-right dep)
      (z/append-newline)
      (z/right)
      (indent 16)))

(defn conj-deps
  [[k z] deps]
  (try [true
        (case k
          :some-vector (let [z' (-> z z/down z/rightmost)]
                         (reduce insert-dep
                                 z'
                                 deps))

          :empty-vector (reduce insert-dep
                                (-> z
                                    (z/replace [(first deps)])
                                    (z/down))
                                (rest deps))
          :no-dep-key     (throw (ex-info :no-dep-key {}))
          :some-sequence  (throw (ex-info :some-sequence {}))
          :something-else (throw (ex-info :something-else {})))]
       (catch Exception e
         [false (.getMessage e)])))

(defn warn [m]
  (println "Warning:" m))

(defn generate-bug-report [ex]
  (let [m (ex-data ex)]
    (doseq [x [(.getMessage ex)
               "File a bug report here: "
               "https://github.com/johnwalker/lein-plz"
               "--"]]
      (println x))
    (println m)))

(defn plz
  [project & args]
  (let [plz-options (:plz project)
        [action & abbrevs] args]
    (when-not (seq plz-options)
      (warn "Using default abbreviatons since no options were found."))
    (try
      (when (and (= "add" action) (seq abbrevs))
        (let [root             (:root project)

              project-file     (str root "/" "project.clj")

              project-str      (slurp project-file)

              artifact->abbrev (merge fallback-abbreviation-map
                                      (cond (string? plz-options)
                                            (read-string (slurp plz-options))

                                            (map? plz-options) plz-options

                                            (seq plz-options)
                                            (apply merge (map
                                                          (comp
                                                           read-string
                                                           slurp)
                                                          plz-options))

                                            :else
                                            (when (seq plz-options)
                                              (throw (ex-info "Merge"
                                                              {:plz-options
                                                               plz-options})))))

              prj-map          (-> (z/of-string project-str)
                                   (z/find-value z/next 'defproject))

              [k z]            (determine-case prj-map)

              present-deps     (get-deps [k z])

              deps             (->> abbrevs
                                    (distinct)
                                    (map #(lookup-abbrev artifact->abbrev %))
                                    (remove nil?)
                                    (remove present-deps)
                                    (map to-updated-pair)
                                    (distinct))]
          (let [[left right] (conj-deps [k z] deps)]
            (if left
              (let [output (with-out-str (z/print-root right))]
                (if (>= (count output) (count project-str))
                  (spit project-file output)
                  (throw (ex-info "Output shrunk" {:project-str project-str
                                                   :attempted-output output}))))
              (throw (ex-info "Something went wrong" {:left left
                                                      :right right}))))))
      (catch Exception e
        (generate-bug-report e)))))
