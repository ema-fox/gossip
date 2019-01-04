(ns gossip.diff)

(defn cacheoize [size f]
  (let [old (ref {})
        new (ref {})]
    (fn [& args]
      (if-let [e (find @new args)]
        (do
          ;(print ".")
          (val e))
        (let [ret (if-let [e (find @old args)]
                    (do
                      ;(print ",")
                      (val e))
                    (do
                      ;(print "x")
                      (apply f args)))]
          (dosync (alter new assoc args ret))
          (dosync
           (when (>= (count @new) size)
             (ref-set old @new)
             (ref-set new {})))
          ret)))))

(defmacro cachefn [name size args & body]
  `(def ~name (cacheoize ~size (fn ~args ~@body))))

(defn count* [x]
  (if (char? x)
    1
    (count x)))

(defn edit-dist [xs]
  (->> (filter (fn [[k _]] (not= k :ab)) xs)
       (map (comp count* second))
       (apply +)))

(cachefn diff2 10000 [a b]
  (cond (empty? a)
        (mapv #(vector :b %) b)
        (empty? b)
        (mapv #(vector :a %) a)
        (= (peek a) (peek b))
        (conj (diff2 (pop a) (pop b)) [:ab (peek a)])
        :else
        (let [aa (diff2 (pop a) b)
              bb (diff2 a (pop b))]
          (if (< (edit-dist aa) (edit-dist bb))
            (conj aa [:a (peek a)])
            (conj bb [:b (peek b)])))))

(defn shrink-wrap [xs]
  (->> (partition-by first xs)
       (map (fn [ys]
              [(ffirst ys) (apply str (map second ys))]))))

(defn diff [a b]
  (shrink-wrap (diff2 (vec a) (vec b))))
