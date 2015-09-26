(ns pplz.test-runner
  (:require
   [cljs.test :refer-macros [run-tests]]
   [pplz.core-test]))

(enable-console-print!)

(defn runner []
  (if (cljs.test/successful?
       (run-tests
        'pplz.core-test))
    0
    1))
