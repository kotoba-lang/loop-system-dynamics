(ns loop-system-dynamics.query
  "Datalog query layer (kotoba-lang/datalog) over this loop's facts.

   Why this exists: ADR-2607203000 asked for entity/actor data to be
   'DataScript/Datomic query で接続' (connected via DataScript/Datomic query),
   not just read from a static file. This namespace ingests three real,
   already-sourced datasets into an in-memory datalog db so a caller can ask
   a genuine `:find/:where` datalog question instead of grepping prose.

   Convention (matches manifest/edn-query.cljs and loop-innen.query): attributes
   are bare strings (no leading colon); datalog queries are plain query strings,
   e.g. \"[:find ?id :where [?e \\\"archetype/id\\\" ?id]]\"."
  (:require [clojure.edn :as edn]
            [datalog.core :as dl]
            [datalog.index :as index]))

(defn- stringify-complex
  "Nested map/vector -> pr-str string blob; scalars pass through unchanged."
  [v]
  (if (or (map? v) (vector? v) (list? v) (set? v))
    (pr-str v)
    v))

(defn- parse-vector-query [query]
  (let [qvec (if (string? query) (edn/read-string query) query)
        idx-in (first (keep-indexed #(when (= %2 :in) %1) qvec))
        idx-where (or (first (keep-indexed #(when (= %2 :where) %1) qvec)) -1)
        find-end (or idx-in idx-where)
        find-syms (vec (subvec qvec 1 find-end))
        in-syms (when idx-in (vec (subvec qvec (inc idx-in) idx-where)))
        where-clauses (when (pos? idx-where) (vec (subvec qvec (inc idx-where))))]
    {:find find-syms :in in-syms :where where-clauses}))

(defn- norm-ground [x]
  (cond
    (symbol? x) x
    (keyword? x) (if-let [ns* (namespace x)] (str ns* "/" (name x)) (name x))
    :else x))

(defn- norm-clause [clause]
  (cond
    (and (seq? clause) (= 'not (first clause)) (vector? (second clause)))
    (list 'not (mapv norm-ground (second clause)))

    (vector? clause)
    (mapv (fn [x]
            (if (seq? x)
              (apply list (map norm-ground x))
              (norm-ground x)))
          clause)

    :else clause))

(defn- records->db [records]
  (reduce (fn [db record]
            (let [subject (str (:db/id record))]
              (reduce (fn [acc [k v]]
                        (if (= k :db/id)
                          acc
                          (index/assert-quad acc
                                             {:s subject
                                              :p (if (keyword? k)
                                                   (if-let [ns* (namespace k)]
                                                     (str ns* "/" (name k))
                                                     (name k))
                                                   (str k))
                                              :o (stringify-complex v)}
                                             (constantly false))))
                      db
                      record)))
          (index/empty-db)
          records))

(defn archetypes->tx-data
  [loop-archetypes structural-strength-fn next-tempid!]
  (vec
   (for [[archetype-id params] loop-archetypes]
     (into {:db/id (next-tempid!) "archetype/id" (name archetype-id)}
           (remove (comp nil? second)
                   (cons ["archetype/structural-strength" (structural-strength-fn params)]
                         (for [[k v] params]
                           [(str "archetype/" (name k)) v])))))))

(defn entities->tx-data
  [entities next-tempid!]
  (vec
   (for [e entities]
     (let [id (:id e)
           s (:stocks e)]
       (into {:db/id (next-tempid!) "entity/id" (name id) "entity/org" (:org e)}
             (remove (comp nil? second)
                     [["entity/repos" (or (get-in s [:repos :value])
                                           (get-in s [:actor-repos :value]))]
                      ["entity/west-registered" (or (get-in s [:west-registered :value])
                                                     (get-in s [:west-registered-org-total :value]))]
                      ["entity/github-stars" (get-in s [:github-social-engagement :value :org-wide-star-total])]
                      ["entity/website-uniques-7d" (get-in s [:website-uniques-7d :value])]
                      ["entity/server-error-pct" (get-in s [:path-level-status-mix :value :server-error-pct])]
                      ["entity/f2-upper-bound-95pct" (get-in s [:f2-upper-bound-95pct :value])]]))))))

(defn fleet-categories->tx-data
  [fleets next-tempid!]
  (vec
   (for [{:keys [label observation]} fleets
         {:keys [id github-total west-registered-t0 west-registered-t1]} (:categories observation)
         :let [days (get-in observation [:window :days])
               backlog (- github-total west-registered-t1)
               rate (/ (- west-registered-t1 west-registered-t0) days)]]
     {:db/id (next-tempid!)
      "fleet/entity" label
      "fleet/category" (name id)
      "fleet/github-total" github-total
      "fleet/west-registered-t0" west-registered-t0
      "fleet/west-registered-t1" west-registered-t1
      "fleet/backlog" backlog
      "fleet/observed-rate-per-day" rate})))

(defn ingest!
  "Transacts all three datasets into one fresh in-memory datalog db.
   Returns the db value (callers historically named this `conn`)."
  [{:keys [archetypes structural-strength-fn entities fleets]}]
  (let [tempid (atom 0)
        next-tempid! (fn [] (swap! tempid dec))
        arch-tx (when (and archetypes structural-strength-fn)
                  (archetypes->tx-data archetypes structural-strength-fn next-tempid!))
        entity-tx (when entities (entities->tx-data entities next-tempid!))
        fleet-tx (when fleets (fleet-categories->tx-data fleets next-tempid!))]
    (records->db (concat arch-tx entity-tx fleet-tx))))

(defn- resolve-db [db-or-conn]
  "ingest! returns a datalog index db (map with :eavt). Callers may still name
   it `conn` historically; do not confuse it with a wrapper `{:db …}`."
  (cond
    (and (map? db-or-conn) (contains? db-or-conn :eavt)) db-or-conn
    (and (map? db-or-conn) (contains? db-or-conn :db)) (:db db-or-conn)
    :else db-or-conn))

(defn q
  "query-str: a real datalog query string. `db-or-conn` is the value returned
   by `ingest!`. Returns a vector of result tuples (DataScript-shaped; datalog
   itself returns a set)."
  [query-str db-or-conn & inputs]
  (let [db (resolve-db db-or-conn)
        {:keys [find in where]} (parse-vector-query query-str)
        in-syms (vec (remove #{'$} (or in [])))
        _ (when (not= (count in-syms) (count inputs))
            (throw (ex-info "loop-system-dynamics.query: :in arity mismatch"
                            {:in in-syms :inputs inputs})))]
    (vec (dl/q db {:find find :in in :where (mapv norm-clause where)}
               (constantly true)
               inputs))))
