import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import type { Session } from "@supabase/supabase-js";
import { isConfigured, supabase } from "../lib/supabase";
import type { Role } from "../lib/types";

interface AuthContextValue {
  session: Session | null;
  roles: Role[];
  loading: boolean;
  error: string | null;
  signIn: (email: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  canSee: (allowed: Role[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(null);
  const [roles, setRoles] = useState<Role[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadRoles = useCallback(async () => {
    if (!supabase) return;
    const { data, error: roleError } = await supabase.from("admin_roles").select("role");
    if (roleError) {
      setError(roleError.message);
      setRoles([]);
      return;
    }
    setRoles(((data ?? []) as { role: Role }[]).map((row) => row.role));
  }, []);

  useEffect(() => {
    if (!isConfigured || !supabase) {
      setLoading(false);
      return;
    }
    let mounted = true;

    supabase.auth.getSession().then(({ data }) => {
      if (!mounted) return;
      setSession(data.session ?? null);
      setLoading(false);
      if (data.session) void loadRoles();
    });

    const { data: subscription } = supabase.auth.onAuthStateChange((_event, nextSession) => {
      setSession(nextSession);
      if (nextSession) void loadRoles();
      else setRoles([]);
    });

    return () => {
      mounted = false;
      subscription.subscription.unsubscribe();
    };
  }, [loadRoles]);

  const signIn = useCallback(async (email: string, password: string) => {
    if (!supabase) {
      setError("Supabase is not configured for this build.");
      return;
    }
    setError(null);
    const { error: signInError } = await supabase.auth.signInWithPassword({ email, password });
    if (signInError) setError(signInError.message);
  }, []);

  const signOut = useCallback(async () => {
    if (!supabase) return;
    await supabase.auth.signOut();
    setRoles([]);
  }, []);

  const canSee = useCallback(
    (allowed: Role[]) => roles.some((role) => allowed.includes(role)),
    [roles],
  );

  const contextValue = useMemo(
    () => ({ session, roles, loading, error, signIn, signOut, canSee }),
    [session, roles, loading, error, signIn, signOut, canSee],
  );

  return <AuthContext.Provider value={contextValue}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside AuthProvider");
  return context;
}
