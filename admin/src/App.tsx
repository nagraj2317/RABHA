import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "./auth/AuthContext";
import { ConfigProvider } from "./lib/ConfigContext";
import { SCREENS } from "./lib/config";
import { Layout } from "./components/Layout";
import { Login } from "./screens/Login";
import { Dashboard } from "./screens/Dashboard";
import { Advertisements } from "./screens/Advertisements";
import { Wallpapers } from "./screens/Wallpapers";
import { Users } from "./screens/Users";
import { Application } from "./screens/Application";
import { Analytics } from "./screens/Analytics";
import { Advanced } from "./screens/Advanced";

const COMPONENTS: Record<string, () => JSX.Element> = {
  "/": Dashboard,
  "/ads": Advertisements,
  "/wallpapers": Wallpapers,
  "/users": Users,
  "/application": Application,
  "/analytics": Analytics,
  "/advanced": Advanced,
};

function Shell() {
  const { session, loading, roles, canSee } = useAuth();

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-rabha-black text-sm text-rabha-muted">
        Checking session…
      </div>
    );
  }

  if (!session) return <Login />;

  if (roles.length === 0) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-rabha-black p-6">
        <div className="max-w-md rounded-2xl border border-rabha-danger/40 bg-rabha-danger/10 p-6 text-sm text-rabha-danger">
          This account has no admin role. Ask a super admin to insert a row in <code>admin_roles</code> for
          your user id.
        </div>
      </div>
    );
  }

  return (
    <Routes>
      <Route element={<Layout />}>
        {SCREENS.map((screen) => {
          const Screen = COMPONENTS[screen.path];
          if (!Screen) return null;
          return (
            <Route
              key={screen.path}
              path={screen.path}
              element={canSee(screen.roles) ? <Screen /> : <Navigate to="/" replace />}
            />
          );
        })}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ConfigProvider>
          <Shell />
        </ConfigProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
