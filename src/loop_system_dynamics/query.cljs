(ns loop-system-dynamics.query
  "A real DataScript (npm `datascript`, same package + same JS-interop
   convention as com-junkawasaki/root's manifest/edn-query.cljs) query layer
   over this loop's facts.

   Why this exists: ADR-2607203000 asked for entity/actor data to be
   'DataScript/Datomic query で接続' (connected via DataScript/Datomic query),
   not just read from a static file. Every cycle before this one answered that
   by hand-copying `gh api` output into resources/entities-seed.edn's nested
   maps -- real and sourced, but only queryable by eyeballing the file. This
   namespace ingests two real, already-sourced datasets --
   kotoba-lang/dynamics's `loop-archetypes` catalog and a curated flat subset
   of this repo's own `entities-seed.edn` -- as datoms, so a caller can ask a
   genuine `:find/:where` datalog question instead of grepping prose.

   This does NOT replace entities-seed.edn as the source of truth: the seed
   stays the hand-curated, dated, sourced snapshot (see README 'Extending
   coverage'). This is a second, queryable *projection* of the same real
   facts. Convention (matches manifest/edn-query.cljs exactly, so anyone who
   already knows that tool already knows this one): datascript.js exposes
   attributes as BARE strings (no leading colon) except \":db/id\" itself,
   which is a colon-prefixed string key on the JS object; datalog queries are
   plain query strings, e.g. \"[:find ?id :where [?e \\\"archetype/id\\\" ?id]]\"."
  (:require ["datascript" :as ds-mod]))

(def ds (.-default ds-mod))

(defn- stringify-complex
  "Same rule as manifest/edn-datomize.cljs: a nested map/vector becomes a
   pr-str string blob (preserves the real data for provenance/debugging
   without forcing irregularly-shaped domain data into a rigid attribute
   schema). Scalars pass through unchanged."
  [v]
  (if (or (map? v) (vector? v) (list? v) (set? v))
    (pr-str v)
    v))

(defn- entity-map->js
  "{:db/id <int> \"ns/attr\" val ...} -> datascript.js entity object.
   :db/id becomes the special colon-prefixed JS key; every other key is
   expected to already be a bare attribute string (this namespace's
   ->tx-data fns produce that shape directly, matching entity->js in
   manifest/edn-query.cljs)."
  [m]
  (let [obj (js-obj)]
    (doseq [[k v] m]
      (if (= k :db/id)
        (aset obj ":db/id" v)
        (aset obj k (stringify-complex v))))
    obj))

(defn archetypes->tx-data
  "loop-archetypes (from dynamics.core, passed in so this namespace has no
   hard dependency on dynamics.core's own require path) -> a vector of entity
   maps ready for entity-map->js. One entity per archetype.
   structural-strength is computed with the SAME dynamics.core/
   loop-structural-strength fn the caller passes in, so this can never
   silently drift from the real scoring formula.
   next-tempid!: a 0-arg fn returning a fresh negative int each call (see
   ingest! below) -- DataScript tempids must be numbers, not strings."
  [loop-archetypes structural-strength-fn next-tempid!]
  (vec
   (for [[archetype-id params] loop-archetypes]
     (into {:db/id (next-tempid!) "archetype/id" (name archetype-id)}
           (remove (comp nil? second)
                   (cons ["archetype/structural-strength" (structural-strength-fn params)]
                         (for [[k v] params]
                           [(str "archetype/" (name k)) v])))))))

(defn entities->tx-data
  "A curated FLAT subset of the observed entities (from loop-system-dynamics.
   core/observe) -- only fields that already exist as clean scalar values in
   entities-seed.edn (never a re-derivation/re-parse of prose). Fields absent
   for a given entity are simply omitted from that entity's datoms, not
   defaulted to 0 or nil -- 'not yet checked' and 'checked and zero' stay
   distinguishable, same discipline as the rest of this loop."
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

(defn ingest!
  "Transacts both datasets into one fresh in-memory DataScript conn and
   returns it. Callers hold the conn and call `q` against it -- no global
   mutable state in this namespace, so tests can build independent confs
   in parallel."
  [{:keys [archetypes structural-strength-fn entities]}]
  (let [conn (.create_conn ds)
        tempid (atom 0)
        next-tempid! (fn [] (swap! tempid dec))
        arch-tx (when (and archetypes structural-strength-fn)
                  (archetypes->tx-data archetypes structural-strength-fn next-tempid!))
        entity-tx (when entities (entities->tx-data entities next-tempid!))
        all-tx (into-array (map entity-map->js (concat arch-tx entity-tx)))]
    (.transact ds conn all-tx)
    conn))

(defn q
  "query-str: a real datalog query, as a STRING (e.g.
   \"[:find ?id ?stars :where [?e \\\"entity/id\\\" ?id] [?e \\\"entity/github-stars\\\" ?stars]]\").
   Matches manifest/edn-query.cljs's own `q` mode exactly -- same query
   syntax works unmodified against either tool. Returns a Clojure data
   structure (a set of result tuples, as vectors)."
  [query-str conn]
  (js->clj (.q ds query-str (.db ds conn))))
