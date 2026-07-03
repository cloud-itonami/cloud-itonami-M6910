# Open Business Blueprint: cloud-itonami-M6910

This repository publishes an OSS business model for operating a
company-incorporation / registration-agent service on itonami.cloud.

## Classification

- Repository name: `cloud-itonami-M6910`
- Primary classification: ISIC Rev.5 6910
- Activity: legal activities (company-formation / registration-agent
  services fall under this class in ISIC)
- Served domain: global company incorporation execution
- Original implementation context: designed alongside `cloud-itonami-6310`
  (`gftd-talent-actor`) and `ai-gftd-itonami` as the third instance of the
  contained-intelligence + independent-governor actor pattern

## Customer

Primary customers:

- founders incorporating in a jurisdiction they are not physically present
  in (cross-border formation)
- accelerators / VC back-office teams that repeatedly incorporate
  portfolio entities
- corporate-services providers and law firms who want a governed,
  auditable intake + assessment tool instead of ad hoc spreadsheets/email
- licensed registered agents who want to operate in a new jurisdiction
  without rebuilding a compliance stack from scratch

## Problem

Company-formation SaaS and agencies today are either narrow (one country),
opaque about which legal source justifies a requirement, or willing to
push a filing through without a clear, auditable KYC/sanctions/document
trail. A customer incorporating cross-border has no way to verify why an
agent asked for a given document, or to prove after the fact that a filing
was screened and approved properly.

## Offer

Operators provide a governed company-formation intake + execution tool:

- application intake and normalization
- per-jurisdiction document checklist + fee estimate, always citing an
  official source (never a fabricated requirement)
- KYC / sanctions screening gate on every officer and shareholder
- draft LEI (ISO 17442) + registry-number assignment
- human-approved filing handoff (the actor never files or pays alone)
- immutable audit ledger of every draft, hold, and approval

The core promise: the Registrar-LLM can draft and check, but it cannot
file or pay unless a human operator -- who holds the actual jurisdiction
license and liability -- approves.

## Revenue

Operators can sell:

- per-incorporation execution fee (intake through filing handoff)
- jurisdiction-pack licensing: a maintained, spec-cited requirement
  catalog for a specific country, kept current
- managed hosting: monthly subscription per tenant (accelerator, law firm)
- KYC/sanctions-screening add-on (integration with a real screening
  provider is the operator's responsibility)
- compliance package: audit export, retention, security review

| Package | Customer | Price shape |
|---|---|---|
| Per-filing | individual founder | flat fee per incorporation |
| Jurisdiction pack | corporate-services provider | subscription per country covered |
| Managed tenant | accelerator / law firm back office | monthly platform fee |
| Operator enablement | new registered agent | training + certification |

## Unit Economics

Track these numbers for every operator:

- setup hours per new jurisdiction added to `formation.facts`
- LLM cost per intake/assessment/screening operation
- KYC/sanctions-screening provider cost per officer
- human-approval hours per filing
- incident and audit hours
- gross margin after infrastructure, screening-provider and support costs

## Open Participation

Anyone may:

- fork the repository
- run the demo
- deploy a self-hosted instance
- submit issues and patches
- publish an additional jurisdiction pack (with a real official
  spec-basis citation)
- create a local operator business

itonami.cloud should require certification -- including proof of the
jurisdiction's actual company-formation / registered-agent license --
before listing an operator as a trusted provider or routing customer
leads.

## Operator Trust Levels

| Level | Capability |
|---|---|
| Contributor | patches, docs, issues, examples, jurisdiction packs |
| Self-host operator | runs their own instance with no platform endorsement |
| Certified operator | listed on itonami.cloud after review, including jurisdiction licensing proof |
| Managed operator | may receive leads and operate customer tenants |
| Core maintainer | can approve changes to governor, security and governance |

## Marketplace Metadata

```edn
{:itonami.blueprint/id "cloud-itonami-M6910"
 :itonami.blueprint/name "Global Incorporation Actor"
 :itonami.blueprint/isic-rev5 "6910"
 :itonami.blueprint/domain :legal/company-formation
 :itonami.blueprint/license "AGPL-3.0-or-later"
 :itonami.blueprint/operator-model :certified-open-business
 :itonami.blueprint/repo "https://github.com/cloud-itonami/cloud-itonami-M6910"
 :itonami.blueprint/status :public-oss}
```

## Non-Negotiables

- Do not commit real customer/officer identification documents or
  screening results.
- Do not bypass the RegistrarGovernor for a filing or payment.
- Do not add a jurisdiction to `formation.facts` without a real,
  citable official source.
- Do not market an uncertified deployment as an itonami.cloud certified
  operator, and do not operate in a jurisdiction without the license that
  jurisdiction actually requires of a registered agent / formation
  professional.
