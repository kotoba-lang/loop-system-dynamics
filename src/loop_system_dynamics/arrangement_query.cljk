(ns loop-system-dynamics.arrangement-query
  "A SECOND real query engine over the same real fleet-registration facts
   loop_system_dynamics/query.cljs already ingests into DataScript --
   kotoba-lang/arrangement's `[s p o]` triple store + `arrangement.datalog`'s
   Datomic-shaped conjunctive `:find`/`:where` join (negation, aggregation,
   recursive rules -- a genuinely more capable query language than
   DataScript's own subset, see arrangement's own docstrings).

   Why this exists: the README 'Next' section asked for `kotoba-lang/kqe`
   to be wired in as a query source. `kqe` itself is retired -- its content
   (a pure `[s p o]` pattern router with no storage of its own) was merged
   into `kotoba-lang/arrangement` as `arrangement.query`/`arrangement.datalog`
   (see the kqe repo's own README). This namespace is that integration,
   pointed at arrangement rather than a repo that no longer exists.

   Real constraint this integration respects rather than works around:
   arrangement's `[s p o]` triples currently only support OPAQUE STRING
   values (`General typed values are a follow-up`, per arrangement's own
   README) -- there is no native number type. Every fleet fact here is
   therefore stored as its `str` form. This is safe for EQUALITY/
   INEQUALITY queries (`\"9\" = \"9\"`, `(not= \"9\" \"67\")`) but NOT for
   numeric ORDERING (`\"9\" > \"67\"` is true lexicographically but false
   numerically) -- queries in this namespace and its tests stick to
   equality/inequality for exactly this reason, never `<`/`>` on a fleet
   number. Silently doing numeric-ordering queries against string values
   would produce quietly wrong answers on real multi-digit backlogs (e.g.
   cloud-itonami's historical isco backlog of 124 vs iso's 6) -- worse than
   not offering the query at all.

   `visible?` is REQUIRED by `arrangement.query`/`arrangement.datalog`
   themselves (ADR-2607050500, 'Query as first-class effect') -- this
   namespace does not default it away. `(constantly true)` is the correct
   choice for this domain specifically: every fleet-registration fact here
   is already a public, already-published finding (real GitHub repo counts
   and manifest/west.yml registration status, the same facts the checked-in
   markdown reports already show) -- there is no access-control boundary
   inside this dataset to enforce."
  (:require [arrangement.core :as arr]
            [arrangement.datalog :as dl]))

(defn fleet-categories->triples
  "Same `fleets` shape as loop_system_dynamics.query/fleet-categories->tx-data
   ({:label _ :observation _} maps) and the SAME backlog/observed-rate-per-day
   formula loop_system_dynamics.fleet_registration_xmile's own build-model
   uses -- so a caller can cross-check this engine's answer against
   DataScript's for the exact same real facts (see the test namespace).
   Subject is `\"<label>:<category>\"` (e.g. \"cloud-itonami:isco\"); every
   value is stringified per this namespace's own opaque-string constraint."
  [fleets]
  (vec
   (for [{:keys [label observation]} fleets
         {:keys [id github-total west-registered-t0 west-registered-t1]} (:categories observation)
         :let [days (get-in observation [:window :days])
               subject (str label ":" (name id))
               backlog (- github-total west-registered-t1)
               rate (/ (- west-registered-t1 west-registered-t0) days)]
         [p o] [["fleet/entity" label]
                ["fleet/category" (name id)]
                ["fleet/github-total" (str github-total)]
                ["fleet/west-registered-t0" (str west-registered-t0)]
                ["fleet/west-registered-t1" (str west-registered-t1)]
                ["fleet/backlog" (str backlog)]
                ["fleet/observed-rate-per-day" (str rate)]]]
     {:s subject :p p :o o})))

(defn ingest!
  "Builds a fresh in-memory arrangement db (immutable value, not a mutable
   conn like DataScript's -- callers hold and query the returned db
   directly, same as arrangement's own README example)."
  [{:keys [fleets]}]
  (reduce arr/assert-quad (arr/empty-db) (fleet-categories->triples fleets)))

(def q
  "Re-exported directly from arrangement.datalog -- `(q db query visible?)`.
   No wrapper needed: arrangement's own signature is already the right
   shape (a real EDN query map, not a query string like DataScript's `q/q`
   -- arrangement.datalog has no string-query parser, by design)."
  dl/q)
