(ns plz.plz-test
  (:require [clojure.test :refer :all]
            [leiningen.plz :refer :all]
            [rewrite-clj.zip :as z]
            [rewrite-clj.zip.indent :refer [indent]]))

(defn- zprint-str [[_ v]]
  (with-out-str (z/print-root v)))

(defn mockup-pipe [prj-map deps]
  (let [[k zpr]          (determine-case prj-map)
        present-deps     (get-deps [k zpr])
        deps             (remove (comp present-deps first) deps)]
    (conj-deps [k zpr] deps)))


(let [test-deps `[[org.clojure/clojurescript "0.0.0"]
                  [org.clojure/core.logic "0.0.0"]
                  [org.clojure/core.typed "0.0.0"]]

      entries (mapv #(-> % (z/of-file)
                         (z/find-value z/next 'defproject))
                    ["test/unit/project-baseline.clj"
                     "test/unit/project-empty-vector.clj"
                     "test/unit/project-invalid-dep-key.clj"
                     "test/unit/project-dep-entries.clj"
                     "test/unit/project-no-dep-key.clj"])

      [project-baseline project-empty-vector project-invalid-dep-key
       project-dep-entries project-no-dep-key] entries

      [r-project-baseline r-project-empty r-project-invalid-dep-key
       r-project-dep-entries r-project-no-dep-key]
      (mapv determine-case entries)]

  (deftest project-baseline
    (is (= :some-vector    (-> r-project-baseline first))
        "project-baseline"))

  (deftest project-empty-vector
    (is (= :empty-vector   (-> r-project-empty first))
        "project-empty-vector"))

  (deftest projects-invalid-dep-key
    (is (= :something-else (-> r-project-invalid-dep-key first))
        "project-invalid-dep-key"))

  (deftest projects-dep-entries
    (is (= :some-vector    (-> r-project-dep-entries first))
        "project-dep-entries"))

  (deftest project-no-dep-key
    (is (= :no-dep-key     (-> r-project-no-dep-key first))
        "project-no-dep-key"))


  (deftest baseline
    (is (= (zprint-str r-project-baseline)
           (zprint-str (mockup-pipe
                        project-baseline
                        test-deps)))))


  (deftest dep-entry
    (is (= (zprint-str r-project-baseline)
           (zprint-str (mockup-pipe
                        project-dep-entries
                        test-deps)))))

  (deftest empty-vector
    (is (= (zprint-str r-project-baseline)
           (zprint-str (mockup-pipe
                        project-empty-vector
                        test-deps))))))
