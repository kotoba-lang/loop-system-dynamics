#!/usr/local/bin/python3
"""yata-sd-tick.py — yataverse system-dynamics tick (no_agent measure job).

Runs ONE loop-system-dynamics cycle per tick, rotated across the runnable
entry points, appending one line to the profile-local append-only ledger.
Never edits repo files: the repo ledger/report writes that the loop performs
inside the shared checkout are reverted before exit (the loop's durable home
is upstream git history; the profile ledger is the bot's own evidence).

Output: MEASURE<TAB>key<TAB>value lines on stdout (empty stdout = silent).
Sync failure or loop failure prints REFUSED + reason and exits 2.
"""
import json
import os
import re
import subprocess
import sys
import time
from datetime import datetime, timezone

PROFILE = os.path.expanduser("~/.hermes/profiles/yataverse-sd")
LEDGER = os.path.join(PROFILE, "workspace", "yata-sd-ledger.jsonl")
LOOP = os.path.expanduser(
    "~/github/com-junkawasaki/orgs/kotoba-lang/loop-system-dynamics"
)
STATE = os.path.join(PROFILE, "workspace", "tick-state.json")

# Rotation order (runnable under kbb --backend sci, all verified 2026-09-14).
CYCLES = [
    "bin/run_cloud_itonami_leverage.cljk",
    "bin/run_cloud_itonami_xmile.cljk",
    "bin/run_kotoba_lang_xmile.cljk",
    "bin/run_world_bank_money.cljk",
]
CLASSPATH_PARTS = [
    "src", "bin", "../text/src", "../edn/src", "../org-oasis-open-xmile/src",
    "../org-omg-sysmlv2/src", "../dsl-core/src", "../datalog/src",
    "../dynamics/src",
]
ROOTS_NAMES = [
    ".",
    "../text",
    "../edn",
    "../org-oasis-open-xmile",
    "../org-omg-sysmlv2",
    "../dsl-core",
    "../datalog",
    "../dynamics",
]


def emit(key, value):
    print(f"MEASURE\t{key}\t{value}")


def refused(reason):
    print(f"REFUSED: {reason}")
    sys.exit(2)


def loop_root_is_clean():
    r = subprocess.run(
        ["git", "status", "--porcelain"], cwd=LOOP, capture_output=True, text=True
    )
    if r.returncode != 0:
        refused(f"git status failed in {LOOP}: {r.stderr.strip()[:120]}")
    return r.stdout.strip() == ""


def revert_loop_writes():
    """The loop appends to repo ledger/ and writes target/. Revert tracked
    edits (append-only ledger lines from this tick); target/ is gitignored."""
    subprocess.run(["git", "checkout", "--", "ledger/"], cwd=LOOP, capture_output=True)
    r = subprocess.run(
        ["git", "status", "--porcelain"], cwd=LOOP, capture_output=True, text=True
    )
    return r.stdout.strip() == ""


def main():
    os.makedirs(os.path.dirname(LEDGER), exist_ok=True)

    # --- preconditions: checkout exists, clean, kbb present -----------------
    if not os.path.isdir(os.path.join(LOOP, "src")):
        refused(f"loop-system-dynamics checkout missing at {LOOP}")
    if not loop_root_is_clean():
        refused("shared checkout is dirty before tick; not running the loop")
    kbb = subprocess.run(["which", "kbb"], capture_output=True, text=True)
    if kbb.returncode != 0 or not kbb.stdout.strip():
        refused("kbb not on PATH")

    # --- rotation state ------------------------------------------------------
    idx = 0
    if os.path.exists(STATE):
        try:
            idx = json.load(open(STATE)).get("idx", 0) % len(CYCLES)
        except Exception:
            idx = 0
    script = CYCLES[idx]

    roots = json.dumps(
        [os.path.realpath(os.path.join(LOOP, n)) for n in ROOTS_NAMES]
    )

    env = dict(os.environ)
    env["NBB_CLJK_ROOTS"] = roots
    env.pop("HERMES_HOME", None)

    classpath = ":".join(
        os.path.realpath(os.path.join(LOOP, part)) for part in CLASSPATH_PARTS
    )

    t0 = time.time()
    r = subprocess.run(
        ["kbb", "--backend", "sci", "--classpath", classpath, os.path.join(LOOP, script)],
        cwd=LOOP, capture_output=True, text=True, env=env, timeout=600,
    )
    dur = round(time.time() - t0, 1)

    if r.returncode != 0:
        revert_loop_writes()
        tail = (r.stderr or r.stdout).strip().splitlines()
        fingerprint = next(
            (ln for ln in tail if "Error" in ln or "not find namespace" in ln), ""
        )
        refused(f"loop rc={r.returncode} script={script} {fingerprint[:160]}")

    # --- parse loop stdout ----------------------------------------------------
    out = r.stdout
    top_intervention = None
    m = re.search(r"top intervention:\s*(:\S+)", out)
    if m:
        top_intervention = m.group(1)
    m = re.search(r"top 3:\s*\[([^\]]*)\]", out)
    top3 = (
        [x.strip() for x in m.group(1).split() if x.strip()] if m else None
    )
    stalled = None
    m = re.search(r"stalled categories:\s*\[([^\]]*)\]", out)
    if m:
        stalled = [x.strip() for x in m.group(1).split(",") if x.strip()]
    depletes = None
    m = re.search(r"depletes within horizon:\s*\[([^\]]*)\]", out)
    if m:
        depletes = [x.strip() for x in m.group(1).split(",") if x.strip()]
    report = None
    m = re.search(r"report:\s*(\S+)", out)
    if m:
        report = m.group(1)
    ledger_rel = None
    m = re.search(r"ledger entry appended to:\s*(\S+)", out)
    if m:
        ledger_rel = m.group(1)

    reverted = revert_loop_writes()

    now = datetime.now(timezone.utc).isoformat(timespec="seconds")
    entry = {
        "event/as-of": now,
        "event/script": script,
        "event/rc": r.returncode,
        "event/duration-seconds": dur,
        "event/top-intervention": top_intervention,
        "event/top3": top3,
        "event/stalled": stalled,
        "event/depletes-within-horizon": depletes,
        "event/report": report,
        "event/loop-ledger": ledger_rel,
        "event/loop-checkout-reverted": reverted,
    }
    with open(LEDGER, "a") as fh:
        fh.write(json.dumps(entry, ensure_ascii=False) + "\n")

    with open(STATE, "w") as fh:
        json.dump({"idx": (idx + 1) % len(CYCLES), "last-as-of": now}, fh)

    emit("tick", script)
    emit("rc", r.returncode)
    emit("duration-seconds", dur)
    emit("top-intervention", top_intervention or "n/a")
    emit("top3", top3)
    emit("stalled", stalled)
    emit("depletes-within-horizon", depletes)
    emit("ledger-appended", LEDGER)
    emit("loop-checkout-reverted", reverted)
    return 0


if __name__ == "__main__":
    sys.exit(main())
