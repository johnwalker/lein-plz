(ns leiningen.plz-test
  (:require [clojure.test :refer :all]
            [leiningen.plz :refer :all]
            [rewrite-clj.zip :as z]
            [rewrite-clj.zip.indent :refer [indent]]))

(deftest unit-tests
  (let [test-deps
        [[:org.clojure/clojurescript "0.0.0"]
         [:org.clojure/core.logic "0.0.0"]
         [:org.clojure/core.typed "0.0.0"]]

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
        (mapv determine-case entries)

        zprint-str (fn [[_ v]] (with-out-str (z/print-root v)))]

    (is (= :some-vector    (-> r-project-baseline first))
        "project-baseline")
    (is (= :empty-vector   (-> r-project-empty    first))
        "project-empty-vector")
    (is (= :something-else (-> r-project-invalid-dep-key first))
        "project-invalid-dep-key")
    (is (= :some-vector    (-> r-project-dep-entries first))
        "project-dep-entries")
    (is (= :no-dep-key     (-> r-project-no-dep-key first))
        "project-no-dep-key")

    (is (= (zprint-str r-project-baseline)
           (zprint-str (conj-deps project-baseline test-deps))))

    (is (= (zprint-str r-project-baseline)
           (zprint-str (conj-deps project-dep-entries test-deps))))

    (is (= (zprint-str r-project-baseline)
           (zprint-str (conj-deps project-empty-vector test-deps))))))
