(ns gossip.diff
  (:require [diff-match-patch-clj.core :refer [diff as-hiccup]])
  (:import [name.fraser.neil.plaintext diff_match_patch diff_match_patch$Operation]))

(defn edit-dist [diffs]
  (apply + (map (fn [d]
                  (if (#{diff_match_patch$Operation/INSERT
                         diff_match_patch$Operation/DELETE}
                       (.operation d))
                    (count (.text d))
                    0))
                diffs)))

(defn diff2 [a b]
  (let [diffs (diff a b)]
    {:edit-dist (edit-dist diffs)
     :diff-html (as-hiccup diffs)}))
