import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { isConfigured, supabase } from "./supabase";
import type { ControlDef } from "./types";

type LoadStatus = "idle" | "loading" | "ready" | "error" | "unconfigured";

interface ConfigContextValue {
  status: LoadStatus;
  error: string | null;
  value: (control: ControlDef) => string;
  isOn: (control: ControlDef) => boolean;
  save: (control: ControlDef, next: string) => Promise<void>;
  savingId: string | null;
  reload: () => Promise<void>;
}

const ConfigContext = createContext<ConfigContextValue | null>(null);

export function ConfigProvider({ children }: { children: ReactNode }) {
  const [appConfig, setAppConfig] = useState<Record<string, string>>({});
  const [flags, setFlags] = useState<Record<string, boolean>>({});
  const [status, setStatus] = useState<LoadStatus>("idle");
  const [error, setError] = useState<string | null>(null);
  const [savingId, setSavingId] = useState<string | null>(null);

  const reload = useCallback(async () => {
    if (!isConfigured || !supabase) {
      setStatus("unconfigured");
      return;
    }
    setStatus("loading");
    setError(null);

    const [configResult, flagResult] = await Promise.all([
      supabase.from("app_config").select("key,value"),
      supabase.from("feature_flags").select("key,enabled"),
    ]);

    if (configResult.error || flagResult.error) {
      setError(configResult.error?.message ?? flagResult.error?.message ?? "Unknown read error");
      setStatus("error");
      return;
    }

    const nextConfig: Record<string, string> = {};
    (configResult.data ?? []).forEach((row: { key: string; value: string }) => {
      nextConfig[row.key] = row.value;
    });

    const nextFlags: Record<string, boolean> = {};
    (flagResult.data ?? []).forEach((row: { key: string; enabled: boolean }) => {
      nextFlags[row.key] = row.enabled;
    });

    setAppConfig(nextConfig);
    setFlags(nextFlags);
    setStatus("ready");
  }, []);

  useEffect(() => {
    void reload();
  }, [reload]);

  const value = useCallback(
    (control: ControlDef) => {
      if (!control.key) return control.fallback;
      if (control.table === "feature_flags") {
        const current = flags[control.key];
        return current === undefined ? control.fallback : String(current);
      }
      return appConfig[control.key] ?? control.fallback;
    },
    [appConfig, flags],
  );

  const isOn = useCallback((control: ControlDef) => value(control) === "true", [value]);

  const save = useCallback(
    async (control: ControlDef, next: string) => {
      if (!control.table || !control.key) return;
      if (!supabase) {
        setError("Supabase is not configured, so nothing can be saved.");
        return;
      }
      setSavingId(control.id);
      setError(null);

      if (control.table === "feature_flags") {
        const enabled = next === "true";
        const { error: writeError } = await supabase
          .from("feature_flags")
          .upsert({ key: control.key, enabled, description: control.help }, { onConflict: "key" });
        if (writeError) {
          setError(writeError.message);
        } else {
          setFlags((previous) => ({ ...previous, [control.key as string]: enabled }));
        }
      } else {
        const { error: writeError } = await supabase
          .from("app_config")
          .upsert({ key: control.key, value: next }, { onConflict: "key" });
        if (writeError) {
          setError(writeError.message);
        } else {
          setAppConfig((previous) => ({ ...previous, [control.key as string]: next }));
        }
      }
      setSavingId(null);
    },
    [],
  );

  const contextValue = useMemo(
    () => ({ status, error, value, isOn, save, savingId, reload }),
    [status, error, value, isOn, save, savingId, reload],
  );

  return <ConfigContext.Provider value={contextValue}>{children}</ConfigContext.Provider>;
}

export function useConfig(): ConfigContextValue {
  const context = useContext(ConfigContext);
  if (!context) throw new Error("useConfig must be used inside ConfigProvider");
  return context;
}
