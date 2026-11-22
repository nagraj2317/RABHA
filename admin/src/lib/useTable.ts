import { useCallback, useEffect, useState } from "react";
import { isConfigured, supabase } from "./supabase";

export interface TableState<T> {
  rows: T[];
  loading: boolean;
  error: string | null;
  reload: () => Promise<void>;
}

/** Reads one table or view under RLS. A policy block surfaces as an error, never as an empty list. */
export function useTable<T>(table: string, order?: { column: string; ascending?: boolean }, limit = 200): TableState<T> {
  const [rows, setRows] = useState<T[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(async () => {
    if (!isConfigured || !supabase) {
      setError("Supabase is not configured for this build.");
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);

    let query = supabase.from(table).select("*").limit(limit);
    if (order) {
      query = query.order(order.column, { ascending: order.ascending ?? false });
    }

    const { data, error: readError } = await query;
    if (readError) {
      setError(readError.message);
      setRows([]);
    } else {
      setRows((data ?? []) as T[]);
    }
    setLoading(false);
  }, [table, order?.column, order?.ascending, limit]);

  useEffect(() => {
    void reload();
  }, [reload]);

  return { rows, loading, error, reload };
}
