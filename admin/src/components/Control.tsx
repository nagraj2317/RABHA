import { useEffect, useState } from "react";
import { useConfig } from "../lib/ConfigContext";
import type { ControlDef } from "../lib/types";
import { Badge } from "./ui";

/** Controls the product forbids turning on. They stay visible and disabled, never silently absent. */
const LOCKED_OFF = new Set([
  "ads_background",
  "ads_notification_bar",
  "ads_targeting",
  "sec_rls",
  "adv_impersonate",
]);

export function Control({ control, disabled }: { control: ControlDef; disabled: boolean }) {
  const { value, isOn, save, savingId } = useConfig();
  const current = value(control);
  const [draft, setDraft] = useState(current);
  const [dirty, setDirty] = useState(false);

  useEffect(() => {
    if (!dirty) setDraft(current);
  }, [current, dirty]);

  const locked = LOCKED_OFF.has(control.id);
  const saving = savingId === control.id;

  const commit = async (next: string) => {
    setDirty(false);
    await save(control, next);
  };

  return (
    <div className="flex flex-col gap-2 rounded-xl border border-white/10 bg-white/[0.02] p-4 sm:flex-row sm:items-center sm:justify-between">
      <div className="pr-4">
        <div className="flex items-center gap-2">
          <p className="text-sm font-medium text-rabha-text">{control.label}</p>
          {locked ? <Badge tone="danger">Locked</Badge> : null}
          {saving ? <Badge tone="amber">Saving</Badge> : null}
        </div>
        <p className="mt-1 text-xs leading-relaxed text-rabha-muted">{control.help}</p>
        {control.table && control.key ? (
          <p className="mt-1 font-mono text-[10px] text-rabha-muted/70">
            {control.table}.{control.key}
          </p>
        ) : null}
      </div>

      <div className="flex shrink-0 items-center gap-2">
        {control.kind === "toggle" ? (
          <button
            type="button"
            role="switch"
            aria-checked={isOn(control)}
            aria-label={control.label}
            disabled={disabled || locked || saving}
            onClick={() => void commit(isOn(control) ? "false" : "true")}
            className={`relative h-7 w-12 rounded-full transition ${
              isOn(control) ? "bg-rabha-amber" : "bg-white/15"
            } ${disabled || locked ? "cursor-not-allowed opacity-50" : ""}`}
          >
            <span
              className={`absolute top-1 h-5 w-5 rounded-full bg-black transition-all ${
                isOn(control) ? "left-6" : "left-1"
              }`}
            />
          </button>
        ) : null}

        {control.kind === "number" ? (
          <div className="flex items-center gap-2">
            <input
              type="number"
              aria-label={control.label}
              value={draft}
              min={control.min}
              max={control.max}
              disabled={disabled || saving}
              onChange={(event) => {
                setDraft(event.target.value);
                setDirty(true);
              }}
              onBlur={() => {
                if (draft !== current) void commit(draft);
              }}
              className="w-24 rounded-lg border border-white/15 bg-black/40 px-3 py-1.5 text-sm text-rabha-text outline-none focus:border-rabha-amber"
            />
            <span className="text-xs text-rabha-muted">{control.unit}</span>
          </div>
        ) : null}

        {control.kind === "text" ? (
          <input
            type="text"
            aria-label={control.label}
            value={draft}
            disabled={disabled || saving}
            onChange={(event) => {
              setDraft(event.target.value);
              setDirty(true);
            }}
            onBlur={() => {
              if (draft !== current) void commit(draft);
            }}
            className="w-64 rounded-lg border border-white/15 bg-black/40 px-3 py-1.5 text-sm text-rabha-text outline-none focus:border-rabha-amber"
          />
        ) : null}

        {control.kind === "action" ? (
          <button
            type="button"
            disabled
            className="cursor-not-allowed rounded-lg border border-white/15 px-3 py-1.5 text-xs text-rabha-muted"
          >
            Not available
          </button>
        ) : null}
      </div>
    </div>
  );
}
