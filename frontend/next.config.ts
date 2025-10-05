import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      { source: "/auth/:path*", destination: "http://localhost:8080/auth/:path*" },
      { source: "/graphql", destination: "http://localhost:8080/graphql" },
      { source: "/logout", destination: "http://localhost:8080/auth/logout" },
      { source: "/oauth2/:path*", destination: "http://localhost:8080/oauth2/:path*" },
      { source: "/login/oauth2/:path*", destination: "http://localhost:8080/login/oauth2/:path*" },
    ];
  },
};

export default nextConfig;
