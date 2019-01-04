(ns gossip.db
  (:require [datomic.api :refer [q pull db transact] :as d]))

(defn schemon [ident type cardinality]
  {:db/ident ident
   :db/valueType type
   :db/cardinality cardinality})

(defn one [ident type]
  (schemon ident type :db.cardinality/one))

(defn many [ident type]
  (schemon ident type :db.cardinality/many))

(def schema [(one ::content :db.type/string)
             (one ::parent :db.type/ref)
             (one ::public :db.type/boolean)])

;(def uri "datomic:mem://gossip")
(def uri "datomic:free://localhost:4334/gossip")

(defn setup []
  (d/create-database uri)
  (def conn (d/connect uri))
  (prn @(transact conn schema)))

(defn get-entry [id]
  (::content (pull (db conn) [::content] id)))

(defn save-entry [content parent public]
  (-> @(transact conn [(cond-> {:db/id "entry"
                                ::content content
                                ::public public}
                               parent (assoc ::parent parent))])
      :tempids
      (get "entry")))

(defn all-heads [public]
  (q (conj '[:find ?e ?content
             :where
             [?e ::content ?content]
             (not [_ ::parent ?e])]
           (cond->> '[?e ::public true]
                    (not public) (list 'not)))
     (db conn)))

(defn all-entries []
  (q '[:find ?e
       :where [?e ::content]]
     (db conn)))

(defn all-content []
  (q '[:find ?content
       :where [_ ::content ?content]]
     (db conn)))

(defn -main []
  (setup)
  (prn (all-entries))
  (save-entry "foo")
  (save-entry "bar")
  (prn (all-content)))
