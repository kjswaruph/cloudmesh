import type { NextConfig } from "next";

const nextConfig: NextConfig = {
    output: "standalone",

    async rewrites() {
        return [
            { source: "/auth/:path*", destination: "/auth/:path*" },
            { source: "/api/:path*", destination: "/api/:path*" },
            { source: "/logout", destination: "/auth/logout" },
            { source: "/oauth2/:path*", destination: "/oauth2/:path*" },
            { source: "/login/oauth2/:path*", destination: "/login/oauth2/:path*" },
        ];
    },
};
export default nextConfig;
