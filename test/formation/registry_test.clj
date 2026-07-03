(ns formation.registry-test
  "Conformance tests for `formation.registry` -- the LEI/ISO 7064 MOD 97-10
  arithmetic ported from `matsurigoto`'s corp-registry module
  (etzhayyim/root, ADR-2606062300); these tests are adapted from that
  module's own conformance suite so the port stays provably equivalent."
  (:require [clojure.test :refer [deftest is]]
            [formation.registry :as r]))

(deftest certificate-is-a-draft-not-a-real-filing
  (let [result (r/register-incorporation "Co" ["o"] 0 "art" "addr" "JPN" 1)]
    (is (nil? (get-in result ["certificate" "proof"])))
    (is (= (get-in result ["certificate" "issued_by_registry"]) false))
    (is (= (get-in result ["certificate" "status"]) "draft-unsigned"))))

(deftest lei-roundtrip-validates
  (let [lei (r/assign-lei "OPER" "000000000001")]
    (is (= (count lei) 20))
    (is (r/validate-lei lei))))

(deftest lei-check-digits-make-mod97-one-for-many-entities
  (doseq [n (range 1 50)]
    (let [lei (r/assign-lei "OPER" (format "%012d" n))]
      (is (r/validate-lei lei) lei))))

(deftest lei-corruption-detected
  (let [lei (r/assign-lei "OPER" "000000000042")
        bad (vec lei)
        bad (assoc bad 8 (if (not= (nth bad 8) \Z) \Z \Y))]
    (is (r/validate-lei lei))
    (is (not (r/validate-lei (apply str bad))))))

(deftest lei-rejects-bad-length-and-chars
  (is (not (r/validate-lei "TOOSHORT")))
  (is (not (r/validate-lei "OPER00000000000001*9")))
  (is (not (r/validate-lei 12345))))

(deftest incorporation-assigns-registry-number-and-lei
  (let [result (r/register-incorporation "Kotoba Trading GK" ["officer:tanaka"] 10000000
                                         "articles" "東京都" "JPN" 7)]
    (is (= (get result "registry_number") "JPN-00000007"))
    (is (r/validate-lei (get result "lei")))
    (is (= (get-in result ["record" "immutable"]) true))
    (is (= (get-in result ["record" "kind"]) "incorporation-draft"))))

(deftest incorporation-validation-rules
  (let [bad-args [["" ["o"] 0 "a" "ad"]
                  ["Co" [] 0 "a" "ad"]
                  ["Co" ["o"] -1 "a" "ad"]
                  ["Co" ["o"] 0 "" "ad"]
                  ["Co" ["o"] 0 "a" ""]]]
    (doseq [[name officers capital articles address] bad-args]
      (is (thrown? Exception
                   (r/register-incorporation name officers capital articles address "JPN" 1))))))

(deftest change-is-append-only
  (let [inc (r/register-incorporation "Co" ["o"] 0 "a" "ad" "JPN" 1)
        hist (r/append [] inc)
        chg (r/register-change (get inc "registry_number") {"address" "new"} "2026-07-03")
        hist2 (r/append hist chg)]
    (is (and (= (count hist) 1) (= (count hist2) 2)))
    (is (= (get-in hist2 [0 "kind"]) "incorporation-draft"))
    (is (= (get-in hist2 [1 "kind"]) "change-draft"))))
