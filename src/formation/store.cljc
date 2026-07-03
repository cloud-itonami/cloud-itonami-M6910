(ns formation.store
  "SSoT for the formation actor, behind a `Store` protocol so the backend is
  a swap, not a rewrite -- the same seam `gftd-talent-actor` /
  `ai-gftd-itonami` use:

    - `MemStore`     -- atom of EDN. The deterministic default for
                        dev/tests/demo (no deps). R0 of this actor ships
                        only this backend; a `DatomicStore` (`langchain.db`,
                        swappable to Datomic Local or a kotoba-server pod)
                        is the natural next seam -- see README `Status`.

  The ledger stays append-only: 'who filed what, for which customer, on
  what jurisdictional basis, approved by whom' is always a query over an
  immutable log -- the audit trail a customer trusting an operator with
  their incorporation needs, and the evidence an operator needs if a
  filing is later disputed."
  (:require [formation.registry :as registry]))

(defprotocol Store
  (application [s id])
  (all-applications [s])
  (officer [s id])
  (kyc-of [s officer-id] "committed KYC screening verdict for an officer, or nil")
  (assessment-of [s app-id] "committed jurisdiction assessment (doc checklist + fee estimate), or nil")
  (ledger [s])
  (next-sequence [s jurisdiction] "next registry-number sequence for a jurisdiction")
  (commit-record! [s record] "apply a committed op's record to the SSoT")
  (append-ledger! [s fact]   "append one immutable decision fact")
  (with-applications [s apps] "replace/seed the application directory (map id->application)")
  (with-officers [s officers] "replace/seed the officer directory (map id->officer)"))

;; ----------------------------- demo data -----------------------------

(defn demo-data
  "A small, self-contained customer set so the actor + tests run offline."
  []
  {:applications
   {"app-1" {:id "app-1" :entity-name "Kotoba Trading GK" :jurisdiction "JPN"
             :officers ["o-1"] :capital 1000000 :articles "定款ドラフト v1"
             :address "東京都千代田区1-1-1" :status :intake}
    "app-2" {:id "app-2" :entity-name "Nowhere Holdings Ltd" :jurisdiction "ATL"
             :officers ["o-2"] :capital 100 :articles "draft"
             :address "unknown" :status :intake}}
   :officers
   {"o-1" {:id "o-1" :name "田中 一郎" :sanctions-hit? false :id-doc "passport-jp-****1234"}
    "o-2" {:id "o-2" :name "J. Doe" :sanctions-hit? true :id-doc nil}}})

;; ----------------------------- MemStore (default) -----------------------------

(defrecord MemStore [a]
  Store
  (application [_ id] (get-in @a [:applications id]))
  (all-applications [_] (sort-by :id (vals (:applications @a))))
  (officer [_ id] (get-in @a [:officers id]))
  (kyc-of [_ id] (get-in @a [:kyc id]))
  (assessment-of [_ app-id] (get-in @a [:assessments app-id]))
  (ledger [_] (:ledger @a))
  (next-sequence [_ jurisdiction]
    (get-in @a [:sequences jurisdiction] 0))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :application/upsert
      (swap! a update-in [:applications (:id value)] merge value)

      :assessment/set
      (swap! a assoc-in [:assessments (first path)] payload)

      :kyc/set
      (swap! a assoc-in [:kyc (first path)] payload)

      :filing/mark-submitted
      (let [app-id (first path)
            app (get-in @a [:applications app-id])
            seq-n (get-in @a [:sequences (:jurisdiction app)] 0)
            result (registry/register-incorporation
                    (:entity-name app) (mapv #(officer s %) (:officers app))
                    (:capital app) (:articles app) (:address app)
                    (:jurisdiction app) seq-n)]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:sequences (:jurisdiction app)] (fnil inc 0))
                       (update-in [:applications app-id] merge
                                  {:status :filed
                                   :registry-number (get result "registry_number")
                                   :lei (get result "lei")})
                       (update :registry registry/append result))))
        result)
      nil)
    s)
  (append-ledger! [_ fact] (swap! a update :ledger conj fact) fact)
  (with-applications [s apps] (when (seq apps) (swap! a assoc :applications apps)) s)
  (with-officers [s officers] (when (seq officers) (swap! a assoc :officers officers)) s))

(defn seed-db
  "A MemStore seeded with the demo customer set. The deterministic default."
  []
  (->MemStore (atom (assoc (demo-data)
                           :assessments {} :kyc {} :ledger [] :sequences {} :registry []))))

(defn registry-history
  "The append-only registry-record history (formation.registry drafts)."
  [^MemStore store]
  (:registry @(:a store)))
