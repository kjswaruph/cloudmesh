import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { ThemeProvider } from "@/components/theme-provider";
import { GraphQLProvider } from "@/lib/ApolloProvider"; // Provides Apollo Client with only supported operations
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "CloudMesh - Multi-Cloud Management",
  description: "CloudMesh is a unified platform for managing multiple cloud providers from a single dashboard.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        {/* Wrap the app with GraphQLProvider. Only use operations defined in backend schema.graphqls.
            For any missing capabilities (auth/session, dashboard stats, resources) keep TODO placeholders.
            Do NOT add REST fetches; extend backend GraphQL instead. */}
        <GraphQLProvider>
          <ThemeProvider defaultTheme="system">
            {children}
          </ThemeProvider>
        </GraphQLProvider>
      </body>
    </html>
  );
}
