import type { NextConfig } from "next";

const apiUrl = process.env.API_URL || "http://localhost:8081";

const nextConfig: NextConfig = {
  output: "standalone",
  async rewrites() {
    return [
      { source: "/auth/:path*", destination: `${apiUrl}/auth/:path*` },
      { source: "/graphql", destination: `${apiUrl}/graphql` },
      { source: "/logout", destination: `${apiUrl}/auth/logout` },
      { source: "/oauth2/:path*", destination: `${apiUrl}/oauth2/:path*` },
      { source: "/login/oauth2/:path*", destination: `${apiUrl}/login/oauth2/:path*` },
    ];
  },
};

export default nextConfig;
