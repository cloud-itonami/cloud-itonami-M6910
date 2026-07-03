(ns formation.facts
  "Per-jurisdiction incorporation requirement catalog -- the G2-style
  spec-basis table the RegistrarGovernor checks every jurisdiction/assess
  proposal against ('did the advisor cite an OFFICIAL public source for
  this jurisdiction's requirements, or did it invent one?').

  Coverage is reported HONESTLY (see `coverage`), the same discipline
  `matsurigoto` (etzhayyim/root, ADR-2606062300) uses for its country
  profiles and `ooyake` (etzhayyim/root) uses for its civic wayfinding
  seed: a jurisdiction not in this table has NO spec-basis, full stop --
  the advisor must not fabricate one, and the governor holds if it tries.

  Seed values are drawn from each jurisdiction's official companies
  registry (see `:provenance`); they are a STARTING catalog, not a
  from-scratch survey of all ~194 jurisdictions. Extending coverage is
  additive: add one map to `catalog`, cite a real source, done -- never
  invent a jurisdiction's requirements to make coverage look bigger.")

(def catalog
  "iso3 -> requirement map. `:required-docs` mirrors the generic filing
  checklist every registry asks for in some form; `:legal-basis` /
  `:owner-authority` / `:provenance` are the G2 citation the governor
  requires before any :jurisdiction/assess proposal can commit."
  {"JPN" {:name "Japan"
          :owner-authority "法務局 (Legal Affairs Bureau)"
          :legal-basis "商業登記法 / 会社法"
          :national-spec "登記・供託オンライン申請システム"
          :provenance "https://www.moj.go.jp/MINJI/minji06.html"
          :required-docs ["定款 (articles of incorporation, notarised for 株式会社)"
                          "発起人・取締役の本人確認書類"
                          "本店所在地の証明"
                          "資本金の払込証明"
                          "登記申請書 + 登録免許税"]}
   "USA-DE" {:name "United States -- Delaware (exemplar; federalism note below)"
             :owner-authority "Delaware Division of Corporations"
             :legal-basis "Delaware General Corporation Law (Title 8)"
             :national-spec "Delaware One Stop / DCIS"
             :provenance "https://corp.delaware.gov/"
             :notes "No federal corporate registry -- incorporation is per-state; Delaware is an exemplar, not a national authority."
             :required-docs ["Certificate of Incorporation"
                             "Registered agent with a Delaware address"
                             "Incorporator identification"
                             "Franchise tax / filing fee"]}
   "GBR" {:name "United Kingdom"
          :owner-authority "Companies House"
          :legal-basis "Companies Act 2006"
          :national-spec "Companies House online + API / GLEIF LEI"
          :provenance "https://www.gov.uk/government/organisations/companies-house"
          :required-docs ["Proposed company name (availability check)"
                          "Registered office address"
                          "Director + PSC (person of significant control) identification"
                          "Memorandum + Articles of Association"
                          "Filing fee"]}
   "DEU" {:name "Germany"
          :owner-authority "Amtsgericht -- Handelsregister"
          :legal-basis "Handelsgesetzbuch (HGB)"
          :national-spec "Handelsregister / unternehmensregister.de + EU BRIS"
          :provenance "https://www.handelsregister.de/"
          :required-docs ["Notarised Gesellschaftsvertrag (articles)"
                          "Managing director identification"
                          "Registered office address"
                          "Minimum share capital deposit proof (GmbH: EUR 25,000 / 12,500 paid-in)"]}
   "EST" {:name "Estonia"
          :owner-authority "Centre of Registers and Information Systems (RIK)"
          :legal-basis "Commercial Code (Äriseadustik)"
          :national-spec "e-Business Register (company in minutes)"
          :provenance "https://www.rik.ee/en"
          :required-docs ["e-Residency or Estonian ID/eID"
                          "Articles of association"
                          "Registered office / contact person (for non-resident founders)"
                          "Share capital declaration"]}
   "KOR" {:name "South Korea"
          :owner-authority "대법원 등기소 (Court Registry)"
          :legal-basis "상법 / 상업등기법"
          :national-spec "인터넷등기소 (IROS)"
          :provenance "https://www.iros.go.kr/"
          :required-docs ["정관 (articles of incorporation)"
                          "발기인·이사 신원 확인"
                          "본점 소재지 증명"
                          "자본금 납입 증명"]}
   "IND" {:name "India"
          :owner-authority "Ministry of Corporate Affairs (MCA)"
          :legal-basis "Companies Act 2013"
          :national-spec "MCA21 / SPICe+"
          :provenance "https://www.mca.gov.in/"
          :required-docs ["Digital Signature Certificate (DSC) for each director"
                          "Director Identification Number (DIN)"
                          "Memorandum + Articles of Association"
                          "Registered office proof"]}
   "SGP" {:name "Singapore"
          :owner-authority "Accounting and Corporate Regulatory Authority (ACRA)"
          :legal-basis "Companies Act 1967"
          :national-spec "BizFile+"
          :provenance "https://www.acra.gov.sg/"
          :required-docs ["Proposed name (ACRA name-availability check)"
                          "Registered office address in Singapore"
                          "At least one locally resident director"
                          "Company constitution"]}
   "NZL" {:name "New Zealand"
          :owner-authority "New Zealand Companies Office"
          :legal-basis "Companies Act 1993"
          :national-spec "Companies Register (companiesoffice.govt.nz)"
          :provenance "https://companies-register.companiesoffice.govt.nz/"
          :required-docs ["Reserved company name"
                          "At least one director who lives in NZ or Australia"
                          "Registered office + address for service"
                          "Consent forms for directors + shareholders"]}
   "CAN" {:name "Canada"
          :owner-authority "Corporations Canada (federal) / provincial registries"
          :legal-basis "Canada Business Corporations Act"
          :national-spec "Corporations Canada online filing"
          :provenance "https://ised-isde.canada.ca/site/corporations-canada/en"
          :notes "Federal incorporation coexists with 13 provincial/territorial registries; this entry covers the federal path only."
          :required-docs ["NUANS name search report"
                          "Articles of Incorporation"
                          "Registered office address in Canada"
                          "Initial director(s) identification"]}})

(defn spec-basis
  "The jurisdiction's requirement map, or nil -- nil means NO spec-basis,
  and the governor must hold any proposal that tries to file for it."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report: how many of the requested jurisdictions actually
  have a spec-basis entry. Never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-M6910 R0: " (count catalog)
                 " jurisdictions seeded with an official spec-basis. "
                 "This is a starting catalog, not a survey of all ~194 "
                 "jurisdictions -- extend `formation.facts/catalog`, never "
                 "fabricate a jurisdiction's requirements.")})))

(defn required-docs-satisfied?
  "Does `submitted` (a set/coll of doc keywords or strings) satisfy every
  required doc listed for `iso3`? Missing spec-basis -> never satisfied."
  [iso3 submitted]
  (when-let [{:keys [required-docs]} (spec-basis iso3)]
    (let [need (count required-docs)
          have (count (filter (set submitted) required-docs))]
      (= need have))))

(defn doc-checklist [iso3]
  (:required-docs (spec-basis iso3) []))
