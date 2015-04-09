(ns leiningen.plz
  (:use leiningen.plz.deps)
  (:require [ancient-clj.core :as anc]
            [clj-http.client :as http]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [leiningen.core.main :as main]
            [rewrite-clj.zip :as z]
            [rewrite-clj.zip.whitespace :refer [prepend-space]]
            [table.core :refer [table]]))

(defn lookup-nick
  [nickname-map nick]
  (->> nickname-map
       (filter (fn [[dependency set-of-nicks]]
                 (when (set-of-nicks nick)
                   dependency)))
       first first))

(defn tokenize [s]
  (let [s (str/lower-case s)]
    (set (concat (str/split s #"\W+") (str/split s #"\/")))))

(def crossclj-index
  (future
    (->> (-> (http/get "http://crossclj.info/api/v1/prefetch-home")
             :body
             (str/replace "\"" "") ; remove quotation marks at the start and end
             (str/split #"\|"))
         (partition 6) ; each project is represented in the index by a 6-tuple
         (map second) ; we only care about the second elem of each tuple (fullname)
         (map (juxt symbol tokenize))))) ; yield a seq of 2-tuples [dep tags]

(defn search-crossclj [query]
  (let [query (tokenize query)
        relevance #(count (clojure.set/intersection (second %) query))]
    (->> @crossclj-index
         (filter (comp pos? relevance))
         (sort-by relevance >)
         ffirst)))

(defn to-updated-pair [s]
  [s (anc/latest-version-string! s {:snapshots? false})])

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
      (z/prepend-space 16))) 

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
  (main/info "Warning:" m))

(defn generate-bug-report [ex]
  (let [m (ex-data ex)]
    (doseq [x [(.getMessage ex)
               "File a bug report here: "
               "https://github.com/johnwalker/lein-plz"
               "--"]]
      (main/info x))
    (main/info m)))

(defn print-instructions []
  (main/info
   (str
    "Add dependencies to your existing project.clj file.\n"
    "Use the following commands:\n\n"
    "  - add <nick or nicks>"
    " -- Adds the dependencies associated with each nick to the project.\n"
    "    Example: lein plz add core.async cljs data.json\n\n"
    "  - list <& filter> -- List the built-in "
    "(total " (count fallback-nicknames) ") dependencies with their nicknames.\n"
    "    If called with a filter, it will be passed as a regular expression "
    "to limit the amount of entries.\n"
    "    Example: lein plz list org.clojure/")))

;;; Listing

(defn match-entry? [s [path nicks]]
  (let [pattern (re-pattern (str s))]
    (or (re-find pattern (str path))
        (some not-empty (map (partial re-find pattern) nicks)))))

(defn search-nicks [s] (filter (partial match-entry? s) fallback-nicknames))

(defn format-entry [[path nicks]] [path (str/join ", " nicks)])

(defn format-entries [matches]
  (->> matches
       (map format-entry)
       (cons ["Dependency" "Nickname(s)"])
       table))

(defn list-nicks [s] (-> s search-nicks format-entries))

(defn list-all [] (format-entries (seq fallback-nicknames)))


;; Adding

(defn entry-parser [entry]
  (cond
    (and (vector? entry)
         (= :as (get entry 1))
         (get entry 2))
    [(get entry 2)
     (-> (first entry)
         slurp
         read-string)]
    (vector? entry)
    [nil
     (-> (first entry)
         slurp
         read-string)]
    (string? entry)
    [nil
     (-> entry
         slurp
         read-string)]))

(defn parse-options [options]
  (cond
    (string? options) [nil (read-string (slurp options))]
    (map? options)    [nil options]
    (seq options)     (reduce
                       (fn [[groups global]
                            [group-name group]]
                         [(if group-name
                            (assoc groups group-name (-> group keys vec))
                            groups)
                          (merge global group)])
                       [nil nil]
                       (mapv entry-parser options))
    :else             (when (seq options)
                        (throw (ex-info "Merge" {:plz-options options})))))

(defn recognize-deps [present-deps m]
  (let [known-pairs (future
                      (->> (get-in m [true])
                           (map second)
                           (remove present-deps)
                           (distinct)
                           (pmap to-updated-pair)))
        unknown-deps (map first (get-in m [false]))]
    (doseq [u unknown-deps]
      (main/info "Unrecognized nickname" u))
    @known-pairs))

(defn nicks->deps [nicks present-deps groups af-map]
  (->> nicks
       (distinct)
       (reduce (fn [normalized-deps word]
                 (if-let [g (groups word)]
                   (into normalized-deps (sort (map (partial vector word) g)))
                   (conj normalized-deps
                         [word (or (lookup-nick af-map word)
                                   (search-crossclj word))]))) [])
       (group-by (comp not nil? second))
       (recognize-deps present-deps)))

(defn add-deps [project nicks]
  (let [root            (:root project)
        project-file    (str root "/" "project.clj")
        project-str     (slurp project-file)
        [groups af-map] (parse-options (:plz project))
        af-map          (merge fallback-nicknames af-map)
        groups          (or groups {})
        prj-map         (-> (z/of-string project-str)
                            (z/find-value z/next 'defproject))
        [k z]           (determine-case prj-map)
        present-deps    (get-deps [k z])
        deps            (nicks->deps nicks present-deps groups af-map)]
    (let [[left right] (conj-deps [k z] deps)]
      (if left
        (let [output (with-out-str (z/print-root right))]
          (if (>= (count output) (count project-str))
            (spit project-file output)
            (throw (ex-info "Output shrunk" {:project-str project-str
                                             :attempted-output output}))))
        (throw (ex-info "Something went wrong" {:left left
                                                :right right}))))))

;;; Main

(defn plz
  "Add dependencies using their nicknames."
  [project & args]
  (let [[action & r-args] args]
    (try
      (cond
        (and (= "add" action) (seq r-args)) (add-deps project r-args)
        (and (= "list" action) (seq r-args)) (list-nicks (str/join " " r-args))
        (= "list" action) (list-all)
        :else (print-instructions))
      (catch Exception e
        (generate-bug-report e)))))
